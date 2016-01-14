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
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.InputStreamPayload;
//import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.domain.ObjectList;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;
import org.jclouds.openstack.swift.v1.SwiftApi;


/**
 * Service to store files in the Swift Object Store.
 */
@Service
public class StorageServiceSwift implements StorageService {

    private static final String PROVIDER = "openstack-swift";
    private SwiftApi swiftApi;

    private String swiftAuthUrl;

    private String swiftTenantId;

    private String swiftRegion;

    /** * The swift location where to store the uploaded files.  */
    private String swiftContainer = "eprtr";

    private String swiftUsername;

    private String swiftPassword;

    /** Set to the string "true" if testing. */
    private String swiftMock;




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
        assert swiftUsername != null;
        swiftApi = ContextBuilder.newBuilder(PROVIDER)
            .endpoint(swiftAuthUrl)
            .credentials(swiftTenantId + ":" + swiftUsername, swiftPassword)
            //.modules(modules)
            .buildApi(SwiftApi.class);

        if (swiftRegion == null || "".equals(swiftRegion)) {
            swiftRegion = getFirstRegion();
        }
        ContainerApi containerApi = swiftApi.getContainerApi(swiftRegion);

        Container container = containerApi.get(swiftContainer);
        if (container == null) {
            CreateContainerOptions options = CreateContainerOptions.Builder
                .metadata(ImmutableMap.of("description", "E-PRTR data"));
            containerApi.create(swiftContainer, options);
        }
    }

    private String getFirstRegion() {
        Set<String> regions = swiftApi.getConfiguredRegions();
        for (String region : regions) {
            System.out.println("Region:" + region);
            return region;
        }
        return null;
    }

    // https://jclouds.apache.org/reference/javadoc/1.9.x/
    @Override
    public String save(MultipartFile myFile, String section) throws IOException {
        if (swiftApi == null) {
            login();
        }
        String fileName = Filenames.removePath(myFile.getOriginalFilename());
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        Payload payload = new InputStreamPayload(myFile.getInputStream());

        if (myFile.getContentType() != null) {
            //swiftObject.setContentType(myFile.getContentType());
        }

        Map<String, String> metadata = new HashMap<String, String>();
        if (myFile.getOriginalFilename() != null) {
            metadata.put("filename", myFile.getOriginalFilename());
        }
        if (myFile.getContentType() != null) {
            metadata.put("content-type", myFile.getContentType());
        }
        objectApi.put(destination, payload, PutOptions.Builder.metadata(metadata));
        return fileName;
    }

    @Override
    public InputStream getById(String fileName, String section) throws IOException {
        if (swiftApi == null) {
            login();
        }
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        SwiftObject swiftObject = null;
        try {
            swiftObject = objectApi.get(destination);
        } catch (Exception e) {
            throw new FileNotFoundException(destination);
        }
        return swiftObject.getPayload().openStream();
    }

    @Override
    public boolean deleteById(String fileName, String section) throws IOException {
        if (swiftApi == null) {
            login();
        }
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        SwiftObject swiftObject = objectApi.get(destination);
        if (swiftObject != null) {
            objectApi.delete(destination);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteAll(String section) {
        if (swiftApi == null) {
            login();
        }
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        ObjectList allObjects = objectApi.list();
        for (SwiftObject obj : allObjects) {
            objectApi.delete(obj.getName());
        }
    }

    @Override
    public long getSizeById(String fileName, String section) throws IOException {
        // FIXME
        return 0L;
    }

    @Override
    public List<Upload> getIndex(String section) {
        if (swiftApi == null) {
            login();
        }
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

   private void close() throws IOException {
      Closeables.close(swiftApi, true);
      swiftApi = null;
   }

}
