package eea.eprtr.cms.controller;


/**
 * An application-specific file-not-found exception. 
 */
public class FileNotFoundException extends RuntimeException {

        public FileNotFoundException(String filename) {
                super(filename + " not found");
        }
}
