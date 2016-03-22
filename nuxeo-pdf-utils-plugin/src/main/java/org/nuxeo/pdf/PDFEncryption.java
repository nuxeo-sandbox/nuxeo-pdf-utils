/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.pdf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * Encrypt/Decrypt a PDF.
 * <p>
 * Notice that encryption is not only about pure encryption, it is also about features available on the pdf: can print,
 * can copy, can modify, ... (see PDFBOx {@link AccessPermission}).
 * <p>
 * To use this class and its encrypt()/removeEncryption() methods you always must first user the appropriate setters to
 * set the misc; infos (original owner password so an encrypted pdf can be handled, encryption key length, ...)
 * 
 * @since 8.1
 */
/**
 * @since TODO
 */
public class PDFEncryption {

    protected Blob pdfBlob;

    protected PDDocument pdfDoc;

    public static final List<Integer> ALLOWED_LENGTH = Arrays.asList(40, 128);

    public static final int DEFAULT_KEYLENGTH = 128;

    int keyLength = DEFAULT_KEYLENGTH;

    String originalOwnerPwd;

    String userPwd;

    String ownerPwd;

    /**
     * Basic constructor
     * 
     * @param inBlob
     */
    public PDFEncryption(Blob inBlob) {

        pdfBlob = inBlob;
    }

    protected void loadPdf() throws IOException, BadSecurityHandlerException, CryptographyException {

        if (pdfDoc == null) {
            pdfDoc = PDDocument.load(pdfBlob.getStream());
            if (pdfDoc.isEncrypted()) {
                pdfDoc.openProtection(new StandardDecryptionMaterial(originalOwnerPwd));
            }
        }
    }

    /**
     * Encrypts the PDF with readonly permission.
     * <p>
     * WARNING: If you are familiar with PDFBox @ AccessPermission}, notice our encryptReadOnly() method is not the same
     * as {@link AccessPermission#AccessPermission#setReadOnly}. The later just makes sure the code cannot call other
     * setter later on.
     * <p>
     * <code>encryptReadOnly</code> sets the following permissions on the document:
     * <ul>
     * <li>Can print: True</li>
     * <li>Can Modify: False</li>
     * <li>Can Extract Content: True</li>
     * <li>Can Add/Modify annotations: False</li>
     * <li>Can Fill Forms: False</li>
     * <li>Can Extract Info for Accessibility: True</li>
     * <li>Can Assemble: False</li>
     * <li>Can print degraded: True</li>
     * </ul>
     * <p>
     * <b>IMPORTANT
     * </p>
     * : It is required that the following setters are called <i>before</i>
     * <ul>
     * <li>{@link setOriginalOwnerPwd}: Only if the original pdf already is encrypted. This password allows to open it
     * for modification</li>
     * <li>{@link setKeyLength}: To set the length of the key</li>
     * <li>{@link setOwnerPwd}: The password for the owner. If not called, <code>originalOwnerPwd</code> is used instead
     * </li>
     * <li>{@link setUserPwd}: The password for the user.</li>
     * </ul>
     * 
     * @return a copy of the blob with the readonly permissions set.
     * @since 8.1
     */
    public Blob encryptReadOnly() {

        AccessPermission ap = new AccessPermission();

        ap.setCanPrint(true);
        ap.setCanModify(false);
        ap.setCanExtractContent(true);
        ap.setCanModifyAnnotations(false);
        ap.setCanFillInForm(false);
        ap.setCanExtractForAccessibility(true);
        ap.setCanAssembleDocument(false);
        ap.setCanPrintDegraded(true);

        return encrypt(ap);

    }

    /**
     * Encrypts the pdf with the new permissions (see {@link AccessPermission})
     * <p>
     * <b>IMPORTANT
     * </p>
     * : It is required that the following setters are called <i>before</i>
     * <ul>
     * <li>{@link setOriginalOwnerPwd}: Only if the original pdf already is encrypted. This password allows to open it
     * for modification</li>
     * <li>{@link setKeyLength}: To set the length of the key</li>
     * <li>{@link setOwnerPwd}: The password for the owner. If not called, <code>originalOwnerPwd</code> is used instead
     * </li>
     * <li>{@link setUserPwd}: The password for the user.</li>
     * </ul>
     * 
     * @param inPerm
     * @return a copy of the blob with the new permissions set.
     * @since 8.1
     */
    public Blob encrypt(AccessPermission inPerm) {

        Blob result = null;

        if (keyLength < 1) {
            keyLength = DEFAULT_KEYLENGTH;
        } else {
            if (!ALLOWED_LENGTH.contains(keyLength)) {
                throw new NuxeoException("Cannot use " + keyLength + " is not allowed as lenght for the encrytion key");
            }
        }

        if (StringUtils.isBlank(ownerPwd)) {
            ownerPwd = originalOwnerPwd;
        }

        try {
            loadPdf();

            StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPwd, userPwd, inPerm);
            spp.setEncryptionKeyLength(keyLength);
            spp.setPermissions(inPerm);

            pdfDoc.protect(spp);

            result = PDFUtils.saveInTempFile(pdfDoc, pdfBlob.getFilename());

        } catch (Exception e) {
            throw new NuxeoException("Failed to encrypt the PDF", e);
        } finally {
            PDFUtils.closeSilently(pdfDoc);
            pdfDoc = null;
        }

        return result;

    }

    /**
     * Removes all protection from the pdf, returns a copy of it. If the PDF was not encrypted, just returns a copy of
     * it with no changes.
     * <p>
     * <b>IMPORTANT
     * </p>
     * : If the pdf is encrypted, it is required for {@link setOriginalOwnerPwd} to be called priori to
     * <code>removeEncryption</code>.
     * <ul>
     * <li>{@link setOriginalOwnerPwd}: Only if the original pdf already is encrypted. This password allows to open it
     * for modification</li>
     * <li>{@link setKeyLength}: To set the length of the key</li>
     * <li>{@link setOwnerPwd}: The password for the owner. If not called, <code>originalOwnerPwd</code> is used instead
     * </li>
     * <li>{@link setUserPwd}: The password for the user.</li>
     * </ul>
     * 
     * @return
     * @since 8.1
     */
    public Blob removeEncryption() {

        Blob result = null;

        try {
            loadPdf();
            // loadPdf() has decrypted the pdf
            pdfDoc.setAllSecurityToBeRemoved(true);
            result = PDFUtils.saveInTempFile(pdfDoc, pdfBlob.getFilename());

        } catch (Exception e) {
            throw new NuxeoException("Failed to remove encryption of the PDF", e);
        } finally {
            PDFUtils.closeSilently(pdfDoc);
            pdfDoc = null;
        }

        return result;
    }

    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Set the lentgh of the key to be used for encryption.
     * <p>
     * Possible values are 40 and 128. Default value is 128 if <code>keyLength</code> is <= 0.
     * 
     * @param keyLength
     * @throws NuxeoException
     * @since 8.1
     */
    public void setKeyLength(int keyLength) throws NuxeoException {

        if (keyLength < 1) {
            keyLength = DEFAULT_KEYLENGTH;
        } else {
            if (!ALLOWED_LENGTH.contains(keyLength)) {
                throw new NuxeoException("Cannot use " + keyLength + " is not allowed as lenght for the encrytion key");
            }
        }

        this.keyLength = keyLength;
    }

    /**
     * Set the password to use when opening a protected PDF. Must be call <i>before</i> encrypting the PDF.
     * 
     * @param originalOwnerPwd
     * @since 8.1
     */
    public void setOriginalOwnerPwd(String originalOwnerPwd) {
        this.originalOwnerPwd = originalOwnerPwd;
    }

    /**
     * Set the owner password to use when encrypting PDF. Must be call <i>before</i> encrypting the PDF.
     * <p>
     * Owners can do whatever they want to the pdf (modify, change protection, ...)
     * 
     * @param ownerPwd
     * @since 8.1
     */
    public void setOwnerPwd(String ownerPwd) {
        this.ownerPwd = ownerPwd;
    }

    /**
     * Set the user password to use when encrypting PDF. Must be call <i>before</i> encrypting the PDF.
     * <p>
     * Users can do have less rights than owners (for example, they can't remove protection)
     * 
     * @param userPwd
     * @since 8.1
     */
    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

}
