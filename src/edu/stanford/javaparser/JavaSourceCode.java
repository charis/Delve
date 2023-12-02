/*
 * File          : JavaSourceCode.java
 * Author        : Charis Charitsis
 * Creation Date : 21 November 2020
 * Last Modified : 19 September 2021
 */
package edu.stanford.javaparser;

// Import Java SE classes
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
// Import com.github.javaparser classes
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.exception.WarningException;
import edu.stanford.javaparser.ast.visitor.ClassAndInterfaceVisitor;
import edu.stanford.javaparser.ast.visitor.ConstructorVisitor;
import edu.stanford.javaparser.ast.visitor.EnumVisitor;
import edu.stanford.javaparser.ast.visitor.MethodVisitor;
import edu.stanford.javaparser.ast.visitor.FieldVisitor;
import edu.stanford.javaparser.body.ClassOrInterface;
import edu.stanford.javaparser.body.Comment;
import edu.stanford.javaparser.body.Constructor;
import edu.stanford.javaparser.body.Field;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
import edu.stanford.javaparser.body.Variable;
import edu.stanford.util.io.FileIOUtil;
import edu.stanford.util.text.StringUtils;
// Import constants
import static edu.stanford.constants.Literals.COLON;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.javaparser.Constants.JDK15_CLASSNAMES;
import static edu.stanford.javaparser.Constants.RESOLVE_JDK_CALLS;
import static com.github.javaparser.ast.Node.SYMBOL_RESOLVER_KEY;

/**
 * <pre>
 * Place holder with the following information after parsing the source code:
 * - The classes that are declared in the source code
 * - The interfaces that are declared in the source code
 * - The enumerations that are declared in the source code
 * - The constructors that are declared in the source code
 * - The methods that are declared in the source code
 * - The variable declarations (both class and instance) as well as the constant
 *   declarations in the source code
 * - The comments in the source code
 * </pre>
 */
public class JavaSourceCode // implements Comparable<? extends JavaSourceCode>
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /**
     * A container for type solvers. All solving is done by the contained type
     * solvers.
     * (this is helpful when an API asks for a single type solver, but we need 
     * several)
     */
    private static final CombinedTypeSolver COMBINED_TYPE_RESOLVER =
                                                     new CombinedTypeSolver();
    static {
        COMBINED_TYPE_RESOLVER.add(new ReflectionTypeSolver(false));
    }
    /** Module used for Java symbol resolution */
    public static final JavaSymbolSolver SYMBOL_RESOLVER =
                                  new JavaSymbolSolver(COMBINED_TYPE_RESOLVER);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The original source code to parse or {@code null} if the source code is
     * in a file.
     */
    private final String                  originalCode;
    /**
     * The file with the original source code to parse or {@code null} if the
     * source code is not in a file.
     */
    private final File                    sourceFile;
    /**
     * Jar files to include in the symbol resolution/file compilation or
     * {@code null} if there are no jar files to include.
     */
    private final List<File>              jarFiles;
    /** The compilation unit for this file */
    private final CompilationUnit         compilationUnit;
    /**
     * List with the comments in the source code
     */
    private final List<Comment>           comments;
    /** The total number of lines */
    private final int                     numOfLines;
    /** The source code package or {@code null} if there is no package */
    private final PackageDeclaration      sourceCodePackage;
    /** List with the import declarations in the source code */
    private final List<ImportDeclaration> imports;
    /** List with the classes that are declared in the source code */
    private final List<ClassOrInterface>  classes;
    /** List with the interfaces that are declared in the source code */
    private final List<ClassOrInterface>  interfaces;
    /** List with the enumerations that are declared in the source code */
    private final List<EnumDeclaration>   enums;
    /** List with the constructors that are declared in the source code */
    private final List<Constructor>       constructors;
    /** List with the methods */
    private final List<Method>            methods;
    /**
     * List with the class variables in the source code.<br>
     * For example the field declaration 'static int instanceCount;' involves
     * a class variables 'instanceCount'
     */
    private final List<Variable>          classVariables;
    /**
     * List with the instance variables in the source code.<br>
     * For example the field declaration 'int i, j;' involves two instance
     * variables 'i' and 'j'
     */    
    private final List<Variable>          instanceVariables;
    /**
     * List with the final variables in the source code.<br>
     * For example the field declaration 'final double PI = 3.14;' involves
     * a final variable 'PI'.
     */
    private final List<Variable>          finalVariables;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new JavaFile from the provided source code.<br>
     * It parses the code and extracts the most important information:<br>
     * <pre>
     * - The classes that are declared in the source code
     * - The interfaces that are declared in the source code
     * - The enumerations that are declared in the source code
     * - The constructors that are declared in the source code
     * - The methods that are declared in the source code
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the source code
     * - The comments that exist in the source code
     * </pre>
     *   
     * @param sourceCode The source code to parse
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null}<br> 
     *                        2) An error occurred while parsing the source code
     */
    public JavaSourceCode(String sourceCode)
           throws ErrorException {
        this(sourceCode, null, null);
    }
    
    /**
     * Constructs a new JavaSourceCode from the provided source code.<br>
     * It parses the code and extracts the most important information:<br>
     * <pre>
     * - The classes that are declared in the source code
     * - The interfaces that are declared in the source code
     * - The enumerations that are declared in the source code
     * - The constructors that are declared in the source code
     * - The methods that are declared in the source code
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the source code
     * - The comments that exist in the source code
     * </pre>
     *   
     * @param originalCode The original source code to parse
     * @param sourceCodeDirs Directories with source code to include in the
     *                       symbol resolution or {@code null} to not include
     *                       such directories.<br>
     *                       Note: The order matters. Directories that appear
     *                             first in the list are added first in the
     *                             classpath. {@code sourceCodeDirs} are added
     *                             before {@code jarFiles}
     * @param jarFiles Jar files to include in the symbol resolution or
     *                 {@code null} if there are no jar files to include.<br>
     *                 Note: The order matters. Files that appear first in
     *                       the list are added first in the classpath.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null} or
     *                            empty<br> 
     *                        2) An error occurred while parsing the source code
     */
    public JavaSourceCode(String     originalCode,
                          List<File> sourceCodeDirs,
                          List<File> jarFiles)
           throws ErrorException {
        this(originalCode,
             null, // sourceFile
             sourceCodeDirs,
             jarFiles);
    }
    
    /**
     * Constructs a new JavaSourceCode from the given Java file.<br>
     * It iterates over the contents of the file and extracts the most important
     * information:<br>
     * <pre>
     * - The classes that are declared in the source code
     * - The interfaces that are declared in the source code
     * - The enumerations that are declared in the source code
     * - The constructors that are declared in the source code
     * - The methods that are declared in the source code
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the source code
     * - The comments that exist in the source code.
     * </pre>
     *   
     * @param file The Java file
     * @param sourceCodeDirs Directories with source code to include in the
     *                       symbol resolution or {@code null} to not include
     *                       such directories.<br>
     *                       Note: The order matters. Directories that appear
     *                             first in the list are added first in the
     *                             classpath. {@code sourceCodeDirs} are added
     *                             before {@code jarFiles}
     * @param jarFiles Jar files to include in the symbol resolution or
     *                 {@code null} if there are no jar files to include.<br>
     *                 Note: The order matters. Files that appear first in
     *                       the list are added first in the classpath.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path name is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br>
     */
    public JavaSourceCode(File       file,
                          List<File> sourceCodeDirs,
                          List<File> jarFiles)
           throws ErrorException {
        this(null, // originalCode
             file, // sourceFile
             sourceCodeDirs,
             jarFiles);
    }
    
    /**
     * Constructs a new JavaSourceCode from the provided source code.<br>
     * It parses the code and extracts the most important information:<br>
     * <pre>
     * - The classes that are declared in the source code
     * - The interfaces that are declared in the source code
     * - The enumerations that are declared in the source code
     * - The constructors that are declared in the source code
     * - The methods that are declared in the source code
     * - The variable declarations (both class and instance) as well as the
     *   constant declarations in the source code
     * - The comments that exist in the source code
     * </pre>
     *   
     * @param originalCode The original source code to parse or {@code null} if
     *                     the source code file is provided instead
     * @param sourceFile The source code file to parse or {@code null} if the
     *                   source code is provided instead
     * @param sourceCodeDirs Directories with source code to include in the
     *                       symbol resolution or {@code null} to not include
     *                       such directories.<br>
     *                       Note: The order matters. Directories that appear
     *                             first in the list are added first in the
     *                             classpath. {@code sourceCodeDirs} are added
     *                             before {@code jarFiles}
     * @param jarFiles Jar files to include in the symbol resolution or
     *                 {@code null} if there are no jar files to include.<br>
     *                 Note: The order matters. Files that appear first in
     *                       the list are added first in the classpath.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null} or
     *                            empty<br> 
     *                        2) An error occurred while parsing the source code
     */
    private JavaSourceCode(String     originalCode,
                           File       sourceFile,
                           List<File> sourceCodeDirs,
                           List<File> jarFiles)
           throws ErrorException {
        if (sourceFile == null) {
            if (originalCode == null) {
                throw new ErrorException("The source code to process is null");
            }
            else if (originalCode.trim().isEmpty()){
                throw new ErrorException("'originalCode' is empty");
            }
            
            InputStream in = new ByteArrayInputStream(originalCode.getBytes());
            try {
                compilationUnit = JavaParser.parse(in);
            }
            catch (ParseProblemException ppe) {
                throw new ErrorException("Error parsing the source code:"
                                       + NEW_LINE + originalCode + ". Details:"
                                       + NEW_LINE + ppe.getMessage());
            }
        }
        else {
            try (InputStream in = new FileInputStream(sourceFile)) {
                compilationUnit = JavaParser.parse(in);
            }
            catch (FileNotFoundException fnfe) {
                throw new ErrorException("File '" + sourceFile.getPath()
                                       + "' not found");
            }
            catch (ParseProblemException | IOException e) {
                throw new ErrorException("Error parsing file '"
                                       + sourceFile.getPath() + "'. Details:"
                                       + NEW_LINE + e.getMessage());
            }
        }
        
        this.originalCode = originalCode;
        this.sourceFile   = sourceFile;
        this.jarFiles     = jarFiles;
        
        // Configure the symbol solver
        setSymbolSolver(sourceCodeDirs, jarFiles);
        
        // Total number of lines = end line - begin line
        Position beginPos = compilationUnit.getBegin().get();
        Position endPos = compilationUnit.getEnd().get();
        numOfLines = endPos.line - beginPos.line + 1;
        
        sourceCodePackage = compilationUnit.getPackageDeclaration().isEmpty() ?
                            null: compilationUnit.getPackageDeclaration().get();
        imports = new ArrayList<ImportDeclaration>();
        List<ImportDeclaration> importsTemp = compilationUnit.getImports();
        if (importsTemp != null) {
            imports.addAll(importsTemp);
        }
        
        classes    = new ArrayList<ClassOrInterface>();
        interfaces = new ArrayList<ClassOrInterface>();
        try {
            visitClassesAndInterfaces();
        }
        catch (ErrorException ee) { // Should never happen
            new WarningException(ee.getMessage());
        }
        
        constructors = new ArrayList<Constructor>();
        visitConstructors();
        
        methods = new ArrayList<Method>();
        visitMethods();
        
        classVariables    = new ArrayList<Variable>();
        instanceVariables = new ArrayList<Variable>();
        finalVariables    = new ArrayList<Variable>();
        visitFields(); // instance variables + class variables + constants
        
        enums = new ArrayList<EnumDeclaration>();
        visitEnums();
        
        List<com.github.javaparser.ast.comments.Comment> commentInfos =
                                                compilationUnit.getComments();
        
        comments = new ArrayList<Comment>(commentInfos.size());
        for (com.github.javaparser.ast.comments.Comment commentInfo :
                                                               commentInfos) {
            try {
                comments.add(new Comment(commentInfo));
            }
            catch (ErrorException ee) { // Should never happen
                new WarningException(ee.getMessage());
            }
        }
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Configures the module that is responsible for the symbol resolution.
     * 
     * @param sourceCodeDirs Directories with source code to include in the
     *                       symbol resolution or {@code null} to not include
     *                       such directories.<br>
     *                       Note: The order matters. Directories that appear
     *                             first in the list are added first in the
     *                             classpath. {@code sourceCodeDirs} are added
     *                             before {@code jarFiles}
     * @param jarFiles Jar files to include in the symbol resolution or
     *                 {@code null} if there are no jar files to include.<br>
     *                 Note: The order matters. Files that appear first in
     *                       the list are added first in the classpath.
     * 
     * @throws ErrorException in case of an error adding the jar file to the
     *                        existing symbol resolvers 
     */
    public void setSymbolSolver(List<File> sourceCodeDirs,
                                List<File> jarFiles)
           throws ErrorException {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(true));
        if (sourceCodeDirs != null) {
            for (File sourceCodeDir : sourceCodeDirs) {
                addJavaCodeSymbolSolver(typeSolver, sourceCodeDir);
            }
        }
        
        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                if (jarFile.getName().toLowerCase().endsWith(".jar")) {
                    addJarSymbolSolver(typeSolver, jarFile);
                }
            }
        }
        
        JavaSymbolSolver symbolResolver = new JavaSymbolSolver(typeSolver);
        compilationUnit.setData(SYMBOL_RESOLVER_KEY, symbolResolver);
    }
    
    /**
     * @return the total number of lines
     */
    public int getNumOfLines() {
        return numOfLines;
    }
    
    /**
     * Returns the number of pure code lines.<br>
     * To handle different coding styles such so that:<br>
     * <pre>
     *     for (int i = 0;
     *              i {@literal <} numberOfLines;
     *              i++) {
     *         etc.
     * </pre>
     * and<br>
     * <pre>
     *     for (int i = 0; i {@literal <} numberOfLines; i++) {
     *         etc.
     * </pre>
     * return the same result, the total number of code lines is equals to the
     * number of appearances of ';' or '}' anywhere in the source code but the
     * comments.
     *  
     * @return the total number of code lines which is equal to the number of
     *         appearances of ';' or '}' anywhere in the source code but the
     *         comments
     */
    public int getNumOfCodeLines() {
        int numOfCommentedOutCodeLines = getNumOfCommentedOutCodeLines();
        
        int totalNumOfLines = 0;
        String text = getText();
        totalNumOfLines += StringUtils.countMatches(text, ";");
        totalNumOfLines += StringUtils.countMatches(text, "}");
        
        return totalNumOfLines - numOfCommentedOutCodeLines;
    }
    
    /**
     * @return the total number of commented out code lines which is the number
     *         of appearances of ';' and '}' anywhere in the comments
     */
    public int getNumOfCommentedOutCodeLines() {
        int numOfCommentedOutCodeLines = 0;
        List<Comment> comments = getComments();
        for (Comment comment : comments) {
            String text = comment.getText();
            numOfCommentedOutCodeLines += StringUtils.countMatches(text, ";");
            numOfCommentedOutCodeLines += StringUtils.countMatches(text, "}");
        }
        
        return numOfCommentedOutCodeLines;
    }
    
    /**
     * @return the package for the source code or {@code null} if there is no
     *         package
     */
    public PackageDeclaration getPackage() {
        return sourceCodePackage;
    }
    
    /**
     * @return a list with the import declarations in the source code which can
     *         be empty if there are no import declarations
     */
    public List<ImportDeclaration> getImports() {
        return imports;
    }
    
    /**
     * @return a list with the classes that are declared in the source code
     *         which can be empty if there are no classes
     */
    public List<ClassOrInterface> getClasses() {
        return classes;
    }
    
    /**
     * @return the first class that is declared in the source code or
     *         {@code null} if there is no class declaration
     */
    public ClassOrInterface getFirstClass() {
        ClassOrInterface firstClass = null;
        if (!classes.isEmpty()) {
            int beginLine = Integer.MAX_VALUE;
            for (ClassOrInterface currClass : classes){
                if (currClass.getBeginLine() < beginLine) {
                    firstClass = currClass;
                    beginLine = currClass.getBeginLine();
                }
            }
        }
        
        return firstClass;
    }
    
    /**
     * @return the first interface that is declared in the source code or
     *         {@code null} if there is no interface declaration
     */
    public ClassOrInterface getFirstInterface() {
        ClassOrInterface firstInterface = null;
        if (!interfaces.isEmpty()) {
            int beginLine = Integer.MAX_VALUE;
            for (ClassOrInterface currInterface : interfaces){
                if (currInterface.getBeginLine() < beginLine) {
                    firstInterface = currInterface;
                    beginLine = currInterface.getBeginLine();
                }
            }
        }
        
        return firstInterface;
    }
    
    /**
     * @return a list with the interfaces that are declared in the source code
     *         which can be empty if there are no interfaces
     */
    public List<ClassOrInterface> getInterfaces() {
        return interfaces;
    }
    
    /**
     * @return a list with the constructors that are declared in the source code
     *         which can be empty if there are no constructors
     */
    public List<Constructor> getConstructors() {
        return constructors;
    }
    
    /**
     * @return a list with the methods that are declared in the source code
     *         which can be empty if there are no methods
     */
    public List<Method> getMethods() {
        return methods;
    }
    
    /**
     * @return the number of methods that are declared in the source code
     */
    public int getNumOfMethods() {
        return methods.size();
    }
    
    /**
     * @return the main method (i.e., 'public static void main(String[])') or
     *         {@code false} otherwise
     */
    public Method getMainMethod() {
        for (Method method : methods) {
            if (method.isMain()) {
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * @return {@code true} if this source code contains method
     *         'public static void main(String[])' or {@code false} otherwise
     */
    public boolean containsMain() {
        return getMainMethod() != null;
    }
    
    /**
     * Returns a list with the methods calls in the source code. If there are no
     * method calls, it returns an empty list.<br>
     * The list is sorted by the order the method calls appear in the code.
     * 
     * 
     * @return a list with the methods calls in the source code which can be
     *         empty if there are no method calls
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The declaration corresponding to a method
     *                           call expression could not be resolved<br>
     *                        2) The scope of a method call cannot be
     *                           determined<br>
     *                        3) The begin or end line of a method call cannot
     *                           be determined
     */
    public List<MethodCall> getMethodCalls()
           throws ErrorException {
        List<MethodCallExpr> methodCallExpressions =
                             compilationUnit.findAll(MethodCallExpr.class);
        List<MethodCall> methodCalls =
                   new ArrayList<MethodCall>(methodCallExpressions.size());
        for (MethodCallExpr methodCallExpr : methodCallExpressions) {
            try {
                methodCalls.add(new MethodCall(methodCallExpr));
            }
            catch (RuntimeException | ErrorException e) {
                String errorMsg = e.getMessage();
                boolean throwException = false;
                if (errorMsg.contains("NotFoundException") ||
                    errorMsg.contains("Unsolved symbol")) {
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
    
    /**
     * @return a list with the enumerations that are declared in the source code
     *         which can be empty if there are no enumerations
     */
    public List<EnumDeclaration> getEnums() {
        return enums;
    }
    
    /**
     * Returns a list with the class variables in the source code.<br>
     * For example the field declaration 'static int instanceCount;' involves
     * the class variable 'instanceCount'.<br>
     * If there are no class variables, it returns an empty list.
     * 
     * @return a list with the class variables in the source code which can be
     *         empty if there are no class variables
     */
    public List<Variable> getClassVariables() {
        return classVariables;
    }
    
    /**
     * Returns a list with the instance variables in the source code.<br>
     * For example the field declaration 'int i, j;' involves two instance
     * variables 'i' and 'j'.<br>
     * If there are no instance variables, it returns an empty list.
     * 
     * @return a list with the instance variables in the source code which can
     *         be empty if there are no instance variables
     */
    public List<Variable> getInstanceVariables() {
        return instanceVariables;
    }
    
    /**
     * Returns a list with the final variables in the source code.<br>
     * For example the field declaration 'final double PI = 3.14;' involves
     * a final variable 'PI'. <br>
     * If there are no final variables, it returns an empty list.
     * 
     * @return a list with the final variables in the Java file which can be
     *         empty if there are no final variables
     */
    public List<Variable> getFinalVariables() {
        return finalVariables;
    }
    
    /**
     * @return a list with the comments in the source code which can be empty if
     *         there are no class comments
     */
    public List<Comment> getComments() {
        return comments;
    }
    
    /**
     * The text in the source code after processing it so that two identical
     * files with different styles do not appear different.<br>
     * If a statement is very long it may consist of multiple text lines or a
     * single line depending on the style/preferences of the person who wrote
     * the code.<br>
     * To deal with that a rule can be applied so that a statement is using a
     * single line (i.e., concatenate the text if the statement extends in
     * multiple lines).<br>
     * Example:<br>
     * The text that is returned from the following for-loop statement<br>
     * <pre>
     *     for (int i = 0;
     *              i {@literal <} numberOfLines;
     *              i++) {
     *         etc.
     * </pre>
     * is:<br>
     * <pre>
     *     for (int i = 0; i {@literal <} numberOfLines; i++) {
     *         etc.
     * </pre>
     * 
     * @return the text in the source code after some basic processing so that
     *         the same coding style is applied to the original text
     */
    public String getText() {
        return compilationUnit.toString();
    }
    
    /**
     * @return the original source code
     */
    public String getOriginalCode() {
        if (originalCode != null) { // The code is coming from originalCode
            return originalCode;
        }
        else { // The code is coming from sourceFile
            try {
                return FileIOUtil.readFile(sourceFile, false);
            }
            catch (ErrorException ee) {
                throw new RuntimeException(ee.getMessage());
            }
        }
    }
    
    /**
     * @return the jar files to compile the file or {@code null} if there are
     *         no jar files required to compile the file
     */
    public List<File> getJarFiles() {
        return jarFiles;
    }
    
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
     *      iii) 'obj' has the same source code name as 'this'
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
        // If that object (obj) is not a JavaSourceCode object return false
        if (!(obj instanceof JavaSourceCode)) {
            return false;
        }
        
        JavaSourceCode other = (JavaSourceCode) obj; // Now can cast safely
        
        // Make sure that both JavaSourceCode instances have the same normalized
        // source code
        if (!getText().equals(other.getText())) {
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
     * To calculate the hash code it takes into consideration hash code of the
     * normalized source code.<br>
     * The normalized source code is the original source code after processing
     * it to that it is style-independent.
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        return getText().hashCode();
    }
    
    // --------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E    D      M   E   T   H   O   D   S   //
    // --------------------------------------------------------------------- //
    /**
     * Visits the classes and interfaces of the source code
     * 
     * @throws ErrorException in case the begin or end line of a class or
     *                        interface cannot be determined
     */
    protected void visitClassesAndInterfaces()
              throws ErrorException {
        @SuppressWarnings("rawtypes")
        ClassAndInterfaceVisitor<?> classAndInterfaceVisitor =
                                            new ClassAndInterfaceVisitor();
        classAndInterfaceVisitor.visit(compilationUnit, null);
        List<ClassOrInterfaceDeclaration> classDeclarations =
                                       classAndInterfaceVisitor.getClasses();
        for (ClassOrInterfaceDeclaration classDeclaration : classDeclarations) {
            classes.add(new ClassOrInterface(classDeclaration));
        }
        List<ClassOrInterfaceDeclaration> interfaceDeclarations =
                                       classAndInterfaceVisitor.getInterfaces();
        for (ClassOrInterfaceDeclaration interfaceDeclaration :
                                                  interfaceDeclarations) {
            interfaces.add(new ClassOrInterface(interfaceDeclaration));
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Visits the constructors of the source code
     */
    private void visitConstructors() {
        @SuppressWarnings("rawtypes")
        ConstructorVisitor<?> constructorVisitor = new ConstructorVisitor();
        constructorVisitor.visit(compilationUnit, null);
        constructors.addAll(constructorVisitor.getConstructors());
    }
    
    /**
     * Visits the methods of the source code
     */
    private void visitMethods() {
        @SuppressWarnings("rawtypes")
        MethodVisitor<?> methodVisitor = new MethodVisitor();
        methodVisitor.visit(compilationUnit, null);
        methods.addAll(methodVisitor.getMethods());
    }
    
    /**
     * Visits the variables and constants of the source code
     */
    private void visitFields() {
        @SuppressWarnings("rawtypes")
        FieldVisitor<?> fieldVisitor = new FieldVisitor();
        fieldVisitor.visit(compilationUnit, null);
        List<Field> fields = fieldVisitor.getFields();
        
        // Find the 'class variables', the 'instance variables' and
        // the 'constants'
        for (Field field : fields) {
            classVariables.addAll(field.getClassVariables());
            instanceVariables.addAll(field.getInstanceVariables());
            finalVariables.addAll(field.getFinalVariables());
        }
    }
    
    /**
     * Visits the enums of the source code
     */
    private void visitEnums() {
        @SuppressWarnings("rawtypes")
        EnumVisitor<?> enumVisitor = new EnumVisitor();
        enumVisitor.visit(compilationUnit, null);
        enums.addAll(enumVisitor.getEnums());
    }
    
    /**
     * Adds a directory with source code files to the existing symbol resolvers.
     * 
     * @param typeSolver The module for symbol resolution 
     * @param sourceCodeDir The directory with the source code files to add to
     *                      the existing symbol resolvers
     */
    private void addJavaCodeSymbolSolver(CombinedTypeSolver typeSolver,
                                         File               sourceCodeDir) {
        if (sourceCodeDir == null) {
            throw new IllegalArgumentException("'sourceCodeDir' is null");
        }
        if (!sourceCodeDir.isDirectory()) {
            throw new IllegalArgumentException("'" + sourceCodeDir.getName()
                                             + "' is not a directory");
        }
        
        typeSolver.add(new JavaParserTypeSolver(sourceCodeDir));
    }
    
    /**
     * Adds a jar file to the existing symbol resolvers.
     * 
     * @param typeSolver The module for symbol resolution 
     * @param jarFile The jar file to add
     * 
     * @throws ErrorException in case of an error adding the jar file to the
     *                        existing symbol resolvers 
     */
    private void addJarSymbolSolver(CombinedTypeSolver typeSolver,
                                    File               jarFile)
           throws ErrorException {
        if (jarFile == null) {
            throw new IllegalArgumentException("'jarFile' is null");
        }
        
        try {
            typeSolver.add(new JarTypeSolver(jarFile));
        }
        catch (IOException ioe) {
            throw new ErrorException("Error adding '" + jarFile.getPath()
                                   + "' to the symbol resolvers. Details:"
                                   + NEW_LINE + ioe.getMessage());
        }
    }
}
