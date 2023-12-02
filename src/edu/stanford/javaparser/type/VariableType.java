/*
 * File          : VariableType.java
 * Author        : Charis Charitsis
 * Creation Date : 3 April 2014
 * Last Modified : 18 August 2018
 */
package edu.stanford.javaparser.type;

// Import Java SE classes
import java.util.List;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import com.github.javaparser classes
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedWildcard;
import com.github.javaparser.utils.Pair;

/**
 * Represents a variable type (e.g. method return type or method argument type)
 */
public class VariableType
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The data structure with the type information or {@code null} if the type
     * is determined via symbol resolution
     */
    private final Type         type;
    /**
     * The data structure with the type information if the type is determined
     * via symbol resolution or {@code null} otherwise
     */
    private final ResolvedType resolvedType;
    /** True if the type is void or false otherwise */
    private final boolean      isVoid;
    /**
     * The array dimension of the variable name only or 0 if the variable name
     * does not denote an array<br>
     * <pre>
     * E.g.
     *      0 in 'int count' 
     *      1 in 'int[] count'
     *      1 in 'int count[]'
     *      2 in 'int[][] count'
     *      2 in 'int[] count[]'
     *      2 in 'int count[][]'
     * </pre>
     */
    private final int          arrayCount;
    /**
     * {@code true} if the type is a numeric type (e.g. int, Integer, long,
     * double,  int[], Double[] etc.) or {@code false} otherwise
     */
    private boolean isNumeric;
    /**
     * {@code true} if the type is a textual type (e.g. String, char, String[],
     * Character etc.) or {@code false} otherwise
     */
    private boolean isText;
    /**
     * {@code true} if the type is a boolean type (e.g. boolean, Boolean,
     * boolean[], Boolean[] etc.) or {@code false} otherwise
     */
    private boolean isBoolean;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new VariableType
     * 
     * @param type The module with the type information
     * 
     * @throws ErrorException in case of an error processing the type to
     *                        determine if it is numeric, text, boolean or
     *                        none of those
     */
    public VariableType(Type type)
           throws ErrorException {
        this.type    = type;
        resolvedType = null;
        isVoid       = type.isVoidType();
        arrayCount   = type.getArrayLevel();
        
        Type componentType = null;
        isNumeric          = false;
        isText             = false;
        isBoolean          = false;
        
        if (isVoid) { // E.g. no arguments in a method call or no return type
            return;
        }
        
        if (type.isPrimitiveType()) { // E.g. 'int count'
            componentType = type;
            Primitive primitive = ((PrimitiveType)type).getType();
            processPrimitive(primitive);
        }
        else if (type.isClassOrInterfaceType()) { // E.g. 'Integer count'
                                                  //      'List<String> lines'
            processClassOrInterfaceType((ClassOrInterfaceType)type);
        }
        else if (type.isArrayType()) {// E.g. 'Integer[] count', 'int[][] point'
            ArrayType arrayType = (ArrayType)type;
            
            // Find the component type (e.g., 'int' in 'int[]' or 'int[][]')
            componentType = arrayType.getComponentType();
            while (componentType.isArrayType()) {
                componentType = ((ArrayType)componentType).getComponentType();
            }
            
            if (componentType.isPrimitiveType()) { // E.g. 'int'
                Primitive primitive = ((PrimitiveType)componentType).getType();
                processPrimitive(primitive);
            }
            else if (componentType.isClassOrInterfaceType()) { // E.g. 'Integer'
                processClassOrInterfaceType(
                              (ClassOrInterfaceType)componentType);
            }
        }
        else {
            throw new ErrorException("Cannot determine component type for'"
                                   + type
                                   + "'. which is an instance of class '"
                                   + type.getClass() + "'");
        }
    }
    
    /**
     * Constructs a new VariableType
     * 
     * @param resolvedType The module with the type information (product of
     *                     symbol resolution)
     * 
     * @throws ErrorException in case of an error processing the type to
     *                        determine if it is numeric, text, boolean or
     *                        none of those
     */
    public VariableType(ResolvedType resolvedType)
            throws ErrorException {
        type               = null;
        this.resolvedType  = resolvedType;
        isVoid             = resolvedType.isVoid();
        arrayCount         = resolvedType.arrayLevel();
        
        isNumeric          = false;
        isText             = false;
        isBoolean          = false;
        
        if (isVoid) { // E.g. no arguments in a method call or no return type
            return;
        }
        
        if (resolvedType.isPrimitive()) { // E.g. 'int count'
            processResolvedPrimitiveType((ResolvedPrimitiveType)resolvedType);
        }
        else if (resolvedType.isReferenceType()) { // E.g. 'Integer count'
                                           //      'List<String> lines'
            processResolvedReferenceType((ResolvedReferenceType)resolvedType);
        }
        else if (resolvedType.isArray()) { // E.g. 'Integer[] count',
                                           //      'int[][] point'
            // Find the component type (e.g., 'int' in 'int[]' or 'int[][]')
            while (resolvedType.isArray()) {
                resolvedType =
                        ((ResolvedArrayType)resolvedType).getComponentType();
            }
            
            if (resolvedType.isPrimitive()) { // E.g. 'int count'
                processResolvedPrimitiveType(
                                        (ResolvedPrimitiveType)resolvedType);
            }
            else if (resolvedType.isReferenceType()) { // E.g. 'Integer'
                processResolvedReferenceType(
                               (ResolvedReferenceType)resolvedType);
            }
        }
        else if (resolvedType.isTypeVariable()) { //
                // Do nothing. Cannot determine if numeric. text or boolean.
        }
        else {
            throw new ErrorException("Cannot determine component type for'"
                                   + resolvedType
                                   + "'. which is an instance of class '"
                                   + resolvedType.getClass() + "'");
        }
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * @return a string representation of the variable type<br>
     *         <pre>
     *         Examples:
     *                   'int' for 'int count'
     *                   'int[][]' for 'int[] count[]'
     *                   'List{@literal <String>}[][]' for
     *                                 'List{@literal <String>}[] documents[]'
     *         </pre>
     */
    @Override
    public String toString() {
        if (type != null) {
            return type.toString();
        }
        else { // resolvedType is not null if type is null
            return resolvedType.describe();
        }
    }
    
    /**
     * Returns the variable type description.<br>
     * If the flag {@code useSimpleNames} is set and the variable type uses
     * fully qualified names it can replaces them with simple names. 
     * 
     * @param useSimpleNames {@code true} to replace any fully qualified names
     *                       in the variable type with simple names or
     *                       {@code false} otherwise
     * 
     * @return the variable type description
     */
    public String getDescription(boolean useSimpleNames) {
        if (!useSimpleNames || type != null) {
            return toString();
        }
        
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
    
    /**
     * @return {@code true} if the type is void or {@code false} otherwise
     */
    public boolean isVoid() {
        return isVoid;
    }
    
    /**
     * @return {@code true} if the type is an array (either of primitives or
     *         objects in general or {@code false} otherwise)
     */
    public boolean isArray() {
         return arrayCount > 0;
    }
    
    /**
     * @return the array dimension of the variable name only or 0 if the
     *         variable name does not denote an array<br>
     *         <pre>
     *         E.g.
     *              0 in 'int count'
     *              1 in 'int[] count'
     *              1 in 'int count[]'
     *              2 in 'int[][] count'
     *              2 in 'int[] count[]'
     *              2 in 'int count[][]'
     *         </pre>
     */
    public int getArrayCount() {
        return arrayCount;
    }
    
    /**
     * @return {@code true} if the type is a numeric type (e.g. int, Integer,
     *         long, double, int[], Double[] etc.) or {@code false} otherwise
     */
    public boolean isNumeric() {
        return isNumeric;
    }
    
    /**
     * @return {@code true} if the type is a textual type (e.g. String, char,
     *         String[], Character etc.) or {@code false} otherwise
     */
    public boolean isText() {
        return isText;
    }
    
    /**
     * @return {@code true} if the type is a boolean type (e.g. boolean,
     *         Boolean, boolean[], Boolean[] etc.) or {@code false} otherwise
     */
    public boolean isBoolean() {
        return isBoolean;
    }
    
    // ------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       M   E   T   H   O   D   S   //
    // ------------------------------------------------------------- //
    /**
     * Processes the given primitive to find out if it is numeric, text or
     * boolean.<br>
     * <pre>
     * Examples:
     *         int count
     *         boolean isEmpty
     *         char letter
     * </pre>
     * 
     * @param primitive The primitive to process.
     */
    private void processPrimitive(Primitive primitive) {
        switch (primitive) {
            case BOOLEAN:
                 isNumeric = false;
                 isText    = false;
                 isBoolean = true;
                 break;
            case CHAR:
            case BYTE:
                 isNumeric = false;
                 isText    = true;
                 isBoolean = false;
                 break;
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                 isNumeric = true;
                 isText    = false;
                 isBoolean = false;
                 break;
            default:// In case we add a new Primitive enum member in the future
                 throw new RuntimeException("Unknown primitive type: " + type);
        }
    }
    
    /**
     * Processes the given class or interface type to find out the type of
     * information (numeric, text or boolean) that it stores.<br>
     * <pre>
     * Examples:
     *         Integer count
     *         List{@literal <String>} lines
     *         List{@literal <? extends String>} lines
     * </pre>
     * 
     * 
     * @param type The class or interface type to process
     * 
     * @throws ErrorException of the class or interface type is 'complicated'
     *                        enough so that the type of information it stores
     *                        cannot be determined
     */
    private void processClassOrInterfaceType(ClassOrInterfaceType type)
           throws ErrorException{
        // Extract the component type
        Type componentType = null;
        
        List<Node> childNodes = type.getChildNodes();
        if (childNodes.size() == 1 &&
            childNodes.get(0) instanceof SimpleName) { // E.g. 'Integer count'
            componentType = type;
        }
        else {
            // E.g. 'List<String> lines', 'List<? extends String> lines'
            for (Node childNode : childNodes) {
                if (childNode instanceof Type) {
                    // E.g. 'String' in 'List<String>' or
                    //      '? extends String' in 'List<? extends String>'
                    if (childNode instanceof WildcardType) {
                        // '? extends String' in 'List<? extends String>'
                        for (Node currNode : childNode.getChildNodes()) {
                            if (currNode instanceof Type) {
                                componentType = (Type)currNode;
                                break;
                            }
                        }
                    }
                    else { // E.g. 'String' in 'List<String>'
                        componentType = type;
                        break;
                    }
                }
            }
        }
        
        if (componentType == null) {
            throw new ErrorException("Cannot determine component type for '"
                                    + type + "'");
        }
        
        if (componentType.isPrimitiveType()) {
            Primitive primitive = ((PrimitiveType)componentType).getType();
            processPrimitive(primitive);
        }
        else {
            if (componentType.isClassOrInterfaceType()) {
                ClassOrInterfaceType classOrInterfaceType = 
                                          (ClassOrInterfaceType)componentType;
                NodeList<Type> typeArguments =
                               classOrInterfaceType.getTypeArguments()
                                                   .orElse(null);
                if (typeArguments != null && !typeArguments.isEmpty()) {
                    if (typeArguments.size() == 1) {
                        Type typeArgument = typeArguments.get(0);
                        if (typeArgument instanceof ClassOrInterfaceType) {
                            classOrInterfaceType =
                                            (ClassOrInterfaceType)typeArgument;
                        }
                    }
                    else {
                        // More than one type arguments
                        // Example: 'String, Integer' in 'Map<String, Integer>'
                        // Cannot classify this as 'numeric', 'text', 'boolean'
                        
                        // Do nothing!
                    }
                }
                
                if (classOrInterfaceType.isBoxedType()) { // Autoboxing
                    // boolean --> Boolean, char   --> Character, byte --> Byte,
                    // short   --> Short,   int    --> Integer,   long --> Long,
                    // float   --> Float,   double --> Double
                    for (Primitive primitive : Primitive.values()) {
                        if (primitive.toBoxedType().equals(
                                                    classOrInterfaceType)) {
                            processPrimitive(primitive);
                            break;
                        }
                    }
                }
                else {
                    // Check if it starts with String.
                    // If so, classify this as 'text'.
                    // E.g. String, StringBuffer, StringBuilder, StringTokenizer
                    // etc.
                    String simpleName = classOrInterfaceType.getNameAsString();
                    if (simpleName.startsWith("String")) {
                        isText = true;
                    }
                }
            }
        }
    }
    
    /**
     * Processes the given resolved primitive type to find out if it is numeric,
     * text or boolean.<br>
     * <pre>
     * Examples:
     *         int count
     *         boolean isEmpty
     *         char letter
     * </pre>
     * 
     * @param resolvedPrimitiveType The resolved primitive type to process.
     */
    private void processResolvedPrimitiveType(
                                ResolvedPrimitiveType resolvedPrimitiveType) {
        switch (resolvedPrimitiveType) {
            case BOOLEAN:
                 isNumeric = false;
                 isText    = false;
                 isBoolean = true;
                 break;
            case CHAR:
            case BYTE:
                 isNumeric = false;
                 isText    = true;
                 isBoolean = false;
                 break;
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                 isNumeric = true;
                 isText    = false;
                 isBoolean = false;
                 break;
            default://In case a new Primitive enum member is added in the future
                 throw new RuntimeException("Unknown primitive type: " + type);
        }
    }
    
    /**
     * Processes the given class, interface or enum type to find out the type of
     * information (numeric, text or boolean) that it stores.<br>
     * <pre>
     * Examples:
     *         Integer count
     *         List{@literal <String>} lines
     *         List{@literal <? extends String>} lines
     * </pre>
     * 
     * 
     * @param resolvedReferenceType The class, interface or enum type to process
     * 
     * @throws ErrorException of the class or interface type is 'complicated'
     *                        enough so that the type of information it stores
     *                        cannot be determined
     */
    private void processResolvedReferenceType(
                               ResolvedReferenceType resolvedReferenceType)
            throws ErrorException{
        // Extract the component type
        ResolvedType resolvedType = null;
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>>
             typeParametersMapList = resolvedReferenceType.getTypeParametersMap();
        
        if (typeParametersMapList.isEmpty()) { // E.g. 'Integer count'
            resolvedType = resolvedReferenceType;
        }
        else {
            if (typeParametersMapList.size() != 1) {
                // Keep the composite type as is. Later it will not be processed
                // because we cannot tell if it numeric, text or boolean.
                resolvedType = resolvedReferenceType;
            }
            else {
                // E.g. 'List<String> lines'
                //      'List<? extends String> lines'
                //      'List<Map<String, Integer>>'
                resolvedType = typeParametersMapList.get(0).b;
                if (resolvedType.isWildcard()) {
                    resolvedType =
                            ((ResolvedWildcard)resolvedType).getBoundedType();
                }
            }
        }
        
        if (resolvedType == null) {
            throw new ErrorException("Cannot determine resolved type for '"
                                    + resolvedReferenceType + "'");
        }
        
        if (resolvedType.isPrimitive()) {
            processResolvedPrimitiveType((ResolvedPrimitiveType)resolvedType);
        }
        else if (resolvedType.isReferenceType()) { // E.g. 'Integer count'
            resolvedReferenceType = (ResolvedReferenceType)resolvedType;
            
            // Check if the reference type is essentially a primitive type
            // (i.e., 'java.lang.Integer' is essentially 'int')
            for (ResolvedPrimitiveType resolvedPrimitiveType :
                         ResolvedPrimitiveType.values()) { // Autoboxing
                // boolean --> Boolean, char   --> Character, byte --> Byte,
                // short   --> Short,   int    --> Integer,   long --> Long,
                // float   --> Float,   double --> Double
                if (resolvedReferenceType.isAssignableBy(resolvedPrimitiveType))
                {
                    processResolvedPrimitiveType(resolvedPrimitiveType);
                    return;
                }
            }
            
            // Check if the simple type name starts with String. If so, classify
            // this as 'text'
            // E.g. String, StringBuffer, StringBuilder, StringTokenizer etc.
            String qualifiedName = resolvedReferenceType.getQualifiedName();
            if (!qualifiedName.contains("<")) {
                // Make sure it is not a generic type with type parameters
                // (e.g. 'java.util.Map<java.lang.Integer, java.lang.String>')
                int dotIndex = qualifiedName.lastIndexOf(".");
                if (dotIndex != -1) { // Should be always true, but just in case
                    String simpleName = qualifiedName.substring(dotIndex + 1);
                    if (simpleName.startsWith("String")) {
                        isText = true;
                    }
                }
            }
        }
    }
}
