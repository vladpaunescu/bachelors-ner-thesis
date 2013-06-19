/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author vlad.paunescu
 */
public class DirectoryTreeCrawler {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            DirectoryTreeCrawler.class.getName());
    private static final String[] TEXT_EXTENSIONS = {"txt"};
    private static final String TEXT_EXTENSION = ".txt";
    private static final String TIKA_PARSED_TXTS = "_tika.txt";
    private static final String TIKA_CLEANED_TXTS = "_tika_cleaned.txt";
    private static final String TIKA_NO_NEWLINE_TXTS = "tika_cleaned_no_newline.txt";
    
    private static final int TIMEOUT_SECONDS = 360;
    private String _rootdir;
    private File _root;

    public DirectoryTreeCrawler(String rootdir) {
        _rootdir = rootdir;
        _root = new File(_rootdir);
    }

    public void listTextFiles() {
        Collection<File> textFiles = FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
        for (File textFile : textFiles) {
            System.out.println(textFile.getName());
        }
    }

    public void annotateTikaTextFiles() throws InterruptedException {
        log.info("Annotating tika parsed text files");
        Collection<File> textFiles = getTikaTextFilesExcludeAnnotations();
        annotateTextFiles(textFiles);

    }

    public void eliminateHyphenationForTikaTextFiles() {
        Collection<File> textFiles = getTikaTextFilesExcludeAnnotations();
        for (File textFile : textFiles) {
            log.info(String.format("Elimination hyphenation for file %s", textFile.getAbsolutePath()));
            try {
                String text = FileUtils.readFileToString(textFile);
                text = eliminateHyphenation(text);
                FileUtils.writeStringToFile(getOutputFileNoHyphenation(textFile), text);
            } catch (IOException ex) {
                log.error(ex);
            }
        }
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

    private String eliminateHyphenation(String text) {
        return text.replaceAll("-((\\r\\n)|[\\n\\r])", "");
    }

    private File getOutputFileNoHyphenation(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        return new File(String.format("%s_no_hyphenation.%s", fileNoExtenesion, extension));
    }

    private StanfordNerAnnotator initializeStanfordAnnotator() {
        log.info("Initializing Stanford Annotator");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        return new StanfordNerAnnotator(pipeline);
    }

    public Collection<File> getTextFiles() {
        return FileUtils.listFiles(_root, TEXT_EXTENSIONS, true);
    }

    public Collection<File> getTextFilesExcludeAnnotations() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TEXT_EXTENSION),
                new NotFileFilter(new NameFileFilter("annotations")));
    }

    public Collection<File> getTikaTextFilesExcludeAnnotations() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_PARSED_TXTS),
                new NotFileFilter(new NameFileFilter("annotations")));
    }

    public static void main(String[] args) throws InterruptedException {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        //dirCrawler.eliminateHyphenationForTikaTextFiles();
        dirCrawler.annotateTikaTextFiles();

    }

    public Collection<File> getTikaProperUnicodeTextFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_CLEANED_TXTS),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getTikaProperUnicodeNoNewlineFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_NO_NEWLINE_TXTS),
                TrueFileFilter.INSTANCE);
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
