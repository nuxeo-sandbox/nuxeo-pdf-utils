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
 *     Thibaud Arguillere
 */

package org.nuxeo.pdf.test;

import com.google.inject.Inject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.pdf.PDFPageExtractor;
import org.nuxeo.pdf.operations.ExtractPDFPagesOp;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@Deploy({"nuxeo-pdf-utils-plugin"})
public class PDFPageExtractorTest {

    protected static final String THE_PDF = "files/13-pages-no-page-numbers.pdf";

    protected static final String ENCRYPTED_PDF = "files/13-pages-no-page-numbers-encrypted-pwd-nuxeo.pdf";

    protected static final String JBIG2_PDF = "files/Transcript_California.pdf";

    protected static final String NOT_A_PDF = "files/Travel-3.jpg";

    protected File pdfFile;

    protected FileBlob pdfFileBlob;

    protected FileBlob encryptedPdfFileBlob;

    TestUtils utils;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    /*
     * The file must have 13 pages
     */
    protected void checkPDFBeforeTest() throws IOException {

        PDDocument doc = PDDocument.load(pdfFile);
        assertNotNull(doc);
        utils.track(doc);

        assertEquals(13, doc.getNumberOfPages());

        doc.close();
        utils.untrack(doc);
    }

    @Before
    public void setup() throws IOException {

        assertNotNull(coreSession);
        assertNotNull(automationService);

        utils = new TestUtils();

        pdfFile = FileUtils.getResourceFileFromContext(THE_PDF);
        pdfFileBlob = new FileBlob(pdfFile);
        checkPDFBeforeTest();

        File f = FileUtils.getResourceFileFromContext(ENCRYPTED_PDF);
        encryptedPdfFileBlob = new FileBlob(f);
    }

    @After
    public void cleanup() {

        utils.cleanup();
    }

    protected void checkExtractedPdf(Blob inBlob, int inExpectedPageCount,
                                     String inExpectedTextAtPos0) throws Exception {

        PDDocument doc = PDDocument.load(inBlob.getStream());
        utils.track(doc);

        assertEquals(inExpectedPageCount, doc.getNumberOfPages());

        String txt = utils.extractText(doc, 1, 1);
        assertEquals(0, txt.indexOf(inExpectedTextAtPos0));

        doc.close();
        utils.untrack(doc);
    }

    @Test
    public void testExtractPages_Basic() throws Exception {

        Blob extracted;
        String originalName = pdfFileBlob.getFilename().replace(".pdf", "");
        PDFPageExtractor pe = new PDFPageExtractor(pdfFileBlob);

        extracted = pe.extract(1, 3);
        assertTrue(extracted instanceof FileBlob);
        checkExtractedPdf(extracted, 3,
                "Creative Brief\r\nDo this\r\nLorem ipsum dolor sit amet");
        assertEquals(originalName + "-1-3.pdf", extracted.getFilename());
        assertEquals("application/pdf", extracted.getMimeType());
    }

    @Test
    public void testExtractPages_Basic_Encrypted() throws Exception {

        Blob extracted;
        String originalName = encryptedPdfFileBlob.getFilename().replace(".pdf", "");
        PDFPageExtractor pe = new PDFPageExtractor(encryptedPdfFileBlob);

        pe.setPassword("nuxeo");

        extracted = pe.extract(1, 3);
        assertTrue(extracted instanceof FileBlob);
        checkExtractedPdf(extracted, 3,
                "Creative Brief");
        assertEquals(originalName + "-1-3.pdf", extracted.getFilename());
        assertEquals("application/pdf", extracted.getMimeType());

    }

    @Test
    public void testExtractPages_WithCustomFileName() throws Exception {

        Blob extracted;
        PDFPageExtractor pe = new PDFPageExtractor(pdfFileBlob);

        extracted = pe.extract(5, 9, "newpdf.pdf", "", "", "");
        assertTrue(extracted instanceof FileBlob);
        checkExtractedPdf(extracted, 5,
                "ipsum");
        assertEquals("newpdf.pdf", extracted.getFilename());
    }

    @Test
    public void testExtractPages_WithSetInfo() throws Exception {

        Blob extracted;
        String originalName = pdfFileBlob.getFilename().replace(".pdf", "");
        PDFPageExtractor pe = new PDFPageExtractor(pdfFileBlob);

        extracted = pe.extract(5, 9, null, "One Upon a Time", "Fairyland",
                "Cool Author");
        assertTrue(extracted instanceof FileBlob);
        assertEquals(originalName + "-5-9.pdf", extracted.getFilename());
        PDDocument doc = PDDocument.load(extracted.getStream());
        utils.track(doc);
        PDDocumentInformation docInfo = doc.getDocumentInformation();
        assertEquals("One Upon a Time", docInfo.getTitle());
        assertEquals("Fairyland", docInfo.getSubject());
        assertEquals("Cool Author", docInfo.getAuthor());
        doc.close();
        utils.untrack(doc);
    }

    @Test
    public void testExtractPagesOperation_BlobInput() throws Exception {

        String originalName = pdfFileBlob.getFilename().replace(".pdf", "");

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(pdfFileBlob);
        chain = new OperationChain("testChain");

        chain.add(ExtractPDFPagesOp.ID).set("startPage", 1).set("endPage", 3);
        Blob extracted = (Blob) automationService.run(ctx, chain);
        assertNotNull(extracted);
        assertTrue(extracted instanceof FileBlob);
        checkExtractedPdf(extracted, 3,
                "Creative Brief");
        assertEquals(originalName + "-1-3.pdf", extracted.getFilename());
        assertEquals("application/pdf", extracted.getMimeType());
    }

    @Test
    public void testExtractPagesOperation_BlobInput_Encrypted() throws Exception {

        String originalName = encryptedPdfFileBlob.getFilename().replace(".pdf", "");

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(encryptedPdfFileBlob);
        chain = new OperationChain("testChain");

        chain.add(ExtractPDFPagesOp.ID).set("startPage", 1).set("endPage", 3).set("password", "nuxeo");
        Blob extracted = (Blob) automationService.run(ctx, chain);
        assertNotNull(extracted);
        assertTrue(extracted instanceof FileBlob);
        checkExtractedPdf(extracted, 3,
                "Creative Brief");
        assertEquals(originalName + "-1-3.pdf", extracted.getFilename());
    }


    @Test
    public void testExtractPagesOperationShouldFail_BlobInput()
            throws Exception {

        File f = FileUtils.getResourceFileFromContext(NOT_A_PDF);
        FileBlob fb = new FileBlob(f);

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(fb);
        chain = new OperationChain("testChain");

        chain.add(ExtractPDFPagesOp.ID).set("startPage", 1).set("endPage", 3);
        try {
            /*Blob extracted = (Blob)*/
            automationService.run(ctx, chain);
            assertTrue("Running the chain should have fail", true);
        } catch (Exception e) {
            // We're good
        }
    }

    @Test
    public void testPagesToPictures_Basic() throws Exception {

        File pdfWithJBIGImages = FileUtils.getResourceFileFromContext(JBIG2_PDF);

        FileBlob testFile = new FileBlob(pdfWithJBIGImages);

        PDFPageExtractor pe = new PDFPageExtractor(testFile);

        BlobList results = pe.getPagesAsImages(null);

        assertEquals(results.size(), 2);
    }

}
