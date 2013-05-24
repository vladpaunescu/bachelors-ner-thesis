/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad.paunescu
 */
public class TextFileStats {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            TextFileStats.class.getName());
    private static final String WHITE_CHARS = "\\s+";
    private static final String END_OF_SENTENCE_REGEXP = ".*[\\.\\?!]";
    private File _textfile;
    private String _text;
    private Map<String, Integer> _capitalWordsAtBeginningOfSentence;
    private Map<String, Integer> _capitalWordsInSentence;

    public TextFileStats(File textfile) {
        _textfile = textfile;
        try {
            _text = FileUtils.readFileToString(textfile);
        } catch (IOException ex) {
            log.error(ex);
        }
        
        _capitalWordsAtBeginningOfSentence = new HashMap<>();
        _capitalWordsInSentence = new HashMap<>();

    }

    public void analyze() {
        String[] words = _text.split(WHITE_CHARS);
        boolean beginingOfSentence = true;
        String capitalWord = null;
        for (String word : words) {

            if (isCapitalWord(word)) {                
                if (beginingOfSentence) {
                    updateMap(_capitalWordsAtBeginningOfSentence, word);
                    beginingOfSentence = false;

                } else {
                    
                    if (capitalWord != null){
                        capitalWord += " " + word;
                        updateMap(_capitalWordsInSentence, capitalWord);
                    }
                }
            }
            else {
                capitalWord = null;
            }

            if (endOfSentence(word)) {
                beginingOfSentence = true;
            }
        }
    }
    
    public void showStats(){
        System.out.println("Capital words at beginning of sentence #: " +
                _capitalWordsAtBeginningOfSentence.size());
        System.out.println("Capital words at inside of sentence #: " +
                _capitalWordsInSentence.size());
        
        System.out.println("Capital words at the beginning of sentence:");
        for(Map.Entry<String, Integer> entry : _capitalWordsAtBeginningOfSentence.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        
        System.out.println("Capital words in sentence:");
        for(Map.Entry<String, Integer> entry : _capitalWordsInSentence.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        
    }

    private void updateMap(Map<String, Integer> wordMap, String word) {
        Integer count = wordMap.get(word);
        if (count == null) {
            count = 0;
        }
        wordMap.put(word, count + 1);
    }

    private boolean endOfSentence(String word) {
        return word.matches(END_OF_SENTENCE_REGEXP);
    }

    private boolean isCapitalWord(String word) {
        char firstChar = word.charAt(0);
        return firstChar >= 'A' && firstChar < 'Z';
    }

    public static void main(String[] args) {
        String textfile = "D:/Work/NLP/corpuses/ms_academic/out/22 - "
                + "Social Science/1021291 - Wolff-Michael  Roth/JRST110.txt";
        TextFileStats fileStats = new TextFileStats(new File(textfile));
        fileStats.analyze();
        fileStats.showStats();
    }
}
