/*
 * File          : MemoryJavaCompiler.java
 * Author        : Charis Charitsis
 * Creation Date : 23 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.compile;

// Import Java SE classes
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
// Import constants
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 */
public class MemoryJavaCompiler
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The module that compiles Java code
     */
    private final JavaCompiler            javaCompiler;
    /**
     * This file manager creates file objects
     */
    private final StandardJavaFileManager javaFileManager;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a new MemoryJavaCompiler.
     */
    public MemoryJavaCompiler() {
        javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new RuntimeException("Could not get Java compiler. Ensure "
                                     + "that JDK is used instead of JRE.");
        }
        javaFileManager = javaCompiler.getStandardFileManager(null, null, null);
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Compiles a single static method, loads it in memory and returns a module
     * that represents that method.
     * 
     * @param methodName The name of the static method
     * @param fullyQualifiedClassName The fully qualified class name
     * @param source The source code for the static method to compile
     * 
     * @return a module that represents the static method after compiling it and
     *         loading it in memory
     * 
     * @throws ClassNotFoundException if the class where the static method
     *                                belongs is not found to load it in memory
     */
    public Method compileStaticMethod(String methodName, 
                                      String fullyQualifiedClassName,
                                      String source)
           throws ClassNotFoundException {
        String filename = fullyQualifiedClassName + JAVA_FILE_EXTENSION;
        Map<String, byte[]> classbyteLookup = compile(filename, source);
        
        Class<?> clazz = null;
        try (
            MemoryClassLoader classLoader =
                                   new MemoryClassLoader(classbyteLookup);
        ) {
            clazz = classLoader.loadClass(fullyQualifiedClassName);
        }
        catch (IOException ioe) {
        }
        if (clazz == null) {
            throw new ClassNotFoundException("Error loading class "
                                           + fullyQualifiedClassName);
        }
        
        Method[] methods = clazz.getDeclaredMethods();
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (!method.canAccess(null)) { // null means static method
                    method.setAccessible(true);
                }
                return method;
            }
        }
        throw new NoSuchMethodError(methodName);
    }
    
    /**
     * Compiles the given source code and returns a map where the key is the
     * classname and the value is the class bytes.
     *
     * @param filename The name of the file for the source code which is the
     *                 fully qualified class name with a .java at the end (e.g.,
     *                 'edu.stanford.java.MemoryJavaCompiler.java')
     * @param source The source code to compile
     * 
     * @return a map where the key is the classname and the value is the class
     *         bytes
     */
    public Map<String, byte[]> compile(String filename,
                                       String source) {
        return compile(filename,
                       source,
                       new PrintWriter(System.err),
                       null,
                       null);
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Compiles the given source code and returns a map where the key is the
     * classname and the value is the class bytes.
     *
     * @param filename The name of the file for the source code which is the
     *                 fully qualified class name with a .java at the end (e.g.,
     *                 'edu.stanford.java.MemoryJavaCompiler.java')
     * @param source The source code to compile
     * @param err Error writer where diagnostic messages are written
     * @param sourcePath Location of additional .java source files
     * @param classpath Location of additional .class files
     * 
     * @return a map where the key is the classname and the value is the class
     *         bytes
     */
    private Map<String, byte[]> compile(String filename,
                                        String source,
                                        Writer err,
                                        String sourcePath,
                                        String classpath) {
        // Create a new memory JavaFileManager
        MemoryJavaFileManager fileManager =
                                  new MemoryJavaFileManager(javaFileManager);
        
        // Prepare the compilation unit
        List<JavaFileObject> compileUnits = new ArrayList<JavaFileObject>(1);
        compileUnits.add(MemoryJavaFileManager.getJavaFileObject(filename,
                                                                 source));
        
        return compile(compileUnits,
                       fileManager,
                       err,
                       sourcePath,
                       classpath);
    }
    
    /**
     * Compiles the given source code and returns a map where the key is the
     * classname and the value is the class bytes.
     *
     * @param compileUnits The compile units (i.e., .java files + .class files)
     * @param fileManager The module that keeps compiled .class bytes in memory
     * @param err Error writer where diagnostic messages are written
     * @param sourcePath Location of additional .java source files
     * @param classpath Location of additional .class files
     * 
     * @return a wap where the key is the classname and the value is the class
     *         bytes
     */
    private Map<String, byte[]> compile(List<JavaFileObject>  compileUnits,
                                        MemoryJavaFileManager fileManager,
                                        Writer                err,
                                        String                sourcePath,
                                        String                classpath) {
        // Collect errors, warnings etc.
        DiagnosticCollector<JavaFileObject> diagnostics =
                                     new DiagnosticCollector<JavaFileObject>();
        
        // javac options
        List<String> options = new ArrayList<String>();
        options.add("-Xlint:all");
        options.add("-deprecation");
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath);
        }
        
        if (classpath != null) {
            options.add("-classpath");
            options.add(classpath);
        }
        
        // Create a compilation task
        CompilationTask compilationTask = javaCompiler.getTask(err,
                                                               fileManager,
                                                               diagnostics,
                                                               options,
                                                               null,
                                                               compileUnits);
        
        if (compilationTask.call() == false) {
            PrintWriter perr = new PrintWriter(err);
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                perr.println(diagnostic);
            }
            perr.flush();
            return null;
        }
        
        Map<String, byte[]> classbyteLookup = fileManager.getClassbyteLookup();
        try {
            fileManager.close();
        }
        catch (IOException exp) {
        }
        
        return classbyteLookup;
    }
}