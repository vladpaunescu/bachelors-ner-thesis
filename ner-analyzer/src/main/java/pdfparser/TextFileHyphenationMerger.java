/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad
 * 
 */
public class TextFileHyphenationMerger {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            TextFileHyphenationMerger.class.getName());
    private static Pattern HYPHEN = Pattern.compile("([^\\s,\\.:!?'\\+\\*\"’”“‘\\(\\)\\[\\]\\{\\}\\\\]+)(-)((\\r\\n)|[\\n\\r])([^\\s!,\\.:!?\\+\\*\"'’”‘\\(\\)\\[\\]\\{\\}\\\\]+)");
    private DirectoryTreeCrawler _crawler;
    private HyphenationChecker _checker;

    public TextFileHyphenationMerger(DirectoryTreeCrawler crawler) {
        _crawler = crawler;
        _checker = new HyphenationChecker();
    }

    public void removeHyphenationForUnicodeFiles() {
        Collection<File> files = _crawler.getTikaProperUnicodeNoNewlineFiles();

        log.info(String.format("Removing hyphenation character for %s files.", files.size()));
        int total = files.size();
        int count = 1;
        for (File textFile : files) {
            if (alreadyProcessed(textFile)) {
                log.info(String.format("File %s already processed. Skipping.", textFile.getAbsolutePath()));
                count++;
                continue;
            }
            log.info(String.format("\nRemoving for %d of %d. Name: %s", count, total, textFile.getAbsolutePath()));
            String text = removeHyphenation(textFile);
            writeTextToFile(textFile, text);
            count++;
        }
        log.info("Finished removing hyphenation.");

    }

    private String removeHyphenation(File textFile) {

        String text = "";
        try {
            text = FileUtils.readFileToString(textFile, "UTF-8");

            // eliminate hyphenation            
            Matcher matcher = HYPHEN.matcher(text);

            while (matcher.find()) {
                String matchedGroup = matcher.group();
                log.info(matchedGroup);
                String wordWithHyphen = matcher.group(1) + matcher.group(2) + matcher.group(5);
                String wordNoHyphen = matcher.group(1) + matcher.group(5);
                if (_checker.chekcHyphenatedWordExists(wordWithHyphen, wordNoHyphen)) {
                    log.info(String.format("Replacing %s with %s.", matchedGroup, wordNoHyphen));
                    text = text.replaceAll(matchedGroup, wordNoHyphen);
                } else {
                    log.info(String.format("Keeping hyphen. Replacing %s with %s",
                            matchedGroup, wordWithHyphen));
                    text = text.replaceAll(matchedGroup, wordWithHyphen);
                }
            }

        } catch (IOException ex) {
            log.error(ex);
        }
        return text;
    }

    private void writeTextToFile(File textFile, String text) {
        String output = getOutputFile(textFile);
        log.info("No hyphenation output file is " + output);
        MyFileUtils.writeStringToFile(output, text);
    }

    public boolean alreadyProcessed(File textFile) {
        File outputFile = new File(getOutputFile(textFile));
        return outputFile.exists();
    }

    private String getOutputFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        return String.format("%s_no_hyphenation.%s", fileNoExtenesion, extension);
    }

    public static void main(String[] args) {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        TextFileHyphenationMerger merger = new TextFileHyphenationMerger(dirCrawler);
        merger.removeHyphenationForUnicodeFiles();
    }
}
