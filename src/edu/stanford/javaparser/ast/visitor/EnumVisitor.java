/*
 * File          : EnumVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 26 June 2018
 * Last Modified : 28 February 2021
 */
package edu.stanford.javaparser.ast.visitor;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import custom classes 
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Defines the behavior when an enum in the code is visited.
 */
public class EnumVisitor<T> extends VoidVisitorAdapter<T>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The enums that are visited */
    private final List<EnumDeclaration> enums;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Default constructor. 
     */
    public EnumVisitor() {
        super();
        
        enums = new ArrayList<EnumDeclaration>();
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Called when an enum is visited.
     * 
     * @param enumeration The module that represents an enum in the source code
     *                    <br>
     *                    <pre>
     *                    Example:
     *                    <code>
     *                      /** Enumeration of the available choices * /
     *                     {@literal @}Deprecated
     *                      public static enum Choice implements Selectable {
     *                          FIRST  ("A"),
     *                          SECOND ("B"),
     *                          THIRD  ("C");
     *                          
     *                          private final String value;
     *                          
     *                          private Choice(String value) {
     *                              this.value = value;
     *                          }
     *                          
     *                         {@literal @}Override
     *                          public boolean isSelected(String choice) {
     *                              return value.equals(choice);
     *                          }
     *                      }
     *                      </code>
     *                      
     *                      where the interface 'Selectable' is declared as
     *                      follows:
     *                      <code>
     *                          public interface Selectable {
     *                              public boolean isSelected(String choice);
     *                          }
     *                      </code>
     *                      </pre>
     * @param paramType The parameter type
     */
    @Override
    public void visit(final EnumDeclaration enumeration, final T paramType) {
        super.visit(enumeration, paramType);
        
        enums.add(enumeration);
    }
    
    /**
     * @return a list with the variables (both class and instance) as well as
     *         the constant declarations in the Java file that are declared in
     *         the Java file
     */
    public List<EnumDeclaration> getEnums() {
        return enums;
    }
}
