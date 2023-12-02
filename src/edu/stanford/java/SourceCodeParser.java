/*
 * File          : SourceCodeParser.java
 * Author        : Charis Charitsis
 * Creation Date : 7 February 2019
 * Last Modified : 8 March 2021
 */
package edu.stanford.java;

// Import Java SE classes
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Import custom classes
import com.github.javaparser.ast.ImportDeclaration;
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.JavaSourceCode;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.SEMICOLON;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.constants.Literals.TAB;

/**
 * Parses Java source code.
 */
public class SourceCodeParser
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** White spaces to align the code */
    public static final String ALIGNMENT_SPACES     = TAB + TAB;
    /** Method to call when entering a method in the instrumented code */
    public static final String METHOD_ENTRY_CALL    = "methodEntry();";
    /** The signature for method 'run' */
    public static final String RUN_METHOD_SIGNATURE = "void run()";
    /** Call to method 'run' */
    public static final String RUN_METHOD_CALL      = "run()";
    /**
     * Returns the method to call when exiting a method in the instrumented code
     * 
     * @param methodName the name of the method that we are about to exit
     * 
     * @return the method call 
     */
    public static final String METHOD_EXIT_CALL(String methodName) {
        return "methodExit(\""  + methodName + "\");";
    }
    /**
     * Returns the modified method run so that it calls the provided method and
     * captures the state before and after the call.
     * 
     * @param methodName the name of the method that run() calls
     * 
     * @return the code for method run() 
     */
    public static final String MODIFIED_RUN_CODE(String methodName) {
        return TAB + "public void run() {"                     + NEW_LINE
             + ALIGNMENT_SPACES + METHOD_ENTRY_CALL            + NEW_LINE
             + ALIGNMENT_SPACES + methodName + "();"           + NEW_LINE
             + ALIGNMENT_SPACES + METHOD_EXIT_CALL(methodName) + NEW_LINE
             + TAB + "}";
    }
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The module that holds the information about the parsed source code
     */ 
    private final JavaSourceCode javaSourceCode;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new SourceCodeParser that processes/parses the given file.
     *  
     * @param sourceCodeFile The source code file to parse
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br>
     */
    public SourceCodeParser(File sourceCodeFile)
           throws ErrorException {
        if (sourceCodeFile == null) {
            throw new ErrorException("'sourceCodeFile' is null");
        }
        
        javaSourceCode = new JavaFile(sourceCodeFile.getPath());
    }
    
    /**
     * Creates a new SourceCodeParser that processes/parses the given file.
     *  
     * @param javaFile The JavaFile to use for parsing
     * 
     * @throws ErrorException if the provided Java file is {@code null} 
     */
    public SourceCodeParser(JavaFile javaFile)
           throws ErrorException {
        if (javaFile == null) {
            throw new ErrorException("'javaFile' is null");
        }
        
        javaSourceCode = javaFile;
    }
    
    /**
     * Creates a new SourceCodeParser that processes/parses the given source
     * code.
     *  
     * @param sourceCode The source code to parse
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null} or
     *                           empty<br> 
     *                        2) An error occurred while parsing the file<br>
     */
    public SourceCodeParser(String sourceCode)
           throws ErrorException {
        if (sourceCode == null || sourceCode.isEmpty()) {
            throw new ErrorException("'sourceCode' is null or empty");
        }
        
        javaSourceCode = new JavaSourceCode(sourceCode);
    }
    
    /**
     * Creates a new SourceCodeParser that processes/parses the given source
     * code.
     *  
     * @param javaSourceCode The JavaSourceCode to use for parsing
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided JavaSourceCode instance is
     *                           {@code null}<br> 
     *                        2) An error occurred while parsing the source
     *                           code<br>
     */
    public SourceCodeParser(JavaSourceCode javaSourceCode)
           throws ErrorException {
        if (javaSourceCode == null) {
            throw new ErrorException("'javaSourceCode' is null or empty");
        }
        
        this.javaSourceCode = javaSourceCode;
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Instruments the source code so that it saves the state every time the
     * method is entered and exited.<br>
     * Instrumenting the code is done by simply copying only the actual code
     * in a consistent format, adding a method call to save the state when
     * entering the method and another method call to save the state when
     * exiting the method.<br>
     * Note: Consistent format means that some rules are applied no matter how
     *       the code is written. For example, in the instrumented code, each
     *       method contains the opening '{' in the same line as the method
     *       signature (i.e., '{' is the last character in that line), does
     *       not contain comments in the same lines as code, does not include
     *       Javadoc comments etc.
     * 
     * @return the instrumented source code
     * 
     * @throws ErrorException in case of error processing the comments in the
     *                        source code (e.g. cannot determine the begin or
     *                        end line)
     */
    public String getInstrumentedSourceCode()
           throws ErrorException {
        String[]            imports      = null;
        Map<String, String> replacements = null;
        return getInstrumentedSourceCode(true,
                                         true,
                                         imports,
                                         replacements);
    }
    
    /**
     * Instruments the source code so that it saves the state every time the
     * method is entered and exited.<br>
     * Instrumenting the code is done by simply copying only the actual code
     * in a consistent format, adding a method call to save the state when
     * entering the method and another method call to save the state when
     * exiting the method.<br>
     * Note: Consistent format means that some rules are applied no matter how
     *       the code is written. For example, in the instrumented code, each
     *       method contains the opening '{' in the same line as the method
     *       signature (i.e., '{' is the last character in that line), does
     *       not contain comments in the same lines as code, does not include
     *       Javadoc comments etc.
     * 
     * @param saveStateBefore {@code true} to make a method call to save the
     *                        state when entering a method or {@code false}
     *                        otherwise 
     * @param saveStateAfter {@code true} to make a method call to save the
     *                        state when exiting a method or {@code false}
     *                        otherwise 
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements. It uses the
     *                     keys as the regular expression (can be also fixed
     *                     text) to look for and the corresponding values as
     *                     replacement to this regular expression/text.
     *                  
     * @return the instrumented source code
     * 
     * @throws ErrorException in case of error processing the comments in the
     *                        source code (e.g. cannot determine the begin or
     *                        end line)
     */
    public String getInstrumentedSourceCode(boolean             saveStateBefore,
                                            boolean             saveStateAfter,
                                            String[]            imports,
                                            Map<String, String> replacements)
           throws ErrorException {
        String sourceCode = javaSourceCode.getText();
        if (replacements != null) {
            for (String regex : replacements.keySet()) {
                sourceCode = sourceCode.replaceAll(regex,
                                                   replacements.get(regex));
            }
        }
        String[] sourceCodeLines = sourceCode.split(NEW_LINE);
        StringBuilder instrumentedCode = new StringBuilder();
        
        Iterator<Method> methodItr = javaSourceCode.getMethods().iterator();
        if (!methodItr.hasNext()) {
            throw new ErrorException("The source code does not have methods");
        }
        Method method = methodItr.next();
        String methodSignature = method != null?
                                 method.getSignature(true, true) : null;
        boolean isStatic = method.isStatic();
        
        // The import statements to add (if not null)
        StringBuilder importStatements = null;
        // The last existing import statement after which we need to insert the
        // additional import statements or null if there are no existing imports
        String lastExistingImportName = null;
        if (imports != null && imports.length > 0) {
            Set<String> importNames = new HashSet<String>(imports.length);
            for (String currImport : imports) {
                String[] tokens = currImport.split(SPACE);
                String importName = tokens[tokens.length - 1];
                if (importName.endsWith(SEMICOLON)) {
                    importName = importName.substring(0, 
                                                      importName.length() - 1);
                }
                importNames.add(importName);
            }
            
            List<ImportDeclaration> existingImports =
                                            javaSourceCode.getImports();
            for (ImportDeclaration currImport : existingImports) {
                // Remove any imports if they already exist to avoid duplicates
                importNames.remove(currImport.getNameAsString());
            }
            if (!existingImports.isEmpty()) {
                int last = existingImports.size() - 1;
                lastExistingImportName = existingImports.get(last)
                                                        .getNameAsString();
            }
            
            // Now, importNames contains all additional import statements that
            // we need to insert
            if (!importNames.isEmpty()) {
                importStatements = new StringBuilder();
                for (String importName : importNames) {
                    importStatements.append("import " + importName + SEMICOLON);
                    importStatements.append(NEW_LINE);
                }
            }
        }
        
        // It is possible that the Java file has no import statements, but it
        // has a package. In this case we should add our import statements after
        // the package declaration
        String packageName =  javaSourceCode.getPackage() == null ?
                              null : javaSourceCode.getPackage()
                                                   .getNameAsString();
        
        for (int i = 0; i < sourceCodeLines.length; i++) {
            String codeLine = sourceCodeLines[i];
            
            if (importStatements != null) {
                // Determine where to add any additional import statements
                if (lastExistingImportName != null) {
                    if (codeLine.startsWith("import " + lastExistingImportName))
                    {
                        // Add the additional import statements here
                        codeLine += NEW_LINE + importStatements;
                        importStatements = null; // We are done
                    }
                }
                else { // There are no existing import statements
                    if (packageName != null) { // There is a package declaration
                        if (codeLine.startsWith("package " + packageName)) {
                            // Add the additional import statements here
                            codeLine += NEW_LINE + importStatements;
                            importStatements = null; // We are done
                        }
                    }
                    else { // There is no package declaration
                        // Add the additional import statements before this line
                        codeLine = importStatements + codeLine;
                        importStatements = null; // We are done
                    }
                }
            }
            
            // Add an instrumentation line at the beginning and in the end
            // of every method
            if (methodSignature != null            &&
                codeLine.contains(methodSignature) &&
                !codeLine.trim().startsWith("//")) { // Not commented code '//'
                int numOfMethodLines = method.getNumOfNonEmptyBodyLines();
                
                // Methods that are static are not instrumented because the
                // 'methodEntry()' and 'methodExit()' are non-static and this
                // would cause a compile error
                boolean instrumentation = !isStatic && numOfMethodLines > 2;
                
                instrumentedCode.append(codeLine + NEW_LINE);
                int remainingLines = numOfMethodLines - 1;
                if (instrumentation && saveStateBefore) {
                    if (!codeLine.endsWith("{")) {
                        i++;
                        instrumentedCode.append(sourceCodeLines[i] + NEW_LINE);
                        remainingLines--;
                    }
                    instrumentedCode.append(ALIGNMENT_SPACES + METHOD_ENTRY_CALL
                                          + NEW_LINE);
                }
                
                if (method.isVoid()) {
                    while (remainingLines > 0) {
                        if (remainingLines == 1 && instrumentation
                                                && saveStateAfter) {
                            String methodName = method.getName();
                            instrumentedCode.append(ALIGNMENT_SPACES
                                                  + METHOD_EXIT_CALL(methodName)
                                                  + NEW_LINE);
                        }
                        i++;
                        codeLine = sourceCodeLines[i];
                        if (codeLine.trim().startsWith("while (true)")) {
                            instrumentation = false;
                        }
                        instrumentedCode.append(codeLine + NEW_LINE);
                        remainingLines--;
                    }
                }
                else {
                    while (remainingLines > 0) { // The last line is 'return'
                        i++;
                        codeLine = sourceCodeLines[i];
                        if (codeLine.trim().startsWith("while (true)")) {
                            instrumentation = false;
                        }
                        
                        if (codeLine.trim().startsWith("return") &&
                            instrumentation && saveStateAfter){
                            String methodName = method.getName();
                            
                            // Corner case 1: if(condition) return value;
                            // Corner case 2: else return value;
                            //
                            // In this case we need to enclose the method call
                            // and the return within '{ ... }'
                            // Corner case 1: if(condition) {
                            //                     methodExit("method-name");
                            //                     return value;
                            //                }
                            // Corner case 2: else {
                            //                     methodExit("method-name");
                            //                     return value;
                            //                }
                            //
                            // Note: Similarly for 'while' although
                            //         while(condition) return value;
                            //       is bad style (use 'if' instead of 'while')
                            // Actually there are many more corner cases
                            // The simplest solution is to enclose always the
                            // 'methodExit()' and the 'return' statement in a
                            // block:
                            //     {
                            //         methodExit("method-name");
                            //         return value;
                            //     }
                            instrumentedCode.append(
                                 "{" + NEW_LINE + ALIGNMENT_SPACES +
                                     METHOD_EXIT_CALL(methodName)  + NEW_LINE +
                                     codeLine   + NEW_LINE +
                                 "}" + NEW_LINE);
                        }
                        else {
                            instrumentedCode.append(codeLine + NEW_LINE);
                        }
                        remainingLines--;
                    }
                }
                
                method = methodItr.hasNext() ? methodItr.next() : null;
                if (method != null) {
                    methodSignature = method.getSignature(true, true);
                    isStatic = method.isStatic();
                }
                else {
                    methodSignature = null;
                }
            }
            else {
               instrumentedCode.append(codeLine + NEW_LINE);
            }
        }
        
        return instrumentedCode.toString();
    }
    
    /**
     * Instruments the source code so that it calls one time the method with the
     * given name.<br>
     * It does so by having method run() call the specified method once and
     * capture the state just before calling and just after returning from the
     * specified method.<br>
     * Example:<br>
     * If we want to monitor the states when a method executes assuming that the
     * pre-condition has been applied we modify the body of method run to:<br>
     * <pre>
     *     public void run() {
     *         methodEntry();     // Save the state before calling foo()
     *         foo();
     *         methodExit("foo"); // Save the state after returning from foo()
     *     }
     * </pre>
     * The original run() method is renamed to run1() (or run2() if run1()
     * exists in the code etc.). This is because in rare cases the method that
     * we want to test may call run() in which case it should call not our
     * modified run() but run1().
     * 
     * Instrumenting the code is done substituting methods run() with the one
     * just mentioned, renaming the original run() to run1() (also update any
     * calls to that) and by simply copying only the actual code in a consistent
     * format all other methods.<br>
     * Note: Consistent format means that some rules are applied no matter how
     *       the code is written. For example, in the instrumented code, each
     *       method contains the opening '{' in the same line as the method
     *       signature (i.e., '{' is the last character in that line), does
     *       not contain comments in the same lines as code, does not include
     *       Javadoc comments etc.
     * 
     * @param methodName The method name to instrument the code around (so that
     *                   it is executed once and we can save the state before
     *                   and after the execution)
     * 
     * @return the instrumented source code adjusted to execute the specified
     *         method one time
     * 
     * @throws ErrorException in case of error processing the comments in the
     *                        source code (e.g. cannot determine the begin or
     *                        end line)
     */
    public String getInstrumentedSourceCode(String methodName)
           throws ErrorException {
        String[] sourceCodeLines = javaSourceCode.getText().split(NEW_LINE);
        StringBuilder instrumentedCode = new StringBuilder();
        
        List<Method> methods = javaSourceCode.getMethods();
        String runAlias = null; // The new name for the original method run()
        for (int number = 1; number < Integer.MAX_VALUE; number++) {
            runAlias = "run" + number;
            for (Method method : methods) {
                if (method.getName().equals(runAlias)) {
                    runAlias = null;
                    break;
                }
            }
            
            if (runAlias != null) {
               break;
            }
        }
        
        boolean methodRunIsAdded = false;
        for (int i = 0; i < sourceCodeLines.length; i++) {
            String codeLine = sourceCodeLines[i];
            
            // Replace method run() with the modified code that calls our
            // desired method. Also rename the original run() to run1()
            if (codeLine.contains(RUN_METHOD_SIGNATURE)) {
                if (!methodRunIsAdded) {
                    instrumentedCode.append(MODIFIED_RUN_CODE(methodName)
                                          + NEW_LINE + NEW_LINE);
                    methodRunIsAdded = true;
                }
                codeLine = codeLine.replace("run", runAlias);
            }
            else if (codeLine.contains(RUN_METHOD_CALL)) {
                codeLine = codeLine.replace(RUN_METHOD_CALL, runAlias + "()");
            }
            
            instrumentedCode.append(codeLine + NEW_LINE);
        }
        
        return instrumentedCode.toString();
    }
    
    /**
     * Instruments the source code so that it saves the state every time the
     * method is entered and exited.<br>
     * Instrumenting the code is done by simply copying only the actual code
     * in a consistent format, adding a method call to save the state when
     * entering the method and another method call to save the state when
     * exiting the method.<br>
     * Moreover, it looks in the source for method with the given name and
     * replaces its body with the provided body.<br>
     * Note: Consistent format means that some rules are applied no matter how
     *       the code is written. For example, in the instrumented code, each
     *       method contains the opening '{' in the same line as the method
     *       signature (i.e., '{' is the last character in that line), does
     *       not contain comments in the same lineas code, does not include
     *       Javadoc comments etc.
     * 
     * @param methodName The name of the method to replace its body
     * @param newMethodBody The method body to include in the returned, modified
     *                      source code
     * 
     * @return the instrumented source code
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The method name is {@code null}
     *                        2) The method body to use as replacement is
     *                           {@code null}
     *                        3) There exists no method with the given name
     *                        4) There is an error processing the comments in
     *                           the source code (e.g. cannot determine the
     *                           begin or end line)
     */
    public String getInstrumentedSourceCode(String methodName,
                                            String newMethodBody)
           throws ErrorException {
        if (methodName == null) {
            throw new ErrorException("The 'methodName' is null");
        }
        if (newMethodBody == null) {
            throw new ErrorException("The 'newMethodBody' is null");
        }
        
        String[] sourceCodeLines = javaSourceCode.getText().split(NEW_LINE);
        StringBuilder instrumentedCode = new StringBuilder();
        
        Iterator<Method> methodItr = javaSourceCode.getMethods().iterator();
        Method method = methodItr.hasNext() ? methodItr.next() : null;
        String methodSignature = method != null?
                                 method.getSignature(true, true) : null;
        
        for (int i = 0; i < sourceCodeLines.length; i++) {
            String codeLine = sourceCodeLines[i];
            
            // Add an instrumentation line at the beginning and in the end
            // of every method
            if (methodSignature != null && codeLine.contains(methodSignature)) {
                int numOfMethodLines = method.getNumOfNonEmptyBodyLines();
                
                boolean instrumentation = numOfMethodLines > 2;
                instrumentedCode.append(codeLine + NEW_LINE);
                
                String currMethodName = method.getName();
                
                if (methodName.equals(currMethodName)) {
                    String[] bodyLines = newMethodBody.split(NEW_LINE);
                    // The first body line is "{" which we already included
                    // in the previous line (i.e., method signature line)
                    for (int j = 1; j < bodyLines.length - 1; j++) {
                        instrumentedCode.append(TAB + bodyLines[j] + NEW_LINE);
                    }
                    instrumentedCode.append(TAB
                                          + bodyLines[bodyLines.length - 1]
                                          + NEW_LINE);
                    i += numOfMethodLines - 1;
                }
                else {
                    if (instrumentation) {
                        instrumentedCode.append(ALIGNMENT_SPACES
                                              + METHOD_ENTRY_CALL + NEW_LINE);
                    }
                    int remainingLines = numOfMethodLines - 1;
                    while (remainingLines > 0) {
                        if (remainingLines == 1 && instrumentation) {
                            instrumentedCode.append(ALIGNMENT_SPACES
                                                  + METHOD_EXIT_CALL(
                                                                currMethodName) 
                                                  + NEW_LINE);
                        }
                        i++;
                        instrumentedCode.append(sourceCodeLines[i] + NEW_LINE);
                        remainingLines--;
                    }
                }
                
                method = methodItr.hasNext() ? methodItr.next() : null;
                if (method != null) {
                    methodSignature = method.getSignature(true, true);
                }
                else {
                    methodSignature = null;
                }
            }
            else {
               instrumentedCode.append(codeLine + NEW_LINE);
            }
        }
        
        return instrumentedCode.toString();
    }
    
    // ------------------------------------------------------------------ //
    //  P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S  //
    // ------------------------------------------------------------------ //
    /**
     * @return a list with the method names that exist in the source code
     */
    protected List<String> getMethodNames() {
        List<String> methodNames = new LinkedList<String>();
        
        List<Method> methods = javaSourceCode.getMethods();
        for (Method method : methods) {
            methodNames.add(method.getName());
        }
        
        return methodNames;
    }
    
    /**
     * Given the signature of a method it returns that method or {@code null} if
     * there is no method with that signature in the code.<br>
     * Note: The method signature should not contain the return type.
     * 
     * @param signature The method signature
     * 
     * @return the method with the given signature or {@code null} if there is
     *         no method with the given signature
     */
    protected Method getMethod(String signature) {
        List<Method> methods = javaSourceCode.getMethods();
        
        for (Method method : methods) {
            if (signature.equals(method.getSignature(false, true))) {
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * Given a method it returns the method source code.<br>
     * 
     * @param method The method to get its source code
     * 
     * @return the method source code
     */
    protected String getMethodCode(Method method) {
        return method.toString() + NEW_LINE + method.getStrippedBody();
    }
    
    /**
     * Returns a map for the method calls. The map uses as key a method in the
     * source code and as value the set of methods that are calling this method
     * or an empty list if this method is not called by another method.
     * 
     * @return a map where the key is a method in the source code and the value
     *         is a set of methods that are calling this method or an empty
     *         list if this method is not called by another method
     * 
     * @throws ErrorException in case of an error resolving a method call or
     *                        determining the argument types of the method that
     *                        is being called
     */
    protected Map<Method, Set<Method>> getMethodCalleeMap()
              throws ErrorException {
        Set<Method> methods = new HashSet<Method>();
        for (Method method : javaSourceCode.getMethods()) {
            methods.add(method);
        }
        
        // Key  : Method that is called
        // Value: Set of methods that call this method
        Map<Method, Set<Method>> methodCalleeMap = 
                         new HashMap<Method, Set<Method>>(methods.size());
        
        for (Method method : methods) {
            // Find all methods that are called by this method
            List<MethodCall> methodCalls = method.getMethodCalls();
            
            if (methodCalls != null) {
                // Iterate through all called methods and filter out the methods
                // whose source code is not in the file that is parsed
                for (MethodCall methodCall : methodCalls) {
                    String methodCalleeSignature =
                                 methodCall.getCalledMethodSignature(true);
                    
                    Method methodCallee = getMethod(methodCalleeSignature);
                    if (methodCallee != null) {
                        // The callee is null if there is a call to a method
                        // that is not in the given source code (e.g. call to an
                        // inherited method)
                        Set<Method> callerMethods;
                        if (methodCalleeMap.containsKey(methodCallee)) {
                            callerMethods = methodCalleeMap.get(methodCallee);
                        }
                        else {
                            callerMethods = new HashSet<Method>();
                            methodCalleeMap.put(methodCallee, callerMethods);
                        }
                        callerMethods.add(method);
                    }
                }
            }
        }
        
        // At this pointed methodCalleeMap has the methods that are called as
        // keys and as values the methods that are calling them
        return methodCalleeMap;
    }
}
