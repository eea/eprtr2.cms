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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

        List<Upload> uploads = storageService.getIndex("docs");
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

    private String storeFile(MultipartFile myFile) throws IOException {
        String fileName = storageService.save(myFile, "docs");
        String userName = getUserName();
        logger.info("Uploaded: " + myFile.getOriginalFilename() + " by " + userName);
        return fileName;
    }

    /**
     * Helper method to get authenticated userid.
     */
    private String getUserName() {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
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
     * Extension is stripped by Spring unless reconfigured.
     * @see <a href="http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-requestmapping-suffix-pattern-match">Suffix Pattern Matching</a>
     * @see <a href="http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/mvc.html#mvc-config-path-matching">21.16.11 Path Matching</a>
     */
    @RequestMapping(value = "/docs/{file_name}", method = RequestMethod.GET)
    public void downloadFile(
        @PathVariable("file_name") String fileId, HttpServletResponse response) throws IOException {

        long fileSize = 0;
        InputStream iStream = null;
        try {
            fileSize = storageService.getSizeById(fileId, "docs");
            iStream = storageService.getById(fileId, "docs");
        } catch (Exception e) {
            throw new java.io.FileNotFoundException(fileId);
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Length", Long.toString(fileSize));
        response.setHeader("Content-Disposition", "attachment; filename=" + fileId);

        org.apache.commons.io.IOUtils.copy(iStream, response.getOutputStream());
        response.flushBuffer();
        iStream.close();
    }

    /*
    @RequestMapping(value = "/delete/{file_name}")
    public String deleteFile(
        @PathVariable("file_name") String fileId, final Model model) throws IOException {
        model.addAttribute("filename", fileId);
        return "deleteConfirmation";
    }
    */

    /**
     * Delete files by filename.
     *
     * @param ids - list of filenames
     */
    @RequestMapping(value = "/deletefiles", method = RequestMethod.POST)
    public String deleteFiles(@RequestParam("filename") List<String> ids,
            final RedirectAttributes redirectAttributes) throws IOException {
        for (String fileId : ids) {
            storageService.deleteById(fileId, "docs");
        }
        redirectAttributes.addFlashAttribute("message", "File(s) deleted");
        return "redirect:/";
    }

    @ExceptionHandler(java.io.FileNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String filenotFoundError(HttpServletRequest req, Exception exception) {
        return "filenotfound";
    }

}

