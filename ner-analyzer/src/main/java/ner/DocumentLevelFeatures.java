/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ner.TextFileStats;
import opennlp.SentenceDetector;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author vlad.paunescu
 */
public class DocumentLevelFeatures {
    
   static Logger log = org.apache.log4j.Logger.getLogger(
            DocumentLevelFeatures.class.getName());
    
    private static final String WHITE_CHARS = "\\s+";
    private String _document;
    private File _textfile;
    private Map<String, Integer> _capitalWordsAtSentenceBeginning;
    private Map<String, Integer> _capitalWordsInsideSentence;

    public DocumentLevelFeatures(File textfile){
        _textfile = textfile;
        try {
            _document = FileUtils.readFileToString(textfile);
        } catch (IOException ex) {
            log.error(ex);
        }
        _capitalWordsAtSentenceBeginning = new HashMap<>();
        _capitalWordsInsideSentence = new HashMap<>();
    }

    public void analyze() {
        String[] sentences = splitInSentences();
        System.out.println("Found " + sentences.length + " sentences");
        for (String sentence : sentences) {
            processSentence(sentence);
        }
    }

    public void showStats() {
        System.out.println("Capital words at beginning of sentence #: "
                + _capitalWordsAtSentenceBeginning.size());
        System.out.println("Capital words at inside of sentence #: "
                + _capitalWordsInsideSentence.size());

        System.out.println("Capital words at the beginning of sentence:");
        for(Map.Entry<String, Integer> entry : _capitalWordsAtSentenceBeginning.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        System.out.println("Capital words in sentence:");
        for (Map.Entry<String, Integer> entry : _capitalWordsInsideSentence.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

    }

    private String[] splitInSentences() {
        SentenceDetector detector = new SentenceDetector("models/apache-openNLP/en-sent.bin");
        String[] sentences = detector.detect(_document);

        return sentences;
    }

    private void processSentence(String sentence) {
        if (sentence.equals("Levinson, S. (1997).")){
            System.out.println(">>>>" + sentence);
            String [] words = sentence.split(WHITE_CHARS);
            for(String word : words){
                System.out.println(word);
            }
        }
        List<String> words = new ArrayList<>(Arrays.asList(sentence.split(WHITE_CHARS)));
        WordLevelFeatures wordScanner = WordLevelFeatures.getInstance();
        
        boolean beginningOfSentence = true;
        String entity = null;

        String firstWord = words.get(0);
        if (wordScanner.isCandidateEntity(firstWord)) {
            entity = firstWord;
        }

        for (String word : words.subList(1, words.size())) {
            if (wordScanner.isCandidateEntity(word)) {
                if (entity != null) {
                    entity += " " + word;
                } else {
                    entity = word;
                }
            } else {
                if (beginningOfSentence) {
                    beginningOfSentence = false;
                    updateMap(_capitalWordsAtSentenceBeginning, entity);
                } else {
                    updateMap(_capitalWordsInsideSentence, entity);
                }
                entity = null;
            }
        }
    }

    private void updateMap(Map<String, Integer> wordMap, String word) {
        Integer count = wordMap.get(word);
        if (count == null) {
            count = 0;
        }
        wordMap.put(word, count + 1);
    }

    public static void main(String[] args) {
        String textfile = "D:/Work/NLP/corpuses/ms_academic/out/22 - "
                + "Social Science/1021291 - Wolff-Michael  Roth/JRST110.txt";
        DocumentLevelFeatures docFeatures = new DocumentLevelFeatures(new File(textfile));
        docFeatures.analyze();
        docFeatures.showStats();
    }
}
