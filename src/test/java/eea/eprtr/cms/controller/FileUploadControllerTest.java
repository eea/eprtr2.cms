package eea.eprtr.cms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor.*;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring-mvc-config.xml",
                    "classpath:spring-db-config.xml",
                    "classpath:spring-security.xml"})

/**
 * Test the File storage controller.
 */
public class FileUploadControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
        .addFilters(this.springSecurityFilterChain)
        .build();
    }

    /**
     * Since it is protected, it will redirect to login.
     */
    @Test
    public void testUploadForm() throws Exception {
        this.mockMvc.perform(get("/filecatalogue"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * This will succeed.
     */
    @Test
    public void authenticatedUploadForm() throws Exception {
        this.mockMvc.perform(get("/filecatalogue")
                .with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(view().name("uploads"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    /**
     * No authentication.
     */
    @Test
    public void failUpload() throws Exception {
        mockMvc.perform(fileUpload("/filecatalogue")
                .file("file", "ABCDEF".getBytes("UTF-8")))
                .andExpect(status().isForbidden());
    }

    /**
     * This upload is expected to succeed.
     */
    @Test
    public void goodUpload() throws Exception {
        uploadFile("uploaded-file.txt", "ABCDEF");
        uploadFile("uploaded-file.csv", "A,B,C,D,E,F");

        // Check that it is there.
        mockMvc.perform(get("/docs/uploaded-file.txt")
                .with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/docs/uploaded-file.doc")
                .with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/docs/uploaded-file")
                .with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().isNotFound());
    }

    /**
     * Helper method
     */
    private void uploadFile(String originalFilename, String content) throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", originalFilename, "text/plain", content.getBytes("UTF-8"));
        mockMvc.perform(fileUpload("/filecatalogue")
                .file(mockFile)
                .with(csrf()).with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("filecatalogue"))
                .andExpect(flash().attributeCount(2));
    }

    /**
     * Attempt to download a non-existent file.
     */
    @Test
    public void downloadNotFound() throws Exception {
        mockMvc.perform(get("/docs/no-such-file")
                .with(user("admin").roles("EXTRANET-EPRTR-EPRTRCMS")))
                .andExpect(status().isNotFound())
                .andExpect(view().name("filenotfound"));
    }

}
