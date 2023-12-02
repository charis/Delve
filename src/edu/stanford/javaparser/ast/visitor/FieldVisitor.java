/*
 * File          : FieldVisitor.java
 * Author        : Charis Charitsis
 * Creation Date : 26 June 2018
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.ast.visitor;

// Import Java SE classes
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
// Import custom classes 
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.body.Field;

/**
 * Defines the behavior when a field (i.e., instance or class variable or
 * constant) in the code is visited.
 */
public class FieldVisitor<T> extends VoidVisitorAdapter<T>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The instance or class variables or constants that are visited */
    private final List<Field> fields;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Default constructor. 
     */
    public FieldVisitor() {
        super();
        
        fields = new ArrayList<Field>();
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Called when an instance or class variables or constant is visited.
     * 
     * @param fieldDeclaration The module that represents a field (i.e.,
     *                         instance or class variable) declaration in the
     *                         source code.<br>
     *                         It consists of the variable name and the
     *                         expression (if any) that follows the name at the
     *                         point where the variable is declared. It may also
     *                         include a Javadoc and a list of annotations (e.g.
     *                         {@literal @}SuppressWarnings("unused")).<br>
     *                         <pre>
     *                         Examples:
     *                         1) Static constant with Javadoc comment
     *                             /** Number of days in a week * /
     *                             public static final int DAYS_IN_WEEK = 7;
     *                         
     *                         2) Instance variable declaration 
     *                             protected int val;
     *                         
     *                         3) Instance variable declaration + instantiation
     *                            + initialization
     *                             String[] names = new String[] { "John",
     *                                                             "Nick" };
     *                         
     *                         4) Instance variable declaration + instantiation
     *                            + initialization (note the difference between
     *                            3 {@literal &} 4)
     *                             String names[] = new String[] { "John",
     *                                                             "Nick" };
     *                         
     *                         5) Unused instance variable
     *                            {@literal @}SuppressWarnings("unused")
     *                             private List{@literal <Integer>} populations;
     *                         </pre>
     * @param paramType The parameter type
     * 
     * @throws RuntimeException in case of error processing the field
     *                          declaration
     */
    @Override
    public void visit(final FieldDeclaration fieldDeclaration, final T paramType) {
        super.visit(fieldDeclaration, paramType);
        
        try {
            fields.add(new Field(fieldDeclaration));
        }
        catch (ErrorException ee) {
            throw new RuntimeException(ee.getMessage());
        }
    }
    
    /**
     * @return a list with the variables (both class and instance) as well as
     *         the constant declarations in the Java file that are declared in
     *         the Java file
     */
    public List<Field> getFields() {
        return fields;
    }
}
