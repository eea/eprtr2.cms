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
import eea.eprtr.cms.util.Filenames;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service to store files in the file system.
 */
public class StorageServiceFiles implements StorageService {

    /**
     * The directory location where to store the uploaded files.
     */
    private String storageDir;

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    /**
     * Experimental method to show the user the max upload size.
     */
    @Override
    public long getFreeSpace() {
        return new File(storageDir).getFreeSpace();
    }

    @Override
    public String save(MultipartFile myFile, String section) throws IOException {
        assert storageDir != null;
        String fileName = Filenames.removePath(myFile.getOriginalFilename());
        File destination = getLocation(section, fileName);
        myFile.transferTo(destination);
        return fileName;
    }

    @Override
    public InputStream getById(String fileName, String section) throws IOException {
        File location = getLocation(section, fileName);
        return new FileInputStream(location);
    }

    @Override
    public boolean deleteById(String fileName, String section) throws IOException {
        File location = getLocation(section, fileName);
        return location.delete();
    }

    @Override
    public List<Upload> getIndex(String section) {
        List<Upload> uploadList = new ArrayList<Upload>();
        File fileDir = new File(storageDir, section);
        if (fileDir.isDirectory()) {
            for (File member : fileDir.listFiles()) {
                Upload uploadRec = new Upload();
                uploadRec.setFilename(member.getName());
                uploadRec.setSize(member.length());
                uploadRec.setModificationTime(new Date(member.lastModified()));
                uploadList.add(uploadRec);
            }
        }
        return uploadList;
    }

    @Override
    public void deleteAll(String section) {
        File fileDir = new File(storageDir, section);
        if (fileDir.isDirectory()) {
            for (File member : fileDir.listFiles()) {
                member.delete();
            }
        }
    }

    @Override
    public long getSizeById(String fileName, String section) throws IOException {
        File location = getLocation(section, fileName);
        return location.length();
    }

    /**
     * Generate a File pointing to a location to store a file.
     */
    private File getLocation(String section, String fileName) throws IOException {
        File sectionFolder = new File(storageDir, section);
        if (sectionFolder.isFile()) {
            throw new IOException("Error: " + section + " is not a directory");
        }
        if (!sectionFolder.exists()) {
            sectionFolder.mkdir(); //FIXME: Check if it was created.
        }
        return new File(sectionFolder, fileName);
    }
}
