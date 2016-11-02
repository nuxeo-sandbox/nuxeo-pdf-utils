/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Michael Vachette
 */

package org.nuxeo.pdf.test.automation;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.pdf.operations.PDFWatermarkWithImageOp;
import org.nuxeo.pdf.test.PDFUtilsTest;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class TestPDFWatermarkWithImageOp {

    public static final String PDF_PATH = "/files/test-watermark.pdf";
    public static final String IMAGE_PATH = "/files/Nuxeo-logo-Gray.jpg";

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    @Test
    public void testWithDefault() throws IOException, OperationException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(blob);
        chain = new OperationChain("testWithDefault");
        chain.add(PDFWatermarkWithImageOp.ID).set("image",image);
        Blob result = (Blob) automationService.run(ctx, chain);
        Assert.assertNotNull(result);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

    @Test
    public void testWithProperties() throws IOException, OperationException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(blob);
        chain = new OperationChain("testWithDefault");
        chain.add(PDFWatermarkWithImageOp.ID).
                set("image",image).
                set("properties","scale=2.0");
        Blob result = (Blob) automationService.run(ctx, chain);
        Assert.assertNotNull(result);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

}
