/*
 * File          : JavaMethod.java
 * Author        : Charis Charitsis
 * Creation Date : 22 February 2019
 * Last Modified : 5 January 2021
 */
package edu.stanford.java;

// Import Java SE classes
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
// Import constants
import static edu.stanford.constants.Literals.LEFT_SQUARE_BRACKET;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.RIGHT_SQUARE_BRACKET;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.constants.Literals.TAB;
import static edu.stanford.constants.Literals.UNDERSCORE;
import static edu.stanford.java.SourceCodeParser.METHOD_ENTRY_CALL;
import static edu.stanford.java.SourceCodeParser.METHOD_EXIT_CALL;

/**
 * Information about a method in a Java program
 */
public class JavaMethod implements Comparable<JavaMethod>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The Java file where the method exists */
    private final JavaFile javaFile;
    /** The object representing the method in the code */
    private final Method   method;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new JavaMethod
     * 
     * @param javaFile The Java file where the method is found
     * @param methodName The method name
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided Java file is {@code null}<br>
     *                        2) The provided method name is {@code null} or
     *                           empty<br>
     *                        3) There is no method body for the specified
     *                           method or this method does not exist in the
     *                           file
     */
    public JavaMethod(JavaFile javaFile,
                      String   methodName)
           throws ErrorException {
        if (javaFile == null) {
            throw new ErrorException("'javaFile' is null");
        }
        if (methodName == null || methodName.trim().isEmpty()) {
            throw new ErrorException("No method name is provided");
        }
        
        this.javaFile = javaFile;
        
        List<Method> methods = javaFile.getMethods();
        Method method        = null;
        for (Method currMethod : methods) {
            if (currMethod.getName().equals(methodName)) {
                method = currMethod;
                break;
            }
        }
        
        if (method == null) {
            throw new ErrorException("Method '" + methodName
                                   + "' either does not exist in file '"
                                   + javaFile.getFilePathname()
                                   + "' or has no body.");
        }
        
        this.method = method;
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
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
     *      iii) 'obj' has the same file path name as 'this'
     *      iv)  'obj' has the same method name as 'this'
     *      v)   'obj' has the same method body as 'this'
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
        // If that object (obj) is not a JavaMethod object return false
        if (!(obj instanceof JavaMethod)) {
            return false;
        }
        
        JavaMethod other = (JavaMethod) obj; // Now can cast safely
        
        // Check #1: Make sure that both JavaMethods have the same file
        //           pathname
        if (!getFilePathname().equals(other.getFilePathname())) {
            return false;
        }
        
        // Check #2: Make sure that both JavaMethods have the same class name
        if (!getClassname().equals(other.getClassname())) {
            return false;
        }
        
        // Check #3: Make sure that both JavaMethods have the same method name
        if (!getMethodName().equals(other.getMethodName())) {
            return false;
        }
        
        // Check #4: Make sure that both JavaMethods have the same method body
        if (!getBody().equals(other.getBody())) {
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
     * To calculate the hash code it takes into consideration the file pathname,
     * the class name and the method.
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getFilePathname().hashCode();
        result = prime * result + getClassname().hashCode();
        result = prime * result + method.hashCode();
        
        return result;
    }
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this Java method with another Java method object for order.
     * <br>
     * The comparison between the two method takes place by the alphabetical
     * order, breaking ties by the alphabetical order of the source file where
     * those methods exist.<br>
     * This method is less than the provided method, if and only if, either
     * {@code this.getMethodName() < other.getMethodName()} or if
     * {@code this.getMethodName() == other.getMethodName()} and
     * {@code this.getFilePathname() < other.getFilePathname()}.
     * 
     * @param other The other Java method to be compared with this method
     * 
     * @return the value {@code 0} if this method is equal to the other method,
     *         -1 if this method is less than the other method and +1 if this
     *         method is greater than the other method
     */
    @Override
    public int compareTo(JavaMethod other) {
        String thisMethodName  = this.getMethodName();
        String otherMethodName = other.getMethodName();
        if (thisMethodName.compareTo(otherMethodName) < 0) {
            return -1;
        }
        
        if (thisMethodName.compareTo(otherMethodName) > 0) {
            return 1;
        }
        
        // thisMethodName == otherMethodName
        String thisFilePathname  = this.getFilePathname();
        String otherFilePathname = other.getFilePathname();
        if (thisFilePathname.compareTo(otherFilePathname) < 0) {
            return -1;
        }
        
        if (thisFilePathname.compareTo(otherFilePathname) > 0) {
            return 1;
        }
        
        // thisFilePathname == otherFilePathname
        return 0;
    }
    
    /**
     * Returns a string with the method name and the name of the file where it
     * exists using the following format:<br>
     * {@literal <method name>[<filename>]>}
     *  
     * @return a string with the method name and the name of the file where it
     *         exists
     */
    @Override
    public String toString() {
        return getMethodName()
             + LEFT_SQUARE_BRACKET + getFilename() + RIGHT_SQUARE_BRACKET; 
    }
    
    /**
     * @return the pathname of the source code file where the method exists
     */
    public String getFilePathname() {
        return javaFile.getFilePathname();
    }
    
    /**
     * @return the name of the source code file where the method exists
     */
    public String getFilename() {
        return new File(javaFile.getFilePathname()).getName();
    }
    
    /**
     * @return the class name that is associated with this source code file
     */
    public String getClassname() {
        return javaFile.getClasses().get(0).getName();
    }
    
    /** 
     * @return the method name
     */
    public String getMethodName() {
        return method.getName();
    }
    
    /**
     * @return the body of the method
     */
    public String getBody() {
        return method.getStrippedBody();
    }
    
    /**
     * It returns the code that is executed by this method. If the method body
     * calls more methods in the same Java file, it returns those methods as
     * well.<br>
     * The methods are optionally prefixed by the provided prefix. If so, the
     * method calls in the returned body are modified accordingly (to call the
     * prefixed/renamed methods).<br>
     * Finally if the method is either recursive or it is called by a method
     * that itself called in its body, the method name (in the recursive call or
     * in the method call in general) is replaced by the provided alias (unless
     * the alias is {@code null} or empty string).
     * 
     * @param prefix The prefix for all internal methods that are called in the
     *               body of this method and in the bodies of those methods as
     *               well or {@code null} to use no prefix
     * @param alias The alias for this method which will be used if this method
     *              is called (either recursively or by another method that is
     *              returned here or {@code null} to use no alias
     * @param instrumentation {@code true} to insert an instrumentation line
     *                        when the method starts and another when the method
     *                        exits. The instrumentation lines are added only to
     *                        the this method, not any other methods that it
     *                        calls (which are also returned by this method).
     * 
     * @return the body of the method along with any internal methods called in
     *         the body
     *  
     * @throws ErrorException in case of error resolving a method call
     */
    public String getExecutedCode(String  prefix,
                                  String  alias,
                                  boolean instrumentation)
           throws ErrorException{
        // Keep track of the internal method calls
        Set<String> calledMethodNames = new HashSet<String>();
        String methodName = method.getName();
        updateInternalCalledMethodNames(methodName,
                                        calledMethodNames);
        
        // Check if this method is called (either recursively by itself or
        // by another method) in which case we should use the alias unless it
        // is null or empty
        boolean useAlias = false; 
        if (calledMethodNames.contains(methodName)) {
            useAlias = alias != null && !alias.trim().isEmpty();
            calledMethodNames.remove(methodName); // Leave the other methods
        }
        
        // Build the body and add called methods (any level deep)
        StringBuilder result = new StringBuilder();
        if (instrumentation) {
            String[] methodBodyLines = method.getStrippedBody().split(NEW_LINE);
            int numOfLines = methodBodyLines.length;
            if (numOfLines > 2) {
                // This is the opening line in the body (i.e., '{')
                result.append(methodBodyLines[0] + NEW_LINE);
                
                // Insert instrumentation line (beginning of body code)
                result.append(TAB + METHOD_ENTRY_CALL + NEW_LINE);
                
                for (int i = 1; i < numOfLines - 1; i++) {
                    result.append(methodBodyLines[i] + NEW_LINE);
                }
                
                // Insert instrumentation line (end of body code)
                if (alias != null && !alias.trim().isEmpty()) {
                    result.append(TAB + METHOD_EXIT_CALL(alias) + NEW_LINE);
                }
                else {
                    result.append(TAB + METHOD_EXIT_CALL(methodName)
                                + NEW_LINE);
                }
                
                // This is the closing line in the body (i.e., '}')
                result.append(methodBodyLines[numOfLines - 1] + NEW_LINE);
            }
            else { // Empty body (i.e., '{}')
                result.append(method.getStrippedBody());
            }
        }
        else {
            result.append(method.getStrippedBody());
        }
        
        for (Method method : javaFile.getMethods()) {
            if (calledMethodNames.contains(method.getName())) {
                result.append(NEW_LINE + NEW_LINE
                           + method.getSignature(true, true)
                           + method.getStrippedBody());
            }
        }
        
        // At this point the result has what we wanted except that we should
        // rename the methods using the prefix
        String resultAfterRename = result.toString();
       
        if (!calledMethodNames.isEmpty() &&
            prefix != null && !prefix.trim().isEmpty()) {
            for (String targetName : calledMethodNames) {
                String regex       = SPACE + targetName + "\\s*\\(";
                String replacement = SPACE + prefix + UNDERSCORE
                                           + targetName + "\\(";
                resultAfterRename = resultAfterRename.replaceAll(regex,
                                                                 replacement);
            }
        }
        
        if (useAlias) {
            // If methodName = foo() and alias = bar() we want replace all
            // method calls 'foo()' with 'bar()'
            // However there can be a method call that starts with foo (e.g.
            // fooRight()) which should not be affected (e.g. not rename it to
            // barRight())
            // This is why we should not call 'replaceAll(methodName, alias)'
            // but 'replaceAll(methodName + "\\(", alias + "\\(")'
            resultAfterRename = resultAfterRename.replaceAll(methodName + "\\(",
                                                             alias + "\\(");
        }
        
        return resultAfterRename;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Retrieves the names of the methods that are called by the method with the
     * given name (any level deep). It updates the provided set only the names
     * of the internal methods (i.e., methods declared in the same Java file).
     * <br>
     * If the method calls itself (i.e., recursive call) the name of the method
     * itself is added to the provided set.<br>
     * If the specified method does not make an internal method call, the
     * provided set is unmodified.
     * 
     * @param methodName The name of method to retrieve the (names of the)
     *                   internal methods that it calls (any level deep)
     * @param calledMethodNames The set to update with the names of the internal
     *                          methods (their names) that are called by the
     *                          specified method
     * 
     * @throws ErrorException in case of error resolving a method call
     */
    private void updateInternalCalledMethodNames(String      methodName,
                                                 Set<String> calledMethodNames)
            throws ErrorException {
        Map<String, Set<String>> internalMethodCallLookup =
                                               getInternalCalledMethodNames();
        
        Set<String> methodNames = internalMethodCallLookup.get(methodName);
        if (methodNames != null) {
            for (String currMethodName : methodNames) {
                if (!calledMethodNames.contains(currMethodName)) {
                    calledMethodNames.add(currMethodName);
                    updateInternalCalledMethodNames(currMethodName,
                                                    calledMethodNames);
                }
            }
        }
    }
    
    /**
     * Returns a map that uses as key the name of a method that calls one or
     * more internal methods. By internal method we mean a method that is
     * declared in the same Java file. The value for that key is a set with the
     * names of those internal methods that are called.<br>
     * If a method does not make internal method calls, there is no key for it
     * (i.e., its name) in the returned map. If no method makes an internal call
     * or if there is no method in the source code, an empty map is returned. 
     * 
     * @return a map that uses as key the name of a method that calls one or
     *         more internal methods. By internal method we mean a method that
     *         is declared in the same Java file. The value for that key is a
     *         set with the names of those internal methods that are called.
     * 
     * @throws ErrorException in case of error resolving a method call
     */
    private Map<String, Set<String>> getInternalCalledMethodNames()
            throws ErrorException {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        
        // Identify first all methods that are declared in the Java file
        Set<String> methodNames = new HashSet<String>();
        for (Method method : javaFile.getMethods()) {
            methodNames.add(method.getName());
        }
        
        // Iterate again though the method and no look if there are any method
        // calls made by each method. If so, check it those method calls are
        // internal (i.e., call method whose names appear in set 'methodNames')
        for (Method method : javaFile.getMethods()) {
            Set<String> internalCalledMethodNames = new HashSet<String>();
            List<MethodCall> methodCalls = method.getMethodCalls();
            for (MethodCall methodCall : methodCalls) {
                String calledMethodName = methodCall.getName();
                if (methodNames.contains(calledMethodName)) { // Internal call
                    internalCalledMethodNames.add(calledMethodName);
                }
            }
            
            if (!internalCalledMethodNames.isEmpty()) {
                result.put(method.getName(), internalCalledMethodNames);
            }
        }
        
        return result;
    }
}
