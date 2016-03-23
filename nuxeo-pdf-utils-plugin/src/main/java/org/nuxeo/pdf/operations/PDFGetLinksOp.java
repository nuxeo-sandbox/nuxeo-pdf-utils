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

package org.nuxeo.pdf.operations;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.pdf.LinkInfo;
import org.nuxeo.pdf.PDFLinks;
import org.nuxeo.pdf.PDFMerge;

/**
 * Returns a JSON string of an array of objects with page, subType, text and link fields.
 * <p>
 * If <code>getAll</code> is <code>false</code>, then <code>type</code> is required.
 * 
 * @since 8.1
 */
@Operation(id = PDFGetLinksOp.ID, category = Constants.CAT_CONVERSION, label = "PDF: Get Links", description = "Returns a JSON string of an array of objects with page, subType, text and link fields. If getLAl is true, returns all the lings (Remote Go To and Launch in current version)")
public class PDFGetLinksOp {

    public static final String ID = "PDF.GetLinks";

    @Context
    protected OperationContext ctx;

    @Param(name = "type", required = false, widget = Constants.W_OPTION, values = { "Launch", "Remote Go To" })
    protected String type;

    @Param(name = "getAll", required = false)
    boolean getAll = false;

    @OperationMethod
    public String run(Blob inBlob) throws IOException, JSONException {

        ArrayList<String> types = new ArrayList<String>();

        if (getAll) {
            types.add("Launch");
            types.add("Remote Go To");
        } else {
            if (StringUtils.isBlank(type)) {
                throw new IllegalArgumentException("type cannot be empty if getAll is false");
            }
            types.add(type);
        }

        PDFLinks pdfl = new PDFLinks(inBlob);

        JSONArray array = new JSONArray();
        for (String theType : types) {
            
            ArrayList<LinkInfo> links = new ArrayList<LinkInfo>();
            
            switch (theType.toLowerCase()) {
            case "remote go to":
                links = pdfl.getRemoteGoToLinks();
                break;

            case "launch":
                links = pdfl.getLaunchLinks();
                break;
            }

            for (LinkInfo li : links) {
                JSONObject object = new JSONObject();
                object.put("page", li.getPage());
                object.put("subType", li.getSubType());
                object.put("text", li.getText());
                object.put("link", li.getLink());

                array.put(object);
            }
        }
        
        pdfl.close();

        return array.toString();
    }
}
