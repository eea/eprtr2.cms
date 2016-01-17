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
import static com.google.common.collect.Iterables.get;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.domain.Location;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.InputStreamPayload;
//import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import static org.jclouds.blobstore.options.ListContainerOptions.Builder.inDirectory;


/**
 * Service to store files in any Object Store.
 * https://jclouds.apache.org/reference/javadoc/1.9.x/
 */
@Service
public class StorageServiceBlob implements StorageService {

    private static final String SEPARATOR = "/";

    private String blobApi = "transient";

    private BlobStore blobStore;

    private String swiftAuthUrl;

    private String swiftTenantId;

    private String swiftRegion;

    /** * The swift container where to store the uploaded files.  */
    private String swiftContainer;

    private String swiftUsername;

    private String swiftPassword;


    private Log logger = LogFactory.getLog(StorageServiceBlob.class);

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
        if (blobStore != null) {
            return;
        }
        if (swiftUsername == null || swiftPassword == null || swiftAuthUrl == null || swiftContainer == null) {
            logger.error("Blob storage account is not configured");
            throw new IllegalArgumentException("ERROR: Blob storage account is not configured");
        }
        blobStore = ContextBuilder.newBuilder(blobApi)
            .endpoint(swiftAuthUrl)
            .credentials(swiftUsername, swiftPassword)
            .buildView(BlobStoreContext.class)
            .getBlobStore();

        if (!blobStore.containerExists(swiftContainer)) {
//          CreateContainerOptions options = CreateContainerOptions.Builder
//              .withMetadata(ImmutableMap.of("description", "E-PRTR data"));
//          blobStore.createContainerInLocation(null, swiftContainer, options);
            Location location = getRegion();
            blobStore.createContainerInLocation(location, swiftContainer);
        }
    }

    /**
     * Find the region if the user has specified one.
     */
    private Location getRegion() {
        Set<? extends Location> locations = blobStore.listAssignableLocations();
        if (!(swiftRegion == null || "".equals(swiftRegion))) {
            for (Location location : locations) {
                if (location.getId().equals(swiftRegion)) {
                    return location;
                }
            }
        }
        if (locations.size() != 0) {
            return get(locations, 0);
        } else {
            return null;
        }
    }

    @Override
    public String save(MultipartFile myFile, String section) throws IOException {
        login();
        String fileName = Filenames.removePath(myFile.getOriginalFilename());
        String destination = getLocation(section, fileName);

        Payload payload = new InputStreamPayload(myFile.getInputStream());


        Map<String, String> metadata = new HashMap<String, String>();
        if (myFile.getOriginalFilename() != null) {
            metadata.put("filename", myFile.getOriginalFilename());
        }
        if (myFile.getContentType() != null) {
            metadata.put("content-type", myFile.getContentType());
        }
        // Content-Encoding is automatically set to "gzip" on Swift object stores.
        Blob blob = blobStore.blobBuilder(destination)
            .payload(payload)
            .contentLength(myFile.getSize())
            .contentType(myFile.getContentType())
            .userMetadata(metadata)
            .build();
        blobStore.putBlob(swiftContainer, blob);
        return fileName;
    }

    @Override
    public InputStream getById(String fileName, String section) throws IOException {
        login();
        String destination = getLocation(section, fileName);
        Blob blob = blobStore.getBlob(swiftContainer, destination);
        if (blob == null) {
            throw new FileNotFoundException(destination);
        }
        return blob.getPayload().openStream();
    }

    @Override
    public boolean deleteById(String fileName, String section) throws IOException {
        login();
        String destination = getLocation(section, fileName);
        if (blobStore.blobExists(swiftContainer, destination)) {
            blobStore.removeBlob(swiftContainer, destination);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteAll(String section) {
        login();
        blobStore.clearContainer(swiftContainer);
    }

    @Override
    public long getSizeById(String fileName, String section) throws IOException {
        login();
        String destination = getLocation(section, fileName);
        BlobMetadata blobMetadata = null;
        blobMetadata = blobStore.blobMetadata(swiftContainer, destination);
        if (blobMetadata == null) {
            throw new FileNotFoundException(destination);
        }
        return blobMetadata.getSize();
    }

    @Override
    public List<Upload> getIndex(String section) {
        login();
        List<Upload> uploadList = new ArrayList<Upload>();
        PageSet<? extends StorageMetadata> list = blobStore.list(swiftContainer, inDirectory(section));
        for (StorageMetadata blobMetadata : list) {
            Upload uploadRec = new Upload();
            uploadRec.setFilename(blobMetadata.getName().substring(section.length() + SEPARATOR.length()));
            uploadRec.setSize(blobMetadata.getSize());
            uploadRec.setModificationTime(blobMetadata.getLastModified());
            uploadList.add(uploadRec);
        }
        return uploadList;
    }

    private String getLocation(String section, String fileName) {
        return section + SEPARATOR + fileName;
    }

    private void close() throws IOException {
        blobStore.getContext().close();
        blobStore = null;
    }

}
