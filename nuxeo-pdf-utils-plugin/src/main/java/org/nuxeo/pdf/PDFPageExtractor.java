/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thiabud Arguillere
 */
package org.nuxeo.pdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.pdfbox.util.PageExtractor;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.api.Framework;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Extract pages from a PDF
 *
 * @since 5.9.5
 */
public class PDFPageExtractor {

    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(PDFPageExtractor.class);

    protected Blob pdfBlob;

    protected String password;

    public PDFPageExtractor(Blob inBlob) {

        pdfBlob = inBlob;
    }

    /**
     * Constructor with a <code>DocumentModel</code>. Default value for <code>inXPath</code> (if passed
     * <code>null</code> or "", if <code>file:content</code>.
     *
     * @param inDoc
     * @param inXPath
     */
    public PDFPageExtractor(DocumentModel inDoc, String inXPath) {

        if (inXPath == null || inXPath.isEmpty()) {
            inXPath = "file:content";
        }
        pdfBlob = (Blob) inDoc.getPropertyValue(inXPath);
    }

    public Blob extract(int inStartPage, int inEndPage) {
        return extract(inStartPage, inEndPage, null, null, null, null);
    }

    /**
     * Return a Blob built from page <code>inStartPage</code> to <code>inEndPage</code> (inclusive).
     * <p>
     * If <code>inEndPage</code> is greater than the number of pages in the source document, it will go to the end of
     * the document. If <code>inStartPage</code> is less than 1, it'll start with page 1. If <code>inStartPage</code> is
     * greater than <code>inEndPage</code> or greater than the number of pages in the source document, a blank document
     * will be returned.
     * <p>
     * If fileName is null or "", if is set to the original name + the page range: mydoc.pdf and pages 10-75 +>
     * mydoc-10-75.pdf
     * <p>
     * The mimetype is always set to "application/pdf"
     * <p>
     * Can set the title, subject and author of the resulting PDF. <b>Notice</b>: If the value is null or "", it is just
     * ignored
     *
     * @param inStartPage
     * @param inEndPage
     * @param inFileName
     * @param inTitle
     * @param inSubject
     * @param inAuthor
     * @return FileBlob
     * @throws CryptographyException
     * @throws BadSecurityHandlerException
     */
    public Blob extract(int inStartPage, int inEndPage, String inFileName, String inTitle, String inSubject,
                        String inAuthor) throws NuxeoException {

        Blob result = null;
        PDDocument pdfDoc = null;
        PDDocument extracted = null;

        try {
            pdfDoc = PDFUtils.load(pdfBlob, password);

            PageExtractor pe = new PageExtractor(pdfDoc, inStartPage, inEndPage);
            extracted = pe.extract();

            PDFUtils.setInfos(extracted, inTitle, inSubject, inAuthor);

            result = PDFUtils.saveInTempFile(extracted);

            result.setMimeType("application/pdf");

            if (inFileName == null || inFileName.isEmpty()) {
                String originalName = pdfBlob.getFilename();
                if (originalName == null || originalName.isEmpty()) {
                    originalName = "extracted";
                } else {
                    int pos = originalName.toLowerCase().lastIndexOf(".pdf");
                    if (pos > 0) {
                        originalName = originalName.substring(0, pos);
                    }

                }
                inFileName = originalName + "-" + inStartPage + "-" + inEndPage + ".pdf";
            }
            result.setFilename(inFileName);
            extracted.close();

        } catch (IOException | COSVisitorException e) {
            throw new NuxeoException("Failed to extract the pages", e);
        } finally {
            PDFUtils.closeSilently(pdfDoc);

            if (extracted != null) {
                try {
                    extracted.close();
                } catch (IOException e) {
                    // Nothing
                }
            }
        }

        return result;
    }

    public BlobList getPagesAsImages(String inFileName) throws NuxeoException {
        // See https://github.com/levigo/jbig2-imageio#what-if-the-plugin-is-on-classpath-but-not-seen
        ImageIO.scanForPlugins();

        BlobList results = new BlobList();
        PDDocument pdfDoc = null;
        String resultFileName = null;

        // Use file name parameter if passed, otherwise use original file name.
        if (inFileName == null || inFileName.isEmpty()) {
            String originalName = pdfBlob.getFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "extracted";
            } else {
                int pos = originalName.toLowerCase().lastIndexOf(".pdf");
                if (pos > 0) {
                    originalName = originalName.substring(0, pos);
                }

            }
            inFileName = originalName + ".pdf";
        }

        try {
            pdfDoc = PDFUtils.load(pdfBlob, password);

            // Get all PDF pages.
            List<PDPage> pages = pdfDoc.getDocumentCatalog().getAllPages();

            int page = 0;

            // Convert each page to PNG.
            for (PDPage pdPage : pages) {
                ++page;

                resultFileName = inFileName + "-" + page;

                BufferedImage bim = pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, 300);
                File resultFile = Framework.createTempFile(resultFileName, ".png");
                FileOutputStream resultFileStream = new FileOutputStream(resultFile);
                ImageIOUtil.writeImage(bim, "png", resultFileStream, 300);

                // Convert each PNG to Nuxeo Blob.
                FileBlob result = new FileBlob(resultFile);
                result.setFilename(resultFileName + ".png");
                result.setMimeType("picture/png");

                // Add to BlobList.
                results.add(result);

                Framework.trackFile(resultFile, result);
            }
            pdfDoc.close();

        } catch (IOException e) {
            throw new NuxeoException("Failed to extract the pages", e);
        } finally {
            PDFUtils.closeSilently(pdfDoc);
        }

        return results;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
