/*
 * File          : JavaFileComments.java
 * Author        : Charis Charitsis
 * Creation Date : 13 November 2020
 * Last Modified : 13 November 2020
 */
package edu.stanford.javaparser;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import custom classes
import edu.stanford.javaparser.body.Comment;

/**
 * Holds information about the comments in a Java file
 */
public class JavaFileComments
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * List with the Javadoc comments in the Java file.<br>
     * Javadoc comment is a comment between '/** .. * /'.
     */
    private final List<Comment> javadocComments;
    /**
     * List with the block comments in the Java file except commented out code
     * (i.e., comments that include ';').<br>
     * Block comment is a comment between '/* .. * /'.
     */
    private final List<Comment> blockComments;
    /**
     * List with the line comments in the Java file except commented out code
     * (i.e., comments that include ';')<br>
     * Line comment is a comment that starts with '//'.
     */
    private final List<Comment> lineComments;
    /**
     * List with the line comments in the Java file that disable/comment out
     * code (i.e., comments that include ';')<br>
     */
    private final List<Comment> disableCodeComments;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new CommentInfo instance with info about the comments in the
     * Java code.
     * 
     * @param javaFile The file with the Java code
     */
    public JavaFileComments(JavaFile javaFile) {
        if (javaFile  == null) {
            throw new IllegalArgumentException("Argument 'javaFile' is null");
        }
        
        javadocComments     = new ArrayList<Comment>();
        blockComments       = new ArrayList<Comment>();
        lineComments        = new ArrayList<Comment>();
        disableCodeComments = new ArrayList<Comment>();
        
        clasifyComments(javaFile.getComments());
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Returns a list with the   the Javadoc comments in the Java file.<br>
     * Javadoc comment is a comment between '/** .. * /'.
     * 
     * @return a list with the Javadoc comments in the Java file or an empty
     *         list if there are no Javadoc comments
     */
    public List<Comment> getJavadocComments() {
        return javadocComments;
    }
    
    /**
     * Returns a list with the block comments in the Java file except commented
     * out code (i.e., comments that include ';').<br>
     * Block comment is a comment between '/* .. * /'.
     * 
     * @return a list with the block comments in the Java file except commented
     *         out code (i.e., comments that include ';') or an empty list if
     *         there are no block comments
     */
    public List<Comment> getBlockComments() {
        return blockComments;
    }
    
    /**
     * Returns a list with the line comments in the Java file except commented
     * out code (i.e., comments that include ';')<br>
     * Line comment is a comment that starts with '//'.
     * 
     * @return a list with the line comments in the Java file except commented
     *         out code (i.e., comments that include ';') or an empty list if
     *         there are no line comments
     */
    public List<Comment> getLineComments() {
        return lineComments;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Classifies the provided comments to Javadoc, block and line comments.
     *  
     * @param comments The comments to classify
     */
    private void clasifyComments(List<Comment> comments) {
        for (Comment comment : comments) {
            if (comment.isJavadocComment()) {
                javadocComments.add(comment);
            }
            else if (comment.getText().contains(";")) {
                disableCodeComments.add(comment);
            }
            else if (comment.isBlockComment()) {
                blockComments.add(comment);
            }
            else if (comment.isLineComment()) {
                lineComments.add(comment);
            }
        }
    }
}
