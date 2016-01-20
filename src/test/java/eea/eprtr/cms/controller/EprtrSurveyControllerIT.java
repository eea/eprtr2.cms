package eea.eprtr.cms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor.*;
import static org.hamcrest.Matchers.*;

import eea.eprtr.cms.model.EprtrSurvey;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        "classpath:spring-securitytest-config.xml"})

/**
 * Test the simple doc controller.
 */
public class EprtrSurveyControllerIT {

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
    public void testUnauthenticatedAbout() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testAbout() throws Exception {
        this.mockMvc.perform(get("/about").with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(model().attributeExists("title"))
                .andExpect(view().name("about"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    public void getStaticTexts() throws Exception {
        this.mockMvc.perform(get("/eprtrsurvey").with(user("uploader")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("texts"))
                .andExpect(view().name("eprtrsurvey"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }


    @Test
    public void loadText() throws Exception {
        this.mockMvc.perform(get("/editsurvey")
                .param("surveyID", "3")
                .with(user("uploader")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("title", "document"))
                .andExpect(model().attribute("document", isA(EprtrSurvey.class)))
                .andExpect(view().name("editsurvey"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }
    
}
