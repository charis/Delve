/*
 * File          : MethodCall.java
 * Author        : Charis Charitsis
 * Creation Date : 18 June 2014
 * Last Modified : 8 March 2021
 */
package edu.stanford.javaparser.body;

// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.type.VariableType;
// Import constants
import static edu.stanford.constants.Literals.COLON;
import static edu.stanford.constants.Literals.COMMA;
import static edu.stanford.constants.Literals.LEFT_PARENTHESIS;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.RIGHT_PARENTHESIS;
import static edu.stanford.constants.Literals.SPACE;

/**
 * Place holder to store information about a particular method call
 */
public class MethodCall implements Comparable<MethodCall>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The name of the method that is called */
    private final String                         name;
    /**
     * The access scope of the method that is called.<br>
     * The method scope which can be one of the following:<br>
     * <pre>
     *  1) The instance name
     *      E.g. 'map' in ' Integer value = map.get(key);'
     *  2) The class name
     *      E.g. 'Integer' in 'value = Integer.valueOf("3");'
     *  3) Keyword {@code this}
     *      E.g. {@code this} in 'String val = this.getValue();'
     *  4) Keyword {@code super}
     *      E.g. {@code super} in 'String val = super.getValue();'
     *  5) {@code null} if the method belongs to the same class or is inherited
     *     from a super class and no keyword (either {@code this} or
     *     {@code super}) is used
     *      E.g. {@code null} in 'String val = getValue();'
     * </pre>
     */
    private final String                         scope;
    /** The line number in the source code where the method call begins */
    private final int                            beginLine;
    /** The column number in the source code where the method call begins */
    private final int                            beginCol;
    /** The line number in the source code where the method call ends */
    private final int                            endLine;
    /** The column number in the source code where the method call ends */
    private final int                            endCol;
    /** The number of arguments passed in the method call */
    private final int                            numOfArguments;
    /** The constructor declaration */
    private final ResolvedConstructorDeclaration ctorDeclaration;
    /** The method declaration */
    private final ResolvedMethodDeclaration      methodDeclaration;
    /** The return type of the method call */
    private final ResolvedType                   returnType;
    /** The String representation of this method */
    private final String                         description;
    /**
     * The argument values represented as text or {@code null} if there are no
     * arguments in the method call.
     */
    private final String[]                       argumentValues;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new MethodCall by resolving the provided method call
     * expression.
     * 
     * @param methodCallExpr The method call expression to resolve
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The declaration corresponding to the method
     *                           call expression could not be resolved<br>
     *                        2) The scope cannot be determined<br>
     *                        3) The begin or end line cannot be determined
     */
    public MethodCall(MethodCallExpr methodCallExpr)
           throws ErrorException{
        this((Expression)methodCallExpr);
    }
    
    /**
     * Constructs a new MethodCall by resolving the provided method or
     * object creation (i.e., constructor) call expression.
     * 
     * @param methodOrCtorCallExpr The method or constructor call expression to
     *                             resolve
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The declaration corresponding to the method
     *                           or constructor call expression could not be
     *                           resolved<br>
     *                        2) The scope cannot be determined<br>
     *                        3) The begin or end line cannot be determined
     */
    protected MethodCall(Expression methodOrCtorCallExpr)
           throws ErrorException {
        NodeList<Expression> argValues;
        
        // The method or constructor call may contain a variadic parameter
        // (i.e., the last parameter may be optional)
        // E.g.. 'suffix' is variadic in the followin method:
        //       public void appendText(String text, String... suffix)
        boolean isVariadic = false;
        if (methodOrCtorCallExpr instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr =
                           (MethodCallExpr)methodOrCtorCallExpr;
            argValues = methodCallExpr.getArguments();
            try {
                methodDeclaration = methodCallExpr.resolve();
            }
            catch(UnsolvedSymbolException use) {
                throw new ErrorException("Error resolving method call '"
                                       + methodCallExpr + "'. Details:"
                                       + NEW_LINE +  use.getMessage(),
                                         use);
            }
            catch (RuntimeException re) {
                String errorMsg = re.getMessage();
                Throwable cause = re.getCause();
                
                if (cause != null) {
                    errorMsg += COLON + SPACE + cause.getMessage();
                }
                
                throw new ErrorException("Error resolving method call '"
                                       + methodCallExpr + "'. Details:"
                                       + NEW_LINE +  errorMsg);
            }
            
            name             = methodCallExpr.getNameAsString();
            Expression scope = methodCallExpr.getScope().orElse(null);
            this.scope       = (scope == null)? null : scope.toString();
            returnType       = methodDeclaration.getReturnType();
            numOfArguments   = methodDeclaration.getNumberOfParams();
            if (numOfArguments > 0) {
                isVariadic = methodDeclaration.getLastParam().isVariadic();
            }
            ctorDeclaration  = null;
        }
        else if (methodOrCtorCallExpr instanceof ObjectCreationExpr) {
            ObjectCreationExpr objectCreationExpr =
                               (ObjectCreationExpr)methodOrCtorCallExpr;
            argValues = objectCreationExpr.getArguments();
            
            try {
                ctorDeclaration = objectCreationExpr.resolve();
            }
            catch(UnsolvedSymbolException use) {
                throw new ErrorException("Error resolving constructor call '"
                                       + objectCreationExpr + "'. Details:"
                                       + NEW_LINE +  use.getMessage(),
                                         use);
            }
            catch (RuntimeException re) {
                String errorMsg = re.getMessage();
                Throwable cause = re.getCause();
                
                if (cause != null) {
                    errorMsg += COLON + SPACE + cause.getMessage();
                }
                
                throw new ErrorException("Error resolving constructor call '"
                                       + objectCreationExpr + "'. Details:"
                                       + NEW_LINE +  errorMsg);
            }
            
            name              = objectCreationExpr.getTypeAsString();
            Expression scope  = objectCreationExpr.getScope().orElse(null);
            this.scope        = (scope == null)? null : scope.toString();
            returnType        = null;
            numOfArguments    = ctorDeclaration.getNumberOfParams();
            if (numOfArguments > 0) {
                isVariadic = ctorDeclaration.getLastParam().isVariadic();
            }
            methodDeclaration = null;
        }
        else {
            throw new ErrorException("The argument passed to this private "
                                   + "constructor must be either a "
                                   + "MethodCallExpr or a ObjectCreationExpr "
                                   + "instance and not a "
                                   + methodOrCtorCallExpr.getClass().getName());
        }
        
        boolean correctNumOfArgs = argValues.size() == numOfArguments || 
                       (isVariadic && (argValues.size() == numOfArguments - 1));
        if (!correctNumOfArgs) {
            String expectedNumOfArgs = String.valueOf(numOfArguments);
            if (isVariadic) {
                expectedNumOfArgs += " (or "
                                   + String.valueOf(numOfArguments - 1) + ")"; 
            }
            // Should never happen
            throw new RuntimeException("The number of expected arguments = "
                                     + expectedNumOfArgs + " is different than "
                                     + "the number of argument values = "
                                     + argValues.size());
        }
        
        if (numOfArguments == 0) {
            argumentValues = null;
        }
        else {
            argumentValues = new String[numOfArguments];
            int i = 0;
            for (Expression argValue : argValues) {
                // Remove inline comments
                // E.g. in a method call 'str.append("."); // appending dot'
                //      remove keep only "." and get rid of "// appending dot"
                if (argValue.getComment().isPresent()) {
                    argValue.removeComment();
                }
                argumentValues[i] = argValue.toString();
                i++;
            }
        }
        
        Position beginPos = methodOrCtorCallExpr.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + name
                                   + "' cannot be determined");
        }
        Position endPos = methodOrCtorCallExpr.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + name
                                   + "' cannot be determined");
        }
        
        beginLine      = beginPos.line;
        beginCol       = beginPos.column;
        endLine        = endPos.line;
        endCol         = endPos.column;
        description    = methodOrCtorCallExpr.toString();
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this method call with another method call object for order.<br>
     * Returns -1, 0, or 1 if this method call begins before, in the same
     * position (i.e., same line and column) or after the other method call.
     * 
     * @param other The method call to be compared
     * 
     * @return the value 0 if the method call begins in the same position as the
     *         other method call; -1 if it begins before the other method call;
     *         and 1 if t begins after the other method call.
     */
    @Override
    public int compareTo(MethodCall other) {
        if (beginLine < other.beginLine) {
            // This method call begins in a line before the other method call
            return -1;
        }
        if (beginLine > other.beginLine) {
            // This method call begins in a line after the other method call
            return 1;
        }
        
        // This method call begins in the same line as the other method call
        // Check the column
        if (beginCol < other.beginCol) {
            // This method call begins before the other method call
            return -1;
        }
        if (beginCol > other.beginCol) {
            // This method call begins after the other method call
            return 1;
        }
        
        // This method call begins in the same position as the other method call
        return 0;
    }
    
    
    /**
     * Returns the signature of the method that is called.<br>
     * The signature consists of the return type, the method name and the method
     * argument types (simple names are used for the types) in order.<br>
     * The returned string should match the returned string of 
     * {@link Method#getSignature(boolean, boolean)} if {@code useSimpleNames}
     * is {@code true}.
     * 
     * @param includeReturnType {@code true} to include the return type or
     *                          {@code false} otherwise
     * 
     * @return the signature of the method that is called
     * 
     * @throws ErrorException in case of error determining the argument types
     */
    public String getCalledMethodSignature(boolean includeReturnType)
           throws ErrorException {
        StringBuilder methodSignature = new StringBuilder();
        
        // Return type
        if (includeReturnType) {
            methodSignature.append(getReturnType() + SPACE);
        }
        
        // Method name
        methodSignature.append(getName());
        
        // Method arguments
        VariableType[] argumentTypes = getArgumentTypes();
        methodSignature.append(LEFT_PARENTHESIS);
        if (argumentTypes != null) {
            for (int i = 0; i < argumentTypes.length; i++) {
                methodSignature.append(argumentTypes[i].getDescription(true));
                if (i != argumentTypes.length - 1) {
                    methodSignature.append(COMMA + SPACE);
                }
            }
        }
        methodSignature.append(RIGHT_PARENTHESIS);
        
        return methodSignature.toString();
    }
    
    
    /**
     * @return the String representation of this method call
     */
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * @return the name of the method that is called
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the access scope of the method that is called.<br>
     *         The method scope which can be one of the following:<br>
     *         <pre>
     *         1) The instance name
     *             E.g. 'map' in ' Integer value = map.get(key);'
     *         2) The class name
     *             E.g. 'Integer' in 'value = Integer.valueOf("3");'
     *         3) Keyword {@code this}
     *             E.g. {@code this} in 'String val = this.getValue();'
     *         4) Keyword {@code super}
     *             E.g. {@code super} in 'String val = super.getValue();'
     *         5) {@code null} if the method belongs to the same class or is
     *            inherited from a super class and no keyword (either
     *            {@code this} or {@code super}) is used
     *             E.g. {@code null} in 'String val = getValue();'
     *         </pre>
     */
    public String getScope() {
        return scope;
    }
    
    /**
     * @return the line number in the source code where the method call begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * Returns the column in the source code where the method call begins.
     * This can be useful for example if there are multiple method calls in the
     * same line and we want to find out which comes first.
     * 
     * @return the column in the source code where the method call begins
     */
    public int getBeginColumn() {
        return beginCol;
    }
    
    /**
     * @return the line number in the source code where the method call ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return the column in the source code where the method call ends
     */
    public int getEndColumn() {
        return endCol;
    }
    
    /**
     * @return the number of arguments passed in the method call
     */
    public int getNumOfArguments() {
        return numOfArguments;
    }
    
    /**
     * @return an array with the argument names or {@code null} in case there
     *         are no arguments in the method call
     */
    public String[] getArgumentNames() {
        if (numOfArguments == 0) {
            return null;
        }
        
        String[] argumentNames = new String[numOfArguments];
        if (methodDeclaration != null) {
            for (int i = 0; i < numOfArguments; i++) {
                argumentNames[i] = methodDeclaration.getParam(i).getName();
            }
        }
        else if (ctorDeclaration != null) {
            for (int i = 0; i < numOfArguments; i++) {
                argumentNames[i] = ctorDeclaration.getParam(i).getName();
            }
        }
        
        return argumentNames;
    }
    
    /**
     * Returns the argument types in the method call in the same order as the
     * returned array by method {@link #getArgumentNames()}.
     * 
     * @return an array with the argument types or {@code null} in case there
     *         are no arguments in the method call
     * 
     * @throws ErrorException in case of error determining the argument types
     */
    public VariableType[] getArgumentTypes()
           throws ErrorException {
        if (numOfArguments == 0) {
            return null;
        }
        
        VariableType[] argumentTypes = new VariableType[numOfArguments];
        
        if (methodDeclaration != null) {
            for (int i = 0; i < numOfArguments; i++) {
                ResolvedType paramType = methodDeclaration.getParam(i).getType();
                argumentTypes[i] = new VariableType(paramType);
            }
        }
        else if (ctorDeclaration != null) {
            for (int i = 0; i < numOfArguments; i++) {
                ResolvedType paramType = ctorDeclaration.getParam(i).getType();
                argumentTypes[i] = new VariableType(paramType);
            }
        }
        
        return argumentTypes;
    }
    
    /**
     * Returns the argument values in the method call in the same order as the
     * returned array by method {@link #getArgumentNames()}.
     * 
     * @return an array with the argument values or {@code null} in case there
     *         are no arguments in the method call
     * 
     * @throws ErrorException in case of error determining the argument types
     */
    public String[] getArgumentValues()
           throws ErrorException {
        return argumentValues;
    }
    
    /**
     * @return {@code true} if the method call does not return a type or
     *         {@code false} otherwise
     */
    public boolean returnsVoid() {
        return returnType.isVoid();
    }
    
    /**
     * @return {@code true} if the method call returns an array or {@code false}
     *         otherwise
     */
    public boolean returnsArray() {
        return returnType.isArray();
    }
    
    /**
     * @return the dimension of the returned array or 0 if the method call does
     *         not return an array
     */
    public int getDimensionOfReturnedArray() {
        return returnType.arrayLevel();
    }
    
    /**
     * Returns the return type description.<br>
     * If the method has no return type it returns "void".<br>
     * If the return type uses fully qualified names it can replaces them with
     * simple names. 
     * 
     * @return the return type description
     */
    public String getReturnType() {
        return getReturnType(true);
    }
    
    /**
     * Returns the return type description.<br>
     * If the method has no return type it returns "void".<br>
     * If the flag {@code useSimpleNames} is set and the return type uses
     * fully qualified names it can replaces them with simple names. 
     * 
     * @param useSimpleNames {@code true} to replace any fully qualified names
     *                       in the variable type with simple names or
     *                       {@code false} otherwise
     * 
     * @return the return type description
     */
    public String getReturnType(boolean useSimpleNames) {
        if (!useSimpleNames) {
            return returnType.describe();
        }
        
        return getSimpleNameDescription(returnType);
    }
    
    /**
     * Returns the return type description.<br>
     * If the method has no return type it returns "void".<br>
     * If the flag {@code useSimpleNames} is set and the base return type uses
     * fully qualified names it can replaces them with simple names. 
     * 
     * @param useSimpleNames {@code true} to replace any fully qualified names
     *                       in the variable type with simple names or
     *                       {@code false} otherwise
     * 
     * @return the base return type description (e.g. 'String' in case of
     *         'String[]' etc.)
     */
    public String getBaseReturnType(boolean useSimpleNames) {
       ResolvedType baseType;
       if (returnsArray()) {
           baseType = returnType.asArrayType().getComponentType();
       }
       else {
           baseType = returnType;
       }
       
       if (!useSimpleNames) {
           return baseType.describe();
       }
       
       return getSimpleNameDescription(baseType);
    }
    
    /**
     * Helper method that prints information about the method call.
     */
    public void printInfo() {
        System.out.println("Name         : " + name);
        System.out.println("Scope        : " + scope);
        System.out.println("Begin line   : " + beginLine);
        System.out.println("End line     : " + endLine);
        System.out.println("# args       : " + numOfArguments); 
        String[] argNames = getArgumentNames();
        if (argNames != null) {
            for (int i = 0; i < argNames.length; i++) {
                System.out.println("arg " + (i + 1) + "        : " + argNames[i]); 
            }
        }
        
        VariableType[] argTypes;
        try {
            argTypes = getArgumentTypes();
            if (argTypes != null) {
                for (VariableType argType : argTypes) {
                    System.out.println("is Array =>" + argType.isArray() + " -- array count=" + argType.getArrayCount());;
                    System.out.println("is Boolean =>" + argType.isBoolean());
                    System.out.println("is Numeric =>" + argType.isNumeric());
                    System.out.println("is Text =>" + argType.isText());
                }
            }
        }
        catch (ErrorException ee) {
             System.out.println("Error getting the method call argument types");
        }
        System.out.println("Returns void : " + returnsVoid()); 
        System.out.println("Returns array: " + returnsArray() + " - dimension: " + getDimensionOfReturnedArray()); 
        System.out.println("Base type    : " + getBaseReturnType(true)); 
        System.out.println("Description  : " + toString()); 
        System.out.println("------------------------------------");
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Replaces any fully qualified names in the description for the given
     * resolved type with simple names
     * 
     * @param resolvedType The resolved type to process
     * 
     * @return a resolved type description that uses simple names
     */
    private String getSimpleNameDescription(ResolvedType resolvedType) {
       StringBuilder result = new StringBuilder();
       String[] tokens = resolvedType.describe().split("<");
       int numOfTokens = tokens.length;
       for (int i = 0; i < numOfTokens; i++) {
           String[] subtokens = tokens[i].split(",");
           int numOfSubtokens = subtokens.length;
           for (int j = 0; j < numOfSubtokens; j++) {
               int dotIndex = subtokens[j].lastIndexOf(".");
               if (dotIndex != -1) {
                   subtokens[j] = subtokens[j].substring(dotIndex + 1);
               }
               result.append(subtokens[j]);
               if (j < numOfSubtokens - 1) {
                   result.append(", ");
               }
           }
           
           if (i < numOfTokens - 1) {
               result.append("<");
           }
       }
       
       return result.toString();
    }
}
