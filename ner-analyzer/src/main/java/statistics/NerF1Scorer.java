/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import annotators.NerAnnotation;
import annotators.StanfordNerAnnotator;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pdfparser.DirectoryTreeCrawler;

/**
 *
 * @author vlad
 */
public class NerF1Scorer {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            NerF1Scorer.class.getName());
    private static final String MODEL_5_CLASS = "D:/Work/NLP/corpuses/ms_academic/models/5-class.ser.gz";
    private static final String MODEL_14_CLASS = "D:/Work/NLP/corpuses/ms_academic/models/14-class.ser.gz";
      private static final String CONLL_MODEL = DefaultPaths.DEFAULT_NER_CONLL_MODEL;
    private String _dirname;
    private DirectoryTreeCrawler _crawler;
    private StanfordNerAnnotator _annotator;
    
    private double _truePositives;
    private double _trueNegatives;
    private double _falsePositives;
    private double _falseNegatives;
    
    private double _precission;
    private double _recall;
    private double _f1score;

    public NerF1Scorer(String testDir) {
        _dirname = testDir;
        _crawler = new DirectoryTreeCrawler(testDir);
        
        _truePositives = _trueNegatives = _trueNegatives = _falseNegatives = 0;
    }

    public void computeScores() {
        
        _annotator = initializeStanfordAnnotator();

        Collection<File> textFiles = _crawler.getTrainingTextFiles();
        log.info(String.format("Total files to process: %s", textFiles.size()));
        int count = 1;
        int total = textFiles.size();
        for (File textFile : textFiles) {
            log.info(String.format("Processing file %d of %d, %s", 
                    count++, total, textFile.getAbsolutePath()));
            computeScoresForFile(textFile);
            //break;
        }
        
        _precission = _truePositives / (_truePositives + _falsePositives);
        _recall = _truePositives / (_truePositives + _falseNegatives);
        _f1score = 2 * _precission * _recall /(_precission + _recall);
        
        showStatistics();
    }

    private StanfordNerAnnotator initializeStanfordAnnotator() {
        log.info("Initializing Stanford Annotator");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("ner.model", MODEL_14_CLASS);
        props.put(NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY, "false");
        props.put(NumberSequenceClassifier.USE_SUTIME_PROPERTY, "false");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        return new StanfordNerAnnotator(pipeline);
    }

    private void computeScoresForFile(File textFile) {
        List<NerAnnotation> annotations = _annotator.annotateFileNoWrite(textFile);
        File validationFile = getCorrespondingValidationFile(textFile);
        List<String> lines = new LinkedList<>();
        try {
               lines = FileUtils.readLines(validationFile, "UTF-8");
        } catch (IOException ex) {
           log.error(ex);
        }
        
        computeScores(annotations, lines);
    }

    private File getCorrespondingValidationFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNameNoExt = FilenameUtils.removeExtension(fileName);
        String correspondingIoFile = String.format("%s_io.txt", fileNameNoExt);
        log.info("Io validation file is "+ correspondingIoFile);
        
        return new File(correspondingIoFile);
        
    }

    private void computeScores(List<NerAnnotation> annotations, List<String> validationLines) {
        int annotationsSize = annotations.size();
        int validationSize = validationLines.size();
        log.info("Annotation, validation size: " + annotationsSize + " " + validationSize);
        
        for(int i = 0; i < annotationsSize; ++i){
            NerAnnotation annotation = annotations.get(i);
            String line = validationLines.get(i);
            String tokens[] = line.split("\\s");
            String validationText = tokens[0];
            String validationClass = tokens[2];
            
            if(!validationText.equals(annotation.getText())){
                log.warn(String.format("Text differs: %s %s", validationText, annotation.getText()));
                continue;
            }
            
            if(!validationClass.equals("O")){
                if(validationClass.equals(annotation.getNeLabel())){
                    _truePositives++;
                }
                else if(!annotation.getNeLabel().equals("O")){
                    log.info(String.format("Entity found, class mistake: %s %s instead of %s",
                           validationText, annotation.getNeLabel(), validationClass));
                    //_truePositives += 0.5;
                   // _falseNegatives++;
                    
                }
                else {
                    // it was an entity, but the system missed it
                    _falseNegatives++;
                }
            }
            
            else{
                // no entity here
                if(annotation.getNeLabel().equals("O")){
                    _trueNegatives++;
                }
                else{
                   _falsePositives++;
                }
            }
        }
        
    }
    
    private void showStatistics() {
        System.out.println("True positives: " + _truePositives);
        System.out.println("True negatives: " + _trueNegatives);
        System.out.println("False positives: " +_falsePositives);
        System.out.println("False negatives: " + _falseNegatives);
        System.out.println("Precision: " + _precission);
        System.out.println("Recall " + _recall);
        System.out.println("F1-score: " + _f1score);
       
    }
    
    
    public static void main(String[] args){
        String dirname = "D:/Work/NLP/corpuses/ms_academic/train-io";
        NerF1Scorer scorer = new NerF1Scorer(dirname);
        scorer.computeScores();
    }

    
}
