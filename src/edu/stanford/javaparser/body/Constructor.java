/*
 * File          : Constructor.java
 * Author        : Charis Charitsis
 * Creation Date : 13 April 2014
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.body;

// Import com.github.javaparser classes
import com.github.javaparser.ast.body.ConstructorDeclaration;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Represents a constructor in the source code.<br>
 * 
 * <pre>
 * Examples:
 * <code>
 *          1) /**
 *              * Creates a new BookCollection
 *              *
 *              * @param titles - The titles of the books in the collection
 *              * /
 *            {@literal @}SuppressWarnings("rawtypes")
 *             public BookCollection(List titles) {
 *                 this.titles = titles;
 *             }
 *          
 *          2) public BookCollection(List{@literal <String>}titles) {
 *                 this.titles = titles;
 *             }
 * </code>
 * </pre>
 */
public class Constructor extends Method
{
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Constructor.
     * 
     * @param constructorDeclaration The module with the constructor information
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     */
    public Constructor(ConstructorDeclaration constructorDeclaration)
           throws ErrorException {
        super(constructorDeclaration,
              null,
              constructorDeclaration.getBody(),
              constructorDeclaration.getJavadocComment().orElse(null),
              constructorDeclaration.getDeclarationAsString());
    }
    
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return {@code null}
     */
    @Override
    public String getReturnType() {
        return null;
    }
}
