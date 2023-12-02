/*
 * File          : Compiler.java
 * Author        : Charis Charitsis
 * Creation Date : 4 February 2019
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.compile;

// Import Java SE classes
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
// Import com.github.javaparser classes
import com.github.javaparser.ast.PackageDeclaration;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.java.SourceCodeParser;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.JavaSourceCode;
import edu.stanford.javaparser.body.ClassOrInterface;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Literals.DOT;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.constants.Literals.STRAIGHT_LINE_SEPARATOR;
import static edu.stanford.constants.Literals.TAB;

/**
 * Compiles source code files written in Java
 */
public class Compiler
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /**
     * Classpath option.<br>
     * Specifies where to find user class files, and (optionally) annotation
     * processors and source files
     */
    public static final String CLASSPATH_OPTION  = "-classpath";
    /**
     * Sourcepath option.<br>
     * Specifies where to find user source files
     */
    public static final String SOURCEPATH_OPTION = "-sourcepath";
    /**
     * Destination directory option.<br>
     * Sets the destination directory for class files. The directory must exist. 
     */
    public static final String DEST_DIR_OPTION   = "-d";
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Given a file with the Java source code it finds the fully qualified class
     * declared in this file and returns it after appending '.java' to it.
     *  
     * @param sourceFile The file with the Java source code
     *  
     * @return the fully qualified class filename which is the fully qualified
     *         class name declared in this file appended with '.java'.
     * 
     * @throws ErrorException in case there is an error retrieving the fully
     *                        qualified class name or if there is no class 
     *                        declaration in the given file 
     */
    public static String getFullyQualifiedClassFilename(File sourceFile)
           throws ErrorException {
        JavaFile javaFile = new JavaFile(sourceFile.getPath());
        List<ClassOrInterface> classesOrInterfaces = javaFile.getClasses();
        classesOrInterfaces.addAll(javaFile.getInterfaces());
        
        String classFilename = null;
        if (!classesOrInterfaces.isEmpty()) {
            int beginLine = Integer.MAX_VALUE;
            for (ClassOrInterface classesOrInterface : classesOrInterfaces) {
                if (classesOrInterface.getBeginLine() < beginLine) {
                    classFilename = classesOrInterface.getName()
                                  + JAVA_FILE_EXTENSION;
                    beginLine = classesOrInterface.getBeginLine();
                }
            }
        }
        else {
            throw new ErrorException("There is no class or interface declared "
                                   + "in file '" + sourceFile.getName() + "'");
        }
        
        PackageDeclaration packageDeclaration = javaFile.getPackage();
        if (packageDeclaration == null) {
            return classFilename;
        }
        else {
            return packageDeclaration.getNameAsString() + DOT + classFilename;
        }
    }
    
    /**
     * Given a source code it finds the fully qualified class declared in it
     * and returns it after appending '.java' to it.
     *  
     * @param sourceCode The Java source code
     *  
     * @return the fully qualified class filename which is the fully qualified
     *         class name declared in this source code appended with '.java'.
     * 
     * @throws ErrorException in case there is an error retrieving the fully
     *                        qualified class name or if there is no class 
     *                        declaration in the given source code 
     */
    public static String getFullyQualifiedClassFilename(String sourceCode)
            throws ErrorException {
         JavaSourceCode javaSourceCode = new JavaSourceCode(sourceCode);
         List<ClassOrInterface> classesOrInterfaces =
                                         javaSourceCode.getClasses();
         classesOrInterfaces.addAll(javaSourceCode.getInterfaces());
         
         String classFilename = null;
         if (!classesOrInterfaces.isEmpty()) {
             int beginLine = Integer.MAX_VALUE;
             for (ClassOrInterface classesOrInterface : classesOrInterfaces) {
                 if (classesOrInterface.getBeginLine() < beginLine) {
                     classFilename = classesOrInterface.getName()
                                   + JAVA_FILE_EXTENSION;
                     beginLine = classesOrInterface.getBeginLine();
                 }
             }
         }
         else {
             throw new ErrorException("There is no class or interface declared "
                                    + "in code:" + NEW_LINE + sourceCode);
         }
         
         PackageDeclaration packageDeclaration = javaSourceCode.getPackage();
         if (packageDeclaration == null) {
             return classFilename;
         }
         else {
             return packageDeclaration.getNameAsString() + DOT + classFilename;
         }
    }
    
    // ---------------------------------------------------------------------- //
    //       C  H  E  C  K     I  F     C  O  M  P  I  L  E  A  B  L  E       //
    // ---------------------------------------------------------------------- //
    /**
     * Compiles the given source file in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param file The file to compile
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return {@code null} if the given source file compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(File   file,
                                      String classpath) {
        String sourceCode;
        try {
            sourceCode = FileIOUtil.readFile(file, false);
        }
        catch (ErrorException ee) {
            return ee.getMessage();
        }
        
        try {
             compileInMemory(sourceCode, classpath);
             return null; // No error
        }
        catch (ErrorException ee) {
            // The source code is non-compilable
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(ee.getMessage() + NEW_LINE);
            errorMessage.append(STRAIGHT_LINE_SEPARATOR + NEW_LINE);
            String[] lines = sourceCode.split(NEW_LINE);
            int numOfLines = lines.length;
            int digits = String.valueOf(numOfLines).length();
            
            for (int i = 0; i < numOfLines; i++) {
                errorMessage.append("L");
                int alignmentSpaces = digits - String.valueOf(i + 1).length();
                while (alignmentSpaces > 0) {
                    errorMessage.append(SPACE);
                    alignmentSpaces--;
                }
                errorMessage.append((i + 1) + TAB + lines[i] + NEW_LINE);
            }
            
            return errorMessage.toString();
        }
    }
    
    /**
     * Compiles the given source code in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param sourceCode The source code to compile and load in memory
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return {@code null} if the given source file compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(String sourceCode,
                                      String classpath) {
        try {
             compileInMemory(sourceCode, classpath);
             return null; // No error
        }
        catch (ErrorException ee) {
            // The source code is non-compilable
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(ee.getMessage() + NEW_LINE);
            errorMessage.append(STRAIGHT_LINE_SEPARATOR + NEW_LINE);
            String[] lines = sourceCode.split(NEW_LINE);
            int numOfLines = lines.length;
            int digits = String.valueOf(numOfLines).length();
            
            for (int i = 0; i < numOfLines; i++) {
                errorMessage.append("L");
                int alignmentSpaces = digits - String.valueOf(i + 1).length();
                while (alignmentSpaces > 0) {
                    errorMessage.append(SPACE);
                    alignmentSpaces--;
                }
                errorMessage.append((i + 1) + TAB + lines[i] + NEW_LINE);
            }
            
            return errorMessage.toString();
        }
    }
    
    /**
     * Compiles the given source file in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param file The file to compile
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpathJarFiles A list of jar-files to include in the classpath
     *                          or {@code null} if there are no jar-files to use
     * 
     * @return {@code null} if the given source file compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(File       file,
                                      Set<File>  requiredSourceFiles,
                                      List<File> classpathJarFiles) {
        return isCompilable(file,
                            false, // saveStateBefore
                            false, // saveStateAfter
                            null,  // imports
                            null,  // replacements
                            requiredSourceFiles,
                            classpathJarFiles);
    }
    
    /**
     * Compiles the given source code in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param sourceCode The source code to compile
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpathJarFiles A list of jar-files to include in the classpath
     *                          or {@code null} if there are no jar-files to use
     * 
     * @return {@code null} if the given source code compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(String     sourceCode,
                                      Set<File>  requiredSourceFiles,
                                      List<File> classpathJarFiles) {
        return isCompilable(sourceCode,
                            false, // saveStateBefore
                            false, // saveStateAfter
                            null,  // imports
                            null,  // replacements
                            requiredSourceFiles,
                            classpathJarFiles);
    }
    
    /**
     * Compiles the given source file in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param file The file to compile
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
     *                     keys as the text to look for and the corresponding
     *                     values as replacement to this text.
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpathJarFiles A list of jar-files to include in the classpath
     *                          or {@code null} if there are no jar-files to use
     * 
     * @return {@code null} if the given source file compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(File                file,
                                      boolean             saveStateBefore,
                                      boolean             saveStateAfter,
                                      String[]            imports,
                                      Map<String, String> replacements,
                                      Set<File>           requiredSourceFiles,
                                      List<File>          classpathJarFiles) {
        try {
            String sourceCode;
            if (saveStateBefore || saveStateAfter ||
                imports != null || replacements != null) {
                SourceCodeParser parser = new SourceCodeParser(file);
                sourceCode = parser.getInstrumentedSourceCode(saveStateBefore,
                                                              saveStateAfter,
                                                              imports,
                                                              replacements);
            }
            else {
                sourceCode = FileIOUtil.readFile(file, false);
            }
            
            String fullyQualifiedClassFilename =
                                 Compiler.getFullyQualifiedClassFilename(file);
            
            return isCompilable(sourceCode,
                                fullyQualifiedClassFilename,
                                requiredSourceFiles,
                                classpathJarFiles);
        }
        catch (ErrorException ee) {
            // Code instrumentation error
            return ee.getMessage();
        }
    }
    
    /**
     * Compiles the given source code in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param sourceCode The source code to compile
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
     *                     keys as the text to look for and the corresponding
     *                     values as replacement to this text.
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpathJarFiles A list of jar-files to include in the classpath
     *                          or {@code null} if there are no jar-files to use
     * 
     * @return {@code null} if the given source code compiles successfully or a
     *         String with the error message if it fails
     */
    public static String isCompilable(String              sourceCode,
                                      boolean             saveStateBefore,
                                      boolean             saveStateAfter,
                                      String[]            imports,
                                      Map<String, String> replacements,
                                      Set<File>           requiredSourceFiles,
                                      List<File>          classpathJarFiles) {
        try {
            if (saveStateBefore || saveStateAfter ||
                imports != null || replacements != null) {
                SourceCodeParser parser = new SourceCodeParser(sourceCode);
                sourceCode = parser.getInstrumentedSourceCode(saveStateBefore,
                                                              saveStateAfter,
                                                              imports,
                                                              replacements);
            }
            
            String fullyQualifiedClassFilename =
                        Compiler.getFullyQualifiedClassFilename(sourceCode);
            
            return isCompilable(sourceCode,
                                fullyQualifiedClassFilename,
                                requiredSourceFiles,
                                classpathJarFiles);
        }
        catch (ErrorException ee) {
            // Code instrumentation error
            return ee.getMessage();
        }
    }
    
    // ---------------------------------------------------------------------- //
    //     C  O  M  P  I  L  E     N  O  T     I  N     M  E  M  O  R  Y      //
    // ---------------------------------------------------------------------- //
    /**
     * Compiles the given list of files.<br>
     * The order of the files in the list does not matter (i.e., it makes no
     * difference if a file that depends on the other files is first or last in
     * the list). 
     * 
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * @param destDirPathname The destination directory pathname for class files
     *                        or {@code null} to use the working directory.<br>
     *                        <pre>
     *                        - If not {@code null}, the directory must already
     *                          exist.
     *                        - If {@code null}, each class file will be output
     *                          in the same directory as the source file from
     *                          which it was compiled.
     *                          If a class is part of a package, the class file
     *                          is added in a subdirectory reflecting the
     *                          package name, creating directories as needed.
     *                        </pre> 
     * @param options Any additional compiler options including their values
     *                (i.e., each element should be in the form 
     *                "-option {@literal <value>}" or {@code null} in case there
     *                are no additional options 
     * @param files The set of files to compile.
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The classpath, if specified, does not exist or
     *                           does not have read access
     *                        2) The destination directory, if specified, does
     *                           not exist or does not have read access
     *                        3) Compilation error
     */
    public static void compile(String       classpath,
                               String       destDirPathname,
                               List<String> options,
                               Set<File>    files)
            throws ErrorException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        
        StandardJavaFileManager fileManager =
          javaCompiler.getStandardFileManager(null, //default diagnosticListener
                                              null, //default locale
                                              null);//default charset
        
        Iterable<? extends JavaFileObject> fileObjects = 
                               fileManager.getJavaFileObjectsFromFiles(files);
        
        List<String> compilerOptions = new ArrayList<String>();
        
        // Classpath
        if (classpath != null && !classpath.trim().isEmpty()) {
            assertClasspath(classpath);
            
            compilerOptions.add(CLASSPATH_OPTION);
            compilerOptions.add(classpath);
        }
        
        // Destination directory
        if (destDirPathname != null) {
            destDirPathname = destDirPathname.trim();
            DirectoryUtil.validateDirToWrite(destDirPathname);
            
            compilerOptions.add(DEST_DIR_OPTION);
            compilerOptions.add(destDirPathname);
        }
        
        // Any other options provided as argument to this method
        if (options != null) {
            for (String option : options) {
                compilerOptions.add(option);
            }
        }
        
        DiagnosticCollector<JavaFileObject> diagnosticCollector =
                                    new DiagnosticCollector<JavaFileObject>();
        CompilationTask compilationTask = javaCompiler.getTask(
               null,  // compiler's output: std err
               fileManager,
               diagnosticCollector,
               compilerOptions, // compiler options
               null,  // no class names to be processed by annotation processing
               fileObjects); // compilation units/files to compile
        
        boolean success = compilationTask.call();
        try {
            fileManager.close();
        }
        catch (IOException ignored) {
        }
        
        if (!success) {
            StringBuilder errorMsg = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic:
                                        diagnosticCollector.getDiagnostics()) {
                String err = String.format("Compilation error: Line %d - %s%n",
                                           diagnostic.getLineNumber(),
                                           diagnostic.getMessage(null));
                errorMsg.append(err);
            }
            
            throw new ErrorException(errorMsg.toString());
        }
    }
    
    // ---------------------------------------------------------------------- //
    //           C  O  M  P  I  L  E     I  N     M  E  M  O  R  Y            //
    // ---------------------------------------------------------------------- //
    /**
     * Compiles in memory the given file.
     * 
     * @param sourceCodeFile The source code file to compile and load in memory
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return a list with the compiled classes in memory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code file does not exist, does
     *                           not have read access or is a directory rather
     *                           than a regular file<br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) A compilation error occurs
     */
    public static List<Class<?>> compileInMemory(File   sourceCodeFile,
                                                 String classpath)
            throws ErrorException {
        return compileInMemory(sourceCodeFile,
                               null,      // fileDependencies
                               classpath);
    }
    
    /**
     * Compiles in memory the given source code.
     * 
     * @param sourceCode The source code to compile and load in memory
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     *                  
     * @return a list with the compiled classes in memory
     *  
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code is {@code null} or empty string
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) A compilation error occurs
     */
    public static List<Class<?>> compileInMemory(String sourceCode,
                                                 String classpath)
            throws ErrorException {
        return compileInMemory(sourceCode,
                               null, // fileDependencies
                               classpath);
    }    
    
    /**
     * Compiles in memory the given source code.
     * 
     * @param sourceCodeFile The source code file to compile and load in memory
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return a list with the compiled classes in memory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code file does not exist, does
     *                           not have read access or is a directory rather
     *                           than a regular file<br>
     *                        2) The array with the files that are needed to
     *                           compile if not {@code null} has an element that
     *                           does not exist or does not have read access<br>
     *                        3) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        4) A compilation error occurs
     */
    public static List<Class<?>> compileInMemory(File      sourceCodeFile,
                                                 Set<File> requiredSourceFiles,
                                                 String    classpath)
           throws ErrorException {
        // Make sure that the source code file exists and has read access
        FileUtil.validateFileToRead(sourceCodeFile);
        
        // Extract the source code from the file
        String sourceCode = FileIOUtil.readFile(sourceCodeFile);
        
        return compileInMemory(sourceCode,
                               requiredSourceFiles,
                               classpath);
    }
    
    /**
     * Compiles in memory the given source code.
     * 
     * @param sourceCode The source code to compile and load in memory.<br>
     *                   The first class in the source code is used as fully
     *                   qualified class name. Otherwise, call method
     *                   {@link #compileInMemory(String, String, Set, String)}
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return a list with the compiled classes in memory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code is {@code null} or empty string
     *                        2) An error occurred while identifying the fully
     *                           qualified class name from the source code
     *                        3) The array with the files that are needed to
     *                           compile if not {@code null} has an element that
     *                           does not exist or does not have read access<br>
     *                        4) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        5) A compilation error occurs
     */
    public static List<Class<?>> compileInMemory(String    sourceCode,
                                                 Set<File> requiredSourceFiles,
                                                 String    classpath)
           throws ErrorException {
        // Make sure that the source code is not null and not an empty string
        if (sourceCode == null) {
            throw new ErrorException("Argument 'sourceCode' is null");
        }
        sourceCode = sourceCode.trim();
        if (sourceCode.isEmpty()) {
            throw new ErrorException("String argumemt 'sourceCode' is empty");
        }
        
        // Find the fully qualified class name and then append .java
        String fullyQualifiedClassFilename =
                                  getFullyQualifiedClassFilename(sourceCode);
        
        return compileInMemory(sourceCode,
                               fullyQualifiedClassFilename,
                               requiredSourceFiles,
                               classpath);
    }
    
    /**
     * Compiles in memory the given file.
     * 
     * @param sourceCodeFile The source code file to compile and load in memory
     * @param sourcepath The path to look for user source files or {@code null}
     *                   for no additional user source files
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * @param options Any additional compiler options including their values
     *                (i.e., each element should be in the form 
     *                "-option {@literal <value>}" or {@code null} in case there
     *                are no additional options
     *  
     * @return a list with the compiled classes in memory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code file does not exist, does
     *                           not have read access or is a directory rather
     *                           than a regular file<br>
     *                        2) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        3) A compilation error occurs
     */
    public static List<Class<?>> compileInMemory(File         sourceCodeFile,
                                                 String       sourcepath,
                                                 String       classpath,
                                                 List<String> options)
            throws ErrorException {
        // Make sure that the source code file exists and has read access
        FileUtil.validateFileToRead(sourceCodeFile);
        
        
        // 1) Create the appropriate file manager that can handle bytecodes
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager javaFileManager =
                    javaCompiler.getStandardFileManager(null, //diagnostic lsner
                                                        null, //default locale
                                                        null);//default charset
        MemoryJavaFileManager fileManager =
                                  new MemoryJavaFileManager(javaFileManager);
        
        // 2) DiagnosticCollector
        DiagnosticCollector<JavaFileObject> diagnosticCollector =
                                      new DiagnosticCollector<JavaFileObject>();
        
        // 3) Compiler options
        List<String> compilerOptions = new ArrayList<String>();
        // Sourcepath
        if (sourcepath != null) {
            DirectoryUtil.validateDirToRead(sourcepath);
            
            compilerOptions.add(SOURCEPATH_OPTION);
            compilerOptions.add(sourcepath);
        }
        
        // Classpath
        if (classpath != null && !classpath.trim().isEmpty()) {
            assertClasspath(classpath);
            
            compilerOptions.add(CLASSPATH_OPTION);
            compilerOptions.add(classpath);
        }
        
        // Any other options provided as argument to this method
        if (options != null) {
            for (String option : options) {
                compilerOptions.add(option);
            }
        }
        
        // Prepare the compilation units
        List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
        String sourceCode = FileIOUtil.readFile(sourceCodeFile);
        String fullyQualifiedClassFilename = 
                             getFullyQualifiedClassFilename(sourceCodeFile);
        compilationUnits.add(
            MemoryJavaFileManager.getJavaFileObject(fullyQualifiedClassFilename,
                                                    sourceCode));
        
        // Create a compilation task
        CompilationTask compilationTask =
              javaCompiler.getTask(null, // std error output (System.err)
                                   fileManager,
                                   diagnosticCollector,
                                   compilerOptions,
                                   null, // no classes for annotation processing
                                   compilationUnits);
        
        boolean success = compilationTask.call();
        if (!success) {
            StringBuilder errorMsg = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic:
                                        diagnosticCollector.getDiagnostics()) {
                String err = String.format("Compilation error: Line %d - %s%n",
                                           diagnostic.getLineNumber(),
                                           diagnostic.getMessage(null));
                errorMsg.append(err);
            }
            
            try {
                fileManager.close();
            }
            catch (IOException exp) {
            }
            throw new ErrorException(errorMsg.toString());
        }
        
        Map<String, byte[]> classbyteLookup = fileManager.getClassbyteLookup();
        try {
            fileManager.close();
        }
        catch (IOException exp) {
        }
        
        MemoryClassLoader classLoader = new MemoryClassLoader(classbyteLookup,
                                                              classpath);
        List<Class<?>> result;
        try { 
            result = classLoader.loadAllClasses();
        }
        catch (ClassNotFoundException cnfe) {
            throw new ErrorException("Class not found. Details:" + NEW_LINE
                                    + cnfe.getMessage());
        }
        finally {
            // Release the class loader resources
            try {
               classLoader.close();
            }
            catch (IOException ignored) {
            }
        }
        
        return result;
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Compiles the given source code in memory and returns {@code null} if it
     * succeeds or a String with the error message if it fails.
     *  
     * @param sourceCode The source code to compile
     * @param fullyQualifiedClassFilename The fully qualified class name for the
     *                                    provided source code appended by
     *                                    '.java'
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpathJarFiles A list of jar-files to include in the classpath
     *                          or {@code null} if there are no jar-files to use
     * 
     * @return {@code null} if the given source code compiles successfully or a
     *         String with the error message if it fails
     */
    private static String isCompilable(String     sourceCode,
                                       String     fullyQualifiedClassFilename,
                                       Set<File>  requiredSourceFiles,
                                       List<File> classpathJarFiles) {
        try {
            String classpath = null;
            if (classpathJarFiles != null && !classpathJarFiles.isEmpty()) {
                classpath = "";
                File[] jarFiles =
                          classpathJarFiles.toArray(new File[0]);
                for (int i = 0; i < jarFiles.length - 1; i++) {
                    classpath += jarFiles[i].getPath() + File.pathSeparator;
                }
                classpath += jarFiles[jarFiles.length-1].getPath();
            }
            
            Compiler.compileInMemory(sourceCode,
                                     fullyQualifiedClassFilename,
                                     requiredSourceFiles,
                                     classpath); // classpath
            return null;
        }
        catch (ErrorException ee) {
            // The source code is non-compilable
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(ee.getMessage() + NEW_LINE);
            errorMessage.append(STRAIGHT_LINE_SEPARATOR + NEW_LINE);
            String[] lines = sourceCode.split(NEW_LINE);
            int numOfLines = lines.length;
            int digits = String.valueOf(numOfLines).length();
            
            for (int i = 0; i < numOfLines; i++) {
                errorMessage.append("L");
                int alignmentSpaces = digits - String.valueOf(i + 1).length();
                while (alignmentSpaces > 0) {
                    errorMessage.append(SPACE);
                    alignmentSpaces--;
                }
                errorMessage.append((i + 1) + TAB + lines[i] + NEW_LINE);
            }
            return errorMessage.toString();
        }
    }
    
    /**
     * Compiles in memory the given source code.
     * 
     * @param sourceCode The source code to compile and load in memory
     * @param fullyQualifiedClassFilename The fully qualified class name for the
     *                                    provided source code appended by
     *                                    '.java'
     * @param requiredSourceFiles A set with source code files that are required
     *                            to compile the source code or {@code null} if
     *                            there are no such dependencies
     * @param classpath The path to look for user class files, and (optionally)
     *                  annotation processors and source files or {@code null}
     *                  to use the current user directory.
     * 
     * @return a list with the compiled classes in memory
     * 
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The source code is {@code null} or empty string
     *                        2) The fully qualified class name is {@code null}
     *                           or empty string
     *                        3) The array with the files that are needed to
     *                           compile if not {@code null} has an element that
     *                           does not exist or does not have read access<br>
     *                        4) The classpath, if specified, does not exist or
     *                           does not have read access<br>
     *                        5) A compilation error occurs
     */
    private static
           List<Class<?>> compileInMemory(String    sourceCode,
                                          String    fullyQualifiedClassFilename,
                                          Set<File> requiredSourceFiles,
                                          String    classpath)
           throws ErrorException {
        // P A R T  1 :   I N P U T   A R G U M E N T   V A L I D A T I O N
        // Make sure that the source code is not null and not an empty string
        if (sourceCode == null) {
            throw new ErrorException("Argument 'sourceCode' is null");
        }
        sourceCode = sourceCode.trim();
        if (sourceCode.isEmpty()) {
            throw new ErrorException("String argumemt 'sourceCode' is empty");
        }
        
        // Make sure that the class name is not null and not an empty string
        if (fullyQualifiedClassFilename == null) {
            throw new ErrorException("Argument 'fullyQualifiedClassFilename' "
                                   + "is null");
        }
        fullyQualifiedClassFilename = fullyQualifiedClassFilename.trim();
        if (fullyQualifiedClassFilename.isEmpty()) {
            throw new ErrorException("String argumemt "
                                   + "'fullyQualifiedClassFilename' is empty");
        }
        
        // Make sure that the files that are needed to compile exist and have
        // read access
        List<File> fileDependencies = new ArrayList<File>();
        if (requiredSourceFiles != null) {
            for (File file : requiredSourceFiles) {
                if (file != null &&
                    file.getName().toLowerCase().endsWith(JAVA_FILE_EXTENSION)) {
                    // Make sure that the file exists and has read access
                    FileUtil.validateFileToRead(file);
                    fileDependencies.add(file);
                }
            }
        }
        
        // Make sure that the files or directories in the classpath exists and
        // have read access
        if (classpath != null && !classpath.trim().isEmpty()) {
            assertClasspath(classpath);
        }
        
        // P A R T  2 :   C O M P I L A T I O N
        // 1) Create the appropriate file manager that can handle bytecodes
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager javaFileManager =
                    javaCompiler.getStandardFileManager(null, //diagnostic lsner
                                                        null, //default locale
                                                        null);//default charset
        
        MemoryJavaFileManager fileManager =
                                  new MemoryJavaFileManager(javaFileManager);
        
        // 2) DiagnosticCollector
        DiagnosticCollector<JavaFileObject> diagnosticCollector =
                                      new DiagnosticCollector<JavaFileObject>();
        
        // 3) Compiler options
        List<String> compilerOptions = new ArrayList<String>();
        // Classpath
        if (classpath != null && !classpath.trim().isEmpty()) {
            assertClasspath(classpath);
            
            compilerOptions.add(CLASSPATH_OPTION);
            compilerOptions.add(classpath);
        }
        
        // Prepare the compilation units
        List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
        if (!fileDependencies.isEmpty()) {
            Iterable<? extends JavaFileObject> dependencyCompilationUnits =
                 javaFileManager.getJavaFileObjectsFromFiles(fileDependencies);
            for (JavaFileObject javaFileObject : dependencyCompilationUnits) {
                 compilationUnits.add(javaFileObject);
            }
        }
        compilationUnits.add(
            MemoryJavaFileManager.getJavaFileObject(fullyQualifiedClassFilename,
                                                    sourceCode));
        
        // Create a compilation task
        CompilationTask compilationTask =
              javaCompiler.getTask(null, // std error output (System.err)
                                   fileManager,
                                   diagnosticCollector,
                                   compilerOptions,
                                   null, // no classes for annotation processing
                                   compilationUnits);
        
        boolean success = compilationTask.call();
        if (!success) {
            StringBuilder errorMsg = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic:
                                        diagnosticCollector.getDiagnostics()) {
                String err = String.format("Compilation error: Line %d - %s%n",
                                           diagnostic.getLineNumber(),
                                           diagnostic.getMessage(null));
                errorMsg.append(err);
            }
            
            try {
                fileManager.close();
            }
            catch (IOException exp) {
            }
            throw new ErrorException(errorMsg.toString());
        }
        
        Map<String, byte[]> classbyteLookup = fileManager.getClassbyteLookup();
        try {
            fileManager.close();
        }
        catch (IOException exp) {
        }
        
        MemoryClassLoader classLoader = new MemoryClassLoader(classbyteLookup,
                                                              classpath);
        List<Class<?>> result;
        try { 
            result = classLoader.loadAllClasses();
        }
        catch (ClassNotFoundException cnfe) {
            throw new ErrorException("Class not found. Details:" + NEW_LINE
                                    + cnfe.getMessage());
        }
        finally {
            // Release the class loader resources
            try {
               classLoader.close();
            }
            catch (IOException ignored) {
            }
        }
        
        return result;
    }
    
    /**
     * Ensures that the files or directories in the given classpath exist and
     * have read permission.
     * 
     * @param classpath The classpath to validate
     * 
     * @throws ErrorException in case a file or directory in the classpath does
     *                        not exist or does not have read access 
     */
    private static void assertClasspath(String classpath)
            throws ErrorException {
        String[] paths = classpath.trim().split(File.pathSeparator);
        
        // Make sure that the paths in the classpath exist and are readable
        for (String path : paths) {
            path = path.trim();
            File classpathDirOrJar = new File(path);
            if (!classpathDirOrJar.exists()) {
                throw new ErrorException("'" + path + "' does not exist");
            }
            if (!classpathDirOrJar.canRead()) {
                throw new ErrorException("'" + path
                                       + "' does not have read access");
            }
        }
    }
}
