/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author vlad
 */
public class TextFileMover {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            TextFileMover.class.getName());
    private static final String FILENAME_TO_TRIM = "tika_cleaned_no_newline_no_hyphenation";
    private DirectoryTreeCrawler _crawler;
    private String _rootDir;
    private String _outputRootDirectory;

    public TextFileMover(DirectoryTreeCrawler crawler, String rootDir, String outputRootDir) {
        _crawler = crawler;
        _rootDir = rootDir;
        _outputRootDirectory = outputRootDir;
    }

    public void copyProperStanfordFiles() {
        log.info("Ouptut dir is " + _outputRootDirectory);
        Collection<File> stanfordFiles = _crawler.getTikaProperStanfordFiles();
        int total = stanfordFiles.size();
        int count = 1;
        log.info("Total Stanford files to move " + total);
        for (File stanfordFile : stanfordFiles) {
            log.info(String.format("Copying file %d of %d : %s", count, total, stanfordFile.getAbsolutePath()));
            copyProperStanfordFile(stanfordFile);
            count++;
        }
    }

    public void copyProperTikaFiles() {
        log.info("Ouptut dir is " + _outputRootDirectory);
        Collection<File> tikaFiles = _crawler.getTikaProperUnicodeNoHyphenFiles();
        int total = tikaFiles.size();
        int count = 1;
        log.info("Total Tika files to move " + total);
        for (File tikaFile : tikaFiles) {
            log.info(String.format("Copying file %d of %d : %s", count, total, tikaFile.getAbsolutePath()));
            copyTikaParsedFile(tikaFile);
            count++;
        }
    }

    private void copyProperStanfordFile(File stanfordFile) {
        String properFilename = shortenStanfordTikaFile(stanfordFile);
        String properFileNameNoDir = FilenameUtils.getName(properFilename);
        String srcDirPath = FilenameUtils.getPath(stanfordFile.getAbsolutePath());
        String authorPath = srcDirPath.substring(_rootDir.length() - 1, srcDirPath.indexOf("proper_unicode"));
        log.info("Author dir path is " + authorPath);
        String outputFilename = String.format("%s/%s%s", _outputRootDirectory, authorPath, properFileNameNoDir);
        log.info("Output file path is " + outputFilename);
        if (MyFileUtils.makeDirectories(new File(FilenameUtils.getFullPath(outputFilename)))) {
            try {
                FileUtils.copyFile(stanfordFile, new File(outputFilename));
            } catch (IOException ex) {
                log.warn(ex);
            }
        } else {
            log.error("Could not create directories for file " + outputFilename);
            
        }
    }

    private void copyTikaParsedFile(File tikaFile) {
        String properFilename = shortenStanfordTikaFile(tikaFile);
        String properFileNameNoDir = FilenameUtils.getName(properFilename);
        String srcDirPath = FilenameUtils.getPath(tikaFile.getAbsolutePath());
        String authorPath = srcDirPath.substring(_rootDir.length() - 1, srcDirPath.indexOf("proper_unicode"));
        log.info("Author dir path is " + authorPath);
        String outputFilename = String.format("%s/%s%s", _outputRootDirectory, authorPath, properFileNameNoDir);
        log.info("Output file path is " + outputFilename);
        if (MyFileUtils.makeDirectories(new File(FilenameUtils.getFullPath(outputFilename)))) {
            try {
                FileUtils.copyFile(tikaFile, new File(outputFilename));
            } catch (IOException ex) {
                log.warn(ex);
            }
        } else {
            log.error("Could not create directories for file " + outputFilename);
        }
    }

    private String shortenStanfordTikaFile(File stanfordFile) {
        String originalFilename = stanfordFile.getName();
        String properFilenme = originalFilename.replaceAll(FILENAME_TO_TRIM, "proper");
        log.info("Original filename: " + originalFilename);
        log.info("Trimmed filename " + properFilenme);

        return properFilenme;
    }

    public static void main(String[] args) {
        DirectoryTreeCrawler dirCrawler = new DirectoryTreeCrawler("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science");
        String outputRootDir = "D:/Work/NLP/corpuses/ms_academic/parsed";
        TextFileMover mover = new TextFileMover(dirCrawler,
                "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science",
                outputRootDir);
        mover.copyProperStanfordFiles();
        mover.copyProperTikaFiles();
    }
}
