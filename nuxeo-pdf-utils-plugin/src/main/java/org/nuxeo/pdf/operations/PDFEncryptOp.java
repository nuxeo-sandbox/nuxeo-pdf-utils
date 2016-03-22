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

package org.nuxeo.pdf.operations;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.pdf.PDFEncryption;

/**
 * Encrypts the PDF with the given permissions
 * 
 * @since 8.1
 */
@Operation(id = PDFEncryptOp.ID, category = Constants.CAT_CONVERSION, label = "PDF: Encrypt", description = "Encrypts the PDF with the given permissions, returns a copy. Permissions are print, modify, copy, modifyAnnot, fillForms, extractForAccessibility, assemble and printDegraded. Any missing permission is set to false (Values are true or false. assemble=true for ex.) originalOwnerPwd is used if the pdf was originally encrypted. Avilable permission are If ownerPwd is empty, use originalOwnerPwd to encrypt. If no keyLentgh is provided, use 128. If the operation is ran on Document(s), xpath lets you specificy where to get the blob from (default: file:content)")
public class PDFEncryptOp {

    public static final String ID = "PDF.Encrypt";

    public static final String[] PERMISSIONS_LOWERCASE = { "print", "modify", "copy", "modifyannot", "fillforms",
            "extractforaccessibility", "assemble", "printdegraded" };

    @Param(name = "originalOwnerPwd")
    protected String originalOwnerPwd;

    @Param(name = "ownerPwd")
    protected String ownerPwd;

    @Param(name = "userPwd")
    protected String userPwd;

    @Param(name = "keyLength", required = false, widget = Constants.W_OPTION, values = { "40", "128" })
    protected String keyLength = "128";

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath = "file:content";

    @Param(name = "permissions", required = false)
    protected Properties permissions;

    @OperationMethod
    public Blob run(Blob inBlob) {

        // Set everything to false by default
        AccessPermission ap = new AccessPermission(0);
        boolean value;
        String valueStr;
        for(Entry<String, String> onePerm : permissions.entrySet()) {
            valueStr = onePerm.getValue();
            if(StringUtils.isBlank(valueStr)) {
                value = false;
            } else {
                value = "true".equals(valueStr.toLowerCase());
            }
            
            switch(onePerm.getKey().toLowerCase()) {
            case "print":
                ap.setCanPrint(value);
                break;
                
            case "modify":
                ap.setCanModify(value);
                break;
                
            case "copy":
                ap.setCanExtractContent(value);
                break;
                
            case "modifyannot":
                ap.setCanModifyAnnotations(value);
                break;
                
            case "fillforms":
                ap.setCanFillInForm(value);
                break;
                
            case "extractforaccessibility":
                ap.setCanExtractForAccessibility(value);
                break;
                
            case "assemble":
                ap.setCanAssembleDocument(value);
                break;
                
            case "printdegraded":
                ap.setCanPrintDegraded(value);
                break;
            }
        }

        PDFEncryption pdfe = new PDFEncryption(inBlob);
        pdfe.setKeyLength(Integer.parseInt(keyLength));
        pdfe.setOriginalOwnerPwd(originalOwnerPwd);
        pdfe.setOwnerPwd(ownerPwd);
        pdfe.setUserPwd(userPwd);

        Blob result = pdfe.encrypt(ap);

        return result;
    }

    @OperationMethod
    public BlobList run(BlobList inBlobs) {
        BlobList bl = new BlobList();
        for (Blob blob : inBlobs) {
            bl.add(this.run(blob));
        }
        return bl;
    }

    @OperationMethod
    public Blob run(DocumentModel inDoc) {

        if (StringUtils.isBlank(xpath)) {
            xpath = "file:content";
        }

        Blob result = null;
        Blob content;
        content = (Blob) inDoc.getPropertyValue(xpath);
        if (content != null) {
            result = this.run(content);
        }

        return result;

    }

    @OperationMethod
    public BlobList run(DocumentModelList inDocs) {

        if (StringUtils.isBlank(xpath)) {
            xpath = "file:content";
        }

        BlobList bl = new BlobList();
        for (DocumentModel doc : inDocs) {
            bl.add(this.run(doc));
        }

        return bl;

    }
}
