/*
 * File          : ScrollableTextPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 5 November 2020
 * Last Modified : 21 September 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentListener;

/**
 * Scrollable text area that shows also the line numbers.
 */
public class ScrollableTextPanel extends JScrollPane
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 3305854158583409751L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The component responsible to add line numbers to the displayed text */
    private final TextLineNumberWrapper textLineNumberWrapper;
    /** The document style */
    private final JavaStyledDocument    styledDocument;
    /** The component responsible to display the text */  
    private final JTextPane             textPane;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new scrollable text area that displays also the line numbers.
     */
    public ScrollableTextPanel() {
        this(null);
    }
    
    /**
     * Creates a new scrollable text area that displays also the line numbers.
     * 
     * @param font The font to use or {@code null} for the default font
     */
    public ScrollableTextPanel(Font font) {
        this(font, null);
    }
    
    /**
     * Creates a new scrollable text area that displays also the line numbers.
     * 
     * @param font The font to use or {@code null} for the default font
     * @param documentListener The modules that receive notifications of text
     *                         changes or {@code null} to prevent sending
     *                         notifications on text changes
     */
    public ScrollableTextPanel(Font             font,
                               DocumentListener documentListener) {
        styledDocument = new JavaStyledDocument(); 
        textPane = new JTextPane(styledDocument);
        if (documentListener != null) {
            textPane.getDocument().addDocumentListener(documentListener);
        }
        textPane.setFont(font);
        setViewportView(textPane);
        textLineNumberWrapper = new TextLineNumberWrapper(textPane);
        setRowHeaderView(textLineNumberWrapper);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Highlights the text lines in the specified range 
     * 
     * @param startLine The start line, inclusive (gets highlighted)
     * @param endLine The start line, exclusive (does not get highlighted)
     */
    public void highlightText(int startLine, int endLine) {
        styledDocument.highlightText(startLine, endLine);
    }
    
    /**
     * Returns the text contained in this text panel.<br>
     * If an exception is thrown while attempting to retrieve the text,
     * {@code null} will be returned.
     * 
     * @return the contained text
     */
    public String getText() {
        return textPane.getText();
    }
    
    /**
     * Sets the text to the specified content.
     * 
     * @param text The new text to be set; if null the old text will be deleted
     * @param coloText {@code true} to color the text or {@code false} to use
     *                 black color only
     */
    public void setText(String text, boolean colorText) {
        styledDocument.setColor(colorText);
        textPane.setText(text);
        textPane.setCaretPosition(0); // Scroll to the top of the text panel
        textLineNumberWrapper.resetUndoRedoManager();
    }
    
    /**
     * Sets the specified boolean to indicate whether or not the text area is
     * editable.
     * 
     * @param flag {@code true} to make the text area editable or {@code false}
     *             to make it non-editable
     */
    public void setEditable(boolean flag) {
        textPane.setEditable(flag);
    }
    
    /**
     * Sets the Color used as background for the line number display.
     *
     * @param lineDisplayBackground The background color for the line number
     *                              display or {@code null} to use the existing
     *                              background color
     */
    public void setLineDisplayBackground(Color lineDisplayBackground) {
        textLineNumberWrapper.setLineDisplayBackground(lineDisplayBackground);
    }
}
