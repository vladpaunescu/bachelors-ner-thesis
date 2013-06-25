/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

/**
 *
 * @author vlad
 */
public class AnnNamedEntity {
    
    private String _type;
    private int _startIndex;
    private int _endIndex;
    private String _namedEntityText;
    
    public AnnNamedEntity(String type){
        _type = type;
    }

    /**
     * @return the _type
     */
    public String getType() {
        return _type;
    }

    /**
     * @param type the _type to set
     */
    public void setType(String type) {
        this._type = type;
    }

    /**
     * @return the _startIndex
     */
    public int getStartIndex() {
        return _startIndex;
    }

    /**
     * @param startIndex the _startIndex to set
     */
    public void setStartIndex(int startIndex) {
        this._startIndex = startIndex;
    }

    /**
     * @return the _endIndex
     */
    public int getEndIndex() {
        return _endIndex;
    }

    /**
     * @param endIndex the _endIndex to set
     */
    public void setEndIndex(int endIndex) {
        this._endIndex = endIndex;
    }

    /**
     * @return the _namedEntityText
     */
    public String getNamedEntityText() {
        return _namedEntityText;
    }

    /**
     * @param namedEntityText the _namedEntityText to set
     */
    public void setNamedEntityText(String namedEntityText) {
        this._namedEntityText = namedEntityText;
    }
    
}
