/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import static annotators.StanfordNerAnnotator.TAB;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pdfparser.DirectoryTreeCrawler;
import pdfparser.MyFileUtils;

/**
 *
 * @author vlad
 */
public class IoEncodingCreatorfromAnnFile {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            IoEncodingCreatorfromAnnFile.class.getName());
    private final static Properties STANFORD_PORPS = new Properties();
    private final static List<String> NAMED_ENTITIES =
            Arrays.asList("PERSON", "ORGANIZATION", "LOCATION", "MISC");

    static {
        STANFORD_PORPS.put("annotators", "tokenize, ssplit, pos");
    }
    private String _rootDirName;
    private String _outputDirName;
    private StanfordCoreNLP _snlp;
    private DirectoryTreeCrawler _crawler;
    private List<AnnNamedEntity> _entities;
    private Set<String> _entityTypes;

    public IoEncodingCreatorfromAnnFile(String rootDir, String outputDir) {
        _rootDirName = rootDir;
        _outputDirName = outputDir;
        initializeStanfordParser();
        _crawler = new DirectoryTreeCrawler(rootDir);
        _entityTypes = new HashSet<>();
    }

    public void convertFiles() {
        if (!createOututDirectory()) {
            log.error("Output directory creation failed");
            return;
        }

        Collection<File> annFiles = _crawler.getAnnotationFiles();
        int total = annFiles.size();
        int count = 1;
        log.info("Total files to process " + total);

        for (File annFile : annFiles) {
            log.info(String.format("Converting to IO Encoding file %d of %d : %s", count, total, annFile.getAbsolutePath()));
            convertFile(annFile);
        }

        log.info("Entity types found");
        for (String type : _entityTypes) {
            System.out.println(type);
        }
    }

    private void convertFile(File annFile) {

        _entities = getEntitiesFromAnnFile(annFile);
        Collections.sort(_entities, new AnnEntitiesComparator());
        String annFileNoExtension = FilenameUtils.removeExtension(annFile.getAbsolutePath());
        String text = readTextCorrespondingTextFile(annFileNoExtension);
        String ioEncodedText = parseFile(text);

        File textFile = new File(String.format("%s.txt", annFileNoExtension));
        File outputIoFile = new File(String.format("%s%s_io.txt", _outputDirName,
                FilenameUtils.getName(annFileNoExtension)));
        try {
            FileUtils.copyFileToDirectory(textFile, new File(_outputDirName));
            FileUtils.writeStringToFile(outputIoFile, ioEncodedText, "UTF-8");
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    private List<AnnNamedEntity> getEntitiesFromAnnFile(File annFile) {

        List<AnnNamedEntity> entities = new ArrayList<>();
        try {
            List<String> annotations = FileUtils.readLines(annFile, "UTF-8");
            for (String line : annotations) {
                String[] tokens = line.split("\\s");
                String entityType = tokens[1];
                String startIndex = tokens[2];
                String endIndex = tokens[3];
                String entityText = line.substring(line.indexOf(tokens[4]));

                if (endIndex.contains(";")) {
                    endIndex = tokens[4];
                    entityText = line.substring(line.indexOf(tokens[5]));
                }

                AnnNamedEntity entity = new AnnNamedEntity(entityType);
                entity.setStartIndex(Integer.parseInt(startIndex));
                entity.setEndIndex(Integer.parseInt(endIndex));
                entity.setNamedEntityText(entityText);

                entities.add(entity);
                if (!_entityTypes.contains(entityType)) {
                    _entityTypes.add(entityType);
                }

            }
        } catch (IOException ex) {
            log.warn(ex);
        }
        log.info(String.format("Found %d entities", entities.size()));
        return entities;
    }

    private String readTextCorrespondingTextFile(String filenameNoExtension) {
        File textFile = new File(String.format("%s.txt", filenameNoExtension));
        String text = "";
        try {
            text = FileUtils.readFileToString(textFile, "UTF-8");
        } catch (IOException ex) {
            log.error(ex);
        }

        return text;
    }

    private String parseFile(String text) {
        StringBuilder collector = new StringBuilder();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        _snlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            parseSentence(sentence, collector);
        }

        return collector.toString();

    }
    // word POS gold class
    // Smith NNP PERSON

    private void parseSentence(CoreMap sentence, StringBuilder collector) {
        // a CoreLabel is a CoreMap with additional token-specific methods
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String originalText = token.originalText();

            // this is the POS tag of the token
            String pos = token.tag();

            int beginPosition = token.beginPosition();
            int endPosition = token.endPosition();

            String namedEntityClass = getNamedEntityClass(beginPosition, endPosition, originalText);

            String[] subtokens = originalText.split("\\s");
            for (String subtoken : subtokens) {
                collector.append(subtoken).append(" ").append(pos).
                        append(" ").append(namedEntityClass).append("\n");
            }
        }
    }

    private String getNamedEntityClass(int beginIndex, int endIndex, String text) {
        for (AnnNamedEntity entity : _entities) {
            if (entity.getStartIndex() <= beginIndex && endIndex <= entity.getEndIndex()) {
                if (!entity.getNamedEntityText().contains(text)) {
                    log.warn(String.format("Named entity %s does not contain %s",
                            entity.getNamedEntityText(), text));
                }
//                return entity.getType();
                if (NAMED_ENTITIES.contains(entity.getType())) {
                    return entity.getType();
                }
                else {
                    return "O";
                }
            }
        }
        return "O";
    }

    private boolean createOututDirectory() {
        return MyFileUtils.makeDirectories(new File(FilenameUtils.getFullPath(_outputDirName)));
    }

    private void initializeStanfordParser() {
        _snlp = new StanfordCoreNLP(STANFORD_PORPS);
    }

    public static void main(String[] args) {

        String rootSrcDir = "D:/Work/NLP/corpuses/ms_academic/brat-data/ann-splits/done";
        String rootOutputDir = "D:/Work/NLP/corpuses/ms_academic/train-io-4-class/";

        IoEncodingCreatorfromAnnFile creator = new IoEncodingCreatorfromAnnFile(rootSrcDir, rootOutputDir);
        creator.convertFiles();
    }
}
