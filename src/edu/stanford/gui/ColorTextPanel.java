/*
 * File          : ColorTextPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 14 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Panel that displays color text.
 */
public class ColorTextPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 933773059438073226L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The text component that is marked up with attributes that are represented
     * graphically
     */
    private final JTextPane textPane;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates new ColorTextPanel.
     */
    public ColorTextPanel() {
        textPane = new JTextPane();
        
        EmptyBorder emptyBorder = new EmptyBorder(new Insets(10, 10, 10, 10));
        textPane.setBorder(emptyBorder);
        //textPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        textPane.setMargin(new Insets(5, 5, 5, 5));
        
        add(textPane);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Appends the provided text using the specified color to the panel.
     * 
     * @param text The text to append
     * @param color The text color or {@code null} for the default (black) color
     */
    public void appendText(String text,
                           Color  color) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        if (color == null) {
            color = Color.BLACK;
        }
        AttributeSet attribs =
                     styleContext.addAttribute(SimpleAttributeSet.EMPTY,
                                               StyleConstants.Foreground,
                                               color);
        
        attribs = styleContext.addAttribute(attribs,
                                            StyleConstants.FontFamily,
                                            "Lucida Console");
        attribs = styleContext.addAttribute(attribs,
                                            StyleConstants.Alignment,
                                            StyleConstants.ALIGN_JUSTIFIED);
        
        int length = textPane.getDocument().getLength();
        textPane.setCaretPosition(length);
        textPane.setCharacterAttributes(attribs, false);
        textPane.replaceSelection(text);
    }
    
    /**
     * Removes all text.
     */
    public void clear() {
       textPane.setText("");
    }
}
