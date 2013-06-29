/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

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
    private static final String TIKA_NO_NEWLINE_NO_HYPHEN_TXTS = "tika_cleaned_no_newline_no_hyphenation.txt";
    private static final String TIKA_PROPER_STANFORD = "tika_cleaned_no_newline_no_hyphenation_stanford.txt";
    private static final String PROPER_FILES = "_proper.txt";
    private static final String GENERIC_FILE_SPLIT = "*_*.txt";
    private static final String DONE_ANN_FILE_SUFFIX = "_done.ann";
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

    public Collection<File> getTikaProperUnicodeTextFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_CLEANED_TXTS),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getTikaProperUnicodeNoNewlineFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_NO_NEWLINE_TXTS),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getTikaProperUnicodeNoHyphenFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_NO_NEWLINE_NO_HYPHEN_TXTS),
                new NotFileFilter(new NameFileFilter("annotations")));
    }

    public Collection<File> getTikaProperStanfordFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(TIKA_PROPER_STANFORD),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getProperFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(PROPER_FILES),
                TrueFileFilter.INSTANCE);
    }
    
    public Collection<File> getGenericFiles(){
        return FileUtils.listFiles(_root, new SuffixFileFilter(".txt"),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getGenericFileSplitFiles() {
        return FileUtils.listFiles(_root, new AndFileFilter(new WildcardFileFilter(GENERIC_FILE_SPLIT),
                new NotFileFilter(new SuffixFileFilter("_stanford.txt"))),
                TrueFileFilter.INSTANCE);
    }

    public Collection<File> getAnnotationFiles() {
        return FileUtils.listFiles(_root, new SuffixFileFilter(DONE_ANN_FILE_SUFFIX),
                TrueFileFilter.INSTANCE);
    }

    public static void main(String[] args) {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        //dirCrawler.eliminateHyphenationForTikaTextFiles();
        dirCrawler.getTikaProperUnicodeNoHyphenFiles();
    }
}
