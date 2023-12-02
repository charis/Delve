/*
 * File          : ConstructorVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 26 June 2018
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.ast.visitor;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes 
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
// Import custom classes 
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.body.Constructor;

/**
 * Defines the behavior when a constructor in the code is visited.
 */
public class ConstructorVisitor<T> extends VoidVisitorAdapter<T>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The constructors that are visited */
    private final List<Constructor> constructors;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Default constructor. 
     */
    public ConstructorVisitor() {
        super();
        
        constructors = new ArrayList<Constructor>();
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Called when a constructor is visited.
     * 
     * @param constructorDeclaration The module that represents a constructor
     *                               declaration in the source code<br>
     *                               <pre>
     *                               Examples:
     *                               <code>
     *                               1) /**
     *                                   * Creates a new BookCollection
     *                                   *
     *                                   * @param titles The titles of the books
     *                                   *               in the collection
     *                                   * /
     *                                 {@literal @}SuppressWarnings("rawtypes")
     *                                  public BookCollection(List titles) {
     *                                      this.titles = titles;
     *                                  }
     *                               
     *                               2) public BookCollection(
     *                                         List{@literal <String>} titles) {
     *                                      this.titles = titles;
     *                                  }
     *                               </code>
     *                               </pre>
     * @param paramType The parameter type
     * 
     * @throws RuntimeException in case of error processing the constructor
     *                          declaration
     */
    @Override
    public void visit(final ConstructorDeclaration constructorDeclaration,
                      final T                      paramType)
           throws RuntimeException {
        super.visit(constructorDeclaration, paramType);
        
        try {
            constructors.add(new Constructor(constructorDeclaration));
        }
        catch (ErrorException ee) {
            throw new RuntimeException(ee.getMessage());
        }
    }
    
    /**
     * @return a list with the constructors that are declared in the Java file
     */
    public List<Constructor> getConstructors() {
        return constructors;
    }
}
