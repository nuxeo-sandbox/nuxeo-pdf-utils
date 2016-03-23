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
 *     thibaud
 */
package org.nuxeo.pdf;

/**
 * Utility class storing informaiton about a link in the PDF.
 * <p>
 * Notice the <code>url</code> member is not strictly speaking a URL, nor an URI etc. It is a String value, as stored in
 * the pdf
 * 
 * @since 8.1
 */
public class LinkInfo {
    protected int page;

    protected String subType;

    protected String text;

    protected String link;

    public LinkInfo(int page, String subType, String text, String link) {
        this.page = page;
        this.subType = subType;
        this.text = text;
        this.link = link;
    }

    public int getPage() {
        return page;
    }

    public String getSubType() {
        return subType;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public String toString() {
        return "Page " + page + ", subType: " + subType + "Text: " + text + ", link: " + link;
    }

}
