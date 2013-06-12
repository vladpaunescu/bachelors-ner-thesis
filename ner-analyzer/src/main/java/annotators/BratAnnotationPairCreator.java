/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author vlad.paunescu
 */
public class BratAnnotationPairCreator {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            BratAnnotationPairCreator.class.getName());
    private static final String OUTSIDE_ENTITY = "O";
    private static final String CRLF = "\r\n";
    private File _outputDirectory;
    private String _outputDirName;

    public BratAnnotationPairCreator(File outputDir) {
        _outputDirectory = outputDir;
        _outputDirName = _outputDirectory.getAbsolutePath();
        log.info(String.format("Output directory is %s", _outputDirName));
    }

    public void toBratAnnotationFormatPair(File textFile, File stanfordFile) {
        exportTextFile(textFile);
        String annFormat = toBratAnnotation(textFile, stanfordFile);
        String filenameNoExtension = FilenameUtils.removeExtension(textFile.getName());
        File annFile = new File(String.format("%s/%s.ann", _outputDirName, filenameNoExtension));
        try {
            FileUtils.writeStringToFile(annFile, annFormat);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    private void exportTextFile(File textFile) {
        log.info(String.format("Exporting text file %s", textFile.getAbsolutePath()));
        try {
            FileUtils.copyFileToDirectory(textFile, _outputDirectory);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    private String toBratAnnotation(File textFile, File stanfordFile) {
        StringBuilder annOutput = new StringBuilder();
        try {
            String inputFile = FileUtils.readFileToString(textFile);
            List<String> lines = FileUtils.readLines(stanfordFile);

            int cursor = 0;
            String previousEntityType = OUTSIDE_ENTITY;
            int currentEntityStartIndex = 0;
            int currenEntityEndIndex = 0;
            int entitiesIndex = 1;
            String currentNamedEntity;

            for (String line : lines) {
                line = line.replace("\\", "");
                String[] tokens = line.split("\\t");
                String word = tokens[0];
                String currentEntityType = tokens[1];
                System.out.println("Word " + word + " type " + currentEntityType);
                int position = inputFile.indexOf(word, cursor);
                System.out.println("Word " + word + " type " + currentEntityType + " cursor " + cursor
                        + " index " + position);

                if (currentEntityType.equals("NUMBER") || currentEntityType.equals("DATE")) {
                    // new entity starts and ends here
                    if (currentEntityStartIndex > 0 && currenEntityEndIndex > 0) {

                        currentEntityStartIndex = position;
                        currenEntityEndIndex = position + word.length();
                        currentNamedEntity = inputFile.substring(currentEntityStartIndex, currenEntityEndIndex);
                        addEntityToBratAnnotation(annOutput, currentNamedEntity, entitiesIndex, currentEntityType,
                                currentEntityStartIndex, currenEntityEndIndex);
                        ++entitiesIndex;
                        currentEntityStartIndex = -1;
                        currenEntityEndIndex = -1;
                    }

                } else if (entityBeginning(previousEntityType, currentEntityType)) {
                    // new entity starts here
                    currentEntityStartIndex = position;
                    currenEntityEndIndex = position + word.length();
                } else if (entityEnd(previousEntityType, currentEntityType)) {
                    // entity span ends here
                    if (currentEntityStartIndex > 0 && currenEntityEndIndex > 0) {
                        currentNamedEntity = inputFile.substring(currentEntityStartIndex, currenEntityEndIndex);
                        addEntityToBratAnnotation(annOutput, currentNamedEntity, entitiesIndex, previousEntityType,
                                currentEntityStartIndex, currenEntityEndIndex);
                        ++entitiesIndex;
                        currentEntityStartIndex = -1;
                        currenEntityEndIndex = -1;
                    }
                } else if (insideEntity(previousEntityType, currentEntityType)) {
                    currenEntityEndIndex = position + word.length();
                    System.out.println("end index " + currenEntityEndIndex);
                }

                cursor = position + word.length();
                previousEntityType = currentEntityType;
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return annOutput.toString();
    }

    private boolean entityBeginning(String previousEntityType, String currentEntityType) {
        return (previousEntityType.equals(OUTSIDE_ENTITY) && !currentEntityType.equals(OUTSIDE_ENTITY));
    }

    private boolean entityEnd(String previousEntityType, String currentEntityType) {
        return (!previousEntityType.equals(OUTSIDE_ENTITY) && currentEntityType.equals(OUTSIDE_ENTITY));
    }

    private boolean insideEntity(String previousEntityType, String currentEntityType) {
        return (!previousEntityType.equals(OUTSIDE_ENTITY) && !currentEntityType.equals(OUTSIDE_ENTITY));
    }

    private void addEntityToBratAnnotation(StringBuilder annOutput, String namedEntity, int entitiesIndex, String entityType,
            int entityStartIndex, int entityEndIndex) {
        annOutput.append('T').append(entitiesIndex).append('\t').append(entityType).append('\t');
        annOutput.append(entityStartIndex).append('\t').append(entityEndIndex);
        annOutput.append('\t').append(namedEntity).append(CRLF);
    }

    public static void main(String[] args) {

        File outputDir = new File("D:/Work/NLP/corpuses/ms_academic/brat-data");
        File textFile = new File("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/2001_The_human_development_index_and_sustainability_a_constructive_proposal_no_hyphenation.txt");

        File stanfordFile = new File("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/annotations/2001_The_human_development_index_and_sustainability_a_constructive_proposal_stanford.txt");

        BratAnnotationPairCreator annotationCreator = new BratAnnotationPairCreator(outputDir);

        annotationCreator.toBratAnnotationFormatPair(textFile, stanfordFile);
    }
}
