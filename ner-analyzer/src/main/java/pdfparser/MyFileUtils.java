/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad.paunescu
 */
public class MyFileUtils {
    
    static Logger log = Logger.getLogger(
                      MyFileUtils.class.getName());
    
    
    public static String getFileExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        
        return extension;   
    }
    
    public static String getFileNoExtension(String filename){
        String extension = getFileExtension(filename);
        if (!extension.equals("")){
            return filename.substring(0, filename.lastIndexOf(extension) - 1);
        }
        return filename;
    }
    
    static class DirectoryFileFilter implements FileFilter{

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }
    
    static class PdfFileFilter implements FileFilter{
        final static String PDF_TYPE = "application/pdf";
        final static String PDF_EXTENSION = "pdf";
        @Override
        public boolean accept(File pathname) {
            if(pathname.isDirectory()){
                return false;
            }
            
            Path path = FileSystems.getDefault().getPath(pathname.getAbsolutePath());
            try {
                String contentType = Files.probeContentType(path);
                boolean isPdf = contentType != null && contentType.equals(PDF_TYPE);
                if(isPdf && !FilenameUtils.getExtension(pathname.getName()).equals(PDF_EXTENSION)){
                    log.info("File " + pathname.getName() + "is PDF, but no pdf extension");
                }
                return isPdf;
            } catch (IOException ex) {
                log.error(ex);
            }
            
            return false;
              
        }
    }
    
    public static void main(String[] args){
        String path ="D:/path/test.pdf";
        System.out.println("File no extension " + getFileNoExtension(path));
        
    }
    
}
