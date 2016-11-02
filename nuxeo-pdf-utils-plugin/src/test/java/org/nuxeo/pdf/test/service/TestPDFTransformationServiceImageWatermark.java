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

package org.nuxeo.pdf.test.service;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.pdf.service.PDFTransformationService;
import org.nuxeo.pdf.service.watermark.WatermarkProperties;
import org.nuxeo.pdf.test.PDFUtilsTest;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class TestPDFTransformationServiceImageWatermark {

    private static final String PDF_PATH = "/files/test-watermark.pdf";
    private static final String IMAGE_PATH = "/files/Nuxeo-logo-Gray.jpg";

    @Inject
    PDFTransformationService pdfTransformationService;


    @Test
    public void testNoWatermark() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        assertFalse(PDFUtilsTest.hasImage(blob));
    }

    @Test
    public void testWatermarkDefault() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        Blob result = pdfTransformationService.applyImageWatermark(blob,image,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testImageWatermarkImageDefault.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

    @Test
    public void testWatermarkScale() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setScale(2.0f);
        Blob result = pdfTransformationService.applyImageWatermark(blob,image,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testImageWatermarkImageScale.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

    @Test
    public void testWatermarkInvert() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setInvertY(true);
        properties.setInvertX(true);
        Blob result = pdfTransformationService.applyImageWatermark(blob,image,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testImageWatermarkImageInvert.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

    @Test
    public void testWatermarkCenter() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        Blob image = new FileBlob(getClass().getResourceAsStream(IMAGE_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setRelativeCoordinates(true);
        properties.setxPosition(0.5);
        properties.setyPosition(0.5);
        properties.setScale(2.0);
        Blob result = pdfTransformationService.applyImageWatermark(blob,image,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testImageWatermarkImageCenter.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
        assertTrue(PDFUtilsTest.hasImage(result));
    }

}
