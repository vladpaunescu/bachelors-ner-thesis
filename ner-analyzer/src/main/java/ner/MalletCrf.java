/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import features.FC;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFCacheStaleIndicator;
import cc.mallet.fst.CRFOptimizableByBatchLabelLikelihood;
import cc.mallet.fst.CRFTrainerByValueGradients;
import cc.mallet.fst.CRFWriter;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.SimpleTagger;
import cc.mallet.fst.ThreadedOptimizable;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.FeaturesInWindow;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenText;
import cc.mallet.pipe.tsf.TokenTextCharNGrams;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.Randoms;
import features.InBracket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author vlad
 */
public class MalletCrf {

    private Pipe TokenSequence2FeatureVectorSequence;
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            MalletCrf.class.getName());

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
            new RegexMatches("MIXEDCAPS", Pattern.compile("[A-Z][a-z]+[A-Z][A-Za-z]*")),
            new RegexMatches("CONTAINSDIGITS", Pattern.compile(".*[0-9].*")),
//            new RegexMatches("SINGLEDIGITS", Pattern.compile("[0-9]")),
//            new RegexMatches("DOUBLEDIGITS", Pattern.compile("[0-9][0-9]")),
//            new RegexMatches("ALLDIGITS", Pattern.compile("[0-9]+")),
//            new RegexMatches("NUMERICAL", Pattern.compile("[-0-9]+[\\.,]+[0-9\\.,]+")),
//            new RegexMatches("ALPHNUMERIC", Pattern.compile("[A-Za-z0-9]+")),
//            new RegexMatches("ROMAN", Pattern.compile("[ivxdlcm]+|[IVXDLCM]+")),
//            new RegexMatches("MULTIDOTS", Pattern.compile("\\.\\.+")),
            new RegexMatches("ENDSINDOT", Pattern.compile("[^\\.]+.*\\.")),
            new RegexMatches("CONTAINSDASH", Pattern.compile(FC.ALPHANUM + "+-" + FC.ALPHANUM + "*")),
            new RegexMatches("ACRO", Pattern.compile("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
            new RegexMatches("LONELYINITIAL", Pattern.compile(FC.CAPS + "\\.")),
            new RegexMatches("SINGLECHAR", Pattern.compile(FC.ALPHA)),
//            new RegexMatches("CAPLETTER", Pattern.compile("[A-Z]")),
//            new RegexMatches("PUNC", Pattern.compile(FC.PUNT)),
//            new RegexMatches("QUOTE", Pattern.compile(FC.QUOTE)),
//            new RegexMatches("STARTDASH", Pattern.compile("-.*")),
//            new RegexMatches("ENDDASH", Pattern.compile(".*-")),
//            new RegexMatches("FORWARDSLASH", Pattern.compile("/")),
//            new RegexMatches("ISBRACKET", Pattern.compile("[()]")),
            new TokenSequenceLowercase(),
            /* Make the word a feature. */
            new TokenText("WORD="),
            new TokenTextCharSuffix("SUFFIX2=", 2),
            new TokenTextCharSuffix("SUFFIX3=", 3),
            new TokenTextCharSuffix("SUFFIX4=", 4),
            new TokenTextCharPrefix("PREFIX2=", 2),
            new TokenTextCharPrefix("PREFIX3=", 3),
            new TokenTextCharPrefix("PREFIX4=", 4),
//            new InBracket("INBRACKET", true),
//            new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}),
            /* FeatureInWindow features. */
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

    public InstanceList readDirectory(String mainDirPath) throws FileNotFoundException {

        File directory = new File(mainDirPath);

        File[] directories = new File[]{directory};

        // Construct a file iterator, starting with the 
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the 
        //   filename to produce a class label. In this case, I've 
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
                new FileIterator(directories,
                new TxtFilter(),
                FileIterator.LAST_DIRECTORY);


        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(buildPipe());
        Collection<File> files = FileUtils.listFiles(new File(mainDirPath),
                new SuffixFileFilter("done_io.txt"),
                TrueFileFilter.INSTANCE);

        for (File file : files) {
            LineGroupIterator lgIter = new LineGroupIterator(
                    new FileReader(file), Pattern.compile("\\n"), true);
            // Now process each instance provided by the iterator.
            instances.addThruPipe(lgIter);


        }
        return instances;

    }

    public InstanceList readFile(String filePath) throws FileNotFoundException {

        File file = new File(filePath);


        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(buildPipe());

        LineGroupIterator lgIter = new LineGroupIterator(
                new FileReader(file), Pattern.compile("\\n"), true);
        // Now process each instance provided by the iterator.
        instances.addThruPipe(lgIter);

        return instances;
    }

    public Classifier trainClassifier(InstanceList trainingInstances) {

        // Here we use a maximum entropy (ie polytomous logistic regression)                               
        //  classifier. Mallet includes a wide variety of classification                                   
        //  algorithms, see the JavaDoc API for details.                                                   

        ClassifierTrainer trainer = new MaxEntTrainer();
        return trainer.train(trainingInstances);
    }

    public Trial testTrainSplit(InstanceList instances) {

        int TRAINING = 0;
        int TESTING = 1;
        int VALIDATION = 2;

        // Split the input list into training (90%) and testing (10%) lists.                               
        // The division takes place by creating a copy of the list,                                        
        //  randomly shuffling the copy, and then allocating                                               
        //  instances to each sub-list based on the provided proportions.                                  

        InstanceList[] instanceLists =
                instances.split(new Randoms(),
                new double[]{0.7, 0.3, 0.0});

        // The third position is for the "validation" set,                                                 
        //  which is a set of instances not used directly                                                  
        //  for training, but available for determining                                                    
        //  when to stop training and for estimating optimal                                               
        //  settings of nuisance parameters.                                                               
        // Most Mallet ClassifierTrainers can not currently take advantage                                 
        //  of validation sets.                                                                            

        Classifier classifier = trainClassifier(instanceLists[TRAINING]);
        return new Trial(classifier, instanceLists[TESTING]);
    }

    public CRF run(InstanceList trainingData, InstanceList testingData) throws FileNotFoundException, IOException {
        //  SimpleTagger tag // setup:
        //    CRF (model) and the state machine
        //    CRFOptimizableBy* objects (terms in the objective function)
        //    CRF trainer
        //    evaluator and writer
        // model
        CRF crf = new CRF(trainingData.getDataAlphabet(),
                trainingData.getTargetAlphabet());
        // construct the finite state machine
        crf.addFullyConnectedStatesForLabels();
        // initialize model's weights
        crf.setWeightsDimensionAsIn(trainingData, false);

        //  CRFOptimizableBy* objects (terms in the objective function)
        // objective 1: label likelihood objective
//        CRFOptimizableByLabelLikelihood optLabel =
//                new CRFOptimizableByLabelLikelihood(crf, trainingData);

        int numThreads = 32;
        CRFOptimizableByBatchLabelLikelihood batchOptLabel =
                new CRFOptimizableByBatchLabelLikelihood(crf, trainingData, numThreads);
        ThreadedOptimizable optLabel = new ThreadedOptimizable(
                batchOptLabel, trainingData, crf.getParameters().getNumFactors(),
                new CRFCacheStaleIndicator(crf));


        // CRF trainer
        Optimizable.ByGradientValue[] opts =
                new Optimizable.ByGradientValue[]{optLabel};
        // by default, use L-BFGS as the optimizer
        CRFTrainerByValueGradients crfTrainer =
                new CRFTrainerByValueGradients(crf, opts);

        // *Note*: labels can also be obtained from the target alphabet
        String[] labels = new String[]{"PERSON", "LOCATION", "ORGANIZATION", "MISC"};
        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{trainingData, testingData},
                new String[]{"train", "test"}, labels, labels) {
            @Override
            public boolean precondition(TransducerTrainer tt) {
                // evaluate model every 5 training iterations
                return tt.getIteration() % 5 == 0;
            }
        };
        crfTrainer.addEvaluator(evaluator);


        CRFWriter crfWriter = new CRFWriter("ner_crf.model") {
            @Override
            public boolean precondition(TransducerTrainer tt) {
                // save the trained model after training finishes
                return tt.getIteration() % Integer.MAX_VALUE == 0;
            }
        };


        crfTrainer.addEvaluator(crfWriter);

        // all setup done, train until convergence
        crfTrainer.setMaxResets(0);
        crfTrainer.train(trainingData, Integer.MAX_VALUE);
        // evaluate
        evaluator.evaluate(crfTrainer);
        optLabel.shutdown();
        try (FileOutputStream fos = new FileOutputStream("ner_crf.model")) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(crf);
        }


        return crf;

    }

    public void process(Sequence input, Sequence output) {
        // assert input.size() == output.size()
        for (int i = 0; i < input.size(); i++) {
            if (output.get(i).equals("O") == false) {
                System.out.println(output.get(i));
                FeatureVector fv = (FeatureVector) input.get(i);
                System.out.println(" " + fv.toString(true));
            }
        }
        System.out.println("");
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        MalletCrf mallet = new MalletCrf();
        InstanceList instances = mallet.readDirectory("D:/Work/NLP/corpuses/ms_academic/train-io-4-class");
        //      InstanceList instances = mallet.readFile("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt");

        int TRAINING = 0;
        int TESTING = 1;
        int VALIDATION = 2;

        // Split the input list into training (90%) and testing (10%) lists.                               
        // The division takes place by creating a copy of the list,                                        
        //  randomly shuffling the copy, and then allocating                                               
        //  instances to each sub-list based on the provided proportions.                                  

        InstanceList[] instanceLists =
                instances.split(new Randoms(),
                new double[]{0.7, 0.3, 0.0});

        // The third position is for the "validation" set,                                                 
        //  which is a set of instances not used directly                                                  
        //  for training, but available for determining                                                    
        //  when to stop training and for estimating optimal                                               
        //  settings of nuisance parameters.                                                               
        // Most Mallet ClassifierTrainers can not currently take advantage                                 
        //  of validation sets.                                                                            



        CRF crf = mallet.run(instanceLists[TRAINING], instanceLists[TESTING]);

        instances = mallet.readFile("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt");
        //instances = mallet.readDirectory("D:/Work/NLP/corpuses/ms_academic/train-io-4-class");
        List<String> lines = FileUtils.readLines(new File("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt"),
                "UTF-8");


        for (Instance input : instances) {
            Sequence seq = (Sequence) input.getData();
            Sequence output = crf.transduce(seq);
            for (int i = 0; i < seq.size(); ++i) {
                String line = lines.get(i);
                String words[] = line.split("\\s");
                mallet.process(seq, output);
                System.out.println(output.get(i) + " " + words[0]);
            }
        }

        for (int i = 0; i < instances.size(); i++) {
            Sequence input = (Sequence) instances.get(i).getData();
            Sequence[] outputs = SimpleTagger.apply(crf, input, 20);
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






        //     Classifier classifier = mallet.trainClassifier(instances);

//        // n-cross validation
//       Trial trial = mallet.testTrainSplit(instances); 
//       System.out.println("Accuracy: " + trial.getAccuracy());
//       System.out.println("F1 for class 'persoane': " + trial.getF1("persoane"));
//       System.out.println("Precision for class 'persoane'" + trial.getPrecision("persoane"));
//       System.out.println("Recall for class 'persoane'" + trial.getRecall("persoane"));
//       System.out.println("F1 for class 'organizatii': " + trial.getF1("organizatii"));
//       System.out.println("Precision for class 'organizatii'" + trial.getPrecision("organizatii"));
//       System.out.println("Recall for class 'organizatii'" + trial.getRecall("organizatii"));
//       System.out.println("F1 for class 'teritorii': " + trial.getF1("teritorii"));
//       System.out.println("Precision for class 'teritorii'" + trial.getPrecision("teritorii"));
//       System.out.println("Recall for class 'teritorii'" + trial.getRecall("teritorii"));
//        
//        try {
//            mallet.printLabelings(classifier, new File("C:\\Users\\adrian.zamfirescu\\Documents\\MalletResults\\abc.txt"));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//           
////        try {
////            mallet.evaluate(classifier, new File("C:\\Users\\adrian.zamfirescu\\Documents\\MalletResults\\unclasified.txt"));
////        } catch (IOException ex) {
////            ex.printStackTrace();
////        }
    }
}
