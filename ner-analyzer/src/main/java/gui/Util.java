/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;

/**
 *
 * @author vlad
 */
public class Util {
    
    public static String getHTMLColor(Color color) {
    if (color == null) {
        return "#000000";
    }
    return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
}
    
}
