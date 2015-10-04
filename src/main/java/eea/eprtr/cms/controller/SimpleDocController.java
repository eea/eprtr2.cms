package eea.eprtr.cms.controller;

import eea.eprtr.cms.model.SimpleDoc;
import eea.eprtr.cms.dao.SimpleDocService;
import eea.eprtr.cms.util.BreadCrumbs;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller class is meant to be super-simple to understand for beginners.
 * We could have optimized it using wildcards in order to have less methods, but that would have made it harder to read.
 */

@Controller
public class SimpleDocController {

    @Autowired
    private SimpleDocService simpleDocService;

    /**
     * Frontpage.
     */
    @RequestMapping(value = "/")
    public String frontpage(Model model) {
        // This is toplevel. No breadcrumbs.
        BreadCrumbs.set(model);
        return "index";
    }

    /**
     * Text edit.
     */
    @RequestMapping(value = "/statictexts")
    public String staticTexts(Model model) {
        model.addAttribute("title", "Static text editor");
        model.addAttribute("texts", simpleDocService.getAll());
        // This is toplevel. No breadcrumbs.
        BreadCrumbs.set(model);
        return "statictexts";
    }

    /**
     * About.
     */
    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about(Model model) {
        loadFromDB("AboutPageContent", model);
        return "simplecontent";
    }

    /**
     * Redirects to welcome page after login.
     *
     * @param model holder for model attributes
     * @return view name
     */
    @RequestMapping(value = "/login")
    public String login(Model model) {
        return frontpage(model);
    }

    /**
     * Shows page which allows to perform SingleSignOut.
     *
     * @return view name
     */
    @RequestMapping(value = "/logout")
    public String logout() {
        return "logout_all_apps";
    }

    /**
     * TODO: Add param
     */
    @RequestMapping(value = "/edittext", method = RequestMethod.GET)
    public String editpage(@RequestParam("key") String page, Model model) {
        SimpleDoc doc = simpleDocService.getByResourceKey(page);
        model.addAttribute("pagetitle", doc.getTitle());
        model.addAttribute("content", doc.getContent());
        model.addAttribute("title", "Edit page");
        return "editpage";
    }

    private void loadFromDB(String name, Model model) {
        SimpleDoc doc = simpleDocService.getByResourceKey(name);
        if (doc == null)
            System.out.println("Doc was null");
        model.addAttribute("title", doc.getTitle());
        model.addAttribute("content", doc.getContent());
        BreadCrumbs.set(model, doc.getTitle());
    }
}
