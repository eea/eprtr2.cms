package eea.eprtr.cms.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eea.eprtr.cms.dao.EprtrSurveyService;
import eea.eprtr.cms.model.EprtrSurvey;
import eea.eprtr.cms.model.EprtrSurveyItem;
import eea.eprtr.cms.util.BreadCrumbs;

/**
 * Controller for text pages.
 */

@Controller
public class EprtrSurveyController {

    @Autowired
    private EprtrSurveyService eprtrSurveyService;
    
    /**
     * Survey list.
     */
    @RequestMapping(value = "/eprtrsurvey")
    public String eprtrSurvey(Model model) {
        String title = "E-PRTR Survey";
        model.addAttribute("title", title);
        model.addAttribute("texts", eprtrSurveyService.getAll());
        BreadCrumbs.set(model, title);
        return "eprtrsurvey";
    }

    @RequestMapping(value = "/eprtrsurvey", params={"add"})
    public String add(EprtrSurvey eprtrSurvey, BindingResult bindingResult, ModelMap model) {
    	this.eprtrSurveyService.add(eprtrSurveyService.getAll().size());

    	String title = "E-PRTR Survey";
        model.addAttribute("title", title);
        model.addAttribute("texts", eprtrSurveyService.getAll());
        model.addAttribute("message", "List is updated");
    	return "redirect:/eprtrsurvey";
    }
    
    @RequestMapping(value = "/eprtrsurvey", params={"delete"})
    public String delete(EprtrSurvey eprtrSurvey, BindingResult bindingResult, HttpServletRequest req, ModelMap model) {
    	final Integer surveyId = Integer.valueOf(req.getParameter("delete"));
    	/**
    	 * Delete items first	
    	 */
    	EprtrSurvey surv = eprtrSurveyService.getBySurveyID(surveyId);
    	for(EprtrSurveyItem item : surv.getEprtrSurveyItems()){
    		this.eprtrSurveyService.deleteItem(item.getSurveyItemID());
    	}
    	this.eprtrSurveyService.delete(surveyId);
        String title = "E-PRTR Survey";
        model.addAttribute("title", title);
        model.addAttribute("texts", eprtrSurveyService.getAll());
        model.addAttribute("message", "List is updated");
    	return "redirect:/eprtrsurvey";
    }
        
    /**
     * TODO: Add param
     */
    @RequestMapping(value = "/editsurvey", method = RequestMethod.GET)
    public String editsurvey(@RequestParam("surveyID") int surveyID, Model model) {
    	EprtrSurvey surv = eprtrSurveyService.getBySurveyID(surveyID);
        model.addAttribute("document", surv);
        model.addAttribute("title", "Edit Survey");
        return "editsurvey";
    }

    /**
     * Saving the page.
     */
  /*  @RequestMapping(value = "/editsurvey", method = RequestMethod.POST)
    public String savepage(EprtrSurvey surv, Model model) {
    	this.eprtrSurveyService.save(surv);
        model.addAttribute("document", surv);
        model.addAttribute("title", "Edit Survey");
        model.addAttribute("message", "Text is updated");
        return "savesurvey";
    }*/
    
    @RequestMapping(value = "/editsurvey", params={"save"})
    public String save(EprtrSurvey surv, BindingResult bindingResult, ModelMap model) {
        if (bindingResult.hasErrors()) {
            return "eprtrsurvey";
        }
        this.eprtrSurveyService.save(surv);
        //model.clear();
        model.addAttribute("document", surv);
        model.addAttribute("title", "Edit Survey");
        model.addAttribute("message", "Text is updated");
        return "editsurvey";
    }


    @RequestMapping(value = "/editsurvey", params={"addAnswer"})
    public String addAnswer(EprtrSurvey surv, BindingResult bindingResult, ModelMap model) {
    	EprtrSurveyItem newItem = this.eprtrSurveyService.newItem(surv.getSurveyID(), surv.getEprtrSurveyItems().size());
    	//surv.getEprtrSurveyItems().add();
    	EprtrSurvey surv2 = eprtrSurveyService.getBySurveyID(surv.getSurveyID());
        model.addAttribute("document", surv2);
        model.addAttribute("title", "Edit Survey");
        model.addAttribute("message", "Text is updated");
    	return "editsurvey";
    }
    
    @RequestMapping(value = "/editsurvey", params={"deleteAnswer"})
    public String deleteAnswer(EprtrSurvey surv, BindingResult bindingResult, HttpServletRequest req, ModelMap model) {
    	final Integer surveyItemId = Integer.valueOf(req.getParameter("deleteAnswer"));
    	this.eprtrSurveyService.deleteItem(surveyItemId);
    	EprtrSurvey surv2 = eprtrSurveyService.getBySurveyID(surv.getSurveyID());
        model.addAttribute("document", surv2);
        model.addAttribute("title", "Edit Survey");
        model.addAttribute("message", "Text is updated");
    	return "editsurvey";
    }
    
}