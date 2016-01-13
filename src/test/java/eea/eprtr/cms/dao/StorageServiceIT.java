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
        uploadAndDelete(storageService);
    }

    @Test
    public void swiftTest() throws Exception {
        StorageService storageService = ctx.getBean(StorageServiceSwift.class);
        uploadAndDelete(storageService);
    }

    @Test
    public void filesTest() throws Exception {
        StorageService storageService = ctx.getBean("storageServiceFiles", StorageServiceFiles.class);
        uploadAndDelete(storageService);
    }

    private void uploadAndDelete(StorageService storageService) throws Exception {
        String testData = "ABCDEF";
        MultipartFile file = new MockMultipartFile("Testfile.txt", testData.getBytes());

        String newId = "Testfile.txt";
        storageService.save(file, SECTION);

        byte[] resultBuf = new byte[100];

        InputStream infp = storageService.getById(newId, SECTION);
        infp.read(resultBuf);
        infp.close();
        assertEquals((byte) 0, resultBuf[6]);
        assertEquals(new String(resultBuf, 0, 6, Charset.forName("US-ASCII")), testData);
        assertTrue(storageService.deleteById(newId, SECTION));

        //exception.expect(IOException.class);
        // Can't delete twice.
        assertFalse(storageService.deleteById(newId, SECTION));
    }
}
