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
 * Controller for text pages.
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
        String title = "Static text editor";
        model.addAttribute("title", title);
        model.addAttribute("texts", simpleDocService.getAll());
        BreadCrumbs.set(model, title);
        return "statictexts";
    }

    /**
     * Netws edit.
     */
    @RequestMapping(value = "/newstexts")
    public String newsTexts(Model model) {
        String title = "News text editor";
        model.addAttribute("title", title);
        model.addAttribute("texts", simpleDocService.getAll());
        BreadCrumbs.set(model, title);
        return "newstexts";
    }

    /**
     * About.
     */
    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about(Model model) {
        String title = "About";
        model.addAttribute("title", title);
        BreadCrumbs.set(model, title);
        return "about";
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
    public String editpage(@RequestParam("key") int page, Model model) {
        SimpleDoc document = simpleDocService.getByResourceValueID(page);
        //model.addAttribute("pagetitle", doc.getTitle());
        //model.addAttribute("content", doc.getContent());
        //model.addAttribute("allowHTML", doc.getAllowHTML());
        model.addAttribute("document", document);
        model.addAttribute("title", "Edit page");
        return "editpage";
    }

    /**
     * Saving the page.
     */
    @RequestMapping(value = "/edittext", method = RequestMethod.POST)
    public String savepage(SimpleDoc doc, Model model) {
        SimpleDoc docInDb = simpleDocService.getByResourceValueID(doc.getResourceValueID());
        docInDb.setTitle(doc.getTitle());
        docInDb.setContent(doc.getContent());
        simpleDocService.save(docInDb);
        model.addAttribute("document", docInDb);
        model.addAttribute("title", "Edit page");
        model.addAttribute("message", "Text is updated");
        return "editpage";
    }


    private void loadFromDB(int name, Model model) {
        SimpleDoc doc = simpleDocService.getByResourceValueID(name);
        if (doc == null)
            System.out.println("Doc was null");
        model.addAttribute("title", doc.getTitle());
        model.addAttribute("content", doc.getContent());
        BreadCrumbs.set(model, doc.getTitle());
    }
}
