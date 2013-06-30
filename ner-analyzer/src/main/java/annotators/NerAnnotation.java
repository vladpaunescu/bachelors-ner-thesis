/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotators;

/**
 *
 * @author vlad
 */
public class NerAnnotation {
    
    private String _text;
    private String _symbol;
    private String _neLabel;
    
    public NerAnnotation(){}
    
    public NerAnnotation(String text, String symbol, String label){
        _text = text;
        _symbol = symbol;
        _neLabel = label;
    }
    
    /**
     * @return the _text
     */
    public String getText() {
        return _text;
    }

    /**
     * @param text the _text to set
     */
    public void setText(String text) {
        this._text = text;
    }

    /**
     * @return the _symbol
     */
    public String getSymbol() {
        return _symbol;
    }

    /**
     * @param symbol the _symbol to set
     */
    public void setSymbol(String symbol) {
        this._symbol = symbol;
    }

    /**
     * @return the _neLabel
     */
    public String getNeLabel() {
        return _neLabel;
    }

    /**
     * @param neLabel the _neLabel to set
     */
    public void setNeLabel(String neLabel) {
        this._neLabel = neLabel;
    }
    
    
}
