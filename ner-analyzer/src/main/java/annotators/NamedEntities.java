/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import com.sun.xml.internal.ws.api.ha.HaInfo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
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
        new Color(255, 255, 0)
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
        return _entityTypes.get(type);
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
        return _categoryColors.get(category);
    }
}
