/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package open_nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad.paunescu
 */
public class SentenceDetector {

    private String _modelpath;

    public SentenceDetector(String modelpath) {
        _modelpath = modelpath;
    }

    public String[] detect(String text) {
        try (InputStream modelIn = new FileInputStream(_modelpath)) {
            SentenceModel model = new SentenceModel(modelIn);
            //After the model is loaded the SentenceDetectorME can be instantiated.
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            return sentenceDetector.sentDetect(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String textfile = "D:/Work/NLP/corpuses/ms_academic/out/22 - "
                + "Social Science/1021291 - Wolff-Michael  Roth/JRST110.txt";
        String text = FileUtils.readFileToString(new File(textfile));
        SentenceDetector detector = new SentenceDetector("models/apache-openNLP/en-sent.bin");
        String[] sentences = detector.detect(text);
        for(String sentence : sentences) {
            System.out.println(sentence);
            System.out.println();
        }
    }
    
}
