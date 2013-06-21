/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pdfparser.DirectoryTreeCrawler;
import pdfparser.MyFileUtils;

/**
 *
 * @author vlad.paunescu
 */
public class BratAnnotationPairCreator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            BratAnnotationPairCreator.class.getName());
    private static final String OUTSIDE_ENTITY = "O";
    private static final String CRLF = "\r\n";
    private static final Pattern NEWLINE = Pattern.compile("([^\\n]+)(\\n)");
    private static final String ANNOTATION_CONFIG = "annotation.conf";
    private DirectoryTreeCrawler _crawler;
    private String _rootDirName;
    private String _outputDirName;
    private String _annotationConfigFileContents;
    private Set<String> _namedEntityTypes;
    private boolean _useGenericNames;
    private String _currentBaseDir;

    public BratAnnotationPairCreator(String rootDir, String outputDir) {
        _rootDirName = rootDir;
        _outputDirName = outputDir;
        _crawler = new DirectoryTreeCrawler(rootDir);
        _namedEntityTypes = new HashSet<>();
        _useGenericNames = false;
        _currentBaseDir = "";
    }

    public void convertFilesToBratFormat() {
        log.info(String.format("Output directory is %s", _outputDirName));
        _annotationConfigFileContents = getAnnotationsConfigFileContents();

        Collection<File> properFiles = _crawler.getProperFiles();
        int total = properFiles.size();
        int count = 1;
        log.info("Total files to process " + total);
        for (File properFile : properFiles) {
            if((count - 1) % 10 == 0){
                createDirectory(count);
            }
            log.info(String.format("Converting to brat format file %d of %d : %s", count, total, properFile.getAbsolutePath()));
            toBratAnnotationFormatPair(properFile, count);
            count++;
          
        }
        
        log.info("Entity types");
        for(String type : _namedEntityTypes){
            log.info(type);
        }
    }

    public void setGenericNames(boolean isGeneric) {
        _useGenericNames = isGeneric;
    }

    private void toBratAnnotationFormatPair(File textFile, int count) {
        String stanfordFilename = getCorrespondingStanfordFile(textFile);
        String outputFilename = getOutputFilename(textFile, count);

        if (ensureDirectoryCreated(outputFilename)) {
            exportTextFile(textFile, new File(outputFilename));
            String annFormat = toBratAnnotation(textFile, new File(stanfordFilename));
            String annFilename = String.format("%s.ann", FilenameUtils.removeExtension(outputFilename));
            log.info("Writing brat ann file " + annFilename);
            try {
                FileUtils.writeStringToFile(new File(annFilename), annFormat, "utf-8");
            } catch (IOException ex) {
                log.error(ex);
            }
        }

    }

    private void exportTextFile(File textFile, File outputFile) {
        log.info(String.format("Exporting text file %s", textFile.getAbsolutePath()));
        try {
            FileUtils.copyFile(textFile, outputFile);
        } catch (IOException ex) {
            log.warn(ex);
        }
    }

//      ]	-RRB-	97	98	O	-RRB-
//      On	On	99	101	O	IN
//      :	:	101	102	O	:
//      11	11	103	105	DATE	CD
// multline T1  DATE 20 25;26 30    jumps over 
// PER 0 20;21 26;27 30	William Henry "Bill" Gates III
// in 002967 - Barry J. Fraser/swproxy_proper    
//    ORGANIZATION 347 405	UK 
//International Journal of Science Education Publication
// newline 
//    
    private String toBratAnnotation(File textFile, File stanfordFile) {
        StringBuilder annOutput = new StringBuilder();
        try {
            String inputFile = FileUtils.readFileToString(textFile, "UTF-8");
            List<String> lines = FileUtils.readLines(stanfordFile);

            Iterator<String> iterator = lines.iterator();
            int entitiesIndex = 1;

            String line = "";
            if (iterator.hasNext()) {
                line = iterator.next();
            }

            while (iterator.hasNext()) {
                String[] tokens = line.split("\\t");
                String entityType = tokens[4];

                if (entityType.equals(OUTSIDE_ENTITY)) {
                    line = iterator.next();
                    continue;
                }

                if (!_namedEntityTypes.contains(entityType)) {
                    _namedEntityTypes.add(entityType);
                }

                int beginIndex = Integer.parseInt(tokens[2]);
                int endIndex = Integer.parseInt(tokens[3]);

                while (iterator.hasNext()) {
                    line = iterator.next();
                    tokens = line.split("\\t");
                    String nextEntityType = tokens[4];
                    if (nextEntityType.equals(entityType)) {
                        // same entity type, extend it
                        endIndex = Integer.parseInt(tokens[3]);
                    } else {
                        // different entity type
                        break;
                    }
                }
                String namedEntity = inputFile.substring(beginIndex, endIndex);
                Matcher matcher = NEWLINE.matcher(namedEntity);
                if (!matcher.find()) {
                    addEntityToBratAnnotation(annOutput, namedEntity, entitiesIndex, entityType,
                            beginIndex, endIndex);
                    entitiesIndex++;
                }

                //log.info(namedEntity);
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return annOutput.toString();
    }

    //PER 0 20;21 26;27 30	William Henry "Bill" Gates III
    private void addEntityToBratAnnotation(StringBuilder annOutput, String namedEntity, int entitiesIndex, String entityType,
            int entityStartIndex, int entityEndIndex) {
        annOutput.append('T').append(entitiesIndex).append('\t').append(entityType).append(' ');
        annOutput.append(entityStartIndex);
        annOutput.append(' ');

        annOutput.append(entityEndIndex);
        annOutput.append('\t').append(namedEntity).append(CRLF);
    }

    private String getCorrespondingStanfordFile(File textFile) {
        String filenameNoExtension = FilenameUtils.removeExtension(textFile.getAbsolutePath());
        String stanfordFilename = String.format("%s_stanford.txt", filenameNoExtension);
        log.info("Stanford filename is " + stanfordFilename);
        return stanfordFilename;
    }
    
     private void createDirectory(int count) {
         _currentBaseDir = String.format("%d-%d", count, count + 9);
         String dirFilename = String.format("%s/%s", _outputDirName, _currentBaseDir);
         log.info(String.format("Creating directory %s", dirFilename));
         MyFileUtils.makeDirectories(new File(dirFilename));
    }

    private String getOutputFilename(File file, int count) {
        if(_useGenericNames){
            return String.format("%s/%s/%d.txt", _outputDirName, _currentBaseDir, count);
        }
        String filename = file.getAbsolutePath();
        String properFileNameNoDir = FilenameUtils.getName(filename);
        String srcDirPath = FilenameUtils.getPath(filename);
        String authorPath = srcDirPath.substring(_rootDirName.length() - 1);
        log.info("Author dir path is " + authorPath);
        String outputFilename = String.format("%s/%s%s", _outputDirName, authorPath, properFileNameNoDir);
        log.info("Output file path is " + outputFilename);
        return outputFilename;
    }

    private boolean ensureDirectoryCreated(String outputFilename) {
        return MyFileUtils.makeDirectories(new File(FilenameUtils.getFullPath(outputFilename)));
    }

    private String getAnnotationsConfigFileContents() {
        StringBuilder sb = new StringBuilder();
        sb.append("[entities]" + CRLF);
        for (String entity : _namedEntityTypes) {
            sb.append(entity).append(CRLF);
        }
        log.info("annotation.config content is");
        log.info(sb.toString());
        return sb.toString();
    }

    private void makeAnnotationsConfigFile(String outputFilename) {
        String outputDirFilename = FilenameUtils.getFullPath(outputFilename);
        String annConfigFilename = outputDirFilename + ANNOTATION_CONFIG;
        File confFile = new File(annConfigFilename);
        if (confFile.exists()) {
            return;
        }

        log.info("Creating " + confFile.getAbsolutePath());
        try {
            FileUtils.writeStringToFile(confFile, _annotationConfigFileContents);
        } catch (IOException ex) {
            log.warn(ex);
        }
    }

    public static void main(String[] args) {

        String rootSrcDir = "D:/Work/NLP/corpuses/ms_academic/parsed";
        String rootOutputDir = "D:/Work/NLP/corpuses/ms_academic/brat-data/";


        BratAnnotationPairCreator annotationCreator = new BratAnnotationPairCreator(rootSrcDir, rootOutputDir);
        annotationCreator.setGenericNames(true);
        annotationCreator.convertFilesToBratFormat();
    }

   
}
