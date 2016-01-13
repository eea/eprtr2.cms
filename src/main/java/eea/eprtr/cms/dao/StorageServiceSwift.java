package eea.eprtr.cms.dao;

import eea.eprtr.cms.controller.FileNotFoundException;
import eea.eprtr.cms.model.Upload;
import eea.eprtr.cms.util.Filenames;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.exception.NotFoundException;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.DirectoryOrObject;
import org.javaswift.joss.model.StoredObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service to store files in the Swift Object Store.
 */
@Service
public class StorageServiceSwift implements StorageService {

    private String swiftAuthUrl;

    private String swiftTenantId;

    /** * The swift location where to store the uploaded files.  */
    private String swiftContainer = "transfer";

    private String swiftUsername;

    private String swiftPassword;

    /** Set to the string "true" if testing. */
    private String swiftMock;

    private AccountConfig config;

    private Account account;

    private Container container;

    private Log logger = LogFactory.getLog(StorageServiceSwift.class);

    public void setSwiftAuthUrl(String swiftAuthUrl) {
        this.swiftAuthUrl = swiftAuthUrl;
    }

    public void setSwiftTenantId(String swiftTenantId) {
        this.swiftTenantId = swiftTenantId;
    }

    public void setSwiftContainer(String swiftContainer) {
        this.swiftContainer = swiftContainer;
    }

    public void setSwiftUsername(String swiftUsername) {
        this.swiftUsername = swiftUsername;
    }

    public void setSwiftPassword(String swiftPassword) {
        this.swiftPassword = swiftPassword;
    }

    public void setSwiftMock(String swiftMock) {
        this.swiftMock = swiftMock;
    }

    /**
     * Logs into the Object store, but note that the token only works for 24 hours.
     * FIXME: Test
     */
    private void login() {
        if (swiftUsername == null || swiftPassword == null || swiftAuthUrl == null) {
            System.out.println("ERROR: Swift storage account is not configured");
            logger.error("Swift storage account is not configured");
        }
        config = new AccountConfig();
        config.setUsername(swiftUsername);
        config.setPassword(swiftPassword);
        config.setAuthUrl(swiftAuthUrl);
        config.setTenantId(swiftTenantId);
//      config.setTenantName(swiftTenantName);
        if (swiftMock != null) {
            config.setMock(Boolean.valueOf(swiftMock));
        }
        account = new AccountFactory(config).createAccount();
        container = account.getContainer(swiftContainer);
        if (!container.exists()) {
            container.create();
        }
        //container.makePublic();
    }

// https://github.com/javaswift/joss/blob/master/src/main/java/org/javaswift/joss/model/StoredObject.java
    @Override
    public String save(MultipartFile myFile, String section) throws IOException {
        if (swiftUsername == null) {
            System.out.println("Swift username is not configured");
        }
        assert swiftUsername != null;
        if (config == null) {
            login();
        }
        String fileName = Filenames.removePath(myFile.getOriginalFilename());
        String destination = getLocation(section, fileName);

        StoredObject swiftObject = container.getObject(destination);
        swiftObject.uploadObject(myFile.getInputStream());
        if (myFile.getContentType() != null) {
            swiftObject.setContentType(myFile.getContentType());
        }

        Map<String, Object> metadata = new HashMap<String, Object>();
        if (myFile.getOriginalFilename() != null) {
            metadata.put("filename", myFile.getOriginalFilename());
        }
        if (myFile.getContentType() != null) {
            metadata.put("content-type", myFile.getContentType());
        }
        swiftObject.setMetadata(metadata);
        swiftObject.saveMetadata();
        return fileName;
    }

    @Override
    public InputStream getById(String fileName, String section) throws IOException {
        if (config == null) {
            login();
        }
        String destination = getLocation(section, fileName);
        StoredObject swiftObject = null;
        try {
            swiftObject = container.getObject(destination);
        } catch (Exception e) {
            throw new FileNotFoundException(destination);
        }
        return swiftObject.downloadObjectAsInputStream();
    }

    @Override
    public boolean deleteById(String fileName, String section) throws IOException {
        String destination = getLocation(section, fileName);
        try {
            StoredObject swiftObject = container.getObject(destination);
            swiftObject.delete();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void deleteAll(String section) {
        Collection<DirectoryOrObject> allObjects = container.listDirectory();
        for (DirectoryOrObject obj : allObjects) {
            StoredObject swiftObject = container.getObject(obj.getBareName());
            swiftObject.delete();
        }
    }

    @Override
    public long getSizeById(String fileName, String section) throws IOException {
        String destination = getLocation(section, fileName);
        StoredObject swiftObject = null;
        try {
            swiftObject = container.getObject(destination);
        } catch (Exception e) {
            throw new FileNotFoundException(destination);
        }
        return swiftObject.getContentLength();
    }

    @Override
    public List<Upload> getIndex(String section) {
      // FIXME
      List<Upload> uploadList = new ArrayList<Upload>();
      return uploadList;
    }

    @Override
    public long getFreeSpace() {
        return 0L;
    }

    private String getLocation(String section, String fileName) {
        return section + "/" + fileName;
    }
}
