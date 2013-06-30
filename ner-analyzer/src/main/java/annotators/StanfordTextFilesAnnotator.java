/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import pdfparser.DirectoryTreeCrawler;

/**
 *
 * @author vlad
 */
public class StanfordTextFilesAnnotator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            StanfordTextFilesAnnotator.class.getName());
    private static final int TIMEOUT_SECONDS = 360;
    private DirectoryTreeCrawler _crawler;
    private boolean _genericEnabled;
    private static final String CONLL_MODEL = DefaultPaths.DEFAULT_NER_CONLL_MODEL;
    private static final String MODEL_5_CLASS = "D:/Work/NLP/corpuses/ms_academic/models/5-class.ser.gz";
    private static final String MODEL_14_CLASS = "D:/Work/NLP/corpuses/ms_academic/models/14-class.ser.gz";
    private String _modelToUse;
    private boolean _numericClassifierEnabled;
    private boolean _timeClassfierEnabled;

    public StanfordTextFilesAnnotator(DirectoryTreeCrawler crawler) {
        _crawler = crawler;
        _genericEnabled = false;
        _modelToUse = CONLL_MODEL;
        _numericClassifierEnabled = true;
        _timeClassfierEnabled = true;
    }

    public void annotateProperUnicodTikaNoHyphen() throws InterruptedException {
        log.info("Annotating tika parsed text files");
        Collection<File> textFiles = _crawler.getTikaProperUnicodeNoHyphenFiles();
        annotateTextFiles(textFiles);
    }

    public void annotateSplitFiles() {
        log.info("Annotating file-split files files");
        _genericEnabled = true;
        Collection<File> textFiles = _crawler.getGenericFileSplitFiles();
        annotateTextFiles(textFiles);
    }
    
    public void annotateTrainingFiles(){
        _modelToUse = MODEL_14_CLASS;
        _numericClassifierEnabled = false;
        _timeClassfierEnabled = false;
        Collection<File> textFiles = _crawler.getTrainingTextFiles();
        annotateTextFiles(textFiles);
    }

    private void annotateTextFiles(Collection<File> textFiles) {
        StanfordNerAnnotator annotator = initializeStanfordAnnotator();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        int count = 0;
        int total = textFiles.size();
        int index = 1;
        log.info(String.format("Annotating a total of %d text files", total));
        for (File textFile : textFiles) {
            log.info(String.format("Annotating file %d of %d.", index, total));

            long startTime = System.currentTimeMillis();
            Future<String> future = executor.submit(new AnnotationTask(annotator, textFile));
            try {
                log.info(String.format("Annotation thread started. Timeout %s seconds.", TIMEOUT_SECONDS));
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                log.info(String.format("Annotation Succeeded. Elapsed time %.2f seconds.", elapsedTime / 1000.0));

            } catch (TimeoutException e) {
                log.warn(String.format("Annotation for file %s terminated at timeout.", textFile.getAbsolutePath()));
                if (!future.cancel(true)) {
                    log.warn("Annotation Thread could not be canceled.");
                }
                annotator = initializeStanfordAnnotator();
            } catch (InterruptedException ex) {
                log.error(ex);
            } catch (ExecutionException ex) {
                log.error(ex);
            }

            count++;
            if (count > 200) {
                // prevent memory leaking
                count = 0;
                annotator = initializeStanfordAnnotator();
            }
            index++;
        }

        log.info("Shutting down executor service");
        executor.shutdownNow();

    }

    private StanfordNerAnnotator initializeStanfordAnnotator() {
        log.info("Initializing Stanford Annotator");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("ner.model", _modelToUse);
        if (_numericClassifierEnabled == false) {
            props.put(NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY, "false");
        }
        if (_timeClassfierEnabled == false) {
            props.put(NumberSequenceClassifier.USE_SUTIME_PROPERTY, "false");
        }

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        return new StanfordNerAnnotator(pipeline, _genericEnabled);
    }

    public static void main(String[] args) throws InterruptedException {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler(
                "D:/Work/NLP/corpuses/ms_academic/brat-data/file-split-no-comma");
        StanfordTextFilesAnnotator annotator = new StanfordTextFilesAnnotator(dirCrawler);
        annotator.annotateSplitFiles();
    }
}

class AnnotationTask implements Callable<String> {

    private StanfordNerAnnotator _annotator;
    private File _textFile;

    public AnnotationTask(StanfordNerAnnotator annotator, File textFile) {
        _annotator = annotator;
        _textFile = textFile;
    }

    @Override
    public String call() throws Exception {
        return _annotator.annotateFile(_textFile);
    }
}
