/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import cc.mallet.fst.CRF;
import cc.mallet.fst.SimpleTagger;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import features.FC;
import features.InBracket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad
 */
public class MalletAnnotator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            MalletAnnotator.class.getName());
    private CRF _crf;
    private IoEncodingTransformer _transformer;
    private Pipe _pipe;
    private static final String MODEL_MALLET_5_CLASS = "D:/Work/NLP/corpuses/ms_academic/models/ner_crf.model";

    public MalletAnnotator(String modelPath) throws IOException, ClassNotFoundException {
        ObjectInputStream s = new ObjectInputStream(new FileInputStream(modelPath));
        _crf = (CRF) s.readObject();
        s.close();
        _pipe = _crf.getInputPipe();
        _pipe.setTargetProcessing(false);
        _transformer = new IoEncodingTransformer();

    }

    public Pipe buildPipe() {
        List<Pipe> pipeList = new ArrayList<>();

        // Tokenize raw strings
        //  pipeList.add(new CharSequence2TokenSequence(tokenPattern));
        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        //  pipeList.add(new CharSequence2TokenSequence("\n"));
        //  pipeList.add(tokenSeq);

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        //  pipeList.add(featureVectorSeq);

        pipeList.add(new SimpleTaggerSentence2TokenSequence());


        pipeList.addAll(Arrays.asList(new Pipe[]{
            new RegexMatches("INITCAP", Pattern.compile(FC.CAPS + ".*")),
            new RegexMatches("CAPITALIZED", Pattern.compile(FC.CAPS + FC.LOW + "*")),
            new RegexMatches("ALLCAPS", Pattern.compile(FC.CAPS + "+")),
            //            new RegexMatches("MIXEDCAPS", Pattern.compile("[A-Z][a-z]+[A-Z][A-Za-z]*")),
            //            new RegexMatches("CONTAINSDIGITS", Pattern.compile(".*[0-9].*")),
            //            new RegexMatches("SINGLEDIGITS", Pattern.compile("[0-9]")),
            //            new RegexMatches("DOUBLEDIGITS", Pattern.compile("[0-9][0-9]")),
            //            new RegexMatches("ALLDIGITS", Pattern.compile("[0-9]+")),
            //            new RegexMatches("NUMERICAL", Pattern.compile("[-0-9]+[\\.,]+[0-9\\.,]+")),
            //            new RegexMatches("ALPHNUMERIC", Pattern.compile("[A-Za-z0-9]+")),
            //            new RegexMatches("ROMAN", Pattern.compile("[ivxdlcm]+|[IVXDLCM]+")),
            //            new RegexMatches("MULTIDOTS", Pattern.compile("\\.\\.+")),
            //            new RegexMatches("ENDSINDOT", Pattern.compile("[^\\.]+.*\\.")),
            new RegexMatches("CONTAINSDASH", Pattern.compile(FC.ALPHANUM + "+-" + FC.ALPHANUM + "*")),
            new RegexMatches("ACRO", Pattern.compile("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
            new RegexMatches("LONELYINITIAL", Pattern.compile(FC.CAPS + "\\.")),
            //            new RegexMatches("SINGLECHAR", Pattern.compile(FC.ALPHA)),
            //            new RegexMatches("CAPLETTER", Pattern.compile("[A-Z]")),
            //            new RegexMatches("PUNC", Pattern.compile(FC.PUNT)),
            //            new RegexMatches("QUOTE", Pattern.compile(FC.QUOTE)),
            //            new RegexMatches("STARTDASH", Pattern.compile("-.*")),
            //            new RegexMatches("ENDDASH", Pattern.compile(".*-")),
            //            new RegexMatches("FORWARDSLASH", Pattern.compile("/")),
            new RegexMatches("ISBRACKET", Pattern.compile("[()]")),
            //            new TokenSequenceLowercase(),
            //            /* Make the word a feature. */
            //            new TokenText("WORD="),
            new TokenTextCharSuffix("SUFFIX2=", 2),
            new TokenTextCharSuffix("SUFFIX3=", 3),
            new TokenTextCharSuffix("SUFFIX4=", 4),
            new TokenTextCharPrefix("PREFIX2=", 2),
            new TokenTextCharPrefix("PREFIX3=", 3),
            new TokenTextCharPrefix("PREFIX4=", 4),
            new InBracket("INBRACKET", true),
            //            new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}),
            //            /* FeatureInWindow features. */
            //            new FeaturesInWindow("WINDOW=", -1, 1,
            //            Pattern.compile("WORD=.*|SUFFIX.*|PREFIX.*|[A-Z]+"), true),
            new OffsetConjunctions(true, Pattern.compile("WORD=.*"),
            new int[][]{{-1}, {1}, {-1, 0}, {-2, -1}})
        }));

        pipeList.add(new TokenSequence2FeatureVectorSequence(true, true));
        // Print out the features and the label
//        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);

    }

    public void evaluateInstanceList(Transducer model, InstanceList data) {
        Object[] segmentStartTags = new String[]{"PERSON", "LOCATION", "ORGANIZATION", "MISC"};
        Object[] segmentContinueTags = new String[]{"PERSON", "LOCATION", "ORGANIZATION", "MISC"};
        int numCorrectTokens, totalTokens;
        int[] numTrueSegments, numPredictedSegments, numCorrectSegments;
        int allIndex = segmentStartTags.length;
        numTrueSegments = new int[allIndex + 1];
        numPredictedSegments = new int[allIndex + 1];
        numCorrectSegments = new int[allIndex + 1];

        totalTokens = numCorrectTokens = 0;
        for (int n = 0; n < numTrueSegments.length; n++) {
            numTrueSegments[n] = numPredictedSegments[n] = numCorrectSegments[n] = 0;
        }
        for (int i = 0; i < data.size(); i++) {
            Instance instance = data.get(i);
            Sequence input = (Sequence) instance.getData();
            //String tokens = null;
            //if (instance.getSource() != null)
            //tokens = (String) instance.getSource().toString();
            Sequence trueOutput = (Sequence) instance.getTarget();
            assert (input.size() == trueOutput.size());
            Sequence predOutput = model.transduce(input);
            assert (predOutput.size() == trueOutput.size());
            int trueStart, predStart;       // -1 for non-start, otherwise index into segmentStartTag
            for (int j = 0; j < trueOutput.size(); j++) {
                totalTokens++;
                if (trueOutput.get(j).equals(predOutput.get(j))) {
                    numCorrectTokens++;
                }
                trueStart = predStart = -1;
                // Count true segment starts
                for (int n = 0; n < segmentStartTags.length; n++) {
                    if (segmentStartTags[n].equals(trueOutput.get(j))) {
                        numTrueSegments[n]++;
                        numTrueSegments[allIndex]++;
                        trueStart = n;
                        break;
                    }
                }
                // Count predicted segment starts
                for (int n = 0; n < segmentStartTags.length; n++) {
                    if (segmentStartTags[n].equals(predOutput.get(j))) {
                        numPredictedSegments[n]++;
                        numPredictedSegments[allIndex]++;
                        predStart = n;
                    }
                }
                if (trueStart != -1 && trueStart == predStart) {
                    // Truth and Prediction both agree that the same segment tag-type is starting now
                    int m;
                    boolean trueContinue = false;
                    boolean predContinue = false;
                    for (m = j + 1; m < trueOutput.size(); m++) {
                        trueContinue = segmentContinueTags[predStart].equals(trueOutput.get(m));
                        predContinue = segmentContinueTags[predStart].equals(predOutput.get(m));
                        if (!trueContinue || !predContinue) {
                            if (trueContinue == predContinue) {
                                // They agree about a segment is ending somehow
                                numCorrectSegments[predStart]++;
                                numCorrectSegments[allIndex]++;
                            }
                            break;
                        }
                    }
                    // for the case of the end of the sequence
                    if (m == trueOutput.size()) {
                        if (trueContinue == predContinue) {
                            numCorrectSegments[predStart]++;
                            numCorrectSegments[allIndex]++;
                        }
                    }
                }
            }
        }
        //TODO(delip): clean up
        DecimalFormat f = new DecimalFormat("0.####");
        System.err.println("token accuracy: " + f.format(((double) numCorrectTokens) / totalTokens));
        for (int n = 0; n < numCorrectSegments.length; n++) {
            System.err.print((n < allIndex ? segmentStartTags[n].toString() : "OVERALL") + ' ');
            double precision = numPredictedSegments[n] == 0 ? 1 : ((double) numCorrectSegments[n]) / numPredictedSegments[n];
            double recall = numTrueSegments[n] == 0 ? 1 : ((double) numCorrectSegments[n]) / numTrueSegments[n];
            double f1 = recall + precision == 0.0 ? 0.0 : (2.0 * recall * precision) / (recall + precision);
            System.err.println(" precision: " + f.format(precision) + "; recall: " + f.format(recall) + "; F1: " + f.format(f1));
            if (n == allIndex) {
                System.err.println("    segments true=" + numTrueSegments[n] + " pred=" + numPredictedSegments[n] + " correct=" + numCorrectSegments[n]
                        + " misses=" + (numTrueSegments[n] - numCorrectSegments[n]) + " alarms=" + (numPredictedSegments[n] - numCorrectSegments[n]));
            }
        }
    
    }
    

    public NamedEntities annotateText(String text) throws FileNotFoundException {
        NamedEntities namedEntities = new NamedEntities();
     //   String ioText = _transformer.transform(text);
      //  String lines[] = ioText.split("\\s");
   //     System.out.println(ioText);
        System.out.println(_pipe.getTargetAlphabet());
        _pipe = buildPipe();
        InstanceList instances = new InstanceList(_pipe);
        instances.addThruPipe(new LineGroupIterator(new StringReader(text), Pattern.compile("^\\s*$"), true));
        System.out.println("Instences size: " + instances.size());
        for (int i = 0; i < instances.size(); i++) {
            Sequence input = (Sequence) instances.get(i).getData();
            Sequence[] outputs = SimpleTagger.apply(_crf, input, 20);
            int k = outputs.length;
            boolean error = false;
            System.out.println("Input size " + input.size());
            for (int a = 0; a < k; a++) {
                if (outputs[a].size() != input.size()) {
                    log.info("Failed to decode input sequence " + i + ", answer " + a);
                    error = true;
                }
            }
            if (!error) {

                for (int j = 0; j < input.size(); j++) {
                    StringBuilder buf = new StringBuilder();

                    for (int a = 0; a < k; a++) {
                        buf.append(outputs[a].get(j).toString()).append(" ");

                    }
                    FeatureVector fv = (FeatureVector) input.get(j);
                    buf.append(fv.toString(true));
                    System.out.println(buf.toString());
                }
                System.out.println();


            }
        }
        
        evaluateInstanceList(_crf, instances);
        return namedEntities;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        MalletAnnotator annotator = new MalletAnnotator(MODEL_MALLET_5_CLASS);
        String text = FileUtils.readFileToString(new File("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt"),
                "UTF-8");
        annotator.annotateText(text);
    }
}
