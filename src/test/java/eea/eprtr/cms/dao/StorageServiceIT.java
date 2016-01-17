package eea.eprtr.cms.dao;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import eea.eprtr.cms.model.Upload;
//import eea.eprtr.cms.controller.FileNotFoundException;
import java.io.FileNotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring-mvc-config.xml",
        "classpath:spring-db-config.xml", "classpath:spring-storagetest-config.xml"})

/**
 * Test the file operations.
 */
public class StorageServiceIT {

    private static String SECTION = "doc";

    @Autowired
    private WebApplicationContext ctx;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void productionTest() throws Exception {
        StorageService storageService = ctx.getBean("storageService", StorageService.class);
        runSuite(storageService);
    }

    @Test
    public void filesTest() throws Exception {
        StorageService storageService = ctx.getBean("storageServiceFiles", StorageServiceFiles.class);
        runSuite(storageService);
    }

    @Test
    public void blobTest() throws Exception {
        StorageService storageService = ctx.getBean(StorageServiceBlob.class);
        runSuite(storageService);
    }

    //@Test
    public void swiftTest() throws Exception {
        StorageService storageService = ctx.getBean(StorageServiceSwift.class);
        runSuite(storageService);
    }

    private void runSuite(StorageService storageService) throws Exception {
        uploadAndDelete(storageService);
        upload10Files(storageService);
        downloadNonExistant(storageService);
    }

    private void uploadAndDelete(StorageService storageService) throws Exception {
        String testData = "ABCDEF";
        String newId = "Testfile.txt";
        MultipartFile file = new MockMultipartFile(newId, newId, "text/plain", testData.getBytes());

        storageService.save(file, SECTION);

        byte[] resultBuf = new byte[100];

        InputStream infp = storageService.getById(newId, SECTION);
        infp.read(resultBuf);
        infp.close();
        // Check that we read the same size as we wrote
        assertEquals((byte) 0, resultBuf[6]);
        // Check that we read the same as we wrote.
        assertEquals(new String(resultBuf, 0, 6, Charset.forName("US-ASCII")), testData);
        // Check delete
        assertTrue(storageService.deleteById(newId, SECTION));

        // Can't delete twice.
        assertFalse(storageService.deleteById(newId, SECTION));
    }

    private void upload10Files(StorageService storageService) throws Exception {
        String testData = "ABCDEF";
        final int BATCH_SIZE = 10;

        // Put a test file in the "documents" section - longer section name than "doc"
        String documentId = "Document here.txt";
        MultipartFile file = new MockMultipartFile(documentId, documentId, "text/plain", testData.getBytes());
        storageService.save(file, "documents");

        for (int i = 0; i < BATCH_SIZE; i++) {
            String newId = "Testfile-" + String.valueOf(i) + ".txt";
            MultipartFile testFile = new MockMultipartFile(newId, newId, "text/plain", newId.getBytes());
            storageService.save(testFile, SECTION);
        }
        List<Upload> files = storageService.getIndex(SECTION);
        assertEquals(BATCH_SIZE, files.size());
        for (Upload upload : files) {
            String fileName = upload.getFilename();
            assertTrue(fileName.startsWith("Testfile-"));
            assertEquals(fileName.getBytes().length, upload.getSize());
        }

        // Delete the files.
        for (Upload upload : files) {
            assertTrue(storageService.deleteById(upload.getFilename(), SECTION));
        }
        assertTrue(storageService.deleteById(documentId, "documents"));
    }

    private void downloadNonExistant(StorageService storageService) throws Exception {
        exception.expect(FileNotFoundException.class);
        InputStream infp = storageService.getById("Non-existant file.xml", SECTION);
    }
}
