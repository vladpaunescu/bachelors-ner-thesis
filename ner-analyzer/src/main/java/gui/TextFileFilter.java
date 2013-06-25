/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad
 */
class TextFileFilter extends FileFilter {

    public TextFileFilter() {
    }
    
    
    
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()){
            return true;
        }
        
        String extension = FilenameUtils.getExtension(pathname.getAbsolutePath());
        if(extension  != null){
            if (extension.equals("txt")){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Text Files";
    }
    
}
