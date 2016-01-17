package eea.eprtr.cms.dao;

//import eea.eprtr.cms.controller.FileNotFoundException;
import eea.eprtr.cms.model.Upload;
import eea.eprtr.cms.util.Filenames;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.get;
import com.google.common.io.Closeables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.jclouds.ContextBuilder;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.BaseMutableContentMetadata;
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
import static org.jclouds.openstack.swift.v1.options.ListContainerOptions.Builder.prefix;
//org.jclouds.openstack.swift.options.ListContainerOptions

/**
 * Service to store files in the Swift Object Store.
 * https://jclouds.apache.org/reference/javadoc/1.9.x/
 */
@Service
public class StorageServiceSwift implements StorageService {

    private static final String SEPARATOR = "/";

    private String blobApi = "transient";

    private SwiftApi swiftApi;

    private String swiftAuthUrl;

    private String swiftTenantId;

    private String swiftRegion;

    /** * The swift container where to store the uploaded files.  */
    private String swiftContainer;

    private String swiftUsername;

    private String swiftPassword;

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

    public void setBlobApi(String blobApi) {
        this.blobApi = blobApi;
    }


    /**
     * Logs into the Object store, but note that the token only works for 24 hours.
     */
    private void login() {
        if (swiftApi != null) {
            return;
        }
        if (swiftUsername == null || swiftPassword == null || swiftAuthUrl == null || swiftContainer == null) {
            logger.error("Swift storage account is not configured");
            throw new IllegalArgumentException("ERROR: Swift storage account is not configured");
        }
        swiftApi = ContextBuilder.newBuilder(blobApi)
            .endpoint(swiftAuthUrl)
            .credentials(swiftUsername, swiftPassword)
            //.modules(modules)
            .buildApi(SwiftApi.class);

        validateSwiftReqion();
        ContainerApi containerApi = swiftApi.getContainerApi(swiftRegion);

        Container container = containerApi.get(swiftContainer);
        if (container == null) {
            CreateContainerOptions options = CreateContainerOptions.Builder
                .metadata(ImmutableMap.of("description", "E-PRTR data"));
            containerApi.create(swiftContainer, options);
        }
    }

    private void validateSwiftReqion() {
        Set<String> regions = swiftApi.getConfiguredRegions();
        if (!(swiftRegion == null || "".equals(swiftRegion))) {
            for (String region : regions) {
                if (region.equals(swiftRegion)) {
                    return;
                }
            }
        }
        swiftRegion = get(regions, 0);
    }

    @Override
    public String save(MultipartFile myFile, String section) throws IOException {
        login();
        String fileName = Filenames.removePath(myFile.getOriginalFilename());
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        Payload payload = new InputStreamPayload(myFile.getInputStream());

        MutableContentMetadata contentMetadata = new BaseMutableContentMetadata();

        if (myFile.getContentType() != null) {
            contentMetadata.setContentType(myFile.getContentType());
        }
        contentMetadata.setContentLength(myFile.getSize());
        payload.setContentMetadata(contentMetadata);

        // Meta data is not needed.
        Map<String, String> metadata = new HashMap<String, String>();
        if (myFile.getOriginalFilename() != null) {
            metadata.put("filename", myFile.getOriginalFilename());
        }
        metadata.put("content-length", String.valueOf(myFile.getSize()));
        if (myFile.getContentType() != null) {
            metadata.put("content-type", myFile.getContentType());
        }

        objectApi.put(destination, payload, PutOptions.Builder.metadata(metadata));
        return fileName;
    }

    @Override
    public InputStream getById(String fileName, String section) throws IOException {
        login();
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        SwiftObject swiftObject = null;
        swiftObject = objectApi.get(destination);
        if (swiftObject == null) {
            throw new FileNotFoundException(destination);
        }
        return swiftObject.getPayload().openStream();
    }

    @Override
    public boolean deleteById(String fileName, String section) throws IOException {
        login();
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
        login();
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        ObjectList allObjects = objectApi.list();
        for (SwiftObject obj : allObjects) {
            objectApi.delete(obj.getName());
        }
    }

    @Override
    public long getSizeById(String fileName, String section) throws IOException {
        login();
        String destination = getLocation(section, fileName);
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        SwiftObject swiftObject = null;
        swiftObject = objectApi.get(destination);
        if (swiftObject == null) {
            throw new FileNotFoundException(destination);
        }
        Payload payload = swiftObject.getPayload();
        return payload.getContentMetadata().getContentLength();
    }

    @Override
    public List<Upload> getIndex(String section) {
        login();
        List<Upload> uploadList = new ArrayList<Upload>();
        ObjectApi objectApi = swiftApi.getObjectApi(swiftRegion, swiftContainer);
        ObjectList allObjects = objectApi.list(prefix(section + SEPARATOR));
        for (SwiftObject obj : allObjects) {
            Upload uploadRec = new Upload();
            Payload payload = obj.getPayload();
            uploadRec.setFilename(obj.getName().substring(section.length() + SEPARATOR.length()));
            uploadRec.setSize(payload.getContentMetadata().getContentLength());
            uploadRec.setModificationTime(obj.getLastModified());
            uploadList.add(uploadRec);
        }
        return uploadList;
    }

    private String getLocation(String section, String fileName) {
        return section + SEPARATOR + fileName;
    }

   private void close() throws IOException {
      Closeables.close(swiftApi, true);
      swiftApi = null;
   }

}
