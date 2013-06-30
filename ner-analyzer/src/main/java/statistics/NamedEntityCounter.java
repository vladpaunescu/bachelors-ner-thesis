/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import db.Entities;
import db.NerHibernateUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import pdfparser.DirectoryTreeCrawler;

/**
 *
 * @author vlad
 */
public class NamedEntityCounter {

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            NamedEntityCounter.class.getName());
    private String _rootDirName;
    private DirectoryTreeCrawler _crawler;
    private Map<String, Integer> _entityTypesCount;
    private Map<NamedEntity, Integer> _entities;

    public NamedEntityCounter(String rootDir) {
        _rootDirName = rootDir;
        _crawler = new DirectoryTreeCrawler(rootDir);
        _entityTypesCount = new HashMap<>();
        _entities = new HashMap<>();
    }

    public void countNamedEntities() {
        Collection<File> annFiles = _crawler.getAnnotationFiles();
        int total = annFiles.size();
        int count = 1;
        log.info("Total files to process " + total);

        for (File annFile : annFiles) {
            log.info(String.format("Counting entities file %d of %d : %s", count, total, annFile.getAbsolutePath()));
            countNamedEntitiesForFile(annFile);
        }

        log.info("Entity types found");
        for (Map.Entry<String, Integer> type : _entityTypesCount.entrySet()) {
            System.out.println(type.getKey() + " " + type.getValue());
        }

        showTopEntities();
        insertEntitiesIntoDb();
    }

    private void countNamedEntitiesForFile(File annFile) {

        List<NamedEntity> entities = getEntitiesFromAnnFile(annFile);
        for (NamedEntity entity : entities) {
            if (_entities.get(entity) != null) {
                _entities.put(entity, _entities.get(entity) + 1);
            } else {
                _entities.put(entity, 1);
            }
        }
    }

    private List<NamedEntity> getEntitiesFromAnnFile(File annFile) {

        List<NamedEntity> entities = new ArrayList<>();
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

                NamedEntity entity = new NamedEntity(entityText, entityType);
                entities.add(entity);
                if (_entityTypesCount.get(entityType) != null) {
                    _entityTypesCount.put(entityType, _entityTypesCount.get(entityType) + 1);
                } else {
                    _entityTypesCount.put(entityType, 1);
                }

            }
        } catch (IOException ex) {
            log.warn(ex);
        }
        log.info(String.format("Found %d entities", entities.size()));
        return entities;
    }

    private void showTopEntities() {
        System.out.println("Total entities count: " + _entities.size());

        ReverseNamedEntityValueComparator rvc = new ReverseNamedEntityValueComparator(_entities);
        TreeMap<NamedEntity, Integer> _sortedEntities = new TreeMap<>(rvc);
        _sortedEntities.putAll(_entities);
        int limit = 100;
        int count = 0;
        for (NamedEntity key : _sortedEntities.navigableKeySet()) {
            System.out.println(_entities.get(key) + " " + key.getText() + " " + key.getType());
            count++;
            if (count == limit) {
                break;
            }
        }
    }

    private void insertEntitiesIntoDb() {
        for (Map.Entry<NamedEntity, Integer> entry : _entities.entrySet()) {
            NamedEntity entity = entry.getKey();
            log.info(String.format("Inserting entity: %s , %s, %d into db.", 
                    entity.getText(), entity.getType(), entry.getValue()));

            Session session = NerHibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Entities row = new Entities();
            row.setName(entity.getText());
            row.setType(entity.getType());
            row.setCount(entry.getValue());
            session.save(row);
            session.getTransaction().commit();
            session.close();
            log.info("Finished inserting entities into db.");
        }


    }

    public static void main(String[] args) {
        String rootDir = "D:/Work/NLP/corpuses/ms_academic/brat-data/done";
        NamedEntityCounter counter = new NamedEntityCounter(rootDir);
        counter.countNamedEntities();
    }
}

class ReverseNamedEntityValueComparator implements Comparator<NamedEntity> {

    Map<NamedEntity, Integer> _map;

    public ReverseNamedEntityValueComparator(Map<NamedEntity, Integer> base) {
        this._map = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    @Override
    public int compare(NamedEntity a, NamedEntity b) {
        if (_map.get(a) >= _map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}