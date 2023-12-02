/*
 * File          : ClassOrInterface.java
 * Author        : Charis Charitsis
 * Creation Date : 18 April 2014
 * Last Modified : 17 December 2020
 */
package edu.stanford.javaparser.body;

import java.util.ArrayList;
import java.util.List;

// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Represents a class or an interface in the source code
 */
public class ClassOrInterface
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** Class or interface name */ 
    private final String       name;
    /**
     * The line number in the source code where the class or interface begins
     */
    private final int          beginLine;
    /**
     * The line number in the source code where the class or interface ends
     */
    private final int          endLine;
    /**
     * Flag that indicates whether this is an interface
     */
    private final boolean      isInterface;
    /**
     * List with the superclass name or the superinterface names that this class
     * or interface extends or an empty list if it does not extend
     */
    private final List<String> extendedModules;
    /**
     * List with the interface names that this class implements or an empty list
     * if it does not implement an interface of if this is an interface (and not
     * a class) given that interfaces cannot implement other interfaces
     */
    private final List<String> implementedInterfaces;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new ClassOrInterface.
     * 
     * @param classOrInterface Module that represents a class or interface
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     */
    public ClassOrInterface(ClassOrInterfaceDeclaration classOrInterface)
           throws ErrorException {
        name        = classOrInterface.getNameAsString();
        
        Position beginPos = classOrInterface.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + name
                                   + "' cannot be determined");
        }
        Position endPos = classOrInterface.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + name
                                   + "' cannot be determined");
        }
        
        beginLine   = beginPos.line;
        endLine     = endPos.line;
        isInterface = classOrInterface.isInterface();
        
        extendedModules = new ArrayList<String>();
        for (ClassOrInterfaceType module :classOrInterface.getExtendedTypes()) {
            extendedModules.add(module.asString());
        }
        
        implementedInterfaces = new ArrayList<String>();
        for (ClassOrInterfaceType implementedInterface :
                                  classOrInterface.getImplementedTypes()) {
            implementedInterfaces.add(implementedInterface.asString());
        }
    }
     
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return the class or interface name
     */ 
    public String getName() {
        return name;
    }
    
    /**
     * @return the line number in the source code where the class or interface
     *         begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the class or interface
     *         ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return true in case this is an interface or false in case this is a
     *         class
     */
    public boolean isInterface() {
        return isInterface;
    }
    
    /**
     * @return a list with the superclass name or the superinterface names that
     *         this class or interface extends or an empty list if it does not
     *         extend
     */
    public List<String> getExtendedModules() {
        return extendedModules;
    }
    
    /**
     * @return a list with the interface names that this class implements or an
     *         empty list if it does not implement an interface of if this is
     *         an interface (and not a class) given that interfaces cannot
     *         implement other interfaces
     */
    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }
}
