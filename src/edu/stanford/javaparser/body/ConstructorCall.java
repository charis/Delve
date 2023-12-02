/*
 * File          : ConstructorCall.java
 * Author        : Charis Charitsis
 * Creation Date : 15 August 2018
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.body;

// Import com.github.javaparser classes
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Place holder to store information about a particular constructor call
 */
public class ConstructorCall extends MethodCall
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The class type of the instance that is created by the constructor call
     */
    private final ClassOrInterfaceType classType;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new ConstructorCall by resolving the provided constructor
     * call expression.
     * 
     * @param constructorCallExpr The constructor call expression to resolve
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The declaration corresponding to the method
     *                           call expression could not be resolved<br>
     *                        2) The scope cannot be determined<br>
     *                        3) The begin or end line cannot be determined
     */
    public ConstructorCall(ObjectCreationExpr constructorCallExpr)
           throws ErrorException{
        super((Expression)constructorCallExpr);
        
        classType = constructorCallExpr.getType();
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Returns the class type description of the instance that is created by the
     * constructor call. The class type description uses only simple names and
     * not fully qualified names.
     * 
     * @return the class type description of the instance that is created by the
     *         constructor call
     */
    @Override
    public String getReturnType() {
        return classType.getNameAsString();
    }
    
    /**
     * Returns the class type description of the instance that is created by the
     * constructor call. The class type description uses only simple names and
     * not fully qualified names.
     * 
     * @param useSimpleNames Not used (i.e., don't care)
     * 
     * @return the class type description of the instance that is created by the
     *         constructor call
     * 
     * @deprecated Use {@link #getReturnType()}
     */
    public String getReturnType(boolean useSimpleNames) {
        return getReturnType();
    }
    
    /**
     * Returns the class type description of the instance that is created by the
     * constructor call. The class type description uses only simple names and
     * not fully qualified names.
     * 
     * @param useSimpleNames Not used (i.e., don't care)
     * 
     * @return the class type description of the instance that is created by the
     *         constructor call
     *         
     * @deprecated Use {@link #getReturnType()}
     */
    @Override
    public String getBaseReturnType(boolean useSimpleNames) {
        return getReturnType();
    }
    
    /**
     * @return {@code false}
     */
    @Override
    public boolean returnsVoid() {
        return false;
    }
    
    /**
     * @return {@code false}
     */
    @Override
    public boolean returnsArray() {
        return false;
    }
    
    /**
     * @return 0
     */
    @Override
    public int getDimensionOfReturnedArray() {
        return 0;
    }
}
