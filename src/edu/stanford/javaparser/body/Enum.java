/*
 * File          : Enum.java
 * Author        : Charis Charitsis
 * Creation Date : 14 April 2014
 * Last Modified : 30 July 2018
 */
package edu.stanford.javaparser.body;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Represents an enum in the source code
 */
public class Enum
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** Enum name */ 
    private final String       name;
    /** The line number in the source code where the enum declaration begins */
    private final int          beginLine;
    /** The line number in the source code where the enum declaration ends */
    private final int          endLine;
    /** List with the enum members (their names only) */
    private final List<String> enumMembers;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Enum.
     * 
     * @param enumDeclaration The instance that represents an enum declaration
     *                        in the source code
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     */
    public Enum(EnumDeclaration enumDeclaration)
           throws ErrorException{
        name        = enumDeclaration.getNameAsString();
        
        Position beginPos = enumDeclaration.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + name
                                   + "' cannot be determined");
        }
        Position endPos = enumDeclaration.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + name
                                   + "' cannot be determined");
        }
        
        beginLine   = beginPos.line;
        endLine     = endPos.line;
        enumMembers = new ArrayList<String>();
        
        List<EnumConstantDeclaration> entries = enumDeclaration.getEntries();
        if (entries != null) { // Should be always non-null, but just in case...
            for (EnumConstantDeclaration entry : entries) {
                enumMembers.add(entry.getNameAsString());
            }
        }
    }
     
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return the enum name
     */ 
    public String getName() {
        return name;
    }
    
    /**
     * @return the line number in the source code where the enum declaration
     *         begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the enum declaration
     *         ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return a list with the enum members (their names only)
     */
    public List<String> getEnumMembers() {
        return enumMembers;
    }
}
