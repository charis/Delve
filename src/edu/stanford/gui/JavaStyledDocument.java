/*
 * File          : JavaStyledDocument.java
 * Author        : Charis Charitsis
 * Creation Date : 5 November 2020
 * Last Modified : 22 September 2021
 */
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
// Import constants
import static edu.stanford.constants.Constants.JAVA_KEYWORDS;
import static edu.stanford.gui.Constants.GREEN_COLOR;
import static edu.stanford.gui.Constants.MAGENTA_COLOR;

/**
 * The container for text that serves as the model for swing text components. 
 */
public class JavaStyledDocument extends DefaultStyledDocument
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -3720473808602594114L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /**
     * The default pool of styles and their associated resources.
     */
    private static final StyleContext STYLE_CONTEXT =
                                      StyleContext.getDefaultStyleContext();
    /**
     * The attribute to print single and multiline comments
     */
    private static final AttributeSet ATTR_COMMENT    =
                         STYLE_CONTEXT.addAttribute(STYLE_CONTEXT.getEmptySet(),
                                                    StyleConstants.Foreground,
                                                    GREEN_COLOR);
    /**
     * The attribute to print the Javadoc comments
     */
    private static final AttributeSet ATTR_JAVADOC    =
                         STYLE_CONTEXT.addAttribute(STYLE_CONTEXT.getEmptySet(),
                                                    StyleConstants.Foreground,
                                                    new Color(65, 100, 190));
    /**
     * The attribute to print the double quotes
     */
    private static final AttributeSet ATTR_DOUBLE_QUOTE =
                         STYLE_CONTEXT.addAttribute(STYLE_CONTEXT.getEmptySet(),
                                                    StyleConstants.Foreground,
                                                    Color.BLUE);
    /**
     * The attribute for boldface font
     */
    private static SimpleAttributeSet BOLD_ON = new SimpleAttributeSet();
    static {
        BOLD_ON.addAttribute(StyleConstants.CharacterConstants.Bold,
                             Boolean.TRUE);
    }
    /**
     * The attribute for no boldface font
     */
    private static SimpleAttributeSet BOLD_OFF = new SimpleAttributeSet();
    static {
        BOLD_OFF.addAttribute(StyleConstants.CharacterConstants.Bold,
                              Boolean.FALSE);
    }
    
    /**
     * The attribute to print the Java keywords
     */
    private static final AttributeSet ATTR_KEYWORDS   =
                         STYLE_CONTEXT.addAttribute(BOLD_ON,
                                                    StyleConstants.Foreground,
                                                    MAGENTA_COLOR);
    
    /**
     * The attribute to print the normal text
     */
    private static final AttributeSet ATTR_PLAIN_TEXT =
                         STYLE_CONTEXT.addAttribute(BOLD_OFF,
                                                    StyleConstants.Foreground,
                                                    Color.BLACK);
    /**
     * The attribute to print text in black
     */
    private static final AttributeSet ATTR_HIGHLIGHT =
                         STYLE_CONTEXT.addAttribute(STYLE_CONTEXT.getEmptySet(),
                                                    StyleConstants.Background,
                                                    new Color(180, 216, 253));
    /**
     * The regular expression to search for Java keywords
     */
    private static final String REGEX_FOR_KEYWORDS = "(\\W)*("
                                                    + String.join("|",
                                                                 JAVA_KEYWORDS)
                                                    +")";
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * {@code true} to color the text or {@code false} to use  black color only
     */
    private boolean colorText;
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Sets the color mode. If the provided flag is set, the text will be
     * colored or else it will be in black
     * 
     * @param enableFlag {@code true} for color text or {@code false} for black
     *                   text only
     */
    public void setColor(boolean enableFlag) {
        colorText = enableFlag;
    }
    
    /**
     * Highlights the text lines in the specified range 
     * 
     * @param startLine The start line, inclusive (gets highlighted)
     * @param endLine The start line, exclusive (does not get highlighted)
     */
    public void highlightText(int startLine, int endLine) {
        try {
            String text = getText(0, getLength());
            String[] lines = text.split("\n");
            int startIndex = 0;
            for (int lineCount = 0; lineCount < startLine; lineCount++) {
                startIndex += lines[lineCount].length() + 1;
            }
            int endIndex = startIndex;
            for (int lineIndex = startLine; lineIndex < endLine; lineIndex++) {
                endIndex += lines[lineIndex].length() + 1;
            }
            
            setCharacterAttributes(startIndex,
                                   endIndex - startIndex,
                                   ATTR_HIGHLIGHT,
                                   false);
        }
        catch (BadLocationException be) {
             System.err.println(be.getMessage());
        }
    }
    
    @Override
    public void insertString (int          offset,
                              String       str,
                              AttributeSet a)
           throws BadLocationException {
        super.insertString(offset, str, a);
        
        String text = getText(0, getLength());
        
        boolean addedTextIsInComments = setStyle(text, offset);
        if (!addedTextIsInComments) {
            // The last non-word character on the left (i.e., in text before
            // offset)
            int indexOfNonWordCharOnTheLeft = findLastNonWordChar(text, offset);
            if (indexOfNonWordCharOnTheLeft < 0) {
                indexOfNonWordCharOnTheLeft = 0;
            }
            
            // The first non-word character on the right (i.e., in text after
            // offset)
            int indexOfNonWordCharOnTheRight =
                              findFirstNonWordChar(text, offset + str.length());
            
            int wordStart = indexOfNonWordCharOnTheLeft;
            int wordEnd   = indexOfNonWordCharOnTheLeft;
            
            while (wordEnd <= indexOfNonWordCharOnTheRight) {
                if (wordEnd == indexOfNonWordCharOnTheRight ||
                    String.valueOf(text.charAt(wordEnd)).matches("\\W")) {
                    String word = text.substring(wordStart, wordEnd);
                    if (word.matches(REGEX_FOR_KEYWORDS)) {
                        setCharacterAttributes(wordStart,
                                               wordEnd - wordStart,
                                               ATTR_KEYWORDS,
                                               false);
                    }
                    else {
                        setCharacterAttributes(wordStart,
                                               wordEnd - wordStart,
                                               ATTR_PLAIN_TEXT,
                                               false);
                    }
                    wordStart = wordEnd;
                }
                wordEnd++;
            }
        }
    }
    
    @Override
    public void remove(int offset, int len)
           throws BadLocationException {
        super.remove(offset, len);
        
        String text = getText(0, getLength());
        
        boolean removedTextIsInComments = setStyle(text, offset);
        if (!removedTextIsInComments) {
            // The last non-word character on the left (i.e., in text before
            // offset)
            int wordStart = findLastNonWordChar(text, offset);
            if (wordStart < 0) {
                wordStart = 0;
            }
            
            // The first non-word character on the right (i.e., in text after
            // offset)
            int wordEnd = findFirstNonWordChar(text, offset);
            
            String word = text.substring(wordStart,
                                         wordEnd);
            if (word.matches(REGEX_FOR_KEYWORDS)) {
                setCharacterAttributes(wordStart,
                                       wordEnd - wordStart,
                                       ATTR_KEYWORDS,
                                       false);
            }
            else {
                setCharacterAttributes(wordStart,
                                       wordEnd - wordStart,
                                       ATTR_PLAIN_TEXT,
                                       false);
            }
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Applies the style to the comments in the source code (i.e., green color
     * for single-line and multiline comments and blue for javadoc comments) and
     * the Java reserved  keywords and detects if the starting offset for the
     * current change (i.e., insertion or deletion) is in a comment region or
     * a double quote.
     * 
     * @param text The source code text
     * @param offset The starting offset for the current change (i.e., insertion
     *               or deletion)
     *               
     * @return {@code true} if the starting offset for the current change (i.e.,
     *         insertion or deletion) is in a comment region or a double quote
     *         or {@code false} otherwise
     */
    private boolean setStyle(String text,
                             int    offset) {
        List<Integer[]> excludeRanges = new ArrayList<Integer[]>();
        
        // By default the text color is black. Later we will overwrite the
        // sections that are not black (i.e., comments and reserved keywords)
        setCharacterAttributes(0, 
                               getLength(),
                               ATTR_PLAIN_TEXT,
                               false);
        
        if (!colorText) {
            return false; // Don't care; we use black color everywhere
        }
        
        // 1. RESERVED KEYWORDS
        // Match entire word (i.e., "\\bint\\b" in "int a, integer a, print"
        // matches only the first)
        Pattern keywordPattern =
            Pattern.compile("\\b" + String.join("\\b|", JAVA_KEYWORDS) + "\\b");
        Matcher matcher = keywordPattern.matcher(text);
        while (matcher.find()) {
            setCharacterAttributes(matcher.start(), 
                                   matcher.end() - matcher.start(),
                                   ATTR_KEYWORDS,
                                   false);
        }
        
        // 2. DOUBLE QUOTE
        Pattern doubleQuotePattern = Pattern.compile("\".*\"");
        matcher = doubleQuotePattern.matcher(text);
        while (matcher.find()) {
            int commentStart = matcher.start();
            int commentEnd   = matcher.end();
            excludeRanges.add(new Integer[] {commentStart, commentEnd});
            setCharacterAttributes(commentStart, 
                                   commentEnd - commentStart,
                                   ATTR_DOUBLE_QUOTE,
                                   true);
        }
        
        // 3. COMMENTS
        // a) Single line comments
        Pattern singleLineCommentsPattern = Pattern.compile("\\/\\/.*");
        matcher = singleLineCommentsPattern.matcher(text);
        while (matcher.find()) {
            int commentStart = matcher.start();
            int commentEnd   = matcher.end();
            excludeRanges.add(new Integer[] {commentStart, commentEnd});
            setCharacterAttributes(commentStart, 
                                   commentEnd - commentStart,
                                   ATTR_COMMENT,
                                   true);
        }
        
        // b) Multiline comments
        Pattern multipleLineCommentsPattern =
                Pattern.compile("\\/\\*.*?\\*\\/", Pattern.DOTALL);
        matcher = multipleLineCommentsPattern.matcher(text);
        while (matcher.find()) {
            int commentStart = matcher.start();
            int commentEnd   = matcher.end();
            excludeRanges.add(new Integer[] {commentStart, commentEnd});
            setCharacterAttributes(commentStart, 
                                   commentEnd - commentStart,
                                   ATTR_COMMENT,
                                   true);
        }
        
        // c) Javadoc comments
        Pattern javadocCommentsPattern =
                Pattern.compile("\\/\\*\\*.*?\\*\\/", Pattern.DOTALL);
        matcher = javadocCommentsPattern.matcher(text);
        while (matcher.find()) {
            int commentStart = matcher.start();
            int commentEnd   = matcher.end();
            excludeRanges.add(new Integer[] {commentStart, commentEnd});
            setCharacterAttributes(commentStart, 
                                   commentEnd - commentStart,
                                   ATTR_JAVADOC,
                                   true);
        }
        
        // Check if offset is in multiline range
        boolean modifiedTextIsInCommentOrDoubleQuote = false;
        for (Integer[] range : excludeRanges) {
            if (offset >= range[0] && offset <= range[1]) {
                modifiedTextIsInCommentOrDoubleQuote = true;
                break;
            }
        }
        
        return modifiedTextIsInCommentOrDoubleQuote;
    }
    
    /**
     * Given a text and an index in that text it searches forward to find a
     * non-word character and return its index.
     * 
     * @param text The text to process
     * @param index The index to start searching forward
     * 
     * @return the index of the non-word character or -1 if the text contains
     *         just word characters
     */
    private int findFirstNonWordChar (String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }
    
    /**
     * Given a text and an index in that text it searches backward to find a
     * non-word character and return its index.
     * 
     * @param text The text to process
     * @param index The index to start searching backward
     * 
     * @return the index of the non-word character or -1 if the text contains
     *         just word characters
     */
    private int findLastNonWordChar (String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }
}
