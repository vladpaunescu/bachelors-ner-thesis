/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
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
    
    public void listTextFiles(){
        Collection<File> textFiles = FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
        for(File textFile : textFiles){
            System.out.println(textFile.getName());
        }
    }
    
    public void annotateTextFiles(){
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        StanfordNerAnnotator annotator = new StanfordNerAnnotator(pipeline);
        
        Collection<File> textFiles = FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
        for(File textFile : textFiles){
            annotator.annotateFile(textFile);
        }
    }
    
    public Collection<File> getTextFiles(){
        return FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
    }
    
    
    public static void main(String[] args){
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        dirCrawler.annotateTextFiles();
    }
    
}
