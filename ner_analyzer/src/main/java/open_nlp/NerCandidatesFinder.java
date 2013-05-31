/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package open_nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad.paunescu
 */
public class NerCandidatesFinder {

    private String _modelpath;

    public NerCandidatesFinder(String modelpath) {
        _modelpath = modelpath;
    }

    public String[] detect(String[] text) {
        try (InputStream modelIn = new FileInputStream(_modelpath)) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            //After the model is loaded the NameFinderME can be instantiated.
            NameFinderME nameFinder = new NameFinderME(model);
            Span nameSpans[] = nameFinder.find(text);
            for (Span entity : nameSpans) {
                StringBuilder sb = new StringBuilder();
                for(int i = entity.getStart(); i < entity.getEnd(); ++i){
                    sb.append(text[i]).append(" ");
                }
                System.out.println(sb.toString());
                
            }
            nameFinder.clearAdaptiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String textfile = "D:/Work/NLP/corpuses/ms_academic/out/22 - "
                + "Social Science/1021291 - Wolff-Michael  Roth/JRST110.txt";
        String text = FileUtils.readFileToString(new File(textfile));



        try (InputStream modelIn = new FileInputStream("models/apache-openNLP/en-token.bin")) {
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);
            String tokens[] = tokenizer.tokenize(text);
            NerCandidatesFinder nerFinder = new NerCandidatesFinder("models/apache-openNLP/ner/en-ner-person.bin");
            nerFinder.detect(tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
