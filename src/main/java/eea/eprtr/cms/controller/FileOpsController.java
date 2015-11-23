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
 * The Original Code is EPRTR CMS 2.0
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. All Rights Reserved.
 *
 * Contributor(s):
 *        Søren Roug
 */
package eea.eprtr.cms.controller;

import eea.eprtr.cms.dao.StorageService;
import eea.eprtr.cms.model.Upload;
import eea.eprtr.cms.util.BreadCrumbs;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * File operations - upload, download, delete.
 */
@Controller
public class FileOpsController {

    @Autowired
    private StorageService storageService;

    private Log logger = LogFactory.getLog(FileOpsController.class);

    /**
     * View uploaded files.
     */
    @RequestMapping(value = "/filecatalogue", method = RequestMethod.GET)
    public String findUploads(Model model) {
        String pageTitle = "File catalogue";

        List<Upload> uploads = storageService.getIndex();
        model.addAttribute("uploads", uploads);
        model.addAttribute("title", pageTitle);
        BreadCrumbs.set(model, pageTitle);
        return "uploads";
    }

    /**
     * Upload file for transfer.
     */
    @RequestMapping(value = "/filecatalogue", method = RequestMethod.POST)
    public String importFile(@RequestParam("file") MultipartFile myFile,
                        final RedirectAttributes redirectAttributes,
                        final HttpServletRequest request) throws IOException {

        if (myFile == null || myFile.getOriginalFilename() == null) {
            redirectAttributes.addFlashAttribute("message", "Select a file to upload");
            return "redirect:filecatalogue";
        }
        String fileName = storeFile(myFile);
        redirectAttributes.addFlashAttribute("filename", fileName);
        StringBuffer requestUrl = request.getRequestURL();
        redirectAttributes.addFlashAttribute("url", requestUrl.substring(0, requestUrl.length() - "/filecatalogue".length()));
        return "redirect:filecatalogue";
    }

    /**
     * AJAX Upload file for transfer.
     */
    /*
    @RequestMapping(value = "/filecatalogue", method = RequestMethod.POST, params="ajaxupload=1")
    public void importFileWithAJAX(@RequestParam("file") MultipartFile myFile,
                        HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (myFile == null || myFile.getOriginalFilename() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Select a file to upload");
            return;
        }
        String fileName = storeFile(myFile);
        response.setContentType("text/xml");
        PrintWriter printer = response.getWriter();
        StringBuffer requestUrl = request.getRequestURL();
        String url = requestUrl.substring(0, requestUrl.length() - "/filecatalogue".length());
        printer.println("<?xml version='1.0'?>");
        printer.println("<package>");
        printer.println("<downloadLink>" + url + "/download/" + fileName + "</downloadLink>");
        printer.println("<deleteLink>" + url + "/delete/" + fileName + "</deleteLink>");
        printer.println("</package>");
        printer.flush();
        response.flushBuffer();
    }
    */

    private String storeFile(MultipartFile myFile) throws IOException {
        String fileName = storageService.save(myFile);
        String userName = getUserName();
        logger.info("Uploaded: " + myFile.getOriginalFilename() + " by " + userName);
        return fileName;
    }

    /**
     * Helper method to get authenticated userid.
     */
    private String getUserName() {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        //if (auth == null) {
        //    throw new IllegalArgumentException("Not authenticated");
        //}
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    /**
     * Page to show upload success.
     */
    /*
    @RequestMapping(value = "/uploadSuccess")
    public String uploadResult(Model model, HttpServletRequest request) {
        String pageTitle = "File uploaded";
        model.addAttribute("title", pageTitle);
        BreadCrumbs.set(model, pageTitle);
        StringBuffer requestUrl = request.getRequestURL();
        model.addAttribute("url", requestUrl.substring(0, requestUrl.length() - "/uploadSuccess".length()));
        return "uploadSuccess";
    }
    */

    /**
     * Download a file.
     * FIXME: Extension is stripped by Spring. See http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-requestmapping-suffix-pattern-match
     */
    @RequestMapping(value = "/docs/{file_name}", method = RequestMethod.GET)
    public void downloadFile(
        @PathVariable("file_name") String fileId, HttpServletResponse response) throws IOException {
        // FIXME. Verify fileId for ../
        long fileSize = storageService.getSizeById(fileId);
        InputStream is = null;
        is = storageService.getById(fileId);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Length", Long.toString(fileSize));
        response.setHeader("Content-Disposition", "attachment; filename=" + fileId);

        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
        is.close();
    }

    @RequestMapping(value = "/delete/{file_name}")
    public String deleteFile(
        @PathVariable("file_name") String fileId, final Model model) throws IOException {
        model.addAttribute("filename", fileId);
        return "deleteConfirmation";
    }

    /**
     * Delete files by filename.
     *
     * @param ids - list of filenames
     */
    @RequestMapping(value = "/deletefiles", method = RequestMethod.POST)
    public String deleteFiles(@RequestParam("id") List<String> ids,
            final RedirectAttributes redirectAttributes) throws IOException {
        for (String fileId : ids) {
            storageService.deleteById(fileId);
        }
        redirectAttributes.addFlashAttribute("message", "File(s) deleted");
        return "redirect:/";
    }

}

