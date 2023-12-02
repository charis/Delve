/*
 * File          : Field.java
 * Author        : Charis Charitsis
 * Creation Date : 15 April 2014
 * Last Modified : 29 September 2018
 */
package edu.stanford.javaparser.body;

// Import Java SE classes
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Represents an instance or class variable or constant declaration in the
 * source code
 */
public class Field
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The line number in the source code where the declaration of the variable
     * or constant begins
     */
    private final int            beginLine;
    /**
     * The line number in the source code where the declaration of the variable
     * or constant ends
     */
    private final int            endLine;
    /**
     * List with the class variables in this declaration.
     * For example the field declaration 'static int instanceCount;' involves
     * a class variables 'instanceCount'
     */
    private final List<Variable> classVariables;
    /**
     * List with the instance variables in this declaration.<br>
     * For example the field declaration 'int i, j;' involves two instance
     * variables 'i' and 'j'
     */
    private final List<Variable> instanceVariables;
    /**
     * List with the final variables in this declaration.<br>
     * For example the field declaration 'final double PI = 3.14;' involves
     * a final variable 'PI'.
     */
    private final List<Variable> finalVariables;
    /**
     * The comment that is associated with this declaration or {@code null} if
     * there is no comment
     */
    private final Comment        comment;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Field.
     * 
     * @param fieldDeclaration The instance that represents a field (i.e.,
     *                         instance or class variable) declaration in the
     *                         source code
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     */
    public Field(FieldDeclaration fieldDeclaration)
           throws ErrorException {
        Position beginPos = fieldDeclaration.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of the field "
                                   + "declaration'" + fieldDeclaration
                                   + "' cannot be determined");
        }
        Position endPos = fieldDeclaration.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of the field "
                                   + "declaration'" + fieldDeclaration
                                   + "' cannot be determined");
        }
        beginLine   = beginPos.line;
        endLine     = endPos.line;
        com.github.javaparser.ast.comments.Comment commentInfo =
                              fieldDeclaration.getComment().orElse(null);
        if (commentInfo != null) {
            comment = new Comment(commentInfo);
        }
        else {
        	comment = null;
        }
        
        List<VariableDeclarator> variableDeclarators =
                                         fieldDeclaration.getVariables();
        EnumSet<Modifier> fieldModifiers = fieldDeclaration.getModifiers();
        int maxSize = variableDeclarators.size();
        classVariables    = new ArrayList<Variable>(maxSize);
        instanceVariables = new ArrayList<Variable>(maxSize);
        finalVariables    = new ArrayList<Variable>(maxSize);
        
        Variable variable;
        if (fieldModifiers.contains(Modifier.FINAL)) {
            for (VariableDeclarator variableDeclarator : variableDeclarators) {
                variable = new Variable(variableDeclarator, fieldModifiers);
                finalVariables.add(variable);
            }
        }
        else if (fieldModifiers.contains(Modifier.STATIC)) {
            for (VariableDeclarator variableDeclarator : variableDeclarators) {
                variable = new Variable(variableDeclarator, fieldModifiers);
                classVariables.add(variable);
            }
        }
        else {
            for (VariableDeclarator variableDeclarator : variableDeclarators) {
                variable = new Variable(variableDeclarator, fieldModifiers);
                instanceVariables.add(variable);
            }
        }
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return the line number in the source code where the declaration of the
     *         variable or constant declaration begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the declaration of the
     *         variable or constant declaration ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * Returns a list with the class variables in the field declaration.<br>
     * For example the field declaration 'static int instanceCount;' involves
     * the class variable 'instanceCount'.<br>
     * If there are no class variables, it returns an empty list.
     * 
     * @return a list with the class variables in the field declaration which
     *         can be empty if there are no class variables
     */
    public List<Variable> getClassVariables() {
        return classVariables;
    }
    
    /**
     * Returns a list with the instance variables in the field declaration.<br>
     * For example the field declaration 'int i, j;' involves two instance
     * variables 'i' and 'j'.<br>
     * If there are no instance variables, it returns an empty list.
     * 
     * @return a list with the instance variables in the field declaration which
     *         can be empty if there are no instance variables
     */
    public List<Variable> getInstanceVariables() {
        return instanceVariables;
    }
    
    /**
     * Returns a list with the final variables in the field declaration.<br>
     * For example the field declaration 'final double PI = 3.14;' involves
     * a final variable 'PI'. <br>
     * If there are no final variables, it returns an empty list.
     * 
     * @return a list with the final variables in the field declaration which
     *         can be empty if there are no final variables
     */
    public List<Variable> getFinalVariables() {
        return finalVariables;
    }
    
    /**
     * @return the comment that is associated with the field declaration or
     *         {@code null} if there is no comment
     */
    public Comment getComment() {
        return comment;
    }
}
