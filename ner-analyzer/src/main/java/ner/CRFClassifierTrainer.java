/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ner;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;

/**
 *
 * @author vlad
 */
public class CRFClassifierTrainer {
    
    public static void main(String[] args){
        String trainDirname = "D:/Work/NLP/corpuses/ms_academic/train-io";
        Properties props = new Properties();
        AbstractSequenceClassifier<CoreMap> crfClassfier = new CRFClassifier<>(props);
        System.out.println(crfClassfier.flags);
              
        crfClassfier.train(trainDirname, ".*_io\\.txt", crfClassfier.defaultReaderAndWriter());
        crfClassfier.serializeClassifier("D:/Work/NLP/corpuses/ms_academic/train-io/14-class.ser.gz");
   
    }
    
}
