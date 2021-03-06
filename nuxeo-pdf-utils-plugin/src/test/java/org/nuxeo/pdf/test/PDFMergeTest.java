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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.pdf.PDFMerge;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class PDFMergeTest {

    protected static final String MERGEPDF_1 = "files/pdf-1-2pages.pdf";

    protected static final String MERGEPDF_2 = "files/pdf-2-3pages.pdf";

    protected static final String MERGEPDF_3 = "files/pdf-3-1page.pdf";

    protected static final String MERGEPDF_CHECK_PREFIX = "This is pdf ";

    protected TestUtils utils;

    protected DocumentModel testDocsFolder, docMergePDF1, docMergePDF2,
            docMergePDF3;

    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(PDFMergeTest.class);

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    protected DocumentModel createMergePDFDocument(String inWHichOne) {

        File f = FileUtils.getResourceFileFromContext(inWHichOne);

        DocumentModel doc = coreSession.createDocumentModel(
                testDocsFolder.getPathAsString(), f.getName(), "File");
        doc.setPropertyValue("dc:title", f.getName());
        doc.setPropertyValue("file:content", new FileBlob(f));
        return coreSession.createDocument(doc);

    }

    @Before
    public void setup() throws IOException {

        utils = new TestUtils();

        assertNotNull(coreSession);
        assertNotNull(automationService);

        testDocsFolder = coreSession.createDocumentModel("/", "test-pictures",
                "Folder");
        testDocsFolder.setPropertyValue("dc:title", "test-pdfutils");
        testDocsFolder = coreSession.createDocument(testDocsFolder);
        testDocsFolder = coreSession.saveDocument(testDocsFolder);

        docMergePDF1 = createMergePDFDocument(MERGEPDF_1);
        docMergePDF2 = createMergePDFDocument(MERGEPDF_2);
        docMergePDF3 = createMergePDFDocument(MERGEPDF_3);

    }

    @After
    public void cleanup() {

        coreSession.removeDocument(testDocsFolder.getRef());
        coreSession.save();

        utils.cleanup();
    }

    /*
     * We check the pdf is in the correct order. Each pdf file used has a text
     * telling us where it should have been merged
     */
    protected void checkMergedPDF(Blob inBlob, boolean jutsFirst2Pages)
            throws IOException {

        File tempFile = File.createTempFile("testmergepdf", ".pdf");
        utils.track(tempFile);
        inBlob.transferTo(tempFile);

        PDDocument doc = PDDocument.load(tempFile);
        assertNotNull(doc);
        utils.track(doc);

        // 2 + 3 + 1
        if (jutsFirst2Pages) {
            assertEquals(5, doc.getNumberOfPages());
        } else {
            assertEquals(6, doc.getNumberOfPages());
        }

        String txt;
        txt = utils.extractText(doc, 1, 1);
        assertTrue(txt.indexOf(MERGEPDF_CHECK_PREFIX + "1") > -1);

        txt = utils.extractText(doc, 3, 3);
        assertTrue(txt.indexOf(MERGEPDF_CHECK_PREFIX + "2") > -1);

        if (!jutsFirst2Pages) {
            txt = utils.extractText(doc, 6, 6);
            assertTrue(txt.indexOf(MERGEPDF_CHECK_PREFIX + "3") > -1);
        }

        doc.close();
        utils.untrack(doc);

        tempFile.delete();
        utils.untrack(tempFile);

    }

    /*
     * Test PDFMerge constructor with simple blobs
     */
    @Test
    public void testMergePDFs_ConstructorSimpleBlobs() throws Exception {

        FileBlob fb;

        FileBlob first = new FileBlob(
                FileUtils.getResourceFileFromContext(MERGEPDF_1));
        PDFMerge pdfm = new PDFMerge(first);

        fb = new FileBlob(FileUtils.getResourceFileFromContext(MERGEPDF_2));
        pdfm.addBlob(fb);
        fb = new FileBlob(FileUtils.getResourceFileFromContext(MERGEPDF_3));
        pdfm.addBlob(fb);

        Blob result = pdfm.merge("merged1.pdf");
        assertNotNull(result);

        checkMergedPDF(result, false);
    }

    /*
     * Test PDFMerge constructor with BlobList
     */
    @Test
    public void testMergePDFs_ConstructorBlobList() throws Exception {

        BlobList bl = new BlobList();

        bl.add(new FileBlob(FileUtils.getResourceFileFromContext(MERGEPDF_1)));
        bl.add(new FileBlob(FileUtils.getResourceFileFromContext(MERGEPDF_2)));
        bl.add(new FileBlob(FileUtils.getResourceFileFromContext(MERGEPDF_3)));

        PDFMerge pdfm = new PDFMerge(bl);

        Blob result = pdfm.merge("merged2.pdf");
        assertNotNull(result);

        checkMergedPDF(result, false);

    }

    @Test
    public void testMergePDFs_ConstructorSimpleDoc() throws Exception {

        PDFMerge pdfm = new PDFMerge(docMergePDF1, null);
        pdfm.addBlob(docMergePDF2, null);
        pdfm.addBlob(docMergePDF3, "");

        Blob result = pdfm.merge("merged1.pdf");
        assertNotNull(result);

        checkMergedPDF(result, false);
    }

    @Test
    public void testMergePDFs_ConstructorDocList() throws Exception {

        DocumentModelList docList = new DocumentModelListImpl();

        docList.add(docMergePDF1);
        docList.add(docMergePDF2);
        docList.add(docMergePDF3);

        PDFMerge pdfm = new PDFMerge(docList, null);

        Blob result = pdfm.merge("merged1.pdf");
        assertNotNull(result);

        checkMergedPDF(result, false);
    }

    @Test
    public void testMergePDFs_WithDocIDs() throws Exception {

        String[] docIDs = new String[3];

        docIDs[0] = docMergePDF1.getId();
        docIDs[1] = docMergePDF2.getId();
        docIDs[2] = docMergePDF3.getId();

        PDFMerge pdfm = new PDFMerge(docIDs, null, coreSession);

        Blob result = pdfm.merge("merged1.pdf");
        assertNotNull(result);

        checkMergedPDF(result, false);
    }
}
