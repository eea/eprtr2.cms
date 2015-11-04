package eea.eprtr.cms.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

import eea.eprtr.cms.model.Culture;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mvctest-config.xml",
        "classpath:spring-dbtest-config.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/seed-culture.xml")
public class CultureServiceIT {

    @Autowired
    private CultureService cultureService;

    @Test
    public void simpleTest() {
        //Read something that was loaded from seed-culture.xml
        Culture doc1 = cultureService.getByEnglishName("Romanian");
        assertNotNull(doc1);
        assertEquals("18", doc1.getCode());

        //Get All
        List<Culture> docList = cultureService.getAll();
        assertTrue(docList.size() > 0);

    }
}
