/*
 * File          : CyclomaticAnalyzer.java
 * Author        : Charis Charitsis
 * Creation Date : 18 July 2021
 * Last Modified : 24 September 2021
 */
package edu.stanford.studentcode.cyclomatic;

// Import Java SE classes
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.java.compile.Compiler;
// Import org.objectweb.asm classes
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
// Import constants
import static org.objectweb.asm.Opcodes.GOTO;
import static edu.stanford.constants.Constants.CLASS_FILE_EXTENSION;
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.TEMP_DIR;

/**
 * Performs cyclomatic analysis
 */
public class CyclomaticAnalyzer
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /** Single instance of the CyclomaticAnalyzer */
    private static final CyclomaticAnalyzer INSTANCE = new CyclomaticAnalyzer();
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Prevent external constructor calls.
     */
    private CyclomaticAnalyzer() {
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return the single CyclomaticAnalyzer instance
     */
    public static final CyclomaticAnalyzer getInstance() {
        return INSTANCE;
    }
    
    public static void main(String[] args) {
        File sourceFile = new File("C:\\Eclipse Projects\\Assignment3_Breakout\\src\\Breakout.java");
        List<File> jarFiles = new ArrayList<File>();
        jarFiles.add(new File("C:\\Eclipse Projects\\Assignment3_Breakout\\acm.jar"));
        
        CyclomaticAnalyzer analyzer = CyclomaticAnalyzer.getInstance();
        ClassMetric classMetric;
        try {
            classMetric = analyzer.getClassMetric(sourceFile, jarFiles);
            System.out.println("Median CY C: " + classMetric.getMedianCyclomaticComplexity());
            System.out.println("Avg CYC    : " + classMetric.getAvgCyclomaticComplexity());
            System.out.println("Std Dev CYC: " + classMetric.getCyclomaticComplexityStdDev());
            System.out.println("Max CYC    : " + classMetric.getMaxCyclomaticComplexity());
        }
        catch (ErrorException ee) {
            System.out.println(ee.getMessage());
        }
    }
    

    /**
     * Given a Java source file and a list of jar files that are required to
     * compile it (if any), it returns the metrics for the class. 
     * 
     * @param sourceFile The source code file
     * @param jarFiles The jar files that are required to compile this file or 
     *                 {@code null} if there are no jar files required
     * 
     * @return the metrics for the class declared in the source file
     * 
     * @throws ErrorException in case of compile error or in case of an error
     *                        reading the bytecode once the file is compiled
     */
    public ClassMetric getClassMetric(File             sourceFile,
                                      Collection<File> jarFiles)
           throws ErrorException {
        String classpath = null;
        if (jarFiles != null) {
            classpath = "";
            Iterator<File> itr = jarFiles.iterator();
            while (itr.hasNext()) {
                classpath += itr.next().getPath();
                if (itr.hasNext()) {
                    classpath += File.pathSeparator;
                }
            }
        }
        
        if (sourceFile == null) {
            throw new IllegalArgumentException("'sourceFile' is null");
        }
        String filename = sourceFile.getName();
        int endIndex = filename.indexOf(JAVA_FILE_EXTENSION);
        if (endIndex == -1) {
            throw new IllegalArgumentException(filename + " is not a '"
                                             + JAVA_FILE_EXTENSION + "' file");
        }
        filename = filename.substring(0, endIndex);
        
        String destDirPathname = TEMP_DIR;
        Set<File> sourceFiles = new HashSet<File>();
        sourceFiles.add(sourceFile);
        
        Compiler.compile(classpath,
                         destDirPathname,
                         null, // options
                         sourceFiles);
        
        File compiledFile = new File(destDirPathname + File.separator
                                   + filename + CLASS_FILE_EXTENSION);
        
        if (!compiledFile.exists()) {
            throw new ErrorException(compiledFile.getPath() + " not found");
        }
        
        return getClassMetric(compiledFile);
    }
    
    /**
     * Returns an ClassMetric Object containing the metrics for the given class
     * file.
     * 
     * @param classFile The class file to process
     * 
     * @return a ClassMetric object containing the metrics for the given class
     *         file
     */
    public ClassMetric getClassMetric(File classFile)
           throws ErrorException {
        try (
                InputStream classFileInStream = new FileInputStream(classFile);
            ) {
                return getClassMetric(classFileInStream);
            }
            catch (IOException ioe) {
                throw new ErrorException(ioe.getMessage());
            }
    }
    
    /**
     * Returns an ClassMetric Object containing the metrics for the given class
     * file.
     * 
     * @param classFile The input stream for the class file to process
     * 
     * @return a ClassMetric object containing the metrics for the given class
     *         file
     */
    public ClassMetric getClassMetric(InputStream classFile)
           throws ErrorException {
        ClassMetric classMetric = null;
        
        try {
            ClassReader classReader = new ClassReader(classFile);
            ClassNode   classNode   = new ClassNode();
            classReader.accept(classNode, 0);
            
            List<MethodNode> methodNodes = classNode.methods;
            String packageName = getPackageName(classNode);
            String className   = getClassName(classNode);
            classMetric = new ClassMetric(className,
                                          packageName,
                                          getMethodMetric(methodNodes));
        }
        catch(IOException ioe) {
            throw new ErrorException(ioe.getMessage());
        }
        
        return classMetric;
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a list of method nodes, it method returns a list with the metrics
     * for each method.
     *  
     * @param methods list of method nodes
     * @return a list with the metrics for the provided methods
     */
    private List<MethodMetric> getMethodMetric(List<MethodNode> methodNodes) {
        List<MethodMetric> methodMetric = new ArrayList<MethodMetric>() ;
        for (MethodNode methodNode : methodNodes) {
            int cyclomaticComplexity = 1 + getNumOfJumpInstuctions(methodNode) 
                                         + getNumOfTryCatchBlocks(methodNode)
                                         + getNumOfExceptions(methodNode)
                                         + getNumOfCaseLabels(methodNode);
            
            methodMetric.add(new MethodMetric(methodNode.name,
                                              cyclomaticComplexity,
                                              methodNode.instructions.size()));
        }
        
        return methodMetric;
    }
    
    /**
     * Given a class node it returns the package name for this class
     * 
     * @param classNode The class node
     * 
     * @return the package name for this class
     */
    private static String getPackageName(ClassNode classNode) {
        String packageName = classNode.name;
        
        int endIndex = packageName.lastIndexOf("/");
        if (endIndex != -1) {
            packageName = packageName.substring(0, endIndex);
            packageName = packageName.replaceAll("/", ".");
        }
        else {
            packageName = "default";
        }
        
        return packageName;
    }
    
    /**
     * Given a class node it returns the name for this class
     * 
     * @param classNode The class node
     * 
     * @return the name for this class
     */
    private String getClassName(ClassNode classNode) {
        String className = classNode.name;
        
        int startIndex = className.lastIndexOf("/");
        if (startIndex != -1) {
            className = className.substring(className.lastIndexOf("/") + 1);
        }
        
        return className;
    }
    
    /**
     * Returns the number of jump instructions in the method.<br>
     * A jump instruction is an instruction that may jump to another
     * instruction.
     *  
     * @param methodNode The method node to get the number of jump instructions
     *                   for
     * 
     * @return the number of jump instructions in the method.
     */
    private int getNumOfJumpInstuctions(MethodNode methodNode) {
        InsnList insnNodes = methodNode.instructions;
        
        int jumpCount = 0;
        for (AbstractInsnNode insnNode : insnNodes) {
            if (insnNode instanceof JumpInsnNode &&
                insnNode.getOpcode() != GOTO) {
                jumpCount++;
            }
        }
        
        return jumpCount;
    }
    
    /**
     * Returns the number of case labels in the switch statements in the method.
     *  
     * @param methodNode The method node to get the number of case labels for
     * 
     * @return the number of case labels in the method.
     */
    private int getNumOfCaseLabels(MethodNode methodNode) {
        InsnList insnNodes = methodNode.instructions;
        
        int caseCount = 0;
        for (AbstractInsnNode insnNode : insnNodes) {
            if (insnNode instanceof TableSwitchInsnNode) {
                List<LabelNode> caseLabelNodes =
                                    ((TableSwitchInsnNode)insnNode).labels;
                caseCount += caseLabelNodes.size();
            }
        }
        
        return caseCount;
    }
    
    /**
     * Returns the number of try-catch blocks in the method.
     *  
     * @param methodNode The method node to get the number of try-catch
     *                   statements for
     * 
     * @return the number of case labels in the method.
     */
    private int getNumOfTryCatchBlocks(MethodNode methodNode) {
        return methodNode.tryCatchBlocks.size();
    }
    
    /**
     * Returns the number of exceptions thrown in the method.
     *  
     * @param methodNode The method node to get the number of exceptions for
     * 
     * @return the number of exceptions thrown in the method.
     */
    private int getNumOfExceptions(MethodNode methodNode) {
         return methodNode.exceptions.size();
    }
}
