/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.util.Comparator;
import java.util.Map;

public class ReverseValueComparator implements Comparator<String> {

    Map<String, Integer> _map;
    public ReverseValueComparator(Map<String, Integer> base) {
        this._map = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    @Override
    public int compare(String a, String b) {
        if (_map.get(a) >= _map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
