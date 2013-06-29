/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import pdfparser.DirectoryTreeCrawler;

/**
 *
 * @author vlad
 */
public class WordCountStatistics {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            WordCountStatistics.class.getName());
    private Map<String, Integer> _wordCount;
    private TreeMap<String, Integer> _sortedWords;
    private int _toatalWordsCount;
    private String _rootDirName;
    private DirectoryTreeCrawler _crawler;

    public WordCountStatistics(String rootDir) {
        _rootDirName = rootDir;
        _crawler = new DirectoryTreeCrawler(rootDir);
        _wordCount = new HashMap<>();
        _toatalWordsCount = 0;

    }

    public void countWords() {
        Collection<File> properFiles = _crawler.getGenericFiles();
        int total = properFiles.size();
        log.info(String.format("Total files to process %s", total));
        int count = 1;
        log.info("Total files to process " + total);
        for (File properFile : properFiles) {

            log.info(String.format("Processing file %d of %d : %s", count, total, properFile.getAbsolutePath()));
            countWordsForFile(properFile);
            count++;
        }

        showStatistics();
    }

    private void countWordsForFile(File properFile) {
        String text = "";
        try {
            text = FileUtils.readFileToString(properFile, "UTF-8");
        } catch (IOException ex) {
            log.error(ex);
        }

        String words[] = text.split("\\s+");
        System.out.println("Total words count " + words.length);
        _toatalWordsCount += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (isWord(word)) {
                if (_wordCount.get(word) != null) {
                    _wordCount.put(word, _wordCount.get(word) + 1);
                } else {
                    _wordCount.put(word, 1);
                }
            }
        }
    }

    public void showStatistics() {
        int vocabularySize = _wordCount.size();
        System.out.println("Vocabulary size: " + vocabularySize);
        System.out.println("Total words count: " + _toatalWordsCount);
        ReverseValueComparator rvl = new ReverseValueComparator(_wordCount);
        _sortedWords = new TreeMap<>(rvl);
        _sortedWords.putAll(_wordCount);

        int limit = 100;
        int count = 0;
        for (String key : _sortedWords.navigableKeySet()) {
            System.out.println(_wordCount.get(key) + " " + key);
            count++;
            if (count == limit) {
                break;
            }
        }
    }
    
    private boolean isWord(String word){
        return word.matches("[a-z'\\?!\\(\\)\\[\\]\\{\\}]+");
    }
            

    public static void main(String[] args) {
        String rootDir = "D:/Work/NLP/corpuses/ms_academic/brat-data/generic";
        WordCountStatistics stats = new WordCountStatistics(rootDir);
        stats.countWords();
    }
}
