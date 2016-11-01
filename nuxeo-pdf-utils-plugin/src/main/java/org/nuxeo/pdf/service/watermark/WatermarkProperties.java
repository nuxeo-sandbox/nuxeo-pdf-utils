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
package org.nuxeo.pdf.service.watermark;


import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.util.Map;

public class WatermarkProperties {

    protected String fontFamily = "Helvetica";

    protected double fontSize = 72f;

    protected int textRotation = 0;

    protected String hex255Color = "#000000";

    protected double alphaColor = 0.5f;

    protected double xPosition = 0f;

    protected double yPosition = 0f;

    protected boolean invertY = false;

    protected boolean invertX = false;

    protected boolean relativeCoordinates = false;

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public int getTextRotation() {
        return textRotation;
    }

    public void setTextRotation(int textRotation) {
        this.textRotation = textRotation%360;
    }

    public String getHex255Color() {
        return hex255Color;
    }

    public void setHex255Color(String hex255Color) {
        this.hex255Color = hex255Color;
    }

    public double getAlphaColor() {
        return alphaColor;
    }

    public void setAlphaColor(double alphaColor) {
        this.alphaColor = alphaColor;
    }

    public double getxPosition() {
        return xPosition;
    }

    public void setxPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getyPosition() {
        return yPosition;
    }

    public void setyPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    public boolean isInvertY() {
        return invertY;
    }

    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    public boolean isInvertX() {
        return invertX;
    }

    public void setInvertX(boolean invertX) {
        this.invertX = invertX;
    }

    public boolean isRelativeCoordinates() {
        return relativeCoordinates;
    }

    public void setRelativeCoordinates(boolean relativeCoordinates) {
        this.relativeCoordinates = relativeCoordinates;
    }

    public void updateFromMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue()))
                continue;
            String value = entry.getValue();
            String key = entry.getKey();
            switch (key) {
                case "fontFamily":
                    setFontFamily(value);
                    break;
                case "fontSize":
                    setFontSize(Double.valueOf(value));
                    break;
                case "textRotation":
                    setTextRotation(Integer.valueOf(value));
                    break;
                case "hex255Color":
                    setHex255Color(value);
                    break;
                case "alphaColor":
                    setAlphaColor(Double.valueOf(value));
                    break;
                case "xPosition":
                    setxPosition(Double.valueOf(value));
                    break;
                case "yPosition":
                    setyPosition(Double.valueOf(value));
                    break;
                case "invertY":
                    setInvertY(Boolean.valueOf(value));
                    break;
                case "invertX":
                    setInvertX(Boolean.valueOf(value));
                    break;
                case "relativeCoordinates":
                    setRelativeCoordinates(Boolean.valueOf(value));
                    break;
                default:
                    throw new NuxeoException("Unknown property: " + key);
            }
        }
    }

}
