/*
 * File          : ProgramExecutor.java
 * Author        : Charis Charitsis
 * Creation Date : 4 February 2019
 * Last Modified : 14 January 2021
 */
package edu.stanford.java.execution;

// Import Java SE classes
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Import com.github.javaparser + javassist classes
import com.github.javaparser.ast.PackageDeclaration;
import javassist.Modifier;
//Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.exception.InvalidArgumentException;
import edu.stanford.java.compile.Compiler;
import edu.stanford.java.SourceCodeParser;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.JavaSourceCode;
import edu.stanford.javaparser.body.ClassOrInterface;
import edu.stanford.util.UIUtil;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
import edu.stanford.util.io.FileIOUtil;
import edu.stanford.util.runtime.RuntimeExec;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Literals.DOT;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.java.compile.Compiler.CLASSPATH_OPTION;

/**
 * Compiles and executes a program.
 */
public class ProgramExecutor
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** The Java binary name */
    private static final String JAVA_BIN = "java";
    
    // ------------------------------------- //
    //   V   A   R   I   A   B   L   E   S   //
    // ------------------------------------- //
    /** List with the compiled classes in memory */
    protected final List<Class<?>> classes;
    /**
     * The class to execute its 'public static void main(String[] args)' or
     * 'public void run()' method
     */
    protected final Class<?>       executionClass;
    /** The number of threads when the ProgramExecutor is instantiated */
    protected final int            initialNumberOfThreads;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new ProgramExecutor to compile and run the provided source
     * code file.
     * 
     * @param sourceCodeFile The source code to compile and run
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) In memory compilation error <br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) Error loading and instantiating the class from
     *                           the compiled bytecode
     */
    public ProgramExecutor(File   sourceCodeFile,
                           String classpath)
           throws ErrorException {
        this(sourceCodeFile,
             null, // fileDependencies
             classpath);
    }
    
    /**
     * Creates a new ProgramExecutor to compile and run the provided source
     * code file.
     * 
     * @param sourceCode The source code to compile and load in memory
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) In memory compilation error <br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) Error loading and instantiating the class from
     *                           the compiled bytecode
     */
    public ProgramExecutor(String sourceCode,
                           String classpath)
           throws ErrorException {
        this(sourceCode,
             null, // fileDependencies
             classpath);
    }
    
    /**
     * Creates a new ProgramExecutor to compile and run the provided source
     * code file.
     * 
     * @param sourceCodeFile The source code to compile and run
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) In memory compilation error <br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) Error loading and instantiating the class from
     *                           the compiled bytecode
     */
    public ProgramExecutor(File      sourceCodeFile,
                           Set<File> requiredSourceFiles,
                           String    classpath)
           throws ErrorException {
        classes = Compiler.compileInMemory(sourceCodeFile,
                                           requiredSourceFiles,
                                           classpath);
        
        String sourceCode = FileIOUtil.readFile(sourceCodeFile, false);
        executionClass = getSourceCodeClass(sourceCode);
        initialNumberOfThreads = Thread.getAllStackTraces().keySet().size();
    }
    
    /**
     * Creates a new ProgramExecutor to compile and run the provided source
     * code file.
     * 
     * @param sourceCode The source code to compile and load in memory
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) In memory compilation error <br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) Error loading and instantiating the class from
     *                           the compiled bytecode
     */
    public ProgramExecutor(String    sourceCode,
                           Set<File> requiredSourceFiles,
                           String    classpath)
           throws ErrorException {
        classes = Compiler.compileInMemory(sourceCode,
                                           requiredSourceFiles,
                                           classpath);
        
        executionClass = getSourceCodeClass(sourceCode);
        initialNumberOfThreads = Thread.getAllStackTraces().keySet().size();
    }
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Returns the fully qualified class name for the given a Java source code.
     *  
     * @param sourceCode The Java source code
     *  
     * @return the fully qualified class name for the given a Java source code
     * 
     * @throws ErrorException in case there is no class declaration in the given
     *                        source code or in case there is no matching class
     *                        for the given source code
     */
    public static String getFullyQualifiedClassName(String sourceCode)
           throws ErrorException {
         JavaSourceCode javaSourceCode = new JavaSourceCode(sourceCode);
         List<ClassOrInterface> classesOrInterfaces =
                                         javaSourceCode.getClasses();
         classesOrInterfaces.addAll(javaSourceCode.getInterfaces());
         
         String className = null;
         if (!classesOrInterfaces.isEmpty()) {
             int beginLine = Integer.MAX_VALUE;
             for (ClassOrInterface classesOrInterface : classesOrInterfaces) {
                 if (classesOrInterface.getBeginLine() < beginLine) {
                     className = classesOrInterface.getName();
                     beginLine = classesOrInterface.getBeginLine();
                 }
             }
         }
         else {
             throw new ErrorException("There is no class or interface declared "
                                    + "in code:" + NEW_LINE
                                    + javaSourceCode.getOriginalCode());
         }
         
         PackageDeclaration packageDeclaration = javaSourceCode.getPackage();
         String fullyQualifiedClassName = packageDeclaration == null?
                                          className :
                                          packageDeclaration.getNameAsString()
                                                             + DOT + className;
         
         return fullyQualifiedClassName;
    }
    
    /**
     * @return a list with the compiled classes in memory
     */
    public List<Class<?>> getCompiledClasses() {
        return classes;
    }
    
    /**
     * @return the class to execute its 'public static void main(String[] args)'
     *         or 'public void run()' method
     */
    public Class<?> getExecutionClass() {
        return executionClass;
    }
    
    /**
     * Executes a program given its source file.<br>
     * It looks for the 'main' (i.e., 'public static void main(String[] args)')
     * and if found it executes it. Otherwise it looks for 'void run()' and if
     * found it executes it. To do so, it instantiates the the class before it
     * invokes method 'run'.
     * 
     * @param sourceFile The source code file for the program to execute
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements.<br>
     *                     It uses the keys as the text to look for and the
     *                     corresponding values as replacement to this text.
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return {@code true} if 'public static void main(String[] args)' is found
     *         and executed of if 'public void run()' is found and executed or
     *         {@code false} otherwise
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path is {@code null}<br>
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the source code
     *                           file, compiling or executing the program<br>
     */
    public static boolean executeProgram(File                sourceFile,
                                         String[]            imports,
                                         Map<String, String> replacements,
                                         String              classpath)
           throws ErrorException {
        if (sourceFile == null) {
            throw new ErrorException("Argument 'sourceFile' is null");
        }
        
        JavaFile javaFile = new JavaFile(sourceFile.getPath());
        SourceCodeParser sourceCodeParser = new SourceCodeParser(javaFile);
        
        String instrumentedCode =
                       sourceCodeParser.getInstrumentedSourceCode(false,
                                                                  false,
                                                                  imports,
                                                                  replacements);
        ProgramExecutor programExecutor;
        try {
            programExecutor = new ProgramExecutor(instrumentedCode, classpath);
        }
        catch (ErrorException ee) {
            String errorMessage = "Error executing program in file '"
                                + sourceFile.getPath()
                                + "'. Details: " + NEW_LINE
                                + ee.getMessage();
            
            throw new ErrorException(errorMessage, ee);
        }
        
        if (programExecutor.executeMainMethod()) {
            return true;
        }
        
        return programExecutor.executeRunMethod();
    }
    
    
    /**
     * Executes a program given its source code.<br>
     * It looks for the 'main' (i.e., 'public static void main(String[] args)')
     * and if found it executes it. Otherwise it looks for 'void run()' and if
     * found it executes it. To do so, it instantiates the the class before it
     * invokes method 'run'. 
     * 
     * @param sourceCode The source code for the program to execute
     * @param configuration Holds the 'configuration' info how to implement the
     *                      code instrumentation.
     * 
     * @return {@code true} if 'public static void main(String[] args)' is found
     *         and executed of if 'public void run()' is found and executed or
     *         {@code false} otherwise
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null}<br>
     *                        2) An error occurred while parsing the source code
     *                           file, compiling or executing the program<br>
     */
    public static boolean executeProgram(String                sourceCode,
                                         InstrumentationConfig configuration)
           throws ErrorException {
        return executeProgram(sourceCode,
                              configuration.getImports(),
                              configuration.getReplacements(),
                              configuration.getRequiredSourceFiles(),
                              configuration.getClasspath());
    }
    
    /**
     * Executes a program given its source code.<br>
     * It looks for the 'main' (i.e., 'public static void main(String[] args)')
     * and if found it executes it. Otherwise it looks for 'void run()' and if
     * found it executes it. To do so, it instantiates the the class before it
     * invokes method 'run'. 
     * 
     * @param sourceCode The source code for the program to execute
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements.<br>
     *                     It uses the keys as the text to look for and the
     *                     corresponding values as replacement to this text.
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return {@code true} if 'public static void main(String[] args)' is found
     *         and executed of if 'public void run()' is found and executed or
     *         {@code false} otherwise
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null}<br>
     *                        2) An error occurred while parsing the source code
     *                           file, compiling or executing the program<br>
     */
    public static boolean executeProgram(String             sourceCode,
                                         String[]           imports,
                                         Map<String,String> replacements,
                                         Set<File>          requiredSourceFiles,
                                         String             classpath)
           throws ErrorException {
        if (sourceCode == null) {
            throw new ErrorException("Argument 'sourceCode' is null");
        }
        
        SourceCodeParser sourceCodeParser = new SourceCodeParser(sourceCode);
        
        String instrumentedCode =
                       sourceCodeParser.getInstrumentedSourceCode(false,
                                                                  false,
                                                                  imports,
                                                                  replacements);
        
        ProgramExecutor programExecutor;
        try {
            programExecutor = new ProgramExecutor(instrumentedCode,
                                                  requiredSourceFiles,
                                                  classpath);
        }
        catch (ErrorException ee) {
            String errorMessage = "Error executing the following source code:"
                                + NEW_LINE + sourceCode + "'. Details: "
                                + NEW_LINE + ee.getMessage();
            
            throw new ErrorException(errorMessage, ee);
        }
        
        if (programExecutor.executeMainMethod()) {
            return true;
        }
        
        return programExecutor.executeRunMethod();
    }
    
    /**
     * Executes a program given its source code.<br>
     * It saves the code to a temporary file, compiles it to the destination
     * directory and then runs the program in a separate java process.
     * 
     * @param sourceCode The source code for the program to execute
     * @param configuration Holds the 'configuration' info how to implement the
     *                      code instrumentation.
     * @param destDir The destination directory to save the compiled code
     * @param timeoutInSec The timeout for the program execution, expressed in
     *                     seconds, or {@code null} for no timeout
     * 
     * @return {@code true} if 'public static void main(String[] args)' is found
     *         and executed of if 'public void run()' is found and executed or
     *         {@code false} otherwise
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null}<br>
     *                        2) An error occurred while parsing the source code
     *                           file, compiling or executing the program<br>
     */
    public static boolean
       compileAndExecuteProgramFromDisk(String                sourceCode,
                                        InstrumentationConfig configuration,
                                        File                  destDir,
                                        Double                timeoutInSec)
           throws ErrorException {
        return
        compileAndExecuteProgramFromDisk(sourceCode,
                                         configuration.saveStateBefore(),
                                         configuration.saveStateAfter(),
                                         configuration.getImports(),
                                         configuration.getReplacements(),
                                         configuration.getRequiredSourceFiles(),
                                         configuration.getClasspath(),
                                         destDir,
                                         timeoutInSec);
    }
    
    /**
     * Executes a program given its source code.<br>
     * It saves the code to a temporary file, compiles it to the destination
     * directory and then runs the program in a separate java process.
     * 
     * @param sourceCode The source code for the program to execute
     * @param saveStateBefore {@code true} to make a method call to save the
     *                        state when entering a method or {@code false}
     *                        otherwise 
     * @param saveStateAfter {@code true} to make a method call to save the
     *                        state when exiting a method or {@code false}
     *                        otherwise 
     * @param imports The import statements to add on top of any existing ones
     *                or {@code null} to add no more import statements
     * @param replacements A lookup map that is used for text replacement or
     *                     {@code null} to avoid text replacements.<br>
     *                     It uses the keys as the text to look for and the
     *                     corresponding values as replacement to this text.
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * @param destDir The destination directory to save the compiled code
     * @param timeoutInSec The timeout for the program execution, expressed in
     *                     seconds, or {@code null} for no timeout
     * 
     * @return {@code true} if 'public static void main(String[] args)' is found
     *         and executed of if 'public void run()' is found and executed or
     *         {@code false} otherwise
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided source code is {@code null}<br>
     *                        2) An error occurred while parsing the source code
     *                           file, compiling or executing the program<br>
     */
    public static boolean
       compileAndExecuteProgramFromDisk(String              sourceCode,
                                        boolean             saveStateBefore,
                                        boolean             saveStateAfter,
                                        String[]            imports,
                                        Map<String, String> replacements,
                                        Set<File>           requiredSourceFiles,
                                        String              classpath,
                                        File                destDir,
                                        Double              timeoutInSec)
           throws ErrorException {
        // Locate the java binary; if not found fail fast!
        String javaBinPathname = FileUtil.findBinaryOnEnvVar("PATH",
                                                             null, // subpath
                                                             JAVA_BIN);
        if (javaBinPathname == null) {
            javaBinPathname = FileUtil.findBinaryOnEnvVar("JAVA_HOME",
                                                          "bin", // subpath
                                                          JAVA_BIN);
            if (javaBinPathname == null) {
                String message = "The java binary is not found" + NEW_LINE
                               + "Set the environemnt variable JAVA_HOME to its"
                               + " location and try again";
                UIUtil.showError(message, "Java binary not found");
                return false;
            }
        }
        
        // STEP 1: Compile the source code in disk
        if (sourceCode == null) {
            throw new ErrorException("Argument 'sourceCode' is null");
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        DirectoryUtil.validateDirToWrite(destDir);
        
        SourceCodeParser sourceCodeParser = new SourceCodeParser(sourceCode);
        
        String instrumentedCode =
                    sourceCodeParser.getInstrumentedSourceCode(saveStateBefore,
                                                               saveStateAfter,
                                                               imports,
                                                               replacements);
        
        String fullyQualifiedClassName =
                                  getFullyQualifiedClassName(instrumentedCode);
        int dotIndex = fullyQualifiedClassName.lastIndexOf(DOT);
        File outputSourceCodeFile;
        if (dotIndex != -1) {
            String[] parentDirs = fullyQualifiedClassName.substring(0, dotIndex)
                                                         .split("\\" + DOT);
            String pathname = destDir.getPath();
            for (String parentDir : parentDirs) {
                pathname += File.separator + parentDir;
                new File(pathname).mkdir();
            }
            outputSourceCodeFile = new File(pathname + File.separator
                               + fullyQualifiedClassName.substring(dotIndex + 1)
                               + JAVA_FILE_EXTENSION);
        }
        else {
            outputSourceCodeFile = new File(destDir.getPath() + File.separator
                                + fullyQualifiedClassName
                                + JAVA_FILE_EXTENSION);
        }
        
        // Save the source code to the destination dir
        FileIOUtil.writeFile(outputSourceCodeFile.getPath(),
                             instrumentedCode,
                             false); // append
        
        Set<File> files = new HashSet<File>();
        if (requiredSourceFiles != null) {
            files.addAll(requiredSourceFiles);
        }
        files.add(outputSourceCodeFile);
        
        Compiler.compile(classpath,
                         destDir.getPath(),
                         null, // options
                         files);
        
        // STEP 2: Run the program from the compiled classes in disk in a
        //         separate Java process
        classpath += File.pathSeparator + destDir.getPath();
        String[] cmdArgs = new String[] { CLASSPATH_OPTION,
                                          classpath,
                                          fullyQualifiedClassName };
        
        RuntimeExec runtimeExec;
        try {
            runtimeExec = new RuntimeExec(javaBinPathname,
                                          cmdArgs,
                                          null,    // workingDir
                                          null,    // input
                                          null,    // env
                                          false,   // showOutput
                                          false);  // showError
            runtimeExec.runCommand(timeoutInSec);
            return true;
        }
        catch (InvalidArgumentException iae) { // Impossible
           throw new ErrorException(iae.getMessage()); // Just in case...
        }
    }
    
    /**
     * Prints the number of threads that are currently running.
     */
    public void printNumOfThreads() {
        int totalNumOfThreads = Thread.getAllStackTraces().keySet().size();
        int numOfThreads = totalNumOfThreads - initialNumberOfThreads + 1;
        System.out.println("     Total number of threads: " + numOfThreads
                         + "  -  All Java threads: " + totalNumOfThreads);
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Executes method 'public static void main(String[] args)' if found in the
     * execution class.
     * 
     * @return {@code true} if the 'main' method is found and executed or
     *         {@code false} if the 'main' method is not found in the execution
     *         class 
     * 
     * @throws ErrorException in case the 'main' method was found but there was
     *                        an error executing it
     */
    private boolean executeMainMethod()
            throws ErrorException {
        for (Method method : executionClass.getMethods()) {
            if (isMainMethod(method)) {
                String[] params = null;
                try {
                    method.invoke(null, (Object)params);
                    return true;
                }
                catch (IllegalAccessException e) {
                    throw new ErrorException("Error running 'public static void" 
                                           + " main(String[] args)' in class "
                                           + executionClass.getName()
                                           + ". Details: " + NEW_LINE
                                           +  e.getMessage());
                }
                catch (IllegalArgumentException e) {
                     throw new ErrorException("Error running 'public static void" 
                                            + " main(String[] args)' in class "
                                            + executionClass.getName()
                                            + ". Details: " + NEW_LINE
                                            +  e.getMessage());
                 }
                catch (//IllegalAccessException | IllegalArgumentException |
                        InvocationTargetException e) {
                     throw new ErrorException("Error running 'public static void" 
                                            + " main(String[] args)' in class "
                                            + executionClass.getName()
                                            + ". Details: " + NEW_LINE
                                            +  e.getMessage());
                 }
            }
        }
        
        // Method 'public static void main(String[] args)' is not found in any
        // of the compiled classes
        return false;
    }
    
    /**
     * Executes method 'public void run()' if found in the execution class.
     * 
     * @return {@code true} if the 'run' method is found and executed or
     *         {@code false} if the 'run' method is not found in the execution
     *         class 
     * 
     * @throws ErrorException in case the 'run' method was found but there was
     *                        an error executing it
     */
    private boolean executeRunMethod()
            throws ErrorException {
        for (Method method : executionClass.getMethods()) {
            if (isRunMethod(method)) {
                Object instance;
                try {
                    instance = executionClass.getDeclaredConstructor()
                                             .newInstance();
                }
                catch (InstantiationException   | IllegalAccessException    |
                       IllegalArgumentException | InvocationTargetException |
                       NoSuchMethodException    | SecurityException e) {
                    throw new ErrorException("Error instantiating " 
                                           + executionClass.getName()
                                           + ". Details: " + NEW_LINE
                                           +  e.getMessage());
                }
                
                try {
                    method.invoke(instance);
                    return true;
                }
                catch (IllegalAccessException | IllegalArgumentException |
                       InvocationTargetException e) {
                    throw new ErrorException("Error running 'public void run()'" 
                                           + " in class "
                                           + executionClass.getName() + ".");
                }
            }
        }
        
        // Method 'public static void main(String[] args)' is not found in any
        // of the compiled classes
        return false;
    }
    
    /**
     * Given a Java source code it returns the matching class from all compiled
     * classes.
     *  
     * @param sourceCode The Java source code
     *  
     * @return the class for the provided Java source code
     * 
     * @throws ErrorException in case there is no class declaration in the given
     *                        source code or in case there is no matching class
     *                        for the given source code
     */
    private Class<?> getSourceCodeClass(String sourceCode)
            throws ErrorException {
         String fullyQualifiedClassName =
                                   getFullyQualifiedClassName(sourceCode);
         
         for (Class<?> clazz : classes) {
             if (clazz.getName().equals(fullyQualifiedClassName)) {
                 return clazz;
             }
         }
         
         // Should not happen; just in case
         throw new ErrorException("No matching class for the given source code:"
                                + NEW_LINE + sourceCode);
    }
    
    /**
     * It checks if the given method is 'public static void main(String[] args)'
     * and if so it returns {@code true}. Otherwise it returns {@code false}.
     * 
     * @param method The method to check
     * 
     * @return {@code true} if the method is
     *         'public static void main(String[] args)' or {@code false}
     *          otherwise
     */
    private static boolean isMainMethod(Method method) {
        int modifiers = method.getModifiers();
        // Must be 'public'
        if (!Modifier.isPublic(modifiers)) {
            return false;
        }
        
        // Must be 'static'
        if (!Modifier.isStatic(modifiers)) {
            return false;
        }
        
        // Must return nothing ('void')
        if (!method.getReturnType().getTypeName().equals("void")) {
            return false;
        }
        
        // The method name must be 'main'
        if (!method.getName().equals("main")) {
           return false;
        }
        
        // Must take one argument
        if (method.getParameterCount() != 1) {
           return false;
        }
        
        // The type of that argument must be 'String[]'
        if (!method.getParameters()[0].getParameterizedType().getTypeName()
                                        .equals("java.lang.String[]")) {
           return false;
        }
        
        return true; // All criteria are met
    }
    
    /**
     * It checks if the given method is 'public void run()' and if so it returns
     * {@code true}. Otherwise it returns {@code false}.
     * 
     * @param method The method to check
     * 
     * @return {@code true} if the method is 'public void run()' or
     *         {@code false} otherwise
     */
    private static boolean isRunMethod(Method method) {
        int modifiers = method.getModifiers();
        // Must be 'public'
        if (!Modifier.isPublic(modifiers)) {
            return false;
        }
        
        // Must not be 'static'
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        
        // Must return nothing ('void')
        if (!method.getReturnType().getTypeName().equals("void")) {
            return false;
        }
        
        // The method name must be 'run'
        if (!method.getName().equals("run")) {
           return false;
        }
        
        // Must take no arguments
        if (method.getParameterCount() != 0) {
           return false;
        }
        
        return true; // All criteria are met
    }
}
