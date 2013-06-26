/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author vlad
 */
public class NamedEntities implements Iterable {

    private Map<String, List<AnnNamedEntity>> _entityTypes;
    private Map<String, Color> _categoryColors;
    
    private static Color[] colors  = {
        new Color(255, 0, 50),
        new Color(153,255,153),
        new Color(153,255,255), 
        new Color(255, 255, 0),
        new Color(234, 174, 245),
        new Color(133, 161, 237),
        new Color(133, 237, 235),
        new Color(51, 204, 89),
        new Color(168, 230, 87),
        new Color(230, 194, 87),
        new Color(87, 230, 156),
        new Color(17, 158, 95),
        new Color(132, 222, 35),
        new Color(234,212,252)
    };
    
    private int _categoriesCount;

    public NamedEntities() {
        _entityTypes = new HashMap<>();
        _categoryColors = new HashMap<>();
        _categoriesCount = 0;
    }
    

    public void addEntityCategory(String type) {
        if (_entityTypes.get(type) == null) {
            _entityTypes.put(type, new ArrayList<AnnNamedEntity>());
            _categoryColors.put(type, colors[_categoriesCount]);
            _categoriesCount++;
        }
    }

    public List<AnnNamedEntity> getEntitiesByCategory(String type) {
        if(_entityTypes.containsKey(type)){
            return _entityTypes.get(type);
        }
        
        return new ArrayList<>();
    }

    public void addEntityToCategory(AnnNamedEntity entity, String type) {
        addEntityCategory(type);
        _entityTypes.get(type).add(entity);
    }

    @Override
    public Iterator iterator() {
        return _entityTypes.keySet().iterator();
    }

    public Set<String> getAllCategories() {
        return _entityTypes.keySet();
    }

    public List<AnnNamedEntity> getAllEntities() {
        List<AnnNamedEntity> entities = new ArrayList<>();
        for (String category : this.getAllCategories()) {
            entities.addAll(_entityTypes.get(category));
        }

        return entities;
    }

    public List<AnnNamedEntity> getAllEntitiesInSortedOrder() {
        List<AnnNamedEntity> entities = this.getAllEntities();
        Collections.sort(entities, new AnnEntitiesComparator());
        return entities;
    }
    
    public Color getCategoryColor(String category){
        if(_categoryColors.containsKey(category)){
            return _categoryColors.get(category);
        }
        return Color.GRAY;
    }
}
