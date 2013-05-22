/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdf_parser;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.log4j.Logger;

/**
 *
 * @author vlad.paunescu
 */
public class FileUtils {
    
    static Logger log = Logger.getLogger(
                      FileUtils.class.getName());
    
    public static String getFileExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        
        return extension;
        
    }
    
    static class DirectoryFileFilter implements FileFilter{

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }
    
    static class PdfFileFilter implements FileFilter{
        final static String pdfType = "application/pdf";
        @Override
        public boolean accept(File pathname) {
            if(pathname.isDirectory()){
                return false;
            }
            
            Path path = FileSystems.getDefault().getPath(pathname.getAbsolutePath());
            try {
                String contentType = Files.probeContentType(path);
                boolean isPdf = contentType != null && contentType.equals(pdfType);
                if(isPdf && !FileUtils.getFileExtension(pathname.getName()).equals("pdf")){
                    log.info("File " + pathname.getName() + "is PDF, but no pdf extension");
                }
                return isPdf;
            } catch (IOException ex) {
                log.error(ex);
            }
            
            return false;
              
        }
        
    }
    
}
