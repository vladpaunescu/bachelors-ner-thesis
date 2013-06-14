/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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
            FileUtils.writeStringToFile(annFile, annFormat, "utf-8");
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

//      ]	-RRB-	97	98	O	-RRB-
//      On	On	99	101	O	IN
//      :	:	101	102	O	:
//      11	11	103	105	DATE	CD
// multline T1  DATE 20 25;26 30    jumps over 
    private String toBratAnnotation(File textFile, File stanfordFile) {
        StringBuilder annOutput = new StringBuilder();
        try {
            String inputFile = FileUtils.readFileToString(textFile);
            List<String> lines = FileUtils.readLines(stanfordFile);

            Iterator<String> iterator = lines.iterator();
            int entitiesIndex = 1;
            
            
            String line = iterator.next();
            
            while (iterator.hasNext()) {
                String[] tokens = line.split("\\t");
                String entityType = tokens[4];

                if (entityType.equals(OUTSIDE_ENTITY)) {
                    line = iterator.next();
                    continue;
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
                        String namedEntity = inputFile.substring(beginIndex, endIndex);                      
                        addEntityToBratAnnotation(annOutput, namedEntity, entitiesIndex, entityType,
                                beginIndex, endIndex);

                        entitiesIndex++;
                        System.out.println(namedEntity);
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return annOutput.toString();
    }

    private void addEntityToBratAnnotation(StringBuilder annOutput, String namedEntity, int entitiesIndex, String entityType,
            int entityStartIndex, int entityEndIndex) {
        annOutput.append('T').append(entitiesIndex).append('\t').append(entityType).append(' ');
        annOutput.append(entityStartIndex).append(' ').append(entityEndIndex);
        annOutput.append('\t').append(namedEntity).append(CRLF);
    }

    public static void main(String[] args) {

        File outputDir = new File("D:/Work/NLP/corpuses/ms_academic/brat-data");
        File textFile = new File("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/2001_The_human_development_index_and_sustainability_a_constructive_proposal_tika.txt");

        File stanfordFile = new File("D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/annotations/2001_The_human_development_index_and_sustainability_a_constructive_proposal_tika_stanford.txt");

        BratAnnotationPairCreator annotationCreator = new BratAnnotationPairCreator(outputDir);

        annotationCreator.toBratAnnotationFormatPair(textFile, stanfordFile);
    }
}
