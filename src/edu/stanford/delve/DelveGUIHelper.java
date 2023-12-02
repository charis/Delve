/*
 * File          : DelveGUIHelper.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 20 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ImageIcon;
// Import com.github.javaparser classes
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
// Import custom classes
import edu.stanford.delve.Constants.HighCYCMethodCount;
import edu.stanford.exception.ErrorException;
import edu.stanford.java.execution.InstrumentationConfig;
import edu.stanford.javaparser.JavaFile;
import edu.stanford.javaparser.body.ClassOrInterface;
import edu.stanford.javaparser.body.Method;
import edu.stanford.javaparser.body.MethodCall;
import edu.stanford.javaparser.body.Variable;
import edu.stanford.util.UIUtil;
import edu.stanford.util.filesystem.DirectoryUtil;
import edu.stanford.util.filesystem.FileUtil;
import edu.stanford.util.io.FileIOUtil;
// Import constants
import static edu.stanford.constants.Constants.JAR_FILE_EXTENSION;
import static edu.stanford.constants.Constants.JAVA_FILE_EXTENSION;
import static edu.stanford.constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static edu.stanford.constants.Literals.DOT;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.SEMICOLON;
import static edu.stanford.constants.Literals.SPACE;
import static edu.stanford.constants.Literals.TAB;
import static edu.stanford.constants.Literals.UNDERSCORE;
import static edu.stanford.delve.Constants.COMPILE_DEST_DIR;
import static edu.stanford.delve.Constants.DELVE_IMAGE_DIR;
import static edu.stanford.delve.Constants.EXECUTION_RESULT_DIR;
import static edu.stanford.delve.Constants.GET_STATE_CLASS;
import static edu.stanford.delve.Constants.GET_STATE_METHOD_BODY;
import static edu.stanford.delve.Constants.GET_STATE_METHOD_SIGNATURE;
import static edu.stanford.delve.Constants.IMAGE_DIR;
import static edu.stanford.delve.Constants.INSTRUMENT_ALL_METHODS;
import static edu.stanford.delve.Constants.INSTRUMENTATION_CLASS_DECLARATION;
import static edu.stanford.delve.Constants.INSTRUMENTATION_CLASS_NAME;
import static edu.stanford.delve.Constants.INSTRUMENTATION_IMPORTS;
import static edu.stanford.delve.Constants.INSTRUMENTATION_PACKAGE;
import static edu.stanford.delve.Constants.INSTRUMENTATION_PUBLIC_METHODS;
import static edu.stanford.delve.Constants.INSTRUMENTATION_VARIABLES;
import static edu.stanford.delve.Constants.MAIN_METHOD_START_INSTRUMENTATION;
import static edu.stanford.delve.Constants.MAIN_METHOD_END_INSTRUMENTATION;
import static edu.stanford.delve.Constants.PROGRAM_STATE_CLASS;
import static edu.stanford.delve.Constants.STUDENT_EXTRA_DIRS;
import static edu.stanford.delve.Constants.TO_STRING_METHOD_SIGNATURE;
import static edu.stanford.gui.Constants.IMAGE_PACKGAGE;
import static edu.stanford.delve.Constants.HighCYCMethodCount.*;

/**
 * Collection of helper methods for DelveGUI.
 */
public class DelveGUIHelper
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /**
     * The minimum threshold for an ivar or a method to be considered common
     * among the submitted programs.<br>
     * If 95% or more of them have this ivar or method then it is considered
     * 'common'.<br>
     * The reason is that 100% cannot be always reached (e.g., incomplete
     * submission etc.) 
     */
    private static final double COMMON_MIN_THRESHOLD = 0.95;
    
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
     * Given a time expressed in seconds it returns the time formatted as:<br>
     * {@literal xx hrs: xx mins: xx secs}
     *  
     * @param timeInSeconds The time expressed in seconds
     * 
     * @return the formatted time showing the hours, the minutes and the seconds
     */
    protected static String getTimeFromSeconds(int timeInSeconds) {
        StringBuilder time = new StringBuilder();
        int hours   = timeInSeconds / 3600;
        if (hours <= 9) {
            time.append("0" + hours + "hr:");
        }
        else {
            time.append(hours + "hrs:");
        }
        
        timeInSeconds = timeInSeconds - (hours * 3600);
        int minutes = timeInSeconds / 60;
        if (minutes <= 9) {
            time.append("0" + minutes + "':");
        }
        else {
            time.append(minutes + "':");
        }
        
        int seconds = timeInSeconds - (minutes * 60);
        if (seconds <= 9) {
            time.append("0" + seconds + "\"");
        }
        else {
            time.append(seconds + "\"");
        }
        
        return time.toString();
    }
    
    /**
     * Processes the given submission folder and produces/returns a report to
     * display on screen.
     *  
     * @param submissionDir The submission folder to process
     * @param lastSnapshot The last snapshot in the submission folder for the
     *                     file with the most snapshots (note: a submission can
     *                     contain multiple files and each has its snapshots)
     * @param snapshotTimeDiffs A with the student snapshots as keys and the
     *                          times the student worked on each snapshot as
     *                          values
     * @param compilableFiles A set with the submission files that are
     *                        compilable or {@code null} to skip this piece of
     *                        information (e.g., when the classpath is not set)
     * 
     * @return a report about the given submission folder
     */
    protected static String
              getSubmissionTimeReport(File                 submissionDir,
                                      JavaFile             lastSnapshot,
                                      Map<String, Integer> snapshotTimeDiffs,
                                      Set<String>          compilableFiles) {
        StringBuilder report = new StringBuilder();
        
        List<File> files;
        try {
            files = DirectoryUtil.listFiles(submissionDir,
                                            JAVA_FILE_EXTENSION);
        }
        catch (ErrorException ee) {
            UIUtil.showError(ee.getMessage(),
                             "Error retrieving java files under '"
                           + submissionDir.getName() + "'");
            return report.toString();
        }
        
        report.append("Number of snapshots: " + files.size());
        report.append(NEW_LINE + NEW_LINE);
        
        int totalTime = 0;
        StringBuilder timeReport = new StringBuilder();
        String spaces = TAB + TAB + TAB + TAB + TAB + TAB;
        timeReport.append(TAB + "Filename" + spaces + "Time Diff"  + TAB + TAB
                                                    + "Total Time");
        if (compilableFiles != null) {
            timeReport.append(TAB + TAB + SPACE + SPACE + "Status");
        }
        timeReport.append(NEW_LINE);
        int length = "Filename".length() + spaces.length();
        boolean start = true;
        // Find how many characters it takes to show the time
        int timelength = DelveGUIHelper.getTimeFromSeconds(totalTime).length();
        // File name w/o the snapshot order number and the file extension
        String filenameBase = null;
        for (String snapshotFilename : snapshotTimeDiffs.keySet()) {
            if (filenameBase == null) {
                int endIndex = snapshotFilename.lastIndexOf(UNDERSCORE);
                filenameBase = snapshotFilename.substring(0, endIndex);
            }
            timeReport.append(TAB + snapshotFilename);
            int alignmentSpaces = length - snapshotFilename.length();
            while (alignmentSpaces > 0) {
                timeReport.append(SPACE);
                alignmentSpaces--;
            }
            int timeDiff = snapshotTimeDiffs.get(snapshotFilename);
            if (timeDiff == Util.BREAK_TIME) {
                if (start) {
                    timeReport.append("s t a r t");
                    start = false;
                }
                else {
                    timeReport.append("b r e a k");
                }
                alignmentSpaces = ("Time Diff" + TAB + TAB).length()
                                         + timelength - "s t a r t".length();
                while (alignmentSpaces > 0) {
                    timeReport.append(SPACE);
                    alignmentSpaces--;
                }
            }
            else {
                int mins = timeDiff / 60;
                int secs = timeDiff - (mins * 60);
                String secPrefix = secs <= 9? "0" : "";
                String timeDiffStr = mins + "':" + secPrefix + secs + "\"";
                timeReport.append(timeDiffStr);
                alignmentSpaces = "b r e a k".length() - timeDiffStr.length();
                while (alignmentSpaces > 0) {
                    timeReport.append(SPACE);
                    alignmentSpaces--;
                }
                timeReport.append(TAB + TAB);
                
                totalTime += timeDiff;
                timeReport.append(DelveGUIHelper.getTimeFromSeconds(totalTime));
                
            }
            if (compilableFiles != null) {
                if (compilableFiles.contains(snapshotFilename)) {
                    timeReport.append(TAB + TAB + "Compilable");
                }
                else {
                    timeReport.append(TAB + TAB + "Non-Compilable");
                }
            }
            timeReport.append(NEW_LINE);
        }
        
        
        report.append("Total time: "        +
                                  DelveGUIHelper.getTimeFromSeconds(totalTime));
        report.append(NEW_LINE);
        if (lastSnapshot != null) {
            report.append("Last snapshot: "
                        + lastSnapshot.getFilename() + NEW_LINE);
            report.append("Number of lines: "
                        + lastSnapshot.getNumOfCodeLines() + NEW_LINE);
            report.append("Number of methods: "
                        + lastSnapshot.getNumOfMethods() + NEW_LINE + NEW_LINE);
        }
        report.append(NEW_LINE);
        report.append("Time breakdown:" + NEW_LINE);
        report.append(timeReport.toString() + NEW_LINE);
        
        return report.toString();
    }
    
    /**
     * Given a map where the key is the snapshot file name in the submission and
     * the value is a list with the complex methods in the file, it returns a
     * map where the key is a snapshot file and the value is the object that
     * specifies what happened to the number of methods with high complexity
     * compared to the previous snapshot
     * 
     * @param complexMethodMap a map where the key is the snapshot file name in
     *                         the submission and the value is a list with the
     *                         complex methods in the file or an empty map if
     *                         there are no complex methods
     * 
     * @return a map where the key is a snapshot file and the value is the
     *         object that specifies what happened to the number of methods with
     *         high complexity compared to the previous snapshot
     */
    protected static Map<File, HighCYCMethodCount>
              getComplexityCountMap(Map<File, List<Method>> complexMethodMap) {
        Map<File, HighCYCMethodCount> result =
                                      new TreeMap<File, HighCYCMethodCount>();
        
        int prevNumOfComplexMethods = 0;// Complex methods for previous snapshot
        for (File snapshot : complexMethodMap.keySet()) {
            int numOfComplexMethods = complexMethodMap.get(snapshot).size();
            if (numOfComplexMethods > prevNumOfComplexMethods) {
                result.put(snapshot, INCREASED);
            }
            else if (numOfComplexMethods < prevNumOfComplexMethods) {
                result.put(snapshot, DECREASED);
            }
            else  {
                result.put(snapshot, NO_CHANGE);
            }
            prevNumOfComplexMethods = numOfComplexMethods;
        }
        
        return result;
    }
    
    /**
     * Given a set of Java files it finds the methods that are common among them
     * and returns their signatures.<br>
     * A method is considered common if it appears in at least 95% of the
     * submitted programs.
     * 
     * @param javaFiles The set of Java files to look for common methods
     * 
     * @return a set with the signatures for the user-defined methods that
     *         appear in 95% or more of the provided Java files
     */
    protected static Set<String> getCommonMethods(Set<JavaFile> javaFiles) {
        // Map where the key is a method signature and the value is the number
        // of submitted programs that have declared a method with that signature
        Map<String, Integer> methodHistogram = new HashMap<String, Integer>();
        for (JavaFile submittedProgram : javaFiles) {
            for (Method method : submittedProgram.getMethods()) {
                String methodSignature = method.getSignature(false, true);
                
                Integer appearancesSoFar = methodHistogram.get(methodSignature);
                if (appearancesSoFar == null) {
                    appearancesSoFar = 1;
                }
                else {
                    appearancesSoFar += 1;
                }
                methodHistogram.put(methodSignature, appearancesSoFar);
            }
        }
        
        int minValue = (int)(javaFiles.size() * COMMON_MIN_THRESHOLD);
        Set<String> commonMethodSignatures = new TreeSet<String>();
        for (String methodSignature : methodHistogram.keySet()) {
            if (methodHistogram.get(methodSignature) >= minValue) {
                commonMethodSignatures.add(methodSignature);
            }
        }
        
        return commonMethodSignatures;
    }
    
    /**
     * Given a set of Java files it finds the method calls that are common among
     * them and returns their signatures.<br>
     * A method call is considered common if it appears in at least 95% of the
     * submitted programs.
     * 
     * @param javaFiles The set of Java files to look for common method calls
     * 
     * @return a set with the signatures for the method calls that appear in 95%
     *         or more of the provided Java files
     */
    protected static Set<String> getCommonMethodCalls(Set<JavaFile> javaFiles) {
        // Map where the key is a signature of a called method and the value is
        // the number of submitted programs that have a method call with that
        // signature
        Map<String, Integer> methodCallHistogram =
                                       new HashMap<String, Integer>();
        int totalCount = 0;
        for (JavaFile submittedProgram : javaFiles) {
            try { 
                for (MethodCall methodCall : submittedProgram.getMethodCalls()){
                    String methodCallSignature =
                                 methodCall.getCalledMethodSignature(true);
                    
                    Integer appearancesSoFar =
                                 methodCallHistogram.get(methodCallSignature);
                    if (appearancesSoFar == null) {
                        appearancesSoFar = 1;
                    }
                    else {
                        appearancesSoFar += 1;
                    }
                    methodCallHistogram.put(methodCallSignature,
                                            appearancesSoFar);
                    totalCount++;
                }
            }
            catch (ErrorException | RuntimeException ignored) {
            }
        }
        
        int minValue = (int)(totalCount * COMMON_MIN_THRESHOLD);
        Set<String> commonMethodCallSignatures = new TreeSet<String>();
        for (String methodCallSignature : methodCallHistogram.keySet()) {
            if (methodCallHistogram.get(methodCallSignature) >= minValue) {
                commonMethodCallSignatures.add(methodCallSignature);
            }
        }
        
        return commonMethodCallSignatures;
    }
    
    /**
     * Given a set of Java files it finds the instance variables that are common
     * among them and returns their types and names.<br>
     * An instance variable is considered common if it is declared in at least
     * 95% of the submitted programs.
     * 
     * @param javaFiles The set of Java files to look for common instance
     *                  variables
     * 
     * @return a set with the type and name for the instance variables that
     *         appear in 95% or more of the provided Java files
     */
    protected static Set<String> getCommonIvars(Set<JavaFile> javaFiles) {
        // Map where the key is an ivar (type + name) and the value is the
        // number of submitted programs that have declared that ivar
        Map<String, Integer> ivarHistogram = new HashMap<String, Integer>();
        for (JavaFile submittedProgram : javaFiles) {
            for (Variable variable : submittedProgram.getInstanceVariables()) {
                String ivar = variable.getType().toString() + SPACE
                            + variable.getName();
                
                Integer appearancesSoFar = ivarHistogram.get(ivar);
                if (appearancesSoFar == null) {
                    appearancesSoFar = 1;
                }
                else {
                    appearancesSoFar += 1;
                }
                ivarHistogram.put(ivar, appearancesSoFar);
            }
        }
        
        int minValue = (int)(javaFiles.size() * COMMON_MIN_THRESHOLD);
        Set<String> commonIvars = new TreeSet<String>();
        for (String ivar : ivarHistogram.keySet()) {
            if (ivarHistogram.get(ivar) >= minValue) {
                commonIvars.add(ivar);
            }
        }
        
        return commonIvars;
    }
    
    /**
     * Given a list of files to use for code instrumentation, it identifies the
     * file with the correct program implementation (i.e., reference file) and
     * returns it.
     * 
     * @param instrumentationFiles The selected files for the code
     *                             instrumentation including the reference file
     * @param submittedPrograms The code final snapshots/submitted programs
     * 
     * @return the file with the correct program implementation (i.e., reference
     *         file)
     * 
     * @throws ErrorException in case of an error or in case the file with the
     *                        correct program implementation is missing from the
     *                        provided list of files
     */
    protected static File
              getReferenceProgram(Set<File>            instrumentationFiles,
                                  Collection<JavaFile> submittedPrograms)
              throws ErrorException {
        // Find the class name for the program that the students submitted
        String programClassName = getProgramClassname(submittedPrograms);
        if (programClassName == null) {
            throw new ErrorException("No class found for the student program");
        }
        
        for (File instrumentationFile : instrumentationFiles) {
            JavaFile javaFile = new JavaFile(instrumentationFile.getPath());
            String classOrInterfaceName = getClassOrInterfaceName(javaFile);
            
            if (classOrInterfaceName.equals(programClassName)) {
                return instrumentationFile; 
            }
        }
        
        throw new ErrorException("The program with correct implementation is "
                               + "missing");
    }
    
    /**
     * Given a list of files to use for code instrumentation, it creates and
     * returns the instrumentation configuration (i.e., structure with
     * information related to how the student program should be instrumented).
     * <br>
     * Note: The reference file with the correct program implementation must not
     *       be included in the provided list of files.
     * 
     * @param instrumentationFiles The selected files for the code
     *                             instrumentation
     * @param requiredSourceFiles Set with source code files that are required
     *                            to compile the code or an empty set if there
     *                            are no such dependencies
     * @param classpathJarFiles Jar-files to include in the classpath or an
     *                          empty list if there are no jar-file dependencies
     * @param referenceProgram The file with the correct program implementation
     *                         (i.e., reference file) 
     * 
     * @return the instrumentation configuration that specifies how the student
     *         program should be instrumented
     * 
     * @throws ErrorException in case the code instrumentation is invalid or
     *                        in case of an error copying the instrumented
     *                        files to their instrumentation package/directory
     */
    protected static InstrumentationConfig
                     instrumentProgram(Set<File>  instrumentationFiles,
                                       Set<File>  requiredSourceFiles,
                                       List<File> classpathJarFiles,
                                       File       referenceProgram)
              throws ErrorException {
        // Clean up the compile destination directory and the execution result
        // directory from previous runs
        DirectoryUtil.removeDir(COMPILE_DEST_DIR);
        DirectoryUtil.removeDir(EXECUTION_RESULT_DIR);
        
        // Give it some time... (0.5sec) and recreate it
        try {
           Thread.sleep(500);
        }
        catch (InterruptedException ignored){
        }
        COMPILE_DEST_DIR.mkdirs();
        DirectoryUtil.validateDirToWrite(COMPILE_DEST_DIR);
        EXECUTION_RESULT_DIR.mkdirs();
        DirectoryUtil.validateDirToWrite(EXECUTION_RESULT_DIR);
        
        // Classify the instrumentation files to:
        // i) jar-files, ii) correct-implementation file and iii) java files to
        // use for instrumentation
        boolean      programStateDefined = false;
        String       getStateMethodBody  = null;
        List<String> imports             = new ArrayList<String>();
        String       importProgramState  = null;
        Set<File>    sourceFiles         = new HashSet<File>();
        for (File instrumentationFile : instrumentationFiles) {
            if (instrumentationFile.getName().endsWith(JAR_FILE_EXTENSION)) {
                classpathJarFiles.add(instrumentationFile);
                continue;
            }
            
            JavaFile javaFile = new JavaFile(instrumentationFile.getPath());
            String classOrInterfaceName = getClassOrInterfaceName(javaFile);
            
            if (classOrInterfaceName.equals(GET_STATE_CLASS)) {
                // Retrieve the import statements from class 'GetState' (if any)
                List<ImportDeclaration> listOfImports = javaFile.getImports();
                if (!listOfImports.isEmpty()) {
                    for (ImportDeclaration currImport : listOfImports) {
                        String importName = currImport.getNameAsString();
                        imports.add("import " + importName + SEMICOLON);
                    }
                }
                
                // Retrieve the body of method 'ProgramState getState()' if
                // this method exists in 'GetState' as we normally expect
                // (otherwise we will use the default body for this method)
                for (Method method : javaFile.getMethods()) {
                    String methodSignature = method.getSignature(false, true);
                    if (methodSignature.equals(GET_STATE_METHOD_SIGNATURE)) {
                        getStateMethodBody = method.getStrippedBody();
                    }
                }
            }
            else {
                if (classOrInterfaceName.equals(PROGRAM_STATE_CLASS)) {
                    programStateDefined = true;
                    // Ensure that method 'String toString()' exists in
                    // ProgramState
                    boolean cloneFound = false;
                    for (Method method : javaFile.getMethods()) {
                        if (method.getSignature(false, true)
                                  .equals(TO_STRING_METHOD_SIGNATURE)) {
                            cloneFound = true;
                            break;
                        }
                    }
                    if (!cloneFound) {
                        throw new ErrorException("Method '"
                                               + TO_STRING_METHOD_SIGNATURE
                                               + "' not defined in class '"
                                               + PROGRAM_STATE_CLASS + "'");
                    }
                }
                
                PackageDeclaration packageDeclaration = javaFile.getPackage();
                String packageName = packageDeclaration != null?
                                     packageDeclaration.getNameAsString() :
                                     INSTRUMENTATION_PACKAGE;
                
                String importStatement = "import " + packageName + DOT
                                       + classOrInterfaceName + SEMICOLON;
                imports.add(importStatement);
                if (classOrInterfaceName.equals(PROGRAM_STATE_CLASS)) {
                    importProgramState = importStatement;
                }
                
                // Copy the source file to the <instrumentation> package dir
                File instrumentedSourceFile =
                     copyFileForInstrumentation(javaFile, classOrInterfaceName);
                sourceFiles.add(instrumentedSourceFile);
            }
        }
        
        if (!programStateDefined) {
            throw new ErrorException("Found no " + PROGRAM_STATE_CLASS
                                   + " class that compiles");
        }
        File instrumentedProgram = getInstrumentedProgram(referenceProgram,
                                                          getStateMethodBody,
                                                          importProgramState);
        sourceFiles.add(instrumentedProgram);
        
        // Now add the requiredSourceFiles as long as there is no conflict
        Set<File> nonConflictingFiles =
                     getNonConfictingFiles(sourceFiles, requiredSourceFiles);
        copyFilesForInstrumentation(nonConflictingFiles, imports);
        sourceFiles.addAll(nonConflictingFiles);
        
        // If there is a new file added, we should copy it to the
        // <instrumentation> package dir
        
        imports.add("import " + INSTRUMENTATION_PACKAGE + DOT
                  + INSTRUMENTATION_CLASS_NAME + SEMICOLON);
        
        String classpath = COMPILE_DEST_DIR.getPath();
        if (classpathJarFiles != null && !classpathJarFiles.isEmpty()) {
            File[] jarFiles = classpathJarFiles.toArray(new File[0]);
            for (int i = 0; i < jarFiles.length; i++) {
                classpath += File.pathSeparator + jarFiles[i].getPath();
            }
        }
        
        Map<String, String> replacements = new HashMap<String, String>();
        // Replace 'class <ReferenceProgramClass> extends <Superclass>' or
        //         'class <ReferenceProgramClass>' with
        //         'class <ReferenceProgramClass> extends InstumentedProgram'
        JavaFile referenceCode = new JavaFile(referenceProgram.getPath());
        
        ClassOrInterface classDeclaration = referenceCode.getFirstClass();
        String classname = classDeclaration.getName();
        String regex = "class\\s+" + classname;
        String replacement = "class " + classname
                           + " extends " + INSTRUMENTATION_CLASS_NAME;
        if (!classDeclaration.getExtendedModules().isEmpty()) {
            // 'class <classname> extends <superclass> ...' should become
            // 'class <classname> extends InstrumentedProgram ...'
            String superclass = referenceCode.getFirstClass()
                                             .getExtendedModules()
                                             .get(0);
            regex += "\\s+extends\\s+" + superclass;
        }
        else if (!classDeclaration.getImplementedInterfaces().isEmpty()) {
            // 'class <classname> implements ...' should become
            // 'class <classname> extends InstrumentedProgram implements ...'
            regex = "class\\s+" + classname + "\\s*implements";
            replacement += " implements";
        }
        else {
            // 'class <classname> {' should become
            // 'class <classname> extends InstrumentedProgram {'
            regex = "class\\s+" + classname + "\\s*\\{";
        }
        replacements.put(regex, replacement);
        
        return new InstrumentationConfig(INSTRUMENT_ALL_METHODS,
                                         INSTRUMENT_ALL_METHODS,
                                         imports.toArray(new String[0]),
                                         replacements,
                                         instrumentationFiles,
                                         requiredSourceFiles,
                                         classpath);
    }
    
    /**
     * Instruments the source code so that it saves the final program state
     * just before the main method returns (i.e., when the program has completed
     * execution).<br>
     * 
     * @param sourceCodeFile The source code file
     * @param timeoutInSec The timeout for the program execution, expressed in
     *                     seconds, or {@code null} for no timeout
     * 
     * @return the instrumented source code
     * 
     * @throws ErrorException in case the source code does not contain method
     *                        'public void main(String[])'
     */
    protected static String instrumentMainMethod(File   sourceCodeFile,
                                                 Double timeoutInSec)
              throws ErrorException {
        JavaFile javaFile = new JavaFile(sourceCodeFile.getPath());
        String sourceCode = javaFile.getText();
        
        if (javaFile.getPackage() == null) { // Default package
            sourceCode = "package " + INSTRUMENTATION_PACKAGE + SEMICOLON
                       + NEW_LINE + NEW_LINE + sourceCode;
        }
        
        String[] sourceCodeLines = sourceCode.split(NEW_LINE);
        StringBuilder instrumentedCode = new StringBuilder();
        
        Iterator<Method> methodItr = javaFile.getMethods().iterator();
        if (!methodItr.hasNext()) {
            throw new ErrorException(sourceCodeFile.getName() + ": no methods");
        }
        
        Method method     = methodItr.next();
        String methodSignature = method != null?
                                 method.getSignature(true, true) : null;
        
        boolean mainMethodFound = false;
        for (int i = 0; i < sourceCodeLines.length; i++) {
            String codeLine = sourceCodeLines[i];
            
            // Add an instrumentation line at the beginning and in the end
            // of every method
            if (methodSignature != null            &&
                codeLine.contains(methodSignature) &&
                !codeLine.trim().startsWith("//")) { // Not commented code '//'
                int numOfMethodLines = method.getNumOfNonEmptyBodyLines();
                
                instrumentedCode.append(codeLine + NEW_LINE);
                int remainingLines = numOfMethodLines - 1;
                if (method.isMain()) {
                    instrumentedCode.append(MAIN_METHOD_START_INSTRUMENTATION
                                          + NEW_LINE);
                    mainMethodFound = true;
                }
                while (remainingLines > 0) {
                    if (remainingLines == 1 && method.isMain()) {
                        File outFile =
                             getInstrumentationOutputFile(sourceCodeFile);
                        String endCode =
                                 MAIN_METHOD_END_INSTRUMENTATION(timeoutInSec,
                                                                 outFile);
                        instrumentedCode.append(endCode + NEW_LINE);
                    }
                    i++;
                    instrumentedCode.append(sourceCodeLines[i] + NEW_LINE);
                    remainingLines--;
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
        
        if (!mainMethodFound) {
            throw new ErrorException(sourceCodeFile.getName() + ": no main()");
        }
        
        return instrumentedCode.toString();
    }
    
    /**
     * Given a student ID it looks in a workspace subdirectory (i.e.,
     * {@literal <temp location>/delve/extra_files/<student ID>}) and if it
     * exists, it updates the provided instrumentation configuration by adding
     * the files in that directory and returns a new copy (the initial
     * configuration instance is not modified) with the changes.
     * 
     * @param studentID The student ID to look for source code files to include
     *                  in the instrumentation 
     * @param initialConfig The initial instrumentation configuration to update
     * 
     * @return a copy with the updated instrumentation configuration or the
     *         initial configuration if there is no workspace subdirectory with
     *         files for the given student
     * 
     * @throws ErrorException in case of an error in code instrumentation
     *                        (i.e., cannot happen if {@code importStatements}
     *                        is {@code null})
     */
    protected static InstrumentationConfig
                     updateConfig(String                studentID,
                                  InstrumentationConfig initialConfig)
              throws ErrorException {
        File extraFilesDir = new File(STUDENT_EXTRA_DIRS + File.separator
                                                         + studentID);
        if (!extraFilesDir.exists()) {
            return initialConfig; // Return the initial configuration
        }
        // Find the last snapshot for each file
        List<File> files;
        try {
            files = DirectoryUtil.listFiles(extraFilesDir, JAVA_FILE_EXTENSION);
        }
        catch (ErrorException ee) { // Error retrieving the files
            return initialConfig; // Return the initial configuration
        }
        
        // Find which of the files in the extra dir are not in conflict with
        // the instrumentation files
        Set<File> updatedRequiredSourceFiles =
                  getNonConfictingFiles(initialConfig.getInstrumentationFiles(),
                                        new HashSet<File>(files));
        // Now, add to those the required files (initial configuration) that are
        // not in conflict
        Set<File> nonConfictingFiles =
                  getNonConfictingFiles(updatedRequiredSourceFiles,
                                        initialConfig.getRequiredSourceFiles());
        updatedRequiredSourceFiles.addAll(nonConfictingFiles);
        // Copy those files to the instrumentation location
        List<String> imports = new ArrayList<String>();
        for (String importStatement : initialConfig.getImports()) {
            imports.add(importStatement);
        }
        copyFilesForInstrumentation(updatedRequiredSourceFiles, imports);
        
        return
             new InstrumentationConfig(initialConfig.saveStateBefore(),
                                       initialConfig.saveStateAfter(),
                                       imports.toArray(new String[0]),
                                       initialConfig.getReplacements(),
                                       initialConfig.getInstrumentationFiles(),
                                       updatedRequiredSourceFiles,
                                       initialConfig.getClasspath());
    }
    
    /**
     * Given a student ID it looks in a workspace subdirectory (i.e.,
     * {@literal <temp location>/delve/extra_files/<student ID>}) and if it
     * exists, it updates the set of required source code files.<br>.
     * If there are conflicts (e.g. a file in the provided set has the same
     * class or interface as a file under the provided directory), it uses the
     * file in the workspace subdirectory.<br>
     * It returns a copy with the updated set of required source files (i.e.,
     * the initial set is not modified) or the initial set if there is no
     * change.
     * 
     * @param studentID The student ID to look for source code files to include
     *                  in the instrumentation
     * @param requiredSourceFiles The initial set of required source code files
     *                            to update
     *  
     * @return a copy with the updated set of required source files
     */
    protected static Set<File>
                     updateRequiredSourceFiles(String    studentID,
                                               Set<File> requiredSourceFiles) {
        File extraFilesDir = new File(STUDENT_EXTRA_DIRS + File.separator
                                                         + studentID);
        if (!extraFilesDir.exists()) {
            return requiredSourceFiles;
        }
        
        // Find the last snapshot for each file
        List<File> files;
        try {
            files = DirectoryUtil.listFiles(extraFilesDir, JAVA_FILE_EXTENSION);
        }
        catch (ErrorException ee) { // Error retrieving the files
            return requiredSourceFiles; // Return the initial set
        }
        
        Set<File> result = new HashSet<File>(files);
        Set<File> nonConfictingFiles =
                     getNonConfictingFiles(result, requiredSourceFiles);
        result.addAll(nonConfictingFiles);
        
        return result;
    }
    
    /**
     * Given a source code file to instrument (to save the program state) it
     * returns the output file with the program state(s) once the instrumented
     * program executes.
     * 
     * @param sourceCodeFile The source code file to instrument and execute
     * 
     * @return the output file with the program state(s) once the instrumented
     *         program executes
     */
    protected static File getInstrumentationOutputFile(File sourceCodeFile) {
        String filename = sourceCodeFile.getName();
        int end = filename.toLowerCase()
                          .lastIndexOf(JAVA_FILE_EXTENSION);
        if (end != -1) {
            filename = filename.substring(0, end) + ".txt";
        }
        
        return new File(EXECUTION_RESULT_DIR.getPath() + File.separator
                                                       + filename);
    }
    
    /**
     * Given a filename it returns the corresponding image icon or {@code null}
     * if the image icon is not found.
     * 
     * @param filename The image icon filename
     * 
     * @return the corresponding image icon or {@code null} if the image icon is
     *         not found
     */
    protected static ImageIcon getIcon(String filename) {
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            return UIUtil.loadIcon(IMAGE_PACKGAGE + "/" + DELVE_IMAGE_DIR
                                                  + "/" + filename);
        }
        else {
            return UIUtil.getIcon(IMAGE_DIR + File.separator + filename);
        }
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Given the student program final submissions, it finds the class name for
     * those programs.<br>
     * It does that by iterating through the programs, finding the class name
     * for each (student) program and then selecting the most popular one as the
     * class name of the given programming assignment.<br>
     * Note: The programs for all students should have the same class name.
     *       Nevertheless, this is a more secure approach in case a few students
     *       submitted incomplete code. 
     * 
     * @param submittedPrograms The code final snapshots/submitted programs
     * 
     * @return the class name of the program that the students submitted or
     *         {@code null}
     */
    private static
            String getProgramClassname(Collection<JavaFile> submittedPrograms) {
        // Find the class name of the student program to instrument
        Map<String, Integer> histogram = new HashMap<String, Integer>();
        for (JavaFile javaFile : submittedPrograms) {
            try {
                String classOrInterfaceName = getClassOrInterfaceName(javaFile);
                Integer appearances = histogram.get(classOrInterfaceName);
                if (appearances == null) {
                    histogram.put(classOrInterfaceName, 1);
                }
                else {
                    histogram.put(classOrInterfaceName, appearances + 1);
                }
            }
            catch (ErrorException ee) {
            }
        }
        String programClassName = null;
        int appearances = 0;
        for (String className : histogram.keySet()) {
            if (appearances < histogram.get(className)) {
                appearances = histogram.get(className);
                programClassName = className;
            }
        }
        
        return programClassName;
    }
    
    /**
     * Writes the instrumentation program file out in disk and returns it.
     * 
     * @param referenceProgram The file with the correct program implementation
     *                         (i.e., reference file) 
     * @param getStateMethodBody The body of method 'getProgramState()' or
     *                           {@code null} to use the default method body
     * @param importProgramState The statement to import the ProgramState class
     * 
     * @return the instrumentation program file
     * 
     * @throws ErrorException in case of an error constructing or writing out
     *                        the instrumented program in the disk
     */
    private static File getInstrumentedProgram(File   referenceProgram,
                                               String getStateMethodBody,
                                               String importProgramState)
            throws ErrorException {
        StringBuilder fileContent = new StringBuilder();
        
        // Use the reference program to find out it it extends from another
        // class. If so, we will have to make our instrumented program extend
        // that class
        JavaFile referenceCode = new JavaFile(referenceProgram.getPath());
        if (referenceCode.getFirstClass() == null) { // Should never happen
            throw new ErrorException("File '" + referenceProgram.getName()
                                   + "' contains no class");
        }
        String superclass       = null;
        String superclassImport = null;
        if (!referenceCode.getFirstClass().getExtendedModules().isEmpty()) {
            superclass = referenceCode.getFirstClass().getExtendedModules()
                                                      .get(0);
            for (ImportDeclaration currImport : referenceCode.getImports()){
                String importName = currImport.getNameAsString();
                int dotIndex = importName.lastIndexOf(DOT);
                String classname = dotIndex != -1?
                                   importName.substring(dotIndex + 1) :
                                   importName;
                if (classname.equals(superclass)) {
                    superclassImport = "import " + importName + SEMICOLON;
                    break;
                }
            }
        }
        
        File packageDir = new File(COMPILE_DEST_DIR.getPath()
                                 + File.separatorChar
                                 + INSTRUMENTATION_PACKAGE);
        packageDir.mkdirs();
        
        // Add package
        fileContent.append("package " + INSTRUMENTATION_PACKAGE + SEMICOLON
                         + NEW_LINE + NEW_LINE);
        
        // Add import declarations
        fileContent.append(INSTRUMENTATION_IMPORTS);
        fileContent.append(importProgramState + NEW_LINE);
        if (superclassImport != null) {
            fileContent.append(superclassImport + NEW_LINE);
        }
        fileContent.append(NEW_LINE);
        
        // Add class declaration
        fileContent.append(INSTRUMENTATION_CLASS_DECLARATION);
        if (superclass != null) {
            fileContent.append(" extends " + superclass);
        }
        fileContent.append(NEW_LINE + "{" + NEW_LINE);
        
        // Add variables
        fileContent.append(INSTRUMENTATION_VARIABLES + TAB + NEW_LINE);
        
        // Add public methods
        fileContent.append(INSTRUMENTATION_PUBLIC_METHODS);
        // public ProgramState getProgramState()
        if (getStateMethodBody != null) {
            String[] lines = getStateMethodBody.split(NEW_LINE);
            // Skip first and last lines: lines[0] = "{" and lines[n -1] = "}"
            StringBuilder methodBody = new StringBuilder();
            for (int i = 1; i < lines.length -1; i++) {
                methodBody.append(TAB + lines[i] + NEW_LINE);
            }
            fileContent.append(methodBody);
        }
        else {
            fileContent.append(GET_STATE_METHOD_BODY + NEW_LINE);
        }
        fileContent.append(TAB + "}" + NEW_LINE + TAB + NEW_LINE);
        
        // Close the file
        fileContent.append("}");
        
        String filePathname = COMPILE_DEST_DIR.getPath() + File.separator 
                            + INSTRUMENTATION_PACKAGE    + File.separator
                            + INSTRUMENTATION_CLASS_NAME + JAVA_FILE_EXTENSION;
        FileIOUtil.writeFile(filePathname,
                             fileContent.toString(),
                             false);
        return new File(filePathname);
    }
    
    /**
     * Copies the given file to temporary directory that is used for 
     * instrumentation and updates the source code with the package name if the
     * source code has no package (i.e., default package) statement at the
     * beginning.
     * 
     * @param javaFile The file with the source code to copy
     * @param className The class or interface name that is declared in the file
     * 
     * @return the newly created file after the copy
     *  
     * @throws ErrorException in case of an error copying the provided file to
     *                        the proper package under the temporary directory
     *                        that is used for instrumentation
     */
    private static File copyFileForInstrumentation(JavaFile javaFile,
                                                   String   className)
            throws ErrorException {
        String outputFilePathname;
        String dirPathname = COMPILE_DEST_DIR.getPath();
        
        PackageDeclaration packageDeclaration = javaFile.getPackage();
        if (packageDeclaration != null) {
            String packageName = packageDeclaration.getNameAsString();
            String[] dirNames =  packageName.split("\\" + DOT); //Escape the '.'
            for (String dirName : dirNames) {
                dirPathname += File.separator + dirName;
                File dir = new File(dirPathname);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new ErrorException("Error creating directory '"
                                           + dirPathname + "'");
                }
            }
            outputFilePathname = dirPathname + File.separator
                               + className + JAVA_FILE_EXTENSION;
            FileUtil.copyFile(javaFile.getFilePathname(),
                              outputFilePathname,
                              StandardCopyOption.REPLACE_EXISTING);
        }
        else {
            dirPathname += File.separator + INSTRUMENTATION_PACKAGE;
            File dir = new File(dirPathname);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ErrorException("Error creating directory '"
                                       + dirPathname + "'");
            }
            outputFilePathname = dirPathname + File.separator
                               + className + JAVA_FILE_EXTENSION;
            String sourceCode = "package " + INSTRUMENTATION_PACKAGE + SEMICOLON
                              + NEW_LINE + NEW_LINE
                              + javaFile.getOriginalCode();
            FileIOUtil.writeFile(outputFilePathname,
                                 sourceCode,
                                 false); // append
        }
        
        return new File(outputFilePathname);
    }
    
    /**
     * Given a file in Java it returns the name of the class or interface for
     * that file.<br>
     * If the file includes multiple class declarations or multiple interface
     * declarations or a class and an interface declaration, then it returns the
     * name of the first module (class or interface) that is declared.
     * 
     * @param javaFile The Java file to get its class name
     * 
     * @return the name of the first class or interface declared in the file
     * 
     * @throws ErrorException in case there is no class or interface declared
     *                        in the Java file
     */
    private static String getClassOrInterfaceName(JavaFile javaFile)
            throws ErrorException {
        ClassOrInterface firstClass = javaFile.getFirstClass();
        ClassOrInterface firstInterface = javaFile.getFirstInterface();
        
        if (firstClass != null) {
            if (firstInterface != null) {
                if (firstClass.getBeginLine() <= firstInterface.getBeginLine()){
                    return firstClass.getName();
                }
                else {
                    return firstInterface.getName();
                }
            }
            else {
                return firstClass.getName();
            }
        }
        else if (firstInterface != null) {
            return firstInterface.getName();
        }
        else {
            throw new ErrorException("Found no class or interface declared in "
                                   + "file '" + javaFile.getFilename() + "'");
        }
    }
    
    /**
     * Given a set of files {@code baseSet} it returns the files of a second set
     * {@code addSet} that declare a unique class or interface that does not
     * exist in any of the files of {@code baseSet}.<br>
     * If {@code addSet} is empty or all its files are conflicting with
     * {@code baseSet} it returns an empty set.<br>
     * This method does not modify any of the input sets.<br>
     * 
     * @param baseSet The set of files to update
     * @param addSet The new files to process and add the non-conflicting ones
     * 
     * @return the non-conflicting files or an empty set if {@code addSet} is
     *         empty or all its files are conflicting with {@code baseSet}
     */
    private static Set<File> getNonConfictingFiles(Set<File> baseSet,
                                                   Set<File> addSet) {
        Set<File> nonConflictingFiles = new HashSet<File>();
        
        if (addSet.isEmpty()) {
            return nonConflictingFiles;
        }
        
        // Find the classes or interfaces for those files
        Set<String> classOrInterfaceNames = new HashSet<String>();
        for (File baseFile : baseSet) {
            try {
                JavaFile javaFile = new JavaFile(baseFile.getPath());
                
                ClassOrInterface classOrInterface = javaFile.getFirstClass();
                if (classOrInterface == null) { // No class; check if interface
                    classOrInterface = javaFile.getFirstInterface();
                }
                if (classOrInterface != null) {
                    classOrInterfaceNames.add(classOrInterface.getName());
                }
            }
            catch (ErrorException ee) {// Move to the next file
            }
        }
        
        // Go over the provided required files (before the update) to find if
        // there is a file with the same class or interface declaration as any
        // of the modules that we just added. If not, add it to the updated set
        // (no conflict). If there is, skip over it (we will keep and use the
        // module in the sourceCodeDir).
        for (File fileToAdd : addSet) {
            JavaFile javaFile;
            try {
                javaFile = new JavaFile(fileToAdd.getPath());
            }
            catch (ErrorException ee) {// Non-parseable file, skip it
                continue;
            }
            
            ClassOrInterface classOrInterface = javaFile.getFirstClass();
            if (classOrInterface == null) { // No class; check if interface
                classOrInterface = javaFile.getFirstInterface();
            }
            
            if (classOrInterface != null &&
                !classOrInterfaceNames.contains(classOrInterface.getName())) {
                // This module does not exist in the modules so far. Add it.
                nonConflictingFiles.add(fileToAdd);
                classOrInterfaceNames.add(classOrInterface.getName());
            }
        }
        
        return nonConflictingFiles;
    }
    
    /**
     * Copies the provided set of required files for instrumentation to the
     * proper instrumentation location/directory.
     *  
     * @param files The set of required files for instrumentation to copy
     * @param imports The import statements to update (if there are new package
     *                locations)
     * 
     * @throws ErrorException in case of an error copying the files to the
     *                        instrumentation location
     */
    private static void copyFilesForInstrumentation(Set<File>    files,
                                                    List<String> imports)
            throws ErrorException {
        for (File file : files) {
            JavaFile javaFile = new JavaFile(file.getPath());
            String classOrInterfaceName = getClassOrInterfaceName(javaFile);
            
            PackageDeclaration packageDeclaration = javaFile.getPackage();
            String packageName = packageDeclaration != null?
                                 packageDeclaration.getNameAsString() :
                                 INSTRUMENTATION_PACKAGE;
            
            String importStatement = "import " + packageName + DOT
                                   + classOrInterfaceName + SEMICOLON;
            if (!imports.contains(importStatement)) {
                imports.add(importStatement);
            }
            
            copyFileForInstrumentation(javaFile, classOrInterfaceName);
        }
    }
}
