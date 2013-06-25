/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pdfparser.MyFileUtils;

/**
 *
 * @author vlad
 */
public class StanfordNerAnnotator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            StanfordNerAnnotator.class.getName());
    public static final char TAB = '\t';
    private StanfordCoreNLP _snlp;
    private boolean _genericEnabled;

    public StanfordNerAnnotator(StanfordCoreNLP snlp) {
        _snlp = snlp;
        _genericEnabled = false;
    }

    public StanfordNerAnnotator(StanfordCoreNLP snlp, boolean generic) {
        _snlp = snlp;
        _genericEnabled = true;
    }

    public String annotateFile(File textFile) {
        try {

            String text = FileUtils.readFileToString(textFile, "UTF-8");

            log.info("Annotating file " + textFile.getAbsolutePath());
            log.info("Cheking if file exists.");
            if (alreadyAnnotated(textFile)) {
                log.info(String.format("Annotation for file %s exists.", textFile));
                return "";
            }

            log.info("File is new. Annotating.");
            StringBuilder collector = new StringBuilder();

            // create an empty Annotation just with the given text
            Annotation document = new Annotation(text);

            // run all Annotators on this text
            _snlp.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                annotateSentence(sentence, collector);
            }

            log.info("Writing annotations for file " + textFile.getAbsolutePath());
            writeAnnotationOutput(textFile, collector);
            return collector.toString();

        } catch (IOException ex) {
            log.error("Annotating file " + textFile.getAbsolutePath()
                    + " failed with exception " + ex);
        }

        return null;
    }

    public NamedEntities annotateText(String text) {
        NamedEntities namedEntities = new NamedEntities();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        _snlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            annotateSentence(sentence, namedEntities);
        }

        return namedEntities;
    }

    private void annotateSentence(CoreMap sentence, StringBuilder collector) {
        // a CoreLabel is a CoreMap with additional token-specific methods
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String originalText = token.originalText();
            String word = token.word();

            int beginPosition = token.beginPosition();
            int endPosition = token.endPosition();

            // this is the POS tag of the token
            String pos = token.tag();
            // this is the NER label of the token
            String ne = token.ner();

            collector.append(originalText).append(TAB).append(word).append(TAB);
            collector.append(beginPosition).append(TAB).append(endPosition).append(TAB);
            collector.append(ne).append(TAB).append(pos).append("\r\n");
        }
    }

    private void annotateSentence(CoreMap sentence, NamedEntities namedEntities) {
        // a CoreLabel is a CoreMap with additional token-specific methods
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String originalText = token.originalText();

            int beginPosition = token.beginPosition();
            int endPosition = token.endPosition();

            // this is the NER label of the token
            String ne = token.ner();
            
            if(ne.equals("O")){
                continue;
            }

            AnnNamedEntity entity = new AnnNamedEntity(ne);
            entity.setStartIndex(beginPosition);
            entity.setEndIndex(endPosition);
            entity.setNamedEntityText(originalText);

            namedEntities.addEntityToCategory(entity, ne);
        }
    }

    private void writeAnnotationOutput(File textFile, StringBuilder collector) {

        String output = getOutputFile(textFile);

        log.info("Output file is " + output);
        File annotationsDirectory = new File(FilenameUtils.getFullPathNoEndSeparator(output));
        if (!annotationsDirectory.exists()) {
            if (!annotationsDirectory.mkdirs()) {
                log.error("Annotation directory creation failed for "
                        + annotationsDirectory.getAbsolutePath());
            }
        }

        MyFileUtils.writeStringToFile(output, collector.toString());

    }

    private String getOutputFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(textFile.getName());
        String parentDirectory = FilenameUtils.getFullPathNoEndSeparator(fileName);

        String extension = FilenameUtils.getExtension(fileName);

        if (!_genericEnabled) {
            return String.format("%s/annotations/%s_stanford.%s", parentDirectory, fileNoExtenesion, extension);
        }
        return String.format("%s_stanford.txt", FilenameUtils.removeExtension(fileName));
    }

    private boolean alreadyAnnotated(File textFile) {
        File outputFile = new File(getOutputFile(textFile));
        //  return outputFile.exists();
        return false;
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        //props.put("ner.model", DefaultPaths.DEFAULT_NER_CONLL_MODEL);
        props.put("ner.model", "D:/Work/NLP/corpuses/ms_academic/models/14-class.ser.gz");
        props.put(NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY, "false");
        props.put(NumberSequenceClassifier.USE_SUTIME_PROPERTY, "false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        StanfordNerAnnotator annotator = new StanfordNerAnnotator(pipeline);
        String textfile = "D:/Work/NLP/corpuses/ms_academic/parsed/47737 - Judee K. Burgoon/biocca1_proper.txt";

        textfile = "D:/Work/NLP/corpuses/ms_academic/train-io/5_10_done.txt";

        String annotations = annotator.annotateFile(new File(textfile));
        System.out.println(annotations);
    }
}
