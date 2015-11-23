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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring-mvc-config.xml", "classpath:spring-db-config.xml",
   "classpath:spring-security.xml"})

/**
 * Test the simple doc controller.
 */
public class FileUploadControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testUploadForm() throws Exception {
        this.mockMvc.perform(get("/filecatalogue"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(view().name("uploads"));
    }

    /**
     * This upload is expected to succeed.
     */
    @Test
    public void goodUpload() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "ABCDEF".getBytes("UTF-8"));
        mockMvc.perform(fileUpload("/filecatalogue")
                .file(mockFile)
                .with(csrf()).with(user("admin").roles("AUTHOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("filecatalogue"))
                .andExpect(flash().attributeCount(2));
    }

}
