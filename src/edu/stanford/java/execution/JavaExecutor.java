/*
 * File          : JavaExecutor.java
 * Author        : Charis Charitsis
 * Creation Date : 23 November 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.execution;

// Import Java SE classes
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.JavaSourceCode;
import edu.stanford.javaparser.body.ClassOrInterface;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Executes a java program
 */
public class JavaExecutor
{
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Runs the 'main' method (i.e., 'public static void main(String[] args)')
     * of the class in the given source code.
     * 
     * @param code The source code with the class to execute (its 'main' method)
     * 
     * @throws ClassNotFoundException if the class cannot be located
     * @throws NoSuchMethodException in case there is no main method
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path name is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br> 
     *                        4) An error invoking the 'public static void main'
     *                           method
     */
    public static void runMainInCode(String code)
           throws ClassNotFoundException, NoSuchMethodException, ErrorException{
        JavaSourceCode sourceCode = new JavaSourceCode(code);
        List<ClassOrInterface> classes = sourceCode.getClasses();
        if (classes.isEmpty()) {
            throw new ErrorException("There are no classes in code:" + NEW_LINE
                                   + code);
        }
        String fqcn = classes.get(0).getName();
        runMain(fqcn);
    }
    
    /**
     * Runs the 'main' method (i.e., 'public static void main(String[] args)')
     * of the class in the given source file.
     * 
     * @param sourceFile The file with the source code to execute (its 'main'
     *                   method)
     * 
     * @throws ClassNotFoundException if the class cannot be located
     * @throws NoSuchMethodException in case there is no main method
     * @throws ErrorException in any of the following cases:<br>
     *                        1) The provided file path name is {@code null}<br> 
     *                        2) The file denoted by the provided path name does
     *                           not exist<br>
     *                        3) An error occurred while parsing the file<br> 
     *                        4) The source file has no class declarations<br>
     *                        4) An error invoking the 'public static void main'
     *                           method
     */
    public static void runMainInFile(File sourceFile)
           throws ClassNotFoundException, NoSuchMethodException, ErrorException{
        JavaFile javaFile = new JavaFile(sourceFile.getPath());
        List<ClassOrInterface> classes = javaFile.getClasses();
        if (classes.isEmpty()) {
            throw new ErrorException("File '" + sourceFile.getPath()
                                   + "' contains no classes");
        }
        String fqcn = classes.get(0).getName();
        runMain(fqcn);
    }
    
    /**
     * Runs the 'main' method (i.e., 'public static void main(String[] args)')
     * of the class with the given name.
     * 
     * @param fullyQualifiedClassName The fully qualified name for the class to
     *                                execute (its 'main' method)
     * 
     * @throws ClassNotFoundException if the class cannot be located
     * @throws NoSuchMethodException in case there is no main method
     * @throws ErrorException in case of an error invoking method
     *                        'public static void main'
     */
    public static void runMain(String fullyQualifiedClassName)
           throws ClassNotFoundException, NoSuchMethodException, ErrorException{
        if (fullyQualifiedClassName == null ||
            fullyQualifiedClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Argument "
                                             + "'fullyQualifiedClassName' is "
                                             + "null or empty String");
        }
        Class<?> clazz = Class.forName(fullyQualifiedClassName);
        runMain(clazz);
    }
    
    /**
     * Runs the 'main' method (i.e., 'public static void main(String[] args)')
     * of the given class.
     * 
     * @param clazz The class to invoke the 'main' method for
     * 
     * @throws NoSuchMethodException in case there is no main method
     * @throws ErrorException in case of an error invoking method
     *                        'public static void main'
     */
    public static void runMain(Class<?> clazz)
           throws NoSuchMethodException, ErrorException {
        if (clazz == null) {
            throw new IllegalArgumentException("Argument 'clazz' is null");
        }
        
        Method mainMethod = clazz.getMethod("main", String[].class);
        runMain(mainMethod);
    }
    
    /**
     * Runs the 'main' method (i.e., 'public static void main(String[] args)')
     * that is provided.
     * 
     * @param mainMethod The 'main' method to run
     * 
     * @throws ErrorException in case of an error invoking method
     *                        'public static void main'
     */
    public static void runMain(Method mainMethod)
           throws ErrorException {
        if (mainMethod == null) {
            throw new IllegalArgumentException("Argument 'mainMethod' is null");
        }
        
        String[] params = null;
        try {
            mainMethod.invoke(null, (Object) params);
        }
        catch (IllegalAccessException   |
               IllegalArgumentException | InvocationTargetException e) {
            throw new ErrorException("Error invoking 'public static void main'."
                                   + " Details:" + NEW_LINE + e.getMessage());
               
        }
    }
}
