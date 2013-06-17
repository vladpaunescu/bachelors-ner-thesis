/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.Normalizer;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad
 */
public class StanfordNerAnnotator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            StanfordNerAnnotator.class.getName());
    public static final char TAB = '\t';
    private StanfordCoreNLP _snlp;

    public StanfordNerAnnotator(StanfordCoreNLP snlp) {
        _snlp = snlp;
    }

    public String annotateFile(File textFile) {
        try {

            String text = FileUtils.readFileToString(textFile, "UTF-8");
            
            text = cleanUpReplacementCharacter(text);

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

    private String cleanUpReplacementCharacter(String text) {
        byte bytes[];
        try {
            bytes = text.getBytes("UTF-8");
            text = new String(bytes, "UTF-8");
            text = text.replaceAll("\\ufffd", "");
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        }
        
        return text;
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

        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output), "UTF-8"))) {
            out.write(collector.toString());
        } catch (IOException ex) {
            log.error("Writing annotation to file " + output + " failed with " + ex);
        }

    }

    private String getOutputFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(textFile.getName());
        String parentDirectory = FilenameUtils.getFullPathNoEndSeparator(fileName);

        String extension = FilenameUtils.getExtension(fileName);
        return String.format("%s/annotations/%s_stanford.%s", parentDirectory, fileNoExtenesion, extension);

    }

    private boolean alreadyAnnotated(File textFile) {
        File outputFile = new File(getOutputFile(textFile));
//        return outputFile.exists();
        return false;
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        StanfordNerAnnotator annotator = new StanfordNerAnnotator(pipeline);
        String textfile = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/2001_The_human_development_index_and_sustainability_a_constructive_proposal_tika.txt";
        String annotations = annotator.annotateFile(new File(textfile));
        System.out.println(annotations);
    }
}
