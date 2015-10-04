/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Transfer 1.0
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. All Rights Reserved.
 *
 * Contributor(s):
 *        Søren Roug
 */
package eea.eprtr.cms.model;

import eea.eprtr.cms.util.Humane;
import java.util.Date;

/**
 * Management and metadata for uploaded files. Contains original file name
 * modification date etc.
 */
public class Upload {

    /** Primary key */
    private String filename;
    private Date modificationTime;
    private String uploader;
    private String contentType;
    private long fileSize;

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return fileSize;
    }

    public void setSize(final long size) {
        this.fileSize = size;
    }

    public String getHumaneSize() {
        return Humane.humaneSize(fileSize);
    }
}
