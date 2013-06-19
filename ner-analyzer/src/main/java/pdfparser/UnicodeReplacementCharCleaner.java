/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import annotators.DirectoryTreeCrawler;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad
 */
public class UnicodeReplacementCharCleaner {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            UnicodeReplacementCharCleaner.class.getName());
    private static final String UNICODE_REPLACEMENT_CHAR = "\\ufffd";
    private DirectoryTreeCrawler _crawler;
    private int _count;

    public UnicodeReplacementCharCleaner(DirectoryTreeCrawler crawler) {
        _crawler = crawler;

    }

    public void cleanUpReplacementCharForTikaTextFiles() {
        _count = 0;
        Collection<File> tikaTextFiles = _crawler.getTikaTextFilesExcludeAnnotations();
        log.info(String.format("Removing replacement character for %s", tikaTextFiles.size()));
        for (File textFile : tikaTextFiles) {
            String text = cleanUpFile(textFile);
            writeTextToFile(textFile, text);
        }
        log.info(String.format("Finished cleaning unicode replacement."
                + " Total files cleaned: %d", _count));;
    }

    private String cleanUpFile(File textFile) {
        String text = "";
        try {
            text = FileUtils.readFileToString(textFile, "UTF-8");
            byte[] bytes = text.getBytes("UTF-8");
            text = new String(bytes, "UTF-8");
            if (text.indexOf("\ufffd") != -1) {
                text = text.replaceAll(UNICODE_REPLACEMENT_CHAR, "?");
                log.info(String.format("File %s has unicode replacement chars. ", textFile.getAbsolutePath()));
                _count++;
                return text;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        }

        return text;
    }

    private void writeTextToFile(File textFile, String text) {
        String output = getOutputFile(textFile);
        log.info("Cleaned output file is " + output);
        if (!ensureDirectoryCreated(output)) {
            return;
        }
        MyFileUtils.writeStringToFile(output, text);

    }

    private String getOutputFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(textFile.getName());
        String parentDirectory = FilenameUtils.getFullPathNoEndSeparator(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        return String.format("%s/proper_unicode/%s_cleaned.%s", parentDirectory, fileNoExtenesion, extension);
    }

    private boolean ensureDirectoryCreated(String outputfile) {
        File unicodeDirectory = new File(FilenameUtils.getFullPathNoEndSeparator(outputfile));
        if (!unicodeDirectory.exists()) {
            if (!unicodeDirectory.mkdirs()) {
                log.error("Proper unicode directory creation failed for "
                        + unicodeDirectory.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        UnicodeReplacementCharCleaner cleaner = new UnicodeReplacementCharCleaner(dirCrawler);
        cleaner.cleanUpReplacementCharForTikaTextFiles();
    }
}
