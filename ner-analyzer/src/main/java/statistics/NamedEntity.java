/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.util.Objects;

/**
 *
 * @author vlad
 */
public class NamedEntity {
    
    private String _text;
    private String _type;
    
    public NamedEntity(String text, String type){
        _text = text;
        _type = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getText());
        hash = 97 * hash + Objects.hashCode(this.getType());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamedEntity other = (NamedEntity) obj;
        if (!Objects.equals(this._text, other._text)) {
            return false;
        }
        if (!Objects.equals(this._type, other._type)) {
            return false;
        }
        return true;
    }

    /**
     * @return the _text
     */
    public String getText() {
        return _text;
    }

    /**
     * @param name the _text to set
     */
    public void setText(String text) {
        this._text = text;
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
    
    
    
}
