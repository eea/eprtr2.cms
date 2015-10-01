package eea.eprtr.cms.dao;
 
import java.util.List;
 
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
 
import javax.sql.DataSource;
import eea.eprtr.cms.model.Culture;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import org.unitils.UnitilsJUnit4;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.database.annotations.TestDataSource;
 
// See http://www.journaldev.com/2593/spring-jdbc-and-jdbctemplate-crud-with-datasource-example-tutorial
@DataSet("seed-culture.xml")
public class CultureServiceTest extends UnitilsJUnit4 {
 
    @TestDataSource
    private DataSource dataSource;

    private CultureServiceJdbc cultureService;

    @Before
    public void setUp() {
        cultureService = new CultureServiceJdbc();
        cultureService.setDataSource(dataSource);
    }

    @Test
    public void simpleTest() {
        //Get the Spring Context
        //ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-dbtest-config.xml");
         
        //Get the CultureService Bean from the context.
        //CultureService cultureService = ctx.getBean("cultureService", CultureService.class);
         
        //Read
        Culture doc1 = cultureService.getByEnglishName("Dutch");
        assertNotNull(doc1);
        assertEquals("4", doc1.getCode());
         
        //Get All
        List<Culture> docList = cultureService.getAll();
        assertTrue(docList.size() > 0);

        //Close Spring Context
        //ctx.close();
    }
 
}
