/*
 * File          : TextLineNumberWrapper.java
 * Author        : Charis Charitsis
 * Creation Date : 6 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;
// Import custom classes
import edu.stanford.util.UIUtil;

/**
 * Displays line numbers for a related text component. The text component must
 * use the same line height for each line. TextLineNumber supports wrapped lines
 * and will highlight the line number of the current line in the text component.
 * <br>
 * This class is designed to be used as a component added to the row header of
 * of a JScrollPane.
 */
public class TextLineNumberWrapper extends JPanel
       implements CaretListener, DocumentListener, PropertyChangeListener
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 4361526898879790187L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** Left alignment for the painted digits */ 
    public  static final float  LEFT              = 0.0f;
    /** Center alignment for the painted digits */ 
    public  static final float  CENTER            = 0.5f;
    /** Right alignment for the painted digits */ 
    public  static final float  RIGHT             = 1.0f;
    /** The outer border for the area that displays the line numbers */
    private static final Border OUTER_BORDER      =
                                      new MatteBorder(0, 0, 0, 2, Color.GRAY);
    /** The height for the line number display column */
    private static final int    HEIGHT            = Integer.MAX_VALUE - 1000000;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The text component to wrap with the line number display
     */
    private final JTextComponent component;
    /**
     * The module that manages the text edits, providing a way to undo or redo
     * the appropriate edits
     */
    private final UndoManager    undoManager;
    /**
     * {@code true} to update the Font and repaint the line numbers or
     * {@code false} to just repaint the line numbers
     */
    private boolean updateFont;
    /**
     * The border gap is used in calculating the left and right insets of the
     * border
     */
    private int     borderGap;
    /**
     * The background color for the line number display or {@code null} to use
     * the existing background color
     */
    private Color   lineDisplayBackground;
    /** The Color used to render the current line digits */
    private Color   currLineForeground;
    /** The alignment of the painted digits */
    private float   digitAlignment;
    /** The minimum display digits for the line numbers */
    private int     minDisplayDigits;
    /**
     * The last number of digits for the line number value.<br>
     * E.g. if there are 999 lines and we enter a new line, then lastDigits is
     *      '3' since the last line numbers were '999' (thus 3 digits)
     */
    private int     lastDigits;
    /** The document's last height */
    private double  lastHeight;
    /** The document's last line */
    private int     lastLine;
    /**
     * Map that uses as key a font family and font size (i.e., appends the font
     * size to the font family string) to retrieve the font metrics for this
     * (font family, font size) tuple.
     */
    private HashMap<String, FontMetrics> fonts;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new TextLineNumberWrapper (i.e., line number component for a
     * text component).<br>
     * The minimum display width will be based on 3 digits.
     *
     * @param component The related text component to wrap with a line number
     *                  display
     */
    public TextLineNumberWrapper(JTextComponent component) {
        this(component, 3);
    }
    
    /**
     * Creates a new TextLineNumberWrapper (i.e., line number component for a
     * text component).<br>
     *
     * @param component The related text component to wrap with a line number
     *                  display
     * @param minDisplayDigits The minimum display digits for the line numbers
     */
    public TextLineNumberWrapper(JTextComponent component,
                                 int            minDisplayDigits) {
        this.component = component;
        // Use the same font as the text component that we want to add line
        // numbers to
        setFont(component.getFont());
        
        setBorderGap(5);
        // Use red to display the line number of the current/active line
        setCurrentLineForeground(Color.RED);
        
        setDigitAlignment(RIGHT);
        setMinDisplayDigits(minDisplayDigits);
        
        component.getDocument().addDocumentListener(this);
        component.addCaretListener(this);
        component.addPropertyChangeListener("font", this);
        
        undoManager = UIUtil.addUndoRedoFunctionality(component);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Resets the module that manages the undo or redo operations so that
     * previous text edits do not count any mode (cannot be un-done/re-done) as
     * if we start monitoring the edits from this point on.
     */
    public void resetUndoRedoManager() {
        undoManager.discardAllEdits();
    }
    
    /**
     * Returns the update font property that indicates whether this Font should
     * be updated automatically when the Font of the related text component is
     * changed.
     *
     * @return {@code true} to update the Font and repaint the line numbers when
     *         the Font of the related text component is changed or
     *         {@code false} to just repaint the line numbers
     */
    public boolean getUpdateFont() {
        return updateFont;
    }
    
    /**
     * Sets the update font property that indicates whether this Font should be
     * updated automatically when the Font of the related text component is
     * changed.
     *
     * @param updateFont {@code true} to update the Font and repaint the line
     *                   numbers or {@code false} to just repaint the line
     *                   numbers
     */
    public void setUpdateFont(boolean updateFont) {
        this.updateFont = updateFont;
    }
    
    /**
     * Returns border gap that is used in calculating the left and right insets
     * of the border
     *
     *  @return the border gap in pixels
     */
    public int getBorderGap() {
        return borderGap;
    }
    
    /**
     * The border gap is used in calculating the left and right insets of the
     * border.<br>
     * Default value is 5.
     *
     * @param borderGap The gap in pixels
     */
    public void setBorderGap(int borderGap) {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder(new CompoundBorder(OUTER_BORDER, inner));
        lastDigits = 0;
        setPreferredWidth();
    }
    
    /**
     * Gets the background color for the line number display.
     *
     * @return the color used to render the current line number
     */
    public Color getLineDisplayBackground() {
        return lineDisplayBackground == null ?
               getBackground() : lineDisplayBackground;
    }
    
    /**
     * Sets the Color used as background for the line number display.
     *
     * @param lineDisplayBackground The background color for the line number
     *                              display or {@code null} to use the existing
     *                              background color
     */
    public void setLineDisplayBackground(Color lineDisplayBackground) {
        this.lineDisplayBackground = lineDisplayBackground;
    }
    
    /**
     * Gets the current line rendering color
     *
     * @return the color used to render the current line number
     */
    public Color getCurrentLineForeground() {
        return currLineForeground == null ?
               getForeground() : currLineForeground;
    }
    
    /**
     * Sets the Color used to render the current line digits.<br>
     * Default is Coolor.RED.
     *
     * @param currentLineForeground The color to render the current line
     */
    public void setCurrentLineForeground(Color currentLineForeground) {
        this.currLineForeground = currentLineForeground;
    }
    
    /**
     * Gets the alignment of the painted digits.
     *
     * @return the alignment of the painted digits
     */
    public float getDigitAlignment() {
        return digitAlignment;
    }
    
    /**
     * Specifies the horizontal alignment of the digits within the component.
     * Common values would be:
     * <ul>
     * <li>TextLineNumberWrapper.LEFT
     * <li>TextLineNumberWrapper.CENTER
     * <li>TextLineNumberWrapper.RIGHT (default)
     * </ul>
     * 
     * @param digitAlignment The horizontal alignment of the digits within the
     *                       component
     */
    public void setDigitAlignment(float digitAlignment) {
        this.digitAlignment = digitAlignment > 1.0f ?
                              1.0f :
                              digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }
    
    /**
     * Returns the minimum display digits for the line numbers.
     *
     * @return the minimum display digits for the line numbers
     */
    public int getMinDisplayDigits() {
        return minDisplayDigits;
    }
    
    /**
     * Specifies the minimum number of digits for the line numbers.<br>
     * Default is 3.
     *
     * @param minDisplayDigits The minimum display digits for the line numbers
     */
    public void setMinDisplayDigits(int minDisplayDigits) {
        this.minDisplayDigits = minDisplayDigits;
        setPreferredWidth();
    }
    
    /**
     * Draws the line numbers
     *  
     * @param g the Graphics object
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Determine the width of the space available to draw the line number
        Font font = component.getFont();
        FontMetrics fontMetrics = component.getFontMetrics(font);
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;
        
        // Determine the rows to draw within the clipped bounds
        Rectangle clip = g.getClipBounds();
        Point pointToTranslate = new Point(0, clip.y);
        int rowStartOffset = component.viewToModel2D(pointToTranslate);
        pointToTranslate = new Point(0, clip.y + clip.height);
        int endOffset = component.viewToModel2D(pointToTranslate);
        
        while (rowStartOffset <= endOffset) {
            try {
                if (isCurrentLine(rowStartOffset)) {
                    g.setColor(getCurrentLineForeground());
                }
                else {
                    g.setColor(getForeground());
                }
                setBackground(getLineDisplayBackground());
                
                // Get the line number as a string and then determine the 'X'
                // and 'Y' offsets for drawing the string.
                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth(lineNumber);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g.drawString(lineNumber, x, y);
                
                //  Move to the next row
                rowStartOffset =
                        Utilities.getRowEnd(component, rowStartOffset) + 1;
            }
            catch(Exception e) {
                break;
            }
        }
    }
    
    // -------------------------------------------------------------------- //
    //   D   O   C   U   M   E   N   T      L   I   S   T   E   N   E   R   //
    //                   I   N   T   E   R   F   A   C   E                  //
    // -------------------------------------------------------------------- //
    // Implement DocumentListener interface
    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }
    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }
    
    // -------------------------------------------------------- //
    //   C   A   R   E   T      L   I   S   T   E   N   E   R   //
    //            I   N   T   E   R   F   A   C   E             //
    // -------------------------------------------------------- //
    //  Implement CaretListener interface
    @Override
    public void caretUpdate(CaretEvent e) {
        // Get the line the caret is positioned on
        int caretPos = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPos);
        
        // Need to repaint so the correct line number can be highlighted
        if (lastLine != currentLine) {
            getParent().repaint();
            lastLine = currentLine;
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   O   P   E   R   T   Y      C   H   A   N   G   E   //
    //                L   I   S   T   E   N   E   R                 //
    //              I   N   T   E   R   F   A   C   E               //
    // ------------------------------------------------------------ //
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof Font) {
            if (updateFont) {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            }
            else {
                getParent().repaint();
            }
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Calculates and sets the width needed to display the maximum line number.
     */
    private void setPreferredWidth() {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minDisplayDigits);
        
        // Update the sizes when number of digits in the line number changes
        if (lastDigits != digits) {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics( getFont() );
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;
            
            Dimension preferredSize = getPreferredSize();
            preferredSize.setSize(preferredWidth, HEIGHT);
            setPreferredSize(preferredSize);
            setSize(preferredSize);
        }
    }
    
    /**
     * Determines if the caret is currently positioned on the line we are about
     * to paint so the line number can be highlighted.
     * 
     * @param rowStartOffset The offset for the line we are about to paint
     * 
     * @return {@code true} if the caret is currently positioned on the line we
     *         are about to paint or {@code false} otherwise
     */
    private boolean isCurrentLine(int rowStartOffset) {
        int caretPos = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        
        return
         root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPos);
    }
    
    /**
     * Returns the line number to be drawn or an empty string when a line of
     * text has wrapped.
     * 
     * @param rowStartOffset The start offset for drawing the row whose line
     *                       number we want to get
     * 
     * @return the line number to be drawn or an empty string when a line of
     *         text has wrapped
     */
    private String getTextLineNumber(int rowStartOffset) {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex(rowStartOffset);
        Element line = root.getElement(index);
        
        if (line.getStartOffset() == rowStartOffset) {
            return String.valueOf(index + 1);
        }
        else {
            return "";
        }
    }
    
    /**
     * Calculates the X offset to properly align the line number when drawn.
     * 
     * @param availableWidth The available width in pixels 
     * @param stringWidth The width in pixels of the line number as string
     *  
     * @return the X offset to properly align the line number when drawn
     */
    private int getOffsetX(int availableWidth, int stringWidth) {
        return (int)((availableWidth - stringWidth) * digitAlignment);
    }
    
    /**
     * Calculates the Y offset for the current row.
     * 
     * @param rowStartOffset The start offset for drawing the current row
     * @param fontMetrics The font metrics information about the rendering of a
     *                    particular font on a particular screen
     * 
     * @return the Y offset for the current row
     * 
     * @throws BadLocationException if the start offset does not represent a
     *                              valid location in the associated document
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
            throws BadLocationException {
        //  Get the bounding rectangle of the row
        Rectangle2D rowBoundRect = component.modelToView2D(rowStartOffset);
        int lineHeight = fontMetrics.getHeight();
        double y = rowBoundRect.getY() + rowBoundRect.getHeight();
        int descent = 0;
        
        // The text needs to be positioned above the bottom of the bounding
        // rectangle based on the descent of the font(s) contained on the row
        if (rowBoundRect.getHeight() == lineHeight) { // default font is being used
            descent = fontMetrics.getDescent();
        }
        else { // We need to check all the attributes for font changes
            if (fonts == null) {
                fonts = new HashMap<String, FontMetrics>();
            }
            
            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);
            
            for (int i = 0; i < line.getElementCount(); i++) {
                Element child = line.getElement(i);
                AttributeSet attribs = child.getAttributes();
                String  fontFamily =
                       (String)attribs.getAttribute(StyleConstants.FontFamily);
                Integer fontSize   =
                       (Integer)attribs.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;
                
                FontMetrics currFontMetrics = fonts.get(key);
                if (currFontMetrics == null) {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    currFontMetrics = component.getFontMetrics(font);
                    fonts.put(key, currFontMetrics);
                }
                
                descent = Math.max(descent, currFontMetrics.getDescent());
            }
        }
        
        return (int)(y - descent);
    }
    
    /**
     * Makes the necessary visual adjustments when the document changes.<br>
     * A document change may affect the number of displayed lines of text.
     * Therefore the line numbers may also change.
     */
    private void documentChanged() {
        //  The view of the component has not been updated at the time the
        // DocumentEvent is fired
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int endPos = component.getDocument().getLength();
                    Rectangle2D rect = component.modelToView2D(endPos);
                    
                    if (rect != null && rect.getY() != lastHeight) {
                        setPreferredWidth();
                        getParent().repaint();
                        lastHeight = rect.getY();
                    }
                }
                catch (BadLocationException ble) {
                   // Do nothing
                }
            }
        });
    }
}
