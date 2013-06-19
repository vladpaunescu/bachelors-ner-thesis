/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import annotators.DirectoryTreeCrawler;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad
 */
public class TextFileNewlineTrimmer {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            TextFileNewlineTrimmer.class.getName());
    private DirectoryTreeCrawler _crawler;

    public TextFileNewlineTrimmer(DirectoryTreeCrawler crawler) {
        _crawler = crawler;
    }

    public void trimNewlineForProperUnicodeFiles() {
        Collection<File> properFiles = _crawler.getTikaProperUnicodeTextFiles();

        log.info(String.format("Removing newline character for %s files.", properFiles.size()));
        for (File textFile : properFiles) {
            String text = removeNewline(textFile);
            writeTextToFile(textFile, text);
        }
        log.info("Finished removing newlines.");

    }

    private String removeNewline(File textFile) {
        String text = "";
        try {
            text = FileUtils.readFileToString(textFile, "UTF-8");
            text = text.replaceAll("([^\\n-])\\n", "$1 ");

        } catch (IOException ex) {
            log.error(ex);
        }
        return text;
    }

    private void writeTextToFile(File textFile, String text) {
        String output = getOutputFile(textFile);
        log.info("No newline output file is " + output);
        MyFileUtils.writeStringToFile(output, text);
    }

    private String getOutputFile(File textFile) {
        String fileName = textFile.getAbsolutePath();
        String fileNoExtenesion = FilenameUtils.removeExtension(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        return String.format("%s_no_newline.%s", fileNoExtenesion, extension);
    }    

    public static void main(String[] args) {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        TextFileNewlineTrimmer trimmer = new TextFileNewlineTrimmer(dirCrawler);
        trimmer.trimNewlineForProperUnicodeFiles();
    }
}
