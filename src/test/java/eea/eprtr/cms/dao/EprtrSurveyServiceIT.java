package eea.eprtr.cms.dao;

import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import eea.eprtr.cms.model.EprtrSurvey;
import eea.eprtr.cms.model.EprtrSurveyItem;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class EprtrSurveyServiceIT {

    @Test
    public void simpleTest() {
        //Get the Spring Context
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-db-config.xml");

        //Get the EprtrSurveyService Bean from the context.
        EprtrSurveyService eprtrSurveyService = ctx.getBean("eprtrSurveyService", EprtrSurveyService.class);

        //Run some tests for JDBC CRUD operations
        EprtrSurvey surv = new EprtrSurvey();
        surv.setSurveyID(99);
        surv.setSurveyLabel("Test Question");
        surv.setSurveyText("What does test mean to you?");
        surv.setListIndex(88);

        EprtrSurveyItem survi = new EprtrSurveyItem();
        survi.setSurveyItemID(99);
        survi.setSurveyItem("Test Answer");
        survi.setListIndex(88);
        survi.setFkSurveyID(99);
        survi.setSurveyItemResultID("99a");

        //Read
        EprtrSurvey surv1 = eprtrSurveyService.getBySurveyID(1);
        assertNotNull(surv1);

        //Get All
        List<EprtrSurvey> survList = eprtrSurveyService.getAll();
        assertTrue(survList.size() > 0);

        //Close Spring Context
        ctx.close();
    }

}
