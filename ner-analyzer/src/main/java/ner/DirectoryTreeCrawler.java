/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad.paunescu
 */
public class DirectoryTreeCrawler {
    
    private final String[] TEXT_EXTENSIONS = {"txt"};
    
    private String _rootdir;
    private File _root;
    
    public DirectoryTreeCrawler(String rootdir){
        _rootdir = rootdir;
        _root = new File(_rootdir);
    }
    
    public void processTextFiles(){
        Collection<File> textFiles = FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
        for(File textFile : textFiles){
            System.out.println(textFile.getName());
        }
        
    }
    
    public static void main(String[] args){
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        dirCrawler.processTextFiles();
    }
    
}
