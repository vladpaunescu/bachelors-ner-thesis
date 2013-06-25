/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

import java.util.Comparator;

/**
 *
 * @author vlad
 */
class AnnEntitiesComparator implements Comparator<AnnNamedEntity> {

    public AnnEntitiesComparator() {
    }

    @Override
    public int compare(AnnNamedEntity o1, AnnNamedEntity o2) {
        return o1.getStartIndex() - o2.getStartIndex();
    }
}
