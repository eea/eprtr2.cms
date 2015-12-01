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
package eea.eprtr.cms.dao;

import eea.eprtr.cms.model.Upload;

import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service to put files in storage. The storage can be file system, object database etc.
 */
public interface StorageService {

    /**
     * Store the file at a location and return a generated unique identifier for it.
     */
    String save(MultipartFile myFile, String section) throws IOException;

    /**
     * Method to show the user the max upload size.
     */
    long getFreeSpace();

    /**
     * Get an open stream to the stored object.
     */
    InputStream getById(String id, String section) throws IOException;

    /**
     * Delete a file in the storage service. If the file does not exist, then return false.
     *
     * @param id - unique identifier for the file.
     */
    boolean deleteById(String id, String section) throws IOException;

    /**
     * Get all records.
     */
    List<Upload> getIndex(String section);

    /**
     * Delete all uploads. Mainly used for testing.
     */
    void deleteAll(String section);

    long getSizeById(String id, String section) throws IOException;
}
