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
 *     Michael Vachette
 */

package org.nuxeo.pdf.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.pdf.service.PDFTransformationService;
import org.nuxeo.pdf.service.watermark.WatermarkProperties;

@Operation(
        id = PDFWatermarkWithTextOp.ID,
        category = Constants.CAT_CONVERSION,
        label = "PDF: Watermark with Text",
        description = PDFWatermarkWithTextOp.DESCRIPTION)
public class PDFWatermarkWithTextOp {

    public static final String ID = "PDF.WatermarkWithText";

    public static final String DESCRIPTION =
            "<p>Return a <em>new</em> blob combining the input PDF and the <code>watermark</code> text.</p>\n" +
            "<p>Properties must be one or more of the following (the default if the property is not set):</p>\n" +
            "<ul>\n" +
            "<li><code>fontFamily</code> (Helvetica)&nbsp;</li>\n" +
            "<li><code>fontSize</code> (72),</li>\n" +
            "<li><code>textRotation</code> (0)-&gt; in&nbsp;counterclockwise degrees</li>\n" +
            "<li><code>hex255Color</code> (#000000)</li>\n" +
            "<li><code>alphaColor</code> (0.5)-&gt; 0 is full transparency, 1 is solid</li>\n" +
            "<li><code>xPosition(0)</code> --&gt; in pixels from left or between 0 (left) and 1 (right) if relativeCoordinates is set to true</li>\n" +
            "<li><code>yPosition(0)</code> --&gt; in pixels from bottom or between 0 (bottom) and 1 (top) if relativeCoordinates is set to true</li>\n" +
            "<li><code>invertX</code> (false) --&gt; xPosition starts from the right going left</li>\n" +
            "<li><code>invertY</code> (false) --&gt; yPosition starts from the top going down</li>\n" +
            "<li><code>relativeCoordinates</code> (false)</li>\n" +
            "</ul>\n" +
            "<p>If <code>watermark</code> is empty, the input blob is returned</p>";

    @Param(name = "watermark", required = true)
    protected String watermark = "watermark";

    @Param(name = "properties", required = false)
    protected Properties properties;

    @Context
    protected PDFTransformationService pdfTransformationService;


    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) {
        return pdfTransformationService.
                applyTextWatermark(inBlob,watermark,convertProperties());
    }

    private WatermarkProperties convertProperties() {
        WatermarkProperties watermarkProperties = pdfTransformationService.getDefaultProperties();
        watermarkProperties.updateFromMap(properties);
        return watermarkProperties;
    }

}
