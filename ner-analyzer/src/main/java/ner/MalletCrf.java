/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFCacheStaleIndicator;
import cc.mallet.fst.CRFOptimizableByBatchLabelLikelihood;
import cc.mallet.fst.CRFOptimizableByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByValueGradients;
import cc.mallet.fst.CRFWriter;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.SimpleTagger.SimpleTaggerSentence2FeatureVectorSequence;
import cc.mallet.fst.ThreadedOptimizable;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import cc.mallet.util.Randoms;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
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

    public Pipe buildPipe() {
        List<Pipe> pipeList = new ArrayList<>();

        // Tokenize raw strings
        //  pipeList.add(new CharSequence2TokenSequence(tokenPattern));
        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        //  pipeList.add(new CharSequence2TokenSequence("\n"));
        //  pipeList.add(tokenSeq);
        pipeList.add(new SimpleTaggerSentence2FeatureVectorSequence());
        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        //  pipeList.add(featureVectorSeq);

        // Print out the features and the label
        pipeList.add(new PrintInputAndTarget());

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
                new double[]{0.9, 0.1, 0.0});

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

    public CRF run(InstanceList trainingData, InstanceList testingData) {
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


        // save the trained model (if CRFWriter is not used)
        // FileOutputStream fos = new FileOutputStream("ner_crf.model");
        // ObjectOutputStream oos = new ObjectOutputStream(fos);
        // oos.writeObject(crf);
        optLabel.shutdown();

        return crf;

    }

    public static void main(String[] args) throws FileNotFoundException {

        MalletCrf mallet = new MalletCrf();
        InstanceList instances = mallet.readDirectory("D:/Work/NLP/corpuses/ms_academic/train-io-4-class");
        //InstanceList instances = mallet.readFile("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt");

        int TRAINING = 0;
        int TESTING = 1;
        int VALIDATION = 2;

        // Split the input list into training (90%) and testing (10%) lists.                               
        // The division takes place by creating a copy of the list,                                        
        //  randomly shuffling the copy, and then allocating                                               
        //  instances to each sub-list based on the provided proportions.                                  

        InstanceList[] instanceLists =
                instances.split(new Randoms(),
                new double[]{0.8, 0.1, 0.1});

        // The third position is for the "validation" set,                                                 
        //  which is a set of instances not used directly                                                  
        //  for training, but available for determining                                                    
        //  when to stop training and for estimating optimal                                               
        //  settings of nuisance parameters.                                                               
        // Most Mallet ClassifierTrainers can not currently take advantage                                 
        //  of validation sets.                                                                            



        CRF crf = mallet.run(instanceLists[TRAINING], instanceLists[VALIDATION]);

        instances = mallet.readFile("D:/Work/NLP/corpuses/ms_academic/train-io-4-class/1_1_done_io.txt");

        for (Instance input : instances) {
            Sequence seq = (Sequence) input.getData();
            Sequence output = crf.transduce(seq);
            for (int i = 0; i < seq.size(); ++i) {
                System.out.println(seq.get(i) + "/" + output.get(i));
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
