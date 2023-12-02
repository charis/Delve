/*
 * File          : MethodVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 24 June 2018
 * Last Modified : 15 August 2018
 */
package edu.stanford.javaparser.ast.visitor;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
// Import custom classes 
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.body.Method;

/**
 * Defines the behavior when a method in the code is visited.
 */
public class MethodVisitor<T> extends VoidVisitorAdapter<T>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The methods that are visited */
    private final List<Method> methods;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Default constructor. 
     */
    public MethodVisitor() {
        super();
        
        methods = new ArrayList<Method>();
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Called when a method is visited.
     * 
     * @param methodDeclaration The module that represents a method declaration
     *                          in the source code<br>
     *                          <pre>
     *                          Examples:
     *                          <code>
     *                          1) /**
     *                              * Sets the title of the story
     *                              *
     *                              * @param title - The title of the story
     *                              * /
     *                            {@literal @}Override
     *                             public void setTitle(String title) {
     *                                 this.title = title.toUpperCase();
     *                             }
     *                          
     *                          2) public void setTitle(String title) {
     *                                 this.title = title;
     *                             }
     *                          </code>
     *                          </pre>
     * @param paramType The parameter type
     * 
     * @throws RuntimeException in case of error processing the constructor
     *                          declaration
     */
    @Override
    public void visit(final MethodDeclaration methodDeclaration,
                      final T paramType)
           throws RuntimeException {
        super.visit(methodDeclaration, paramType);
        
        try {
            methods.add(new Method(methodDeclaration));
        }
        catch (ErrorException ee) {
            throw new RuntimeException(ee.getMessage());
        }
    }
    
    /**
     * @return a list with the methods that are declared in the Java file
     */
    public List<Method> getMethods() {
        return methods;
    }
}
