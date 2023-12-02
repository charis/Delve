/*
 * File          : Constants.java
 * Author        : Charis Charitsis
 * Creation Date : 25 October 2020
 * Last Modified : 19 September 2021
 */
package edu.stanford.delve;

// Import Java SE classes
import java.io.File;
import java.util.HashMap;
import java.util.Map;
// Import custom classes
import edu.stanford.util.OSPlatform;
// Import constants
import static edu.stanford.constants.Constants.TEMP_DIR;
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.TAB;
import static edu.stanford.gui.Constants.IMAGE_REPOSITORY;

/**
 * Contains constants used in Delve
 */
public class Constants
{
    /**
     * The delimiter used in the the file with the timestams to concatenate the
     * file name with the timestamp that corresponds to this file
     */
    protected static final String  TIMESTAMP_DELIM      = "#";
    
    /**
     * The name of the file with the git snapshot timestamps 
     */
    protected static final String  TIMESTAMP_FILENAME   = "timestamps.txt";
    
    /**
     * {@code true} to remove the files that are not related to the student
     * submissions or {@code false} to keep them around
     */
    protected static final boolean REMOVE_IRRELEVANT_FILES = false;
    
    /**
     * The name of the subdirectory under the student submission folder with
     * the extra files (other than the main program) that the student submitted
     */
    protected static final String  EXTRA_FILES_DIR      = "extra_files";
    
    /**
     * The name of the file with the snapshot files that have errors and the
     * classification of those errors:<br>
     * {'compile error', 'execution error', 'abrupt termination'}
     */
    protected static final String  ERROR_CLASSIFICATION = 
                                                     "error-classification.txt";
    
    /**
     * Classification category for snapshots that do not compile
     */
    protected static final String  COMPILE_ERROR        = "compile error";
    
    /**
     * Classification category for snapshots that hit an error upon execution
     */
    protected static final String  EXECUTION_ERROR      = "execution error";
    
    /**
     * Classification category for snapshots that timed out and are terminated
     * abruptly (unable to retrieve the state)
     */
    protected static final String  ABRUPT_TERMINATION   = "abrupt termination";
    
    /**
     * The lower threshold between two consecutive snapshots to check if there
     * is a break. Two snapshots with timestamp difference less than this
     * threshold are not compared (even if there is no code change).<br>
     * It is assumed that there is no break. If the difference is between this
     * and the upper threshold the two snapshots are compared to see if there
     * are significant changes.
     */
    protected static final int     BREAK_THRESHOLD_MIN    = 600; // 10mins
    
    /**
     * The upper threshold between two consecutive snapshots to check if there
     * is a break. Two snapshots with timestamp difference greater than this
     * threshold are not compared (even if there is no code change).<br>
     * It is assumed that there is a break. If the difference is between this
     * and the lower threshold the two snapshots are compared to see if there
     * are significant changes.
     */
    protected static final int     BREAK_THRESHOLD_MAX    = 3600; // 60mins
    
    /**
     * The minimum number of lines that must be added so that a two subsequent
     * snapshots with timestamp difference between 10-60mins do not account for
     * a break.
     */
    protected static final int     LINE_DIFF_THRESHOLD    = 5; // new code lines
    
    /**
     * The default threshold for the cyclomatic complexity for a complex method
     * (i.e., method with value higher than the threshold)
     */
    protected static final int     DEFAULT_CYC_THRESHOLD  = 10;
    /**
     * The default threshold for the number of instructions for a complex method
     * (i.e., method with value higher than the threshold)
     */
    protected static final int     DEFAULT_INSTUCTION_THRESHOLD = 200;
    
    /** The name of the subdirectory with the images used in Delve */
    protected static final String  DELVE_IMAGE_DIR        = "delve";
    
    /** Delve's image repository location */
    protected static final String  IMAGE_DIR              = IMAGE_REPOSITORY
                                                          + File.separator
                                                          + DELVE_IMAGE_DIR;
    
    /**
     * The temporary workspace path name
     */
    protected static final String  WORKSPACE              = TEMP_DIR 
                                                          + File.separator
                                                          + "delve";
    
    /**
     * The destination directory to compile files in disk which is used then in
     * the classpath for program execution.
     */
    protected static final String  STUDENT_EXTRA_DIRS     = WORKSPACE
                                                          + File.separator
                                                          + EXTRA_FILES_DIR;
    
    /**
     * The destination directory to compile files in disk which is used then in
     * the classpath for program execution.
     */
    protected static final File    COMPILE_DEST_DIR       = new File(WORKSPACE
                                                                + File.separator
                                                                + "classes");
    /**
     * The directory to save the output files upon execution of the instrumented
     * programs.
     */
    protected static final File    EXECUTION_RESULT_DIR   = new File(WORKSPACE
                                                                + File.separator
                                                                + "output");
    
    /**
     * The classification for the snapshot based on the number of the methods
     * with high CYC compared to the previous snapshot
     */
    protected enum HighCYCMethodCount {
        /**
         * The number of methods with high CYC increased compared to the
         * previous snapshot
         */
        INCREASED,
        /**
         * The number of methods with high CYC decreased compared to the
         * previous snapshot
         */
        DECREASED,
        /**
         * The number of methods with high CYC did not change compared to the
         * previous snapshot
         */
        NO_CHANGE
    }
    
    /**
     * The instrumentation package name
     */
    protected static final String  INSTRUMENTATION_PACKAGE = "instrumentation";
    
    /**
     * The regular expression that matches a package statement.<br>
     * Examples of matching strings:<br>
     * <pre>
     *      1) package java.util.concurrent;
     *      2) package java. util.    concurrent   ;
     *      3) package java.
     *                    util.concurrent
     *                 ;
     * </pre>
     */
    protected static final String  PACKAGE_REGEX       = "package\\s+"
             +"[a-zA-Z_][a-zA-Z0-9_]*\\s*(\\.\\s*[a-zA-Z_][a-zA-Z0-9_]*)*\\s*;";
    
    /**
     * The options for the maximum time to let a program run before it times out
     */
    protected static final String[] TIMEOUT_OPTIONS     = new String[] {
                                                                 "0.5 sec",
                                                                 "  1 sec",
                                                                 "  2 sec",
                                                                 "  5 sec",
                                                                 " 10 sec",
                                                                 " 30 sec",
                                                                 "  1 min",
                                                                 "  2 min",
                                                                 "  5 min",
                                                                 " 10 min",
                                                                 " 30 min",
                                                                 "   none" 
                                                          };
    
    /**
     * Map that uses as keys the timeout execution values that the user can
     * choose from and as values the corresponding values expressed in
     * milliseconds
     */
    protected static final Map<String, Double> TIMEOUT_LOOKUP = 
                                             new HashMap<String, Double>();
    static {
        for (String option : TIMEOUT_OPTIONS) {
            int secIndex = option.indexOf("sec");
            if (secIndex != -1) {
                double value =
                       Double.valueOf(option.substring(0, secIndex).trim());
                TIMEOUT_LOOKUP.put(option, value);
            }
            else {
                int minIndex = option.indexOf("min");
                if (minIndex != -1) {
                    double value =
                           Double.valueOf(option.substring(0, minIndex).trim());
                    TIMEOUT_LOOKUP.put(option, value * 60);
                }
                else {
                    TIMEOUT_LOOKUP.put(option, null);
                }
            }
        }
    }
    
    /**
     * Returns the regular expression that matches the import statement for the
     * given class name.<br>
     * Examples of matching strings when we pass 'ConcurrentMap' as argument:
     * <br>
     * <pre>
     *      1) import java.util.concurrent.ConcurrentMap;
     *      2) package java. util.    concurrent.ConcurrentMap   ;
     *      3) package java.
     *                    util.concurrent.
     *                    ConcurrentMap
     *                 ;
     * </pre>
     * 
     * @param className The class name that we want to find all possible import
     *                  matches (i.e., something like 'import *.className;'
     *                  
     * @return the regular expression that matches the import statement for the
     *         class name
     */
    protected static final String   IMPORT_REGEX(String className) {
        return "import\\s+"
             + "([a-zA-Z_][a-zA-Z0-9_]*\\.\\s*)*" + className + "\\s*;";
    }
    
    /**
     * {@code true} to instrument every method to save the program state when
     * entering and exiting the method or {@code false} otherwise
     */
    protected static final boolean  INSTRUMENT_ALL_METHODS = true;
    
    /**
     * The name of the class for the program state.
     */
    protected static final String   PROGRAM_STATE_CLASS    = "ProgramState";
    
    /**
     * The name of the class with the code to get the program state.
     */
    protected static final String   GET_STATE_CLASS        = "GetState";
    
    /**
     * The signature for the method that returns the program state.
     */
    protected static final String   GET_STATE_METHOD_SIGNATURE =
                                               "ProgramState getProgramState()";
    
    /**
     * The signature for the method that provides a String representation for
     * the program state.
     */
    protected static final String   TO_STRING_METHOD_SIGNATURE = 
                                               "String toString()";
    
    /**
     * The default body for the method that returns the program state.
     */
    protected static final String   GET_STATE_METHOD_BODY  =
                                    TAB + TAB + "return programState;";
    
    /**
     * The code to add to at the beginning of method
     * 'public static void main(String[])' of the instrumented program to allow
     * for timeout detection.<br>
     */
    protected static final String MAIN_METHOD_START_INSTRUMENTATION =
                        "long startExecutionTime = System.currentTimeMillis();";
    
    /**
     * Given an output file it returns the code to add to method 
     * 'public static void main(String[])' of the instrumented program to export
     * its final state to this file.<br>
     * If a timeout is specified, then the code execution will time out if the
     * program is still running after that time.
     * 
     * @param timeoutInSec The timeout in seconds
     * @param outputFile The file to store the final program state
     * 
     * @return the code to add at the end of 'public static void main(String[])'
     */
    protected static final String
                           MAIN_METHOD_END_INSTRUMENTATION(Double timeoutInSec,
                                                           File   outputFile) {
        String outputPath = outputFile.getPath();
        if (OSPlatform.isWindows()) {
            outputPath = outputPath.replaceAll("\\\\", "\\\\\\\\");
        }
        return (timeoutInSec != null && timeoutInSec > 0) ?
               "long elapsedTime = System.currentTimeMillis() - startExecutionTime;"      + NEW_LINE 
             + "while (elapsedTime < " + ((int)(timeoutInSec * 1000)) + ") {"             + NEW_LINE
             + "    try {"                                                                + NEW_LINE
             + "        Thread.sleep(200);"                                               + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "    catch(InterruptedException ignored) {"                                + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "    "                                                                     + NEW_LINE
             + "    String finalProgramState = getFinalProgramState();"                   + NEW_LINE
             + "    if (finalProgramState != null) {"                                     + NEW_LINE
             + "        try {"                                                            + NEW_LINE
             + "            java.io.FileWriter writer ="                                  + NEW_LINE
             + "              new java.io.FileWriter(\"" + outputPath + "\");"            + NEW_LINE
             + "            writer.write(finalProgramState);"                             + NEW_LINE
             + "            writer.close();"                                              + NEW_LINE
             + "        }"                                                                + NEW_LINE
             + "        catch (java.io.IOException e) {"                                  + NEW_LINE
             + "        }"                                                                + NEW_LINE
             + "        break;"                                                           + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "    elapsedTime = System.currentTimeMillis() - startExecutionTime;"       + NEW_LINE
             + "}" :
               
               "while (true) {"                                                           + NEW_LINE
             + "    try {"                                                                + NEW_LINE
             + "        Thread.sleep(200);"                                               + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "    catch(InterruptedException ignored) {"                                + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "    "                                                                     + NEW_LINE
             + "    String finalProgramState = getFinalProgramState();"                   + NEW_LINE
             + "    if (finalProgramState != null) {"                                     + NEW_LINE
             + "        try {"                                                            + NEW_LINE
             + "            java.io.FileWriter writer ="                                  + NEW_LINE
             + "              new java.io.FileWriter(\"" + outputPath + "\");"            + NEW_LINE
             + "            writer.write(finalProgramState);"                             + NEW_LINE
             + "            writer.close();"                                              + NEW_LINE
             + "        }"                                                                + NEW_LINE
             + "        catch (java.io.IOException e) {"                                  + NEW_LINE
             + "        }"                                                                + NEW_LINE
             + "        break;"                                                           + NEW_LINE
             + "    }"                                                                    + NEW_LINE
             + "}";
    }
    
    // ---------------------------------------------------------------------- //
    //            I N S T R U M E N T A T I O N    T E M P L A T E            //
    // ---------------------------------------------------------------------- //
    /**
     * The instrumentation code with the import declarations
     */
    protected static final String INSTRUMENTATION_IMPORTS           =
                                  "import java.util.Map;"                                 + NEW_LINE
                                + "import java.util.ArrayList;"                           + NEW_LINE
                                + "import java.util.HashMap;"                             + NEW_LINE
                                + "import java.util.List;"                                + NEW_LINE;
    
    /**
     * The instrumentation class name
     */
    protected static final String INSTRUMENTATION_CLASS_NAME        =
                                  "InstrumentedProgram";
    
    /**
     * The instrumentation code with the class declaration
     */
    protected static final String INSTRUMENTATION_CLASS_DECLARATION =
       "/** Instrumented program */"                                                      + NEW_LINE
     + "public class " + INSTRUMENTATION_CLASS_NAME;
    
    /**
     * The instrumentation code with the instance variables
     */
    protected static final String INSTRUMENTATION_VARIABLES         =
       "    // --------------------------------------------------------------------- //"  + NEW_LINE
     + "    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //"  + NEW_LINE
     + "    // --------------------------------------------------------------------- //"  + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * The program state"                                                         + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    private static ProgramState programState = null;"                             + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * Lookup map where the key is a method signature and the value is a list of" + NEW_LINE
     + "     * the pre- (array index 0) and post-conditions (array index 1) every time"   + NEW_LINE
     + "     * this method was called.<br>"                                               + NEW_LINE
     + "     * The pre- and post-conditions are represented as String."                   + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    private static final Map<String, List<String[]>> programStateMap ="           + NEW_LINE
     + "                                         new HashMap<String, List<String[]>>();"  + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     *  The precondition as String for the method that is currently being called" + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    private String preCondition;"                                                 + NEW_LINE;
     
    /**
     * The instrumentation code with the public methods
     */
    protected static final String INSTRUMENTATION_PUBLIC_METHODS    =
       "    // ------------------------------------------------------ //"                 + NEW_LINE
     + "    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //"                 + NEW_LINE
     + "    // ------------------------------------------------------ //"                 + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * Called when we enter an instance method."                                  + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public void methodEntry() {"                                                  + NEW_LINE
     + "        if (getProgramState() != null) {"                                         + NEW_LINE
     + "            preCondition = getProgramState().toString();"                         + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        else {"                                                                   + NEW_LINE
     + "            preCondition = null;"                                                 + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "    }"                                                                            + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * Called just before we return from an instance method."                     + NEW_LINE
     + "     *"                                                                           + NEW_LINE
     + "     * @param methodName The name of the method that we are about to return from" + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public void methodExit(String methodName) {"                                  + NEW_LINE
     + "        programState = getProgramState();"                                        + NEW_LINE
     + "        if (preCondition == null) {"                                              + NEW_LINE
     + "            return;"                                                              + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        "                                                                         + NEW_LINE
     + "        String postCondition;"                                                    + NEW_LINE
     + "        if (programState != null) {"                                              + NEW_LINE
     + "            postCondition = programState.toString();"                             + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        else {"                                                                   + NEW_LINE
     + "            preCondition = null; // Reset for next run"                           + NEW_LINE
     + "            return;"                                                              + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        "                                                                         + NEW_LINE
     + "        List<String[]> prePostStates = programStateMap.get(methodName);"          + NEW_LINE
     + "        if (prePostStates == null) {"                                             + NEW_LINE
     + "            prePostStates = new ArrayList<String[]>();"                           + NEW_LINE
     + "            programStateMap.put(methodName, prePostStates);"                      + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        prePostStates.add(new String[] {preCondition, postCondition});"           + NEW_LINE
     + "        "                                                                         + NEW_LINE
     + "        preCondition = null; // Reset for next run"                               + NEW_LINE
     + "    }"                                                                            + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * Sets the new program state."                                               + NEW_LINE
     + "     *"                                                                           + NEW_LINE
     + "     * @param state The new program state"                                        + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public static void setProgramState(ProgramState state) {"                     + NEW_LINE
     + "        programState = state;"                                                    + NEW_LINE
     + "    }"                                                                            + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * @return the final program state represented as String"                     + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public static String getFinalProgramState() {"                                + NEW_LINE
     + "        if (programState == null) {"                                              + NEW_LINE
     + "            return null;"                                                         + NEW_LINE
     + "        }"                                                                        + NEW_LINE
     + "        return programState.toString();"                                          + NEW_LINE
     + "    }"                                                                            + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * @return a lookup map where the key is a method signature and the value is" + NEW_LINE
     + "     *         a list of the pre- (array index 0) and post-conditions (array"     + NEW_LINE
     + "     *         index 1) every time this method was called. The pre- and"          + NEW_LINE
     + "     *         post-conditions are represented as String."                        + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public static Map<String, List<String[]>> getProgramStateLookup() {"          + NEW_LINE
     + "        return programStateMap;"                                                  + NEW_LINE
     + "    }"                                                                            + NEW_LINE
     + "    "                                                                             + NEW_LINE
     + "    /**"                                                                          + NEW_LINE
     + "     * @return the program state"                                                 + NEW_LINE
     + "     */"                                                                          + NEW_LINE
     + "    public ProgramState getProgramState() {"                                      + NEW_LINE;
     
    // ---------------------------------------------------------------------- //
    //          I N S T R U M E N T A T I O N    G U I D E L I N E S          //
    // ---------------------------------------------------------------------- //
    /**
     * Guidelines to instrument the code to validate its functionality 
     */
    protected static final String[] INSTRUMENTATION_GUIDELINES = new String[] {
       // Overall Process
       "This step-by-step guide will help you instrument the code to verify" + NEW_LINE
     + "the correctness of the student submissions."                         + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "Proess summary:"                                                     + NEW_LINE
     + "  1. Define 'program-state'"                                         + NEW_LINE
     + "  2. Replace GUI with non-GUI components"                            + NEW_LINE
     + "  3. Remove user-interaction code (e.g. keyboard input)"             + NEW_LINE
     + "  4. Replace random with deterministic behavior"                     + NEW_LINE
     + "  5. Identify code that is common to all student programs"           + NEW_LINE
     + "  6. Provide a correct/reference program implementation"             + NEW_LINE
     + "  7. Provide additional libaries (jar-files) if required",
     
       // Step 1
       "1. Define 'program-state'"                                           + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "  - Most programs have states. The 'program-state' is often"         + NEW_LINE
     + "    captured by one or more instance variables."                     + NEW_LINE
     + "  - Thus, variables is a good place to start. How does the program"  + NEW_LINE
     + "    execution affect them?"                                          + NEW_LINE
     + "  - The ones that change with program progess are likely to be part" + NEW_LINE
     + "    of the 'program state'."                                         + NEW_LINE
     + "  - On the other hand, variables that are set initially and do not"  + NEW_LINE
     + "    change later, hold configuration information and not program"    + NEW_LINE
     + "    state."                                                          + NEW_LINE
     + "  - Create a 'ProgramState' class that holds the values for those"   + NEW_LINE
     + "    variables."                                                      + NEW_LINE
     + "  - Override method 'toString()' that returns a text representation" + NEW_LINE
     + "    of the ProgramState which can be used to compare two states"     + NEW_LINE
     + "  - Define what 'program-state' means for the given program, create" + NEW_LINE
     + "    file 'ProgramState.java' and then click on button 'Browse' to"   + NEW_LINE
     + "    locate it",
     
       // Step 2
       "2. Replace GUI with non-GUI components"                              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + " - Identify the top-level class with the JFrame/window"              + NEW_LINE
     + " - Keep track of all methods that are called by the program"         + NEW_LINE
     + " - Create a .java file with the same name as that class"             + NEW_LINE
     + " - In the first line of this file include the same package as that"  + NEW_LINE
     + "   class (i.e., 'package path.to.package;')"                         + NEW_LINE
     + " - Add the same methods that are called by the program, but use"     + NEW_LINE
     + "   empty body for each unless a method returns a value that is"      + NEW_LINE
     + "   non-UI related and needed for the program. If so, return a"       + NEW_LINE
     + "   deterministic value for its arguments (if any)."                  + NEW_LINE
     + " - For elements that require user-interaction see next."             + NEW_LINE
     + " - For elements that introduce randomness see next.",
     
       // Step 3
       "3. Remove user-interaction code (e.g. keyboard input)"               + NEW_LINE
     + ""                                                                    + NEW_LINE
     + " - A common example is getting input from the user."                 + NEW_LINE
     + "   In this case replace, do not block and wait. Instead, replace "   + NEW_LINE
     + "   the method that gets the user input with one that returns always" + NEW_LINE
     + "   a fixed value or a value that is generated by a deterministic"    + NEW_LINE
     + "   algorithm."                                                       + NEW_LINE
     + " - Similarly, in other user-interaction cases (mouse move, mouse"    + NEW_LINE
     + "   click etc.) return fixed or deterministic values",
     
       // Step 4
       "4. Replace random with deterministic behavior"                       + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "  If there is a random generator, use a fixed seed or replace it"    + NEW_LINE
     + "  completely with a module that returns values that are generated"   + NEW_LINE
     + "  deterministically.",
     
      // Step 5
      "5. Identify common methods/method calls in every program and use "    + NEW_LINE
     + "  them to update the program state"                                  + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "  - Identify the methods that all student program use"               + NEW_LINE
     + "  - A typical case is a method that is provided by the assignment"   + NEW_LINE
     + "    starter code or even a method signature mentioned in the"        + NEW_LINE
     + "    handout. Unlike user-defined methods that may be unique, those"  + NEW_LINE
     + "    are common."                                                     + NEW_LINE
     + "  - Another example is a call to a library method (e.g. method"      + NEW_LINE
     + "    that exists in a provided assignment jar-file)"                  + NEW_LINE
     + "  - Those methods are candidates to capture the program state"       + NEW_LINE
     + "  - Assuming we can save the program state when calling or return"   + NEW_LINE
     + "    from a method, which methods would you use for it?"              + NEW_LINE
     + "  - The example will help you get a better idea and then upload"     + NEW_LINE
     + "    a file called 'GetState.java'. Add to this file any necessary"   + NEW_LINE
     + "    import statements that are needed to capture the state, but are" + NEW_LINE
     + "    missing from the main program class."                            + NEW_LINE
     + "    Note: You do not need to import 'ProgramState'"                  + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "      import <class that needs to be imported>;"                     + NEW_LINE
     + "      import <another class that needs to be imported>;//etc."       + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "      public class GetState"                                         + NEW_LINE
     + "      {"                                                             + NEW_LINE
     + "          public ProgramState getProgramState() {"                   + NEW_LINE
     + "              // Fill in the code assuming that this method is in"   + NEW_LINE
     + "              // the main class for the programming assignment and"  + NEW_LINE
     + "              // has access to every ivar."                          + NEW_LINE
     + "              // If the ProgramState cannot be expressed with those" + NEW_LINE
     + "              // ivars or if it is possible that the students use"   + NEW_LINE
     + "              // different names/types for them, DO NOT CREATE"      + NEW_LINE
     + "              // 'GetState.java' at all"                             + NEW_LINE
     + "          }"                                                         + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + "   ----------------------------------------------------------------",
     
       // Step 6
       "6. Provide a correct/reference program implementation"               + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "  Locate a source code file that with the correct implementation"    + NEW_LINE
     + "  (i.e., solution) for this assignment"                              + NEW_LINE
     + "  Executing the correct program deterministically will produce a"    + NEW_LINE
     + "  sequence of program states. These are the 'expected states' (i.e." + NEW_LINE
     + "  correct program states)."                                          + NEW_LINE
     + "  When a student program executed it produced its own sequence of"   + NEW_LINE
     + "  states which Devle compares with the 'expected states'. A match"   + NEW_LINE
     + "  means 'correct functionality'."                                    + NEW_LINE
     + "  A mismatch means that functionality error(s) in the student code.",
     
        // Step 7
       "7. Provide additional libraries (jar-files), if necessary"           + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "  Locate any libraries/jar-files that are not need for the code"     + NEW_LINE
     + "  instrumentation despite the fact they were not needed for the"     + NEW_LINE
     + "  student submissions."
    };
    
    /**
     * Delimiter to split the text to insert an image in between when displayed
     * on screen.
     */
    protected static final String   IMAGE_DELIMITER          = "<image>";
    
    /**
     * Examples for each code instrument step
     */
    protected static final String[] INSTRUMENTATION_EXAMPLES = new String[] {
        // Overall Process
        "", // No example
        
        // Step 1: Define 'program state'
       "Let's assume we have to write a program to play chess"               + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   In this example the state of the program consists of:"            + NEW_LINE
     + "    a) The state of the board"                                       + NEW_LINE
     + "    b) The player whose turn is next"                                + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Even if there is a UI for the chess board (see below), the state" + NEW_LINE
     + "   of the board can be represented by an 8-by-8 array for the 64 "   + NEW_LINE
     + "   squares of the board."                                            + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   On each square there can be a piece or nothing. For the pieces we"+ NEW_LINE
     + "   can use classes like 'Pawn', 'Knight', 'Bishop', 'Rook', 'Queen'" + NEW_LINE
     + "   and 'King' and have each of those classes extend 'Piece'."        + NEW_LINE
     + "   Thus, the state of the chess board can be represented by a 2D"    + NEW_LINE
     + "   array 'Piece[][] chessboard' (8-by-8 array). If there is no piece"+ NEW_LINE
     + "   on a given square then the element in the array for the given"    + NEW_LINE
     + "   (row, col) indices will be null."                                 + NEW_LINE
     + "   In this example, file 'ProgramState.java' looks like:"            + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "     import <homework assignment package>.Piece;"                    + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "     public class ProgramState"                                      + NEW_LINE
     + "     {"                                                              + NEW_LINE
     + "         private final Piece[][] chessboard;"                        + NEW_LINE
     + "         private final int nextPlayer;"                              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public ProgramState(Piece[][] chessboard, int nextPlayer) {"+ NEW_LINE
     + "             this.chessboard = chessboard;"                          + NEW_LINE
     + "             this.nextPlayer = nextPlayer;"                          + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public Piece[][] getChessboard() {"                         + NEW_LINE
     + "             return chessboard;"                                     + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public int getNextPlayer() {"                               + NEW_LINE
     + "             return nextPlayer;"                                     + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + "     }"                                                              + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   The last thing that is missing from this code is to override"     + NEW_LINE
     + "   method 'public String toString()' to provide a meaningful text"   + NEW_LINE
     + "   representation of the ProgramState which we can use also to"      + NEW_LINE
     + "   verify if two states are the same."                               + NEW_LINE
     + "   Thus, the complete file 'ProgramState.java' for chess is:"        + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "     import <homework assignment package>.Piece;"                    + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "     public class ProgramState"                                      + NEW_LINE
     + "     {"                                                              + NEW_LINE
     + "         private final Piece[][] chessboard;"                        + NEW_LINE
     + "         private final int nextPlayer;"                              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public ProgramState(Piece[][] chessboard, int nextPlayer) {"+ NEW_LINE
     + "             this.chessboard = chessboard;"                          + NEW_LINE
     + "             this.nextPlayer = nextPlayer;"                          + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public Piece[][] getChessboard() {"                         + NEW_LINE
     + "             return chessboard;"                                     + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public int getNextPlayer() {"                               + NEW_LINE
     + "             return nextPlayer;"                                     + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         @Override"                                                  + NEW_LINE
     + "         public String toString() {"                                 + NEW_LINE
     + "             StringBuilder text = new StringBuilder();"              + NEW_LINE
     + "             int size = chessboard.length; // size = 8"              + NEW_LINE
     + "             for (int i = 0; i < size; i++) {"                       + NEW_LINE
     + "                 for (int j = 0; j < size; j++) {"                   + NEW_LINE
     + "                     Piece piece = chessboard[i][j];"                + NEW_LINE
     + "                     if (piece != null) {"                           + NEW_LINE
     + "                         text.append(\"[\"+i+\",\"+j+\"]:\"+piece);" + NEW_LINE
     + "                         text.append(\"\\n\");"                      + NEW_LINE
     + "                     }"                                              + NEW_LINE
     + "                 }"                                                  + NEW_LINE
     + "             }"                                                      + NEW_LINE
     + "             text.append(\"next player = \" + nextPlayer);"          + NEW_LINE
     + "             return text.toString();"                                + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + "     }"                                                              + NEW_LINE
     + "   ----------------------------------------------------------------",
     
        // Step 2: Replace GUI with non-GUI components
        "Let's assume we have to write a program to play chess."             + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   The program uses often a GUI that allows the players interact"    + NEW_LINE
     + "   with the chess board"                                             + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   GUI offers visual feedback, but slows down the program execution" + NEW_LINE
     + "   and therefore needs to be replaced."                              + NEW_LINE
     + "   The 'program-state' can be represented in non-visual ways."       + NEW_LINE
     + "   For example, chess players use the following notation to mark"    + NEW_LINE
     + "   their moves:"                                                     + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   There are multiple ways to represent a move. We can choose any to"+ NEW_LINE
     + "   avoid the UI delay. We just have to express the UI commands in"   + NEW_LINE
     + "   our preferred representation."                                    + NEW_LINE
     + "   For example, we can use the 'ProgramState' class (see previous"   + NEW_LINE
     + "   step) to store the state of the board before and after the move." + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   The UI class provides methods to move a piece. The UI knows how"  + NEW_LINE
     + "   to display the move on screen."                                   + NEW_LINE
     + "   For example, 'move(int row, int col, int destRow, int destCol)'"  + NEW_LINE
     + "   can be used to move the piece from a square (row, col) to another"+ NEW_LINE
     + "   square (destRow, destCol). The UI interprets this by drawing the" + NEW_LINE
     + "   pixels on screen."                                                + NEW_LINE
     + "   An easy way to bypass the UI is to fake its behavior. We can"     + NEW_LINE
     + "   create a class with the same name (also same package) as the UI"  + NEW_LINE
     + "   class, add the UI methods that our program calls, but replace"    + NEW_LINE
     + "   them with non-UI code."                                           + NEW_LINE
     + "   Thus, our fake UI class will still have the 'move()' method, but" + NEW_LINE
     + "   the body of this method can simply store the new state (after the"+ NEW_LINE
     + "   move) and draw nothing on the screen."                            + NEW_LINE
     + "   Thus, the code to replace the GUI class can look like is:"        + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "     package <same package as GUI_Class to replace>;"                + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "     public class <GUI_Class>"                                       + NEW_LINE
     + "     {"                                                              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public void move(int row,int col,int destRow,int destCol) {"+ NEW_LINE
     + "             // Make sure there is a piece at (row, col) that"       + NEW_LINE
     + "             // belongs to the player who makes the move"            + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Make sure this piece can move to the destination"    + NEW_LINE
     + "             // square (destRow, destCol)"                           + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Determine if an opponent piece is captured"          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Update the program state:"                           + NEW_LINE
     + "             // remove the captured piece (if any), move the piece"  + NEW_LINE
     + "             // to the destination and change the player's turn to"  + NEW_LINE
     + "             // play next"                                           + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public void touchPiece(int row, int col) {"                 + NEW_LINE
     + "             // The GUI hightlights the squares where the selected"  + NEW_LINE
     + "             // piece can land on. Thus, we don't need to do"        + NEW_LINE
     + "             // do anything and leave the code empty since it does"  + NEW_LINE
     + "             // not affect the current program state"                + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         <other GUI methods>"                                        + NEW_LINE
     + "         ..."                                                        + NEW_LINE
     + "     }"                                                              + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "     Note that we need to replace every GUI method that the program" + NEW_LINE
     + "     may call, but we do not necessarily have to add code for all"   + NEW_LINE
     + "     all methods. For example, when the player selectes a piece the" + NEW_LINE
     + "     program calls 'touchPiece(row,col)' which takes care of any"    + NEW_LINE
     + "     visual effects (e.g., highlight the squares where this piece"   + NEW_LINE
     + "     can move to)."                                                  + NEW_LINE
     + "     However, we do not have to do anything given that we are"       + NEW_LINE
     + "     replacing the UI and don't have to worry about visual feedback."+ NEW_LINE
     + "     Thus, the method 'void touchPiece(int row, int col)' will be"   + NEW_LINE
     + "     empty.",
     
        // Step 3: Remove user-interaction code (e.g. keyboard input)
        "Let's assume we have to write a program to play chess."             + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   When the game starts, a dialog pops up and asks for the player's" + NEW_LINE
     + "   name. At this point there is no progress util we type the names." + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   We want to avoid this behavior in automated program execution."   + NEW_LINE
     + "   This can done by replacing the method with the user-intreactive"  + NEW_LINE
     + "   behavior with to return a value without waiting for user input."  + NEW_LINE
     + "   For example, let's assume that there is a method"                 + NEW_LINE
     + "   'getUserInput(\"What's your name?\")' that causes a user-input"   + NEW_LINE
     + "   dialog appear on screen to type a name which is then returned by" + NEW_LINE
     + "   the method."                                                      + NEW_LINE
     + "   In this example, we can use an one-line method that returns"      + NEW_LINE
     + "   always the same fixed value:"                                     + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "      public String getUserInput(String prompt) {"                   + NEW_LINE
     + "          return \"Player One\";"                                    + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Note that the user-interaction method can be anything that pauses"+ NEW_LINE
     + "   the execution till the user takes an action (e.g., mouse move,"   + NEW_LINE
     + "   mouse click etc) and it does not necessarily have to involve UI"  + NEW_LINE
     + "   (e.g. asking for the player name via console still pauses the"    + NEW_LINE
     + "   execution)."                                                      + NEW_LINE
     + "   Our goal is to identify and replace all methods that wait for"    + NEW_LINE
     + "   for user input or user action of any kind in general.",
     
       // Step 4: Replace random with deterministic behavior
       "Let's assume we have to write a program to play backgmammon."        + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   With each roll of the dice, players must choose from numerous"    + NEW_LINE
     + "   options for moving their checkers."                               + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Rolling the dice means that there is an element of randomness to" + NEW_LINE
     + "   the game. Testing the program functionality is much easier if the"+ NEW_LINE
     + "   program execution is deterministic. Therefore, we must replace"   + NEW_LINE
     + "   the random generator that produces the dice values with logic"    + NEW_LINE
     + "   that returns values deterministically."                           + NEW_LINE
     + "   This can be done if we either return the same value for every"    + NEW_LINE
     + "   dice roll or an algorithm to produce the value  (e.g., use a"     + NEW_LINE
     + "   counter to keep track of the rolls and use it to determine the"   + NEW_LINE
     + "   dice values."                                                     + NEW_LINE
     + "   Assuming there is a RandomGenerator class with a method"          + NEW_LINE
     + "   'public int generateValue(int min, int max)' that retruns a"      + NEW_LINE
     + "   random value between [min, max] we can replace it with code like:"+ NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "      public int generateValue(int min, int max) {"                  + NEW_LINE
     + "          // counter is an instance variable to keep track of the"   + NEW_LINE
     + "          // number of times we call this method"                    + NEW_LINE
     + "          counter++;"                                                + NEW_LINE
     + "          return min + counter % max;"                               + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "",
     
       // Step 5: Identify common methods/method calls in every program
       //         and use them to update the program state
       "Let's assume we have to write a program to play chess."              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Our goal is to save the program state."                           + NEW_LINE
     + "   Sometimes this is easy. Other times it is be more difficult."     + NEW_LINE
     + "   For example, if the homework assignment provides starter code"    + NEW_LINE
     + "   variables and/or methods, all submitted programs are expected"    + NEW_LINE
     + "   to include those."                                                + NEW_LINE
     + "   Suppose that all programs use variables 'Piece[][] chessboard'"   + NEW_LINE
     + "   and 'int nextPlayer'."                                            + NEW_LINE
     + "   This means that capturing the ProgramState is possible by using"  + NEW_LINE
     + "   instance variables that are common in every submission."          + NEW_LINE
     + "   If the program to instrument falls into this category, we need"   + NEW_LINE
     + "   to create a file 'GetState.java' and click the 'Browse' button"   + NEW_LINE
     + "   to locate it. This file should include the code for a method"     + NEW_LINE
     + "   'getProgramState()' that returns the program state using the"     + NEW_LINE
     + "   program's instance variables."                                    + NEW_LINE
     + "   You may assume method 'getProgramState()' can access every"       + NEW_LINE
     + "   instance variable of the submitted program as if it was a method" + NEW_LINE
     + "   in that class."                                                   + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "      // Import classes (except 'ProgramState') not imported in our" + NEW_LINE
     + "      // program but needed in method 'getProgramState()'"           + NEW_LINE
     + "      import <any class we need in the code below>;"                 + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "      public class GetState"                                         + NEW_LINE
     + "      {"                                                             + NEW_LINE
     + "          public ProgramState getProgramState() {"                   + NEW_LINE
     + "              return new ProgramState(chessboard, nextPlayer);"      + NEW_LINE
     + "          }"                                                         + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "    Note: If the body of 'getProgramState()' uses a class other than"+ NEW_LINE
     + "          'ProgramState' which is not present (imported) in the"     + NEW_LINE
     + "          student program, it has to imported in 'GetState'."        + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Now, suppose that there is no starter code and the students can"  + NEW_LINE
     + "   use their preferred notation, data structures, methods etc."      + NEW_LINE
     + "   In this case, every student program uses its own internal"        + NEW_LINE
     + "   representation."                                                  + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   We need common interpretation for the 'program state' for all"    + NEW_LINE
     + "   submitted programs. In such cases, we have to identify common"    + NEW_LINE
     + "   method calls. In GUI appications the UI methods often offer this" + NEW_LINE
     + "   opportunity."                                                     + NEW_LINE
     + "   For example, even if our program to play chess has no common"     + NEW_LINE
     + "   variables, methods etc. with the other student programs, it still"+ NEW_LINE
     + "   calls UI's methods to display a chess move."                      + NEW_LINE
     + "   Then we can have the UI determine the program state, translate it"+ NEW_LINE
     + "   to universal 'program state' representation (i.e., ProgramState)" + NEW_LINE
     + "   and save that state."                                             + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   Back to our chess program. In our \"fake\" UI class that replaces"+ NEW_LINE
     + "   the original module, we need to get the program state when one or"+ NEW_LINE
     + "   more UI methods are called by the program and save that state."   + NEW_LINE
     + "   We know that every program calls the UI when a player makes a"    + NEW_LINE
     + "   move. Thus, when the student's program calls UI's method"         + NEW_LINE
     + "   'move(int row, int col, int destRow, int destCol)' we can grab"   + NEW_LINE
     + "   the opportunity to calculate the new state and translate it to a" + NEW_LINE
     + "   'ProgramState' ('Piece[][] chessboard' and 'int nextPlayer'),"    + NEW_LINE
     + "   which is a common representation for all programs."               + NEW_LINE
     + "   The problem is that we want this interpretation not in the"       + NEW_LINE
     + "   instrumented UI class, but in the student program class."         + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   To solve this probem, Delve adds two methods to the student"      + NEW_LINE
     + "   code:"                                                            + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "      public static void setProgramState(ProgramState state) {"      + NEW_LINE
     + "          // Delve updates the current state and puts it also to a"  + NEW_LINE
     + "          // lookup map"                                             + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "      public ProgramState getProgramState() {"                       + NEW_LINE
     + "          // Fill in this code so that this method returns the"      + NEW_LINE
     + "          // current state if the ProgramState instance can be"      + NEW_LINE
     + "          // initialized by ivars that are common to all student"    + NEW_LINE
     + "          // programs"                                               + NEW_LINE
     + "          return new ProgramState(<common ivars>);"                  + NEW_LINE
     + "          // Otherwise, this method will return the ProgramState"    + NEW_LINE
     + "          // instance that was last passed by the static call to"    + NEW_LINE
     + "          // 'void setProgramState(ProgramState state)'"             + NEW_LINE
     + "      }"                                                             + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   <image>"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   The instrumented UI class can pass back the new state by calling" + NEW_LINE
     + "   the static method 'void setProgramState(ProgramState state)'."    + NEW_LINE
     + "   Note: The static method is added automatically."                  + NEW_LINE
     + "   ----------------------------------------------------------------" + NEW_LINE
     + "     package <same package as GUI_Class to replace>;"                + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "     public class <GUI_Class>"                                       + NEW_LINE
     + "     {"                                                              + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public void move(int row,int col,int destRow,int destCol) {"+ NEW_LINE
     + "             // Make sure there is a piece at (row, col) that"       + NEW_LINE
     + "             // belongs to the player who makes the move"            + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Make sure this piece can move to the destination"    + NEW_LINE
     + "             // square (destRow, destCol)"                           + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Determine if an opponent piece is captured"          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Update the program state:"                           + NEW_LINE
     + "             // remove the captured piece (if any), move the piece"  + NEW_LINE
     + "             // to the destination and change the player's turn to"  + NEW_LINE
     + "             // play next"                                           + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "             // Pass back the new ProgramState to the class which"   + NEW_LINE
     + "             // called this method (for convenience static method)"  + NEW_LINE
     + "             <ProgramClass>.setProgramState(state);"                 + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         public void touchPiece(int row, int col) {"                 + NEW_LINE
     + "             // The GUI hightlights the squares where the selected"  + NEW_LINE
     + "             // piece can land on. Thus, we don't need to do"        + NEW_LINE
     + "             // do anything and leave the code empty since it does"  + NEW_LINE
     + "             // not affect the current program state"                + NEW_LINE
     + "         }"                                                          + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "         <other GUI methods>"                                        + NEW_LINE
     + "         ..."                                                        + NEW_LINE
     + "     }"                                                              + NEW_LINE
     + "   --------------------------------------------------------",
      
       // Step 6: Provide a correct/reference program implementation
       "", // No example
       
       // Step 7: Provide additional libraries (jar-files), if required
       "Let's assume we have to write a program to play backgmammon."        + NEW_LINE
     + ""                                                                    + NEW_LINE
     + "   There are many ways to roll the dice deterministically."          + NEW_LINE
     + "   Let's suppose that we want to generate the dice values based on"  + NEW_LINE
     + "   a given distribution."                                            + NEW_LINE
     + "   To do so, we may need to import a library (e.g., package 'stats')"+ NEW_LINE
     + "   that the original program did not use."                           + NEW_LINE
     + "   In this case we would have to upload that library (.jar file)."
    };
}
