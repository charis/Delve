/*
 * File          : Variable.java
 * Author        : Charis Charitsis
 * Creation Date : 14 April 2014
 * Last Modified : 2 August 2018
 */
package edu.stanford.javaparser.body;

// Import Java SE classes
import java.util.EnumSet;
// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.type.VariableType;

/**
 * Represents an instance or class variable or a parameter (in a
 * method/constructor) or constant in the source code.
 */
public class Variable
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The variable type in the declaration */
    private final VariableType      varType;
    /** The variable name in the declaration */
    private final String            varName;
    /**
     * The line number in the source code where the variable declaration begins
     */
    private final int               beginLine;
    /**
     * The line number in the source code where the variable declaration ends
     */
    private final int               endLine;
    /** The variable modifiers */
    private final EnumSet<Modifier> modifiers;
    /**
     * The comment that is associated with the variable or {@code null} in case
     * there is no comment
     */
    private final Comment           comment;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Variable.
     * 
     * @param parameter The module with the information about the parameter that
     *                  is declared in a constructor or method
     * 
     * @throws ErrorException in case of an error processing the type to
     *                        determine if it is numeric, text, boolean or
     *                        none of those
     */
    public Variable(Parameter parameter)
           throws ErrorException {
        varType   = new VariableType(parameter.getType());
        varName   = parameter.getNameAsString();
        modifiers = parameter.getModifiers();
        
        com.github.javaparser.ast.comments.Comment comment =
                                           parameter.getComment().orElse(null);
        this.comment = (comment != null)? new Comment(comment): null;
        
        Position beginPos = parameter.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + varName
                                   + "' cannot be determined");
        }
        Position endPos = parameter.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + varName
                                   + "' cannot be determined");
        }
        
        beginLine   = beginPos.line;
        endLine     = endPos.line;
    }
    
    /**
     * Constructs a new Variable.
     * 
     * @param varDeclarator The module with the information about the variable
     *                      that is declared
     * @param modifiers The variable modifiers
     *  
     * @throws ErrorException in case of an error processing the type to
     *                        determine if it is numeric, text, boolean or
     *                        none of those
     */
    public Variable(VariableDeclarator varDeclarator,
                    EnumSet<Modifier>  modifiers)
           throws ErrorException {
        varType = new VariableType(varDeclarator.getType());
        varName = varDeclarator.getNameAsString();
        
        com.github.javaparser.ast.comments.Comment comment =
                                  varDeclarator.getComment().orElse(null);
        this.comment = (comment != null)? new Comment(comment): null;
        
        Position beginPos = varDeclarator.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + varName
                                   + "' cannot be determined");
        }
        Position endPos = varDeclarator.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + varName
                                   + "' cannot be determined");
        }
        
        beginLine      = beginPos.line;
        endLine        = endPos.line;
        this.modifiers = modifiers;
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return the variable type
     */
    public VariableType getType() {
        return varType;
    }
    
    /**
     * @return the variable name
     */
    public String getName() {
        return varName;
    }
    
    /**
     * @return the line number in the source code where the variable declaration
     *         begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the variable declaration
     *         ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return the variable modifiers
     */
    public EnumSet<Modifier> getModifiers() {
        return modifiers;
    }
    
    /**
     * @return {@code true} if this variable is also an argument in a method
     *         or constructor or {@code false} otherwise
     */
    public boolean isArgument() {
        return false;
    }
    
    /**
     * @return the comment that is associated with the variable or {@code null}
     *         in case there is no comment
     */
    public Comment getComment() {
        return comment;
    }
}
