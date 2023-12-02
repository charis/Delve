/*
 * File          : ClassAndInterfaceVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 25 June 2018
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.ast.visitor;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes 
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
// Import custom classes 

/**
 * Defines the behavior when a class or interface in the code is visited.
 */
public class ClassAndInterfaceVisitor<T> extends VoidVisitorAdapter<T>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The classes and interfaces that are visited */
    private final List<ClassOrInterfaceDeclaration> classesAndInterfaces;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Default constructor. 
     */
    public ClassAndInterfaceVisitor() {
        super();
        
        classesAndInterfaces = new ArrayList<ClassOrInterfaceDeclaration>();
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Called when a class or interface is visited.
     * 
     * @param classOrInterface The module that represents a method in the source code
     *                         <pre>
     *                         Examples:
     *                         <code>
     *                         1) Class
     *                            /**
     *                             * Represents a book
     *                             * /
     *                             protected class Book {
     *                                 final String name;
     *                                 final int    pages;
     *                                 
     *                                 public Book(String name, int pages) {
     *                                     this.name  = name;
     *                                     this.pages = pages;
     *                                 }
     *                             }
     *                              
     *                         2) Interface
     *                            /**
     *                             * Selectable interface
     *                             * /
     *                            {@literal @}SuppressWarnings("rawtypes")
     *                             public interface Selectable {
     *                                 public boolean isSelected(String choice);
     *                             }
     *                         </code>
     *                         </pre>
     * @param paramType The parameter type
     */
    @Override
    public void visit(final ClassOrInterfaceDeclaration classOrInterface,
                      final T paramType) {
        super.visit(classOrInterface, paramType);
        
        classesAndInterfaces.add(classOrInterface);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * @return a list with the classes that are declared in the Java file
     */
    public List<ClassOrInterfaceDeclaration> getClasses() {
        int maxSize = classesAndInterfaces.size();
        List<ClassOrInterfaceDeclaration> classes =
                    new ArrayList<ClassOrInterfaceDeclaration>(maxSize);
        for (ClassOrInterfaceDeclaration classOrInterface :
                                                classesAndInterfaces) {
            if (!classOrInterface.isInterface()) {
              classes.add(classOrInterface);
            }
        }
        return classes;
    }
    
    /**
     * @return a list with the interfaces that are declared in the Java file
     */
    public List<ClassOrInterfaceDeclaration> getInterfaces() {
        int maxSize = classesAndInterfaces.size();
        List<ClassOrInterfaceDeclaration> interfaces =
                    new ArrayList<ClassOrInterfaceDeclaration>(maxSize);
        for (ClassOrInterfaceDeclaration classOrInterface :
                                                classesAndInterfaces) {
            if (classOrInterface.isInterface()) {
              interfaces.add(classOrInterface);
            }
        }
        return interfaces;
    }
}
