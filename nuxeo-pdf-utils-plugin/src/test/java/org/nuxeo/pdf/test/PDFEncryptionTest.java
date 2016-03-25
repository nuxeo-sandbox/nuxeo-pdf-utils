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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.pdf.PDFEncryption;
import org.nuxeo.pdf.operations.PDFEncryptReadOnlyOp;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class PDFEncryptionTest {

    // WARNING: If you change this pdf, a lot of tests will fail (count pages,
    // text in the pdf, ...)
    private static final String THE_PDF = "files/13-pages-no-page-numbers.pdf";

    protected static final String ENCRYPTED_PDF = "files/13-pages-no-page-numbers-encrypted-pwd-nuxeo.pdf";

    protected static final String ENCRYPTED_PDF_PWD = "nuxeo";

    protected File pdfFile;

    protected FileBlob pdfFileBlob;

    protected DocumentModel testDocsFolder, pdfDocModel;

    protected TestUtils utils;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    @Before
    public void setup() throws Exception {

        utils = new TestUtils();

        testDocsFolder = coreSession.createDocumentModel("/", "test-pdf", "Folder");
        testDocsFolder.setPropertyValue("dc:title", "test-pdfutils");
        testDocsFolder = coreSession.createDocument(testDocsFolder);
        testDocsFolder = coreSession.saveDocument(testDocsFolder);
        assertNotNull(testDocsFolder);

        pdfFile = FileUtils.getResourceFileFromContext(THE_PDF);
        assertNotNull(pdfFile);
        pdfFileBlob = new FileBlob(pdfFile);
        assertNotNull(pdfFileBlob);
        pdfFileBlob.setMimeType("application/pdf");
        pdfFileBlob.setFilename(pdfFile.getName());

        pdfDocModel = coreSession.createDocumentModel(testDocsFolder.getPathAsString(), pdfFile.getName(), "File");
        pdfDocModel.setPropertyValue("dc:title", pdfFile.getName());
        pdfDocModel.setPropertyValue("file:content", pdfFileBlob);
        pdfDocModel = coreSession.createDocument(pdfDocModel);
        pdfDocModel = coreSession.saveDocument(pdfDocModel);
        assertNotNull(pdfDocModel);
    }

    @After
    public void cleanup() {

        coreSession.removeDocument(testDocsFolder.getRef());
        coreSession.save();

        utils.cleanup();
    }

    // Used by more than one test
    protected void checkIsReadOnly(Blob inBlob, String ownerPwd, String userPwd) throws Exception {

        assertNotNull(inBlob);

        PDDocument pdfDoc = utils.loadAndTrack(inBlob);
        assertTrue(pdfDoc.isEncrypted());

        // Decrypt as user
        pdfDoc.openProtection(new StandardDecryptionMaterial(userPwd));
        assertFalse(pdfDoc.isEncrypted());
        AccessPermission ap = pdfDoc.getCurrentAccessPermission();
        assertTrue(ap.canExtractContent());
        assertTrue(ap.canExtractForAccessibility());
        assertTrue(ap.canPrint());
        assertTrue(ap.canPrintDegraded());

        assertFalse(ap.canAssembleDocument());
        assertFalse(ap.canFillInForm());
        assertFalse(ap.canModifyAnnotations());

        // Decrypt as owner
        utils.closeAndUntrack(pdfDoc);
        pdfDoc = utils.loadAndTrack(inBlob);
        pdfDoc.openProtection(new StandardDecryptionMaterial(ownerPwd));
        assertFalse(pdfDoc.isEncrypted());
        ap = pdfDoc.getCurrentAccessPermission();
        assertTrue(ap.isOwnerPermission());

        utils.closeAndUntrack(pdfDoc);

    }

    @Test
    public void testEncryptPDF_readOnly() throws Exception {

        PDFEncryption pdfe = new PDFEncryption(pdfFileBlob);

        pdfe.setKeyLength(128);
        pdfe.setOwnerPwd("owner");
        pdfe.setUserPwd("user");
        Blob result = pdfe.encryptReadOnly();

        checkIsReadOnly(result, "owner", "user");

    }

    @Test
    public void testRemoveEncryption() throws Exception {

        // Test with encrypted PDF
        File f = FileUtils.getResourceFileFromContext(ENCRYPTED_PDF);
        FileBlob fb = new FileBlob(f);

        // Just check it is encrypted first
        PDDocument pdfDoc = utils.loadAndTrack(fb);
        assertTrue(pdfDoc.isEncrypted());
        utils.closeAndUntrack(pdfDoc);

        PDFEncryption pdfe = new PDFEncryption(fb);
        pdfe.setOriginalOwnerPwd(ENCRYPTED_PDF_PWD);
        Blob result = pdfe.removeEncryption();

        assertNotNull(result);

        pdfDoc = utils.loadAndTrack(result);
        assertFalse(pdfDoc.isEncrypted());
        utils.closeAndUntrack(pdfDoc);

        // Test with a non-encrypted PDF (removing encryption should not trigger an error)
        pdfe = new PDFEncryption(pdfFileBlob);
        pdfe.setOriginalOwnerPwd(ENCRYPTED_PDF_PWD);
        result = pdfe.removeEncryption();

        assertNotNull(result);

        pdfDoc = utils.loadAndTrack(result);
        assertFalse(pdfDoc.isEncrypted());
        utils.closeAndUntrack(pdfDoc);

    }

    @Test
    public void testEncryptReadOnlyOperation_Blob() throws Exception {

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(pdfFileBlob);
        chain = new OperationChain("testChain");
        // Default values for others.
        // And testing the Blob -> Blob instance
        chain.add(PDFEncryptReadOnlyOp.ID).set("ownerPwd", "owner").set("userPwd", "user");
        Blob result = (Blob) automationService.run(ctx, chain);

        checkIsReadOnly(result, "owner", "user");
    }

    @Test
    public void testEncryptReadOnlyOperation_BlobList() throws Exception {

        BlobList bl = new BlobList();
        bl.add(pdfFileBlob);
        File f = FileUtils.getResourceFileFromContext(ENCRYPTED_PDF);
        bl.add(new FileBlob(f));

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(bl);
        chain = new OperationChain("testChain");
        // Default values for others.
        // And testing the Blob -> Blob instance
        chain.add(PDFEncryptReadOnlyOp.ID)
             .set("originalOwnerPwd", ENCRYPTED_PDF_PWD)
             .set("ownerPwd", "owner")
             .set("userPwd", "user");
        BlobList result = (BlobList) automationService.run(ctx, chain);

        assertEquals(2, result.size());
        for(Blob b : result) {
            checkIsReadOnly(b, "owner", "user");
        }
    }

    @Test
    public void testEncryptReadOnlyOperation_Document() throws Exception {

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        assertNotNull(ctx);

        ctx.setInput(pdfDocModel);
        chain = new OperationChain("testChain");
        // Default values for others.
        // And testing the Blob -> Blob instance
        chain.add(PDFEncryptReadOnlyOp.ID).set("ownerPwd", "owner").set("userPwd", "user");
        Blob result = (Blob) automationService.run(ctx, chain);

        checkIsReadOnly(result, "owner", "user");
    }

}
