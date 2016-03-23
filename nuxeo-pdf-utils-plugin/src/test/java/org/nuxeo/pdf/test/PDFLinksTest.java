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
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.pdf.LinkInfo;
import org.nuxeo.pdf.PDFLinks;
import org.nuxeo.pdf.operations.PDFGetLinksOp;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class PDFLinksTest {
    
    private static final String PDF_1 = "linked-pdf-1.pdf";

    private static final String PDF_2 = "linked-pdf-2.pdf";

    private static final String PDF_3 = "linked-pdf/linked-pdf-3.pdf";

    private static final String PDF_1_PATH = "files/" + PDF_1;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    /*
    @Before
    public void setup() throws Exception {
        
    }

    @After
    public void cleanup() {
        
    }
    */
    
    @Test
    public void testRemoteGoTo() throws Exception {
        
        File pdf1 = FileUtils.getResourceFileFromContext(PDF_1_PATH);
        FileBlob fb1 = new FileBlob(pdf1);

        PDFLinks pdfl = new PDFLinks(fb1);
       
        ArrayList<LinkInfo> launchLinks = pdfl.getLaunchLinks();
        
        assertEquals(2, launchLinks.size());
        // Not sure the parsing is done top-bottom, so let's just check we do have our values
        String [] check = {PDF_2, PDF_3};
        int okCount = 0;
        for(String value : check) {
            for(LinkInfo li : launchLinks) {
                if(li.getLink().equals(value)) {
                    okCount += 1;
                    break;
                }
            }
        }
        assertEquals(check.length, okCount);

        // Check the "Go to page in external document" links
        ArrayList<LinkInfo> remoteLinks = pdfl.getRemoteGoToLinks();
        // We have only one, to pdf-2 here.
        assertEquals(1, remoteLinks.size());
        assertEquals(PDF_2, remoteLinks.get(0).getLink());
        
        pdfl.close();
        
    }
    
    @Test
    public void testGetLinksOperation() throws Exception {

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);

        File pdf1 = FileUtils.getResourceFileFromContext(PDF_1_PATH);
        FileBlob fb1 = new FileBlob(pdf1);
        
        ctx.setInput(fb1);
        chain = new OperationChain("testChain");
        chain.add(PDFGetLinksOp.ID).set("getAll", true);
        String result = (String) automationService.run(ctx, chain);

        assertNotNull(result);
        assertNotEquals("", result);
        // It is a JSON array of 3 elements
        JSONArray array = new JSONArray(result);
        int size = array.length();
        assertEquals(3, size);
        for(int i = 0; i < size; ++i) {
            JSONObject object = (JSONObject) array.get(i);
            String link = object.getString("link");
            assertTrue(link.equals(PDF_2) || link.equals(PDF_3));
        }
        
    }
}
