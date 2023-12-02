/*
 * File          : Comment.java
 * Author        : Charis Charitsis
 * Creation Date : 1 April 2014
 * Last Modified : 21 July 2019
 */
package edu.stanford.javaparser.body;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.Position;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.constants.Literals.SPACE_CHAR;
import static edu.stanford.constants.Literals.STAR;

/**
 * Represents a comment in the source code
 */
public class Comment
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /**
     * Enumeration with all possible comment types
     */
    public enum CommentType {
          /** Javadoc comment */
          JAVADOC,
          /** Block comment */
          BLOCK_COMMENT,
          /** Line comment */
          LINE_COMMENT;
    }
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The non-empty lines in the comment */ 
    private final String[]    textLines;
    /** The line number in the source code where the comment begins */
    private final int         beginLine;
    /** The line number in the source code where the comment ends */
    private final int         endLine;
    /** The comment type */
    private final CommentType commentType;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Comment.
     * 
     * @param commentInfo The module with the comment information
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The {@code commentInfo} is {@code null}<br>
     *                        2) The begin or end line cannot be determined
     */    
    public Comment(com.github.javaparser.ast.comments.Comment commentInfo)
           throws ErrorException {
        assertNotNull(commentInfo, "commentInfo");
        
        Position beginPos = commentInfo.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of the comment '"
                                   + commentInfo.getContent()
                                   + "' cannot be determined");
        }
        Position endPos = commentInfo.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of the comment '"
                                   + commentInfo.getContent()
                                   + "' cannot be determined");
        }
        
        beginLine = beginPos.line;
        endLine   = endPos.line;
        textLines = commentInfo.getContent().split(NEW_LINE);
        
        if (commentInfo.isJavadocComment()) {
            commentType = CommentType.JAVADOC;
        }
        else if (commentInfo.isLineComment()) {
            commentType = CommentType.LINE_COMMENT;
        }
        else {
            commentType = CommentType.BLOCK_COMMENT;
        }
    }
    
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return a String representation of the comment so that it looks nice
     *         based on the type of comment (Javadoc, block or line comment).
     */
    @Override
    public String toString() {
        if (commentType == CommentType.LINE_COMMENT) {
            String text = textLines[0].trim();
            if (text.startsWith("//")) {
                if (text.length() > 2 && text.charAt(2) != SPACE_CHAR) {
                    return SPACE + text;
                }
                else {
                    return text;
                }
            }
            else {
                return "//" + SPACE + text;
            }
        }
        
        int numOfLines = textLines.length;
        if (numOfLines == 1) {
            String text = textLines[0].trim();
            if (commentType == CommentType.JAVADOC) { // Javadoc comment
                return "/** " + text + " */";  
            }
            else { // Block comment
                return "/* " + text + " */";  
            }
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numOfLines; i++) {
            String textLine = textLines[i].trim();
            if (i == 0) {
                if (commentType == CommentType.JAVADOC) { // Javadoc comment
                    result.append("/**" + NEW_LINE);
                }
                else { // Block comment
                    result.append("/*" + NEW_LINE);
                }
                
                if (textLine.isEmpty() || textLine.equals(STAR)) {
                    continue;
                }
            }
            
            if (i == numOfLines - 1) {
                if (textLine.isEmpty() || textLine.equals(STAR)) {
                    result.append(SPACE + "*/");
                    break;
                }
            }
            
            if (textLine.startsWith(STAR)) {
                result.append(SPACE + textLine + NEW_LINE);
            }
            else {
                result.append(SPACE + STAR + SPACE + textLine + NEW_LINE);
            }
            
            if (i == numOfLines - 1) {
                result.append(SPACE + "*/");
            }
        }
        
        return result.toString();
    }
    
    /**
     * @return the line number in the source code where the comment begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the comment ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return {@code true} in case of a Javadoc comment or {@code false}
     *         otherwise
     */
    public boolean isJavadocComment() {
        return commentType == CommentType.JAVADOC;
    }
    
    /**
     * @return {@code true} in case of a line comment or {@code false} otherwise
     */
    public boolean isLineComment() {
        return commentType == CommentType.LINE_COMMENT;
    }
    
    /**
     * @return {@code true} in case of a block comment or {@code false}
     *         otherwise
     */
    public boolean isBlockComment() {
        return commentType == CommentType.BLOCK_COMMENT;
    }
    
    /**
     * Returns the comment text as a single String.<br>
     * If the comment is Javadoc it removes the '*' from the beginning of each
     * comment line (if present).<br>
     * Note 1: Every comment line gets trimmed.<br>
     * Note 2: Empty comment lines are skipped.
     * 
     * @return text of the comment
     */
    public String getText() {
        List<String> textLines = getTextLines(true, true);
        int numOfLines = textLines.size();
        int lineNumber = 1;
        StringBuilder text = new StringBuilder();
        for (String line : textLines) {
            text.append(line);
            if (lineNumber != numOfLines) {
                text.append(NEW_LINE);
            }
            lineNumber++;
        }
        
        return text.toString();
    }
    
    /**
     * Returns the comment text as a list of lines where each line is trimmed.
     * <br>
     * If the flag {@code stripJavadoc} is set then it removes the '*' from the
     * beginning of each comment line (if a Javadoc comment).<br>
     * 
     * @param skipEmptyLines {@code true} to skip empty comment lines or
     *                       {@code false} otherwise 
     * @param stripJavadoc {@code true} to remove the '*' from the beginning of
     *                     each comment line, if this is Javadoc comment). For
     *                     a non-Javadoc comment this is a don't care.
     * 
     * @return the comment text as a list of lines
     */
    public List<String> getTextLines(boolean skipEmptyLines,
                                     boolean stripJavadoc) {
        List<String> processedLines = new ArrayList<String>(textLines.length);
        
        for (int i = 0; i < textLines.length; i++) {
            String line = textLines[i].trim();
            if (stripJavadoc && isJavadocComment() && line.startsWith("*")) {
                line = line.substring(1).trim();
            }
            if (!line.isEmpty() || !skipEmptyLines) {
                processedLines.add(line);
            }
        }
        
        return processedLines;
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Asserts that an object is not {@code null}.
     * 
     * @param obj The object to make sure it is not {@code null}
     * @param objName The name of the argument (used in the exception message in
     *                case the argument is {@code null})
     * 
     * @throws ErrorException in case of a {@code null} or empty argument value
     */
    private void assertNotNull(Object obj,
                               String objName)
            throws ErrorException {
        if (obj == null) {
            throw new ErrorException(objName + " is null");
        }
    }
}
