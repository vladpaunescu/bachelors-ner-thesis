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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author vlad
 */
public class IoEncodingTransformer {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            IoEncodingTransformer.class.getName());
    private final static Properties STANFORD_PORPS = new Properties();

    static {
        STANFORD_PORPS.put("annotators", "tokenize, ssplit, pos");
    }
    private StanfordCoreNLP _snlp;

    public IoEncodingTransformer() {
        initializeStanfordParser();
    }

    public String transform(String text) {
        StringBuilder collector = new StringBuilder();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        _snlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            parseSentence(sentence, collector);
        }

        return collector.toString();

    }

    // word POS
    // Smith NNP 
    private void parseSentence(CoreMap sentence, StringBuilder collector) {
        // a CoreLabel is a CoreMap with additional token-specific methods
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String originalText = token.originalText();

            // this is the POS tag of the token
            String pos = token.tag();

            int beginPosition = token.beginPosition();
            int endPosition = token.endPosition();

            String[] subtokens = originalText.split("\\s");
            for (String subtoken : subtokens) {
                collector.append(subtoken).append(" ").append(pos).append("\n");
            }
        }
    }

    private void initializeStanfordParser() {
        _snlp = new StanfordCoreNLP(STANFORD_PORPS);
    }
}
