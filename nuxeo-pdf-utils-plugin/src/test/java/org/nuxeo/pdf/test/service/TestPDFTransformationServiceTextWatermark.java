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
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-pdf-utils-plugin" })
public class TestPDFTransformationServiceTextWatermark {

    public static final String PDF_PATH = "/files/test-watermark.pdf";
    public static final String WATERMARK = "No No No !!!";

    @Inject
    PDFTransformationService pdfTransformationService;

    @Test
    public void testWatermarkDefault() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkDefault.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkInvert() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setInvertY(true);
        properties.setInvertX(true);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkInvert.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkCenter() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setRelativeCoordinates(true);
        properties.setxPosition(0.5f);
        properties.setyPosition(0.5f);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkCenter.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkRotateDown() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setTextRotation(-45);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateDown.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkRotateUp() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setTextRotation(45);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateUp.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkRotateDownCenter() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setRelativeCoordinates(true);
        properties.setxPosition(0.5f);
        properties.setyPosition(0.5f);
        properties.setTextRotation(-90);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateDownCenter.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkRotateUpCenter() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setRelativeCoordinates(true);
        properties.setxPosition(0.5f);
        properties.setyPosition(0.5f);
        properties.setTextRotation(90);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateUpCenter.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }


    @Test
    public void testWatermarkRotateDownInvert() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setInvertY(true);
        properties.setInvertX(true);
        properties.setTextRotation(-45);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateDownInvert.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testWatermarkRotateUpInvert() throws IOException {
        Blob blob = new FileBlob(getClass().getResourceAsStream(PDF_PATH));
        WatermarkProperties properties = pdfTransformationService.getDefaultProperties();
        properties.setInvertY(true);
        properties.setInvertX(true);
        properties.setTextRotation(45);
        Blob result = pdfTransformationService.applyTextWatermark(
                blob,WATERMARK,properties);
        Files.copy(
                result.getStream(),
                Paths.get("target","testTextWatermarkRotateUpInvert.pdf"),
                StandardCopyOption.REPLACE_EXISTING);
    }


}
