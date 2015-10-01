/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Transfer 1.0
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. All Rights Reserved.
 *
 * Contributor(s):
 *        Søren Roug
 */
package eea.eprtr.cms.controller;

import eea.eprtr.cms.dao.UserManagementService;
import eea.eprtr.cms.model.UserRole;
import eea.eprtr.cms.model.Authorisation;
import eea.eprtr.cms.util.BreadCrumbs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * User managing controller.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final String VIEW_USERS_HTML = "view_users";
    private static final String EXISTING_USER_HTML = "existing_user";

    /**
     * Service for user management.
     */
    @Autowired
    UserManagementService userManagementService;

    @ModelAttribute("allRoles")
    public List<String> allRoles() {
        ArrayList<String> grantedAuthorities = new ArrayList<String>();
        for (UserRole authority : UserRole.values()) {
            grantedAuthorities.add(authority.toString());
        }
        return grantedAuthorities;
    }

    /**
     * View for all users.
     *
     * @param model - contains attributes for the view
     * @param message
     * @return view name
     */
    @RequestMapping({"", "/", "/view"})
    public String viewUsers(Model model, @RequestParam(required = false) String message) {
        BreadCrumbs.set(model, "Users");
        model.addAttribute("allUsers", userManagementService.getAllUsers());
        Authorisation user = new Authorisation();
        model.addAttribute("user", user);
        if(message != null) model.addAttribute("message", message);
        return VIEW_USERS_HTML;
    }

    /**
     * Adds new user to database.
     * @param user user name
     * @param redirectAttributes
     * @return view name or redirection
     */
    @RequestMapping("/add")
    public String addUser(Authorisation user, RedirectAttributes redirectAttributes) {
        String userName = user.getUserId();
        if (userName.trim().equals("")) {
            redirectAttributes.addFlashAttribute("message", "User's username cannot be empty");
            return "redirect:view";
        }

        ArrayList<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<SimpleGrantedAuthority>();
        if (user.getAuthorisations() != null) {
            for (String authority : user.getAuthorisations()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        User userDetails = new User(user.getUserId(), "", grantedAuthorities);
        if (userManagementService.userExists(userName)) {
            redirectAttributes.addFlashAttribute("message", "User " + userName + " already exists");
            return "redirect:view";
        }
        userManagementService.createUser(userDetails);
        redirectAttributes.addFlashAttribute("message", "User " + user.getUserId()
                + " added with " + rolesAsString(user.getAuthorisations()));
        return "redirect:view";
    }

    private String rolesAsString(List<String> authorisations) {
        if (authorisations == null) {
            return "no roles";
        } else {
            //return String.join(", ", authorisations); // Only works on Java 8
            return "roles " + StringUtils.collectionToDelimitedString(authorisations, ", ");
        }
    }

    /**
     * Form for editing existing user.
     * @param userName
     * @param model - contains attributes for the view
     * @param message
     * @return view name
     */
    @RequestMapping("/existing")
    public String existingUser(@RequestParam String userName, Model model,
            @RequestParam(required = false) String message) {
        model.addAttribute("userName", userName);
        BreadCrumbs.set(model, "Modify user");
        UserDetails userDetails = userManagementService.loadUserByUsername(userName);

        ArrayList<String> userRoles = new ArrayList<String>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            userRoles.add(authority.getAuthority());
        }
        Authorisation user = new Authorisation(userName, userRoles);
        model.addAttribute("user", user);
        if (message != null) model.addAttribute("message", message);
        return EXISTING_USER_HTML;
    }

    /**
     * Save user record to database.
     *
     * @param user
     * @param bindingResult
     * @param model - contains attributes for the view
     * @return view name
     */
    @RequestMapping("/edit")
    public String editUser(Authorisation user, BindingResult bindingResult, ModelMap model) {
        ArrayList<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<SimpleGrantedAuthority>();
        if (user.getAuthorisations() != null) {
            for (String authority : user.getAuthorisations()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        User userDetails = new User(user.getUserId(), "", grantedAuthorities);
        userManagementService.updateUser(userDetails);
        model.addAttribute("message", "User " + user.getUserId() + " updated with "
                + rolesAsString(user.getAuthorisations()));
        return "redirect:view";
    }

    /**
     * Deletes user.
     *
     * @param userName
     * @param model - contains attributes for the view
     * @return view name
     */
    @RequestMapping("/delete")
    public String deleteUser(@RequestParam String userName, Model model) {
        if (!userManagementService.userExists(userName)){
            model.addAttribute("message", "User " + userName + " was not deleted, because it does not exist ");
        } else {
            userManagementService.deleteUser(userName);
            model.addAttribute("message", "User " + userName + " deleted ");
        }
        return "redirect:view";
    }
}
