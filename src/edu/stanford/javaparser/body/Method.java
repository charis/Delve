/*
 * File          : Method.java
 * Author        : Charis Charitsis
 * Creation Date : 29 July 2018
 * Last Modified : 28 July 2021
 */
package edu.stanford.javaparser.body;

// Import Java SE classes
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.Position;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import constants
import static edu.stanford.constants.Literals.COLON;
import static edu.stanford.constants.Literals.COMMA;
import static edu.stanford.constants.Literals.LEFT_PARENTHESIS;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.RIGHT_PARENTHESIS;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.javaparser.Constants.JDK15_CLASSNAMES;
import static edu.stanford.javaparser.Constants.RESOLVE_JDK_CALLS;

/**
 * Represents a method in the source code.<br>
 * 
 * <pre>
 * Examples:
 * <code>
 *          1) /**
 *              * Sets the title of the story
 *              *
 *              * @param title - The title of the story
 *              * /
 *            {@literal @}Override
 *             public void setTitle(String title) {
 *                 this.title = title.toUpperCase();
 *             }
 *          
 *          2) public void setTitle(String title) {
 *                 this.title = title;
 *             }
 * </code>
 * </pre>
 */
public class Method implements Comparable<Method>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The method name */ 
    private final String            name;
    /** The method modifiers */
    private final EnumSet<Modifier> modifiers;
    /** The line number in the source code where the method begins */
    private final int               beginLine;
    /** The line number in the source code where the method ends */
    private final int               endLine;
    /** List with the method or method arguments */
    private final List<Argument>    arguments;
    /** The method body or {@code null} if there is no body */
    private final BlockStmt         body;
    /** The method return type or {@code null} in case of a constructor */
    private final Type              returnType;
    /** The Javadoc comment or {@code null} if there is no Javadoc */
    private final JavadocComment    javadocComment;
    /** The String representation of this method */
    private final String            description;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new Method.
     * 
     * @param methodDeclaration The module with the method information
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     */    
    public Method(MethodDeclaration methodDeclaration)
           throws ErrorException {
        this(methodDeclaration,
             methodDeclaration.getType(),
             methodDeclaration.getBody().orElse(null),
             methodDeclaration.getJavadocComment().orElse(null),
             methodDeclaration.getDeclarationAsString());
    }
    
    /**
     * Constructs a new Method.
     * 
     * @param <T> The type of the CallableDeclaration (e.g. MethodDeclaration or
     *            ConstructorDeclaration)
     * @param callableDeclaration The module with the information about the
     *                            callable declaration (e.g. MethodDeclaration
     *                            or ConstructorDeclaration instance)
     * @param returnType The method return type or {@code null} in case of a
     *                   constructor
     * @param body The method body or {@code null} in case of an abstract method
     *             (which has no body)
     * @param javadocComment The Javadoc comment or {@code null} if there is no
     *                       Javadoc comment
     * @param description The String representation of this method
     * 
     * @throws ErrorException in case the begin or end line cannot be determined
     *                        for the method or any comment contained in it
     */
    protected <T extends CallableDeclaration<T>>
              Method(CallableDeclaration<T> callableDeclaration,
                     Type                   returnType,
                     BlockStmt              body,
                     JavadocComment         javadocComment,
                     String                 description) 
           throws ErrorException {
        name      = callableDeclaration.getNameAsString();
        modifiers = callableDeclaration.getModifiers();
        
        Position beginPos = callableDeclaration.getBegin().orElse(null);
        if (beginPos == null) {
            throw new ErrorException("The begin position of '" + name
                                   + "' cannot be determined");
        }
        Position endPos = callableDeclaration.getEnd().orElse(null);
        if (endPos == null) {
            throw new ErrorException("The end position of '" + name
                                   + "' cannot be determined");
        }
        
        beginLine   = beginPos.line;
        endLine     = endPos.line;
        
        NodeList<Parameter> parameters = callableDeclaration.getParameters();
        arguments = new ArrayList<Argument>(parameters.size());
        
        for (Parameter parameter : parameters) {
            arguments.add(new Argument(parameter));
        }
        
        this.returnType     = returnType;
        this.body           = body;
        this.javadocComment = javadocComment;
        this.description    = description;
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    // ---------------------------------- //
    //    e   q   u   a   l   s   (  )    //
    // ---------------------------------- //
    /**
     * Indicates whether some other object is "equal to" this one.<br>
     * The equals method implements an equivalence relation on non-null object
     * references.<br>
     * For any non-null object (obj) to compare to this reference object (this),
     * this method returns true if and only if:<br>
     * <pre>
     *   1) 'this' and 'obj' refer to the same object (this == obj has the value
     *      true)
     *   OR
     *   2) 'this' and 'obj' refer to different objects, but:
     *      i)   'obj' is not null
     *      ii)  'obj' is an instance of the same class as 'this' 
     *      iii) 'obj' has the same list of lines as 'this' (i.e.,
     *            'this.lines.equals(obj.lines)')
     *      iv)  two Method instances that are compared have the same 
     *           method signatures and the same body
     *      Note: It does not matter if two methods start and end in different
     *            lines as long as they have the same signature and body
     * </pre>
     *   
     * @param obj The reference object to compare against
     * 
     * @return {@code true} if this object is the same as the obj argument or
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        // 'this' and 'obj' are pointing to the same object => return true
        if (this == obj) {
            return true;
        }
        
        // 'obj' is null and not pointing to the same object as 'this'
        // => return false
        if (obj == null) {
            return false;
        }
        
        // 'obj' is pointing to a different object than 'this'
        // If that object (obj) is not a Method object return false
        if (!(obj instanceof Method)) {
            return false;
        }
        
        Method other = (Method) obj; // Now can cast safely
        
        // Check #1: Make sure that both methods have the same signature
        String thisSignature  = this.getSignature(false, true);
        String otherSignature = other.getSignature(false, true);
        if (!thisSignature.equals(otherSignature)) {
            return false;
        }
        
        // Check #2: Make sure that both methods have the same body
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        }
        else if (!body.equals(other.body)) {
            return false;
        }
        
        // If we reach here => the two objects are considered equal
        return true;
    }
    
    // ------------------------------------------ //
    //    h   a   s   h   C   o   d   e   (  )    //
    // ------------------------------------------ //
    /**
     * {@inheritDoc}
     * 
     * To calculate the hash code it takes into consideration the signature
     * and the body.
     * @see List#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + getSignature(false, true).hashCode();
        return result;
    }
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this method with another method object for order.<br>
     * Returns a negative integer, zero, or a positive integer if this method
     * has an name that is alphabetically lower than, equal to, or greater than
     * the name of the other method.
     * 
     * @param other The method to be compared
     * 
     * @return a negative integer if this method has a name that is
     *         alphabetically lower than the name of the other method, zero if
     *         the names are equal and a positive integer if this method has an
     *         name alphabetically greater than the one of the other method
     */
    @Override
    public int compareTo(Method other) {
        return this.name.compareTo(other.name);
    }
    
    /**
     * @return the String representation of this method
     */
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * @return the method name
     */ 
    public String getName() {
        return name;
    }
    
    /**
     * @return method access scope.<br>
     *         Either "public", "protected", "private" or an empty string if no
     *         access scope is specified
     */
    public String getScope() {
        StringBuilder scope = new StringBuilder();
        for (Modifier modifier : modifiers) {
            scope.append(modifier.asString() + " ");
        }
        
        return scope.toString().trim();
    }
    
    /**
     * @return the line number in the source code where the method begins
     */
    public int getBeginLine() {
        return beginLine;
    }
    
    /**
     * @return the line number in the source code where the method ends
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * @return a list with the method arguments or an empty list if there
     *         there are no arguments
     */
    public List<Argument> getArguments() {
        return arguments;
    }
    
    /*
     * (non-Javadoc)
     * 
     * Lazy evaluation for better performance.
     * We do not want to minimize the constructor workload and thus execute this
     * piece of code only when the corresponding call/request is made.
     */
    /**
     * @return a list with the variables declared in the method body or an empty
     *         list if there there are no such variables
     *
     * @throws ErrorException in case of an error processing the variable to
     *                        determine if it is numeric, text, boolean or
     *                        none of those 
     */
    public List<Variable> getVariables()
           throws ErrorException {
        List<Variable> variables = new ArrayList<Variable>();
        
        if (body != null) {
            List<VariableDeclarationExpr> varDeclarationExpressions =
                                    body.findAll(VariableDeclarationExpr.class);
            for (VariableDeclarationExpr currExpression :
                                            varDeclarationExpressions) {
                EnumSet<Modifier> modifiers = currExpression.getModifiers();
                
                for (Node node : currExpression.getChildNodes()) {
                    if (node instanceof VariableDeclarator) {
                        // Should be always true, but just in case
                        variables.add(new Variable((VariableDeclarator)node,
                                                    modifiers));
                    }
                }
            }
        }
        return variables;
    }
    
    /**
     * Returns the signature of this method.<br>
     * The signature consists of the method name and its list of arguments.
     * 
     * @param includeArgumentNames {@code true} to include the name of each
     *                             argument after its type or {@code false} to
     *                             include only the argument types in the list
     *                             of arguments
     * @param includeReturnType {@code true} to include the return type or
     *                          {@code false} otherwise
     *  
     * @return a String representation of the method signature<br>
     *         E.g. 'String getSignature(boolean includeArgumentNames)'
     *               if includeArgumentNames is {@code true} or<br>
     *              'String getSignature(boolean)'
     *               if includeArgumentNames is {@code false}
     */
    public String getSignature(boolean includeArgumentNames,
                               boolean includeReturnType) {
        StringBuilder methodArguments = new StringBuilder();
        methodArguments.append(LEFT_PARENTHESIS);
        Iterator<Argument> itr = arguments.iterator();
        Argument argument;
        while (itr.hasNext()) {
            argument = itr.next();
            
            // Add the argument type
            methodArguments.append(argument.getType().toString());
            // Add the argument name (optional)
            if (includeArgumentNames) {
                methodArguments.append(SPACE + argument.getName());
            }
            // Add ", " to separate this argument from its next
            if (itr.hasNext()) {
                methodArguments.append(COMMA + SPACE);
            }
        }
        methodArguments.append(RIGHT_PARENTHESIS);
        
        if (returnType != null && includeReturnType) {
            // Return type + method name + arguments
            return returnType.asString() + SPACE + getName() + methodArguments;
        }
        else {
            // Constructor name + arguments
            return getName() + methodArguments;
        }
    }
    
    /**
     * @return the method body stripped of comments, empty lines etc. or
     *         {@code null} if there is no body
     */
    public String getStrippedBody() {
        return (body == null)? null : body.toString();
    }
    
    /**
     * Returns the method body lines after removing empty lines.<br>
     * Note: If there is a line that starts with code and continues with a
     *       comment this method returns it as two lines<br>
     *       <pre>
     *       Example:
     *                   int i = 0; // Counter
     *                 Although this is a single line, the method returns this
     *                 as two lines:
     *                   // Counter
     *                   int i = 0;
     *       </pre>
     * 
     * @return a list with the method body lines or {@code null} in case of an
     *         abstract method (that has no body). Each element in the list
     *         accounts for a line in the method body that is not an empty line.
     */
    public List<String> getNonEmptyBodyLines() {
        if (body == null) {
            return null;
        }
        
        String[] lines = body.toString().split(NEW_LINE);
        return Arrays.asList(lines);
    }
    
    /**
     * Returns the method body lines after removing empty lines, comment lines
     * etc.
     * 
     * @return a list with the method body lines that are actual code or
     *         {@code null} in case of an abstract method (that has no body).
     */
    public List<String> getBodyCodeLines() {
        if (body == null) { // No method body; abstract method
            return null;
        }
        
        BlockStmt body = this.body.clone();
        List<com.github.javaparser.ast.comments.Comment> comments =
                                                body.getAllContainedComments();
        for (com.github.javaparser.ast.comments.Comment comment : comments) {
            comment.remove();
        }
        
        String[] lines = body.toString().split(NEW_LINE);
        return Arrays.asList(lines);
    }
    
    /**
     * Returns the total number of lines in the method calculated as the difference
     * between the end line and the begin line of the method body.
     * 
     * @return the number of lines in the method body or zero if there is no
     *        body (i.e., abstract method)
     */
    public int getNumOfBodyLines() {
        if (body == null) {
            return 0;
        }
        
        Position beginPos = body.getBegin().orElse(null);
        Position endPos   = body.getEnd().orElse(null);
        if (beginPos == null || endPos == null) {
            return 0;
        }
        
        return endPos.line - beginPos.line + 1;
    }
    
    /**
     * Returns the total number of non-empty lines in the method.<br>
     * Note: If there is a line that starts with code and continues with a
     *       comment this method double-counts it.<br>
     *       <pre>
     *       Example:
     *                   int i = 0; // Counter
     *                 Although this is a single line, the method considers this
     *                 two lines:
     *                   // Counter
     *                   int i = 0;
     *       </pre>
     * 
     * @return the number of non-empty lines in the method body or zero if there
     *         is no body (i.e., abstract method)
     */
    public int getNumOfNonEmptyBodyLines() {
        if (body == null) {
            return 0;
        }
        else {
            String[] lines = body.toString().split(NEW_LINE);
            return lines.length;
        }
    }
    
    /**
     * Returns the total number of non-empty lines in the method.
     * The number of lines that have comments can be included or excluded from
     * the sum based on the specified flag.<br>
     * Note: If there is a line that starts with code and continues with a
     *       comment this method counts it correctly as a single line.<br>
     *       <pre>
     *       Example:
     *                   int i = 0; // Counter
     *                 Although this is a single line, the method considers this
     *                 two lines:
     *                   // Counter
     *                   int i = 0;
     *                 but because the comments are ignored, it finally takes into
     *                 account only line:
     *                   int i = 0;
     *                 Thus 'int i = 0; // Counter' counts a 1 line)
     *        </pre>
     * 
     * @param excludeComments {@code true} to exclude the lines that have
     *                        comments from the total count or {@code false} 
     *                        otherwise
     * 
     * @return the number of lines in the method body or zero if there is no
     *        body (i.e., abstract method)
     * 
     * @throws ErrorException in case of error processing the comments (e.g.
     *                        cannot determine the begin or end line)
     */
    public int getNumOfNonEmptyBodyLines(boolean excludeComments)
           throws ErrorException {
        if (body == null) {
            return 0;
        }
        
        int totalNumOfNonEmptyLines = getNumOfNonEmptyBodyLines();
        if (excludeComments) {
            List<Comment> comments = getBodyComments();
            int numOfCommentLines = 0;
            if (comments != null) {
                for (Comment comment : comments) {
                    numOfCommentLines += comment.getTextLines(false, false)
                                                .size();
                }
            }
            return totalNumOfNonEmptyLines - numOfCommentLines;
        }
        else {
            return totalNumOfNonEmptyLines;
        }
    }
    
    /**
     * @return the method modifiers
     */
    public EnumSet<Modifier> getModifiers() {
        return modifiers;
    }
    
    /**
     * @return {@code true} if the method is static or {@code false} otherwise
     */
    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }
    
    /**
     * @return {@code true} if the method is public or {@code false} otherwise
     */
    public boolean isPublic() {
        return modifiers.contains(Modifier.PUBLIC);
    }
    
    /**
     * @return {@code true} if the method is protected or {@code false}
     *         otherwise
     */
    public boolean isProtected() {
        return modifiers.contains(Modifier.PROTECTED);
    }
    
    /**
     * @return {@code true} if the method is private or {@code false} otherwise
     */
    public boolean isPrivate() {
        return modifiers.contains(Modifier.PRIVATE);
    }
    
    /**
     * @return {@code true} if the method is void or {@code false} otherwise
     */
    public boolean isVoid() {
        if (returnType == null) {
            return false;
        }
        return returnType.asString().equals("void");
    }
    
    /**
     * @return the method return type
     */
    public String getReturnType() {
        if (returnType != null) {
            return returnType.asString();
        }
        else {
            return null;
        }
    }
    
    /**
     * @return {@code true} if this is the 'public static void main(String[])'
     *         method or {@code false} otherwise
     */
    public boolean isMain() {
        return isPublic()                      &&
               isStatic()                      &&
               isVoid()                        &&
               getName().equals("main")        &&
               getArguments().size() == 1      &&
               getArguments().get(0).getType().toString().equals("String[]");
    }
    
    /**
     * @return the Javadoc comment for the method or {@code null} in case there
     *         there is no Javadoc comment
     */
    public String getJavadoc() {
        if (javadocComment == null) {
            return null;
        }
        
        return javadocComment.toString();
    }
    
    /**
     * Returns a list with all the comments in the method body irrespective of
     * their type.
     * 
     * @return a list with all the comments in the method body or {@code null}
     *         in case there are no comments or no body
     * 
     * @throws ErrorException in case of error processing the comments (e.g.
     *                        cannot determine the begin or end line)
     */
    public List<Comment> getBodyComments()
           throws ErrorException {
        if (body == null) {
            return null;
        }
        
        List<com.github.javaparser.ast.comments.Comment> commentInfos =
                                                body.getAllContainedComments();
        
        if (commentInfos.isEmpty()) {
            return null;
        }
        
        List<Comment> comments = new ArrayList<Comment>(commentInfos.size());
        for (com.github.javaparser.ast.comments.Comment comment : commentInfos){
            comments.add(new Comment(comment));
        }
        
        return comments;
    }
    
    /**
     * Returns a list with the comments in the method body
     * 
     * @param includeLineComments {@code true} to include the line comments
     *                            (i.e, comments that start with '//') or
     *                            {@code false} otherwise
     * @param includeBlockComments {@code true} to include the block comments
     *                             (i.e, comments that start with '/*') or
     *                             {@code false} otherwise
     * @param includeJavadocComments {@code true} to include the Javadoc
     *                               comments  (i.e, comments that start with
     *                               '/**') or {@code false} otherwise
     * 
     * @return a list with all the comments in the method body or {@code null}
     *         in case there are no comments or no body
     * 
     * @throws ErrorException in case of error processing the comments (e.g.
     *                        cannot determine the begin or end line)
     */
    public List<Comment> getBodyComments(boolean includeLineComments,
                                         boolean includeBlockComments,
                                         boolean includeJavadocComments)
           throws ErrorException {
        if (body == null) {
            return null;
        }
        
        List<com.github.javaparser.ast.comments.Comment> commentInfos =
                                                body.getAllContainedComments();
        List<Comment> comments = new ArrayList<Comment>(commentInfos.size());
        for (com.github.javaparser.ast.comments.Comment comment : commentInfos){
            if (comment.isLineComment()) {
                if (includeLineComments) {
                    comments.add(new Comment(comment));
                }
            }
            else if (comment.isBlockComment()) {
                if (includeBlockComments) {
                    comments.add(new Comment(comment));
                }
            }
            else if (comment.isJavadocComment()) {
                if (includeJavadocComments) {
                    comments.add(new Comment(comment));
                }
            }
        }
        
        if (comments.isEmpty()) { // No comments in the body => return null
            return null;
        }
        
        return comments;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Returns a list with the constructor calls that are made in the body of the
     * method or {@code null} in case of an abstract method.
     *  
     * @return a list with the constructor calls that are made in the body of the
     *         method or {@code null} in case of an abstract method
     * .
     * @throws ErrorException in case of error resolving a method call
     */
    public List<ConstructorCall> getConstructorCalls()
           throws ErrorException {
        if (body == null) { 
             return null;
        }
        
        List<ObjectCreationExpr> ctorCallExpressions =
                                         body.findAll(ObjectCreationExpr.class);
        
        int numOfCtorCalls = ctorCallExpressions.size();
        List<ConstructorCall> constructorCalls =
                              new ArrayList<ConstructorCall>(numOfCtorCalls);
        for (ObjectCreationExpr ctorCallExpression : ctorCallExpressions) {
            ConstructorCall constructorCall = 
                                       new ConstructorCall(ctorCallExpression);
            constructorCalls.add(constructorCall);
        }
        
        return constructorCalls;
    }
    
    /**
     * Returns a list with the method calls that are made in the body of the
     * method or {@code null} in case of an abstract method.
     *  
     * @return a list with the method calls that are made in the body of the
     *         method or {@code null} in case of an abstract method
     * .
     * @throws ErrorException in case of error resolving a method call
     */
    public List<MethodCall> getMethodCalls()
           throws ErrorException {
        if (body == null) { 
             return null;
        }
        
        List<MethodCallExpr> methodCallExpressions = 
                                       body.findAll(MethodCallExpr.class);
        
        int numOfMethodCalls = methodCallExpressions.size();
        List<MethodCall> methodCalls =
                               new ArrayList<MethodCall>(numOfMethodCalls);
        for (MethodCallExpr methodCallExpr : methodCallExpressions) {
            try {
                MethodCall methodCall = new MethodCall(methodCallExpr);
                methodCalls.add(methodCall);
            }
            catch (ErrorException ee) {
                String errorMsg = ee.getMessage();
                boolean throwException = false;
                if (errorMsg.contains("NotFoundException")) {
                    int colonIndex = errorMsg.lastIndexOf(COLON);
                    if (colonIndex != -1) {
                        String fqcn = errorMsg.substring(colonIndex + 1).trim();
                        if (!RESOLVE_JDK_CALLS &&
                            JDK15_CLASSNAMES.contains(fqcn)) {
                            // It's ok. We are not interested in resolving calls
                            // with JDK fqcn parameters
                        }
                        else {
                            errorMsg = "Not found: " + fqcn;
                            throwException = true;
                        }
                    }
                }
                if (throwException) {
                    throw new ErrorException(errorMsg);
                }
            }
        }
            
        return methodCalls;
    }
}
