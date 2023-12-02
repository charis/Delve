/*
 * File          : RuntimeExec.java
 * Author        : Charis Charitsis
 * Creation Date : 9 February 2015
 * Last Modified : 24 May 2021
 */
package edu.stanford.util.runtime;

// Import Java SE classes
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
// Import custom classes
import edu.stanford.exception.ErrorException;
import edu.stanford.exception.InvalidArgumentException;
import edu.stanford.util.filesystem.DirectoryUtil;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;
import static edu.stanford.constants.Literals.SPACE;

/**
 * Allows execution of programs through Java.
 */
public class RuntimeExec
{
    /** Successful execution */
    public static final int SUCCESS   = 0;
    //public static final int ERROR     = 1;
    /** Exception during the execution */
    public static final int EXCEPTION = 2;
    /** The execution timed out */
    public static final int TIMEOUT   = 3;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Lock that must be acquired to execute the runtime exec command.<br>
     * This is a lookup map where the process ID (case-insensitive) is used as
     * key to get back the lock associated with this node-name (if any)
     * This lock effectively blocks parallel executions.<br>
     * If a map lookup does not return a match (i.e., the process ID is not
     * contained in the map keys) then the runtime execution is not controlled
     * by a semaphore and therefore it does not block.<br>
     * 
     * Note: The map is a synchronized (thread-safe) map that guarantees serial
     *       access (e.g. get(), clear() etc. are serialized which has
     *       insignificant if not zero performance overhead given the size and
     *       use of the map, but on the other had it is critical to ensure that
     *       clear() is not executed by a thread while another thread is
     *       modifying the map)
     */
    private static final Map<String, Semaphore> executionSemaphoreMap = 
                 Collections.synchronizedMap(new HashMap<String, Semaphore>());
    
    /**
     * A unique ID that can be used for blocking execution (i.e., do not allow
     * two processes with the same ID run in parallel) or {@code null} for a
     * non-blocking process
     */
    private final String    processID;
    /**
     * An array that holds the absolute path name of the command to execute
     * (first element in the array) followed by the command arguments (if any)
     */
    private final String[]  cmd;
    /**
     * The user input in case the command to execute expects an input from the
     * user at runtime or {@code null} in case there is no user input<br>
     * E.g. if the command is a Windows batch file like 'C:\test.bat'<br>
     * <pre>
     *      {@literal @}echo off
     *      set /p id="Enter ID: "
     *      echo %id%
     * </pre>
     * Then <pre>String   cmd   = "C:\\test.bat";</pre> and 
     *      <pre>String[] input = new String[] { "123" };</pre><br>
     * which will print '123' due to 'echo %id%' 
     */
    private final String[]  input;
    /**
     * The specified environment in the command to execute or {@code null} if
     * there is no environment.<br>
     * E.g. on Windows the following command<br>
     * <pre>
     *      String   cmdPath = "cmd";
     *      String[] cmdArgs = new String[] {  "/C", "echo FOO: %FOO%" };
     *      String[] env     = new String[] { "FOO=false" };
     * </pre>
     * will output: 'FOO: false'
     */
    private final String[]  env;
    /**
     * The working directory in the command to execute or {@code null} if the
     * command should inherit the working directory of the current process
     */
    private final File      workingDir;
    /**
     * {@code true} to show the command output or {@code false} otherwise
     */
    private final boolean   showOutput;
    /**
     * {@code true} to show the command error output or {@code false} otherwise
     */
    private final boolean   showError;
    /**
     * {@code true} to label the output and the error streams or {@code false}
     * otherwise
     */
    private final boolean   labelStreams;
    /**
     * The command output upon execution
     */
    private       String[]  output;
    /**
     * The error messages upon execution
     */
    private       String[]  errors;
    /**
     * The command exit value
     */
    private       int       exitValue = 0;
    /**
     * {@code true} for debugging or {@code false} otherwise
     */
    private static boolean  DEBUG     = false;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new RuntimeExec
     * 
     * @param cmdPath The absolute path name of the command to execute
     * @param cmdArgs The arguments of the command to execute or {@code null} if
     *                there are no command arguments
     * 
     * @throws InvalidArgumentException in any of the following cases:<br>
     *                                  1) The command array or any of its
     *                                     elements is {@code null}<br>
     *                                  2) The command array has zero elements<br>
     *                                  3) The working directory (if not
     *                                     {@code null}) does not exist or does
     *                                     not have read access
     */
    public RuntimeExec(String   cmdPath,
                       String[] cmdArgs)
        throws InvalidArgumentException {
        this(cmdPath,
             cmdArgs,
             null); // workingDir
    }
    
    /**
     * Constructs a new RuntimeExec
     * 
     * @param cmdPath The absolute path name of the command to execute
     * @param cmdArgs The arguments of the command to execute or {@code null} if
     *                there are no command arguments
     * @param workingDir The working directory in the command to execute or
     *                   {@code null} if the command should inherit the working
     *                   directory of the current process
     * 
     * @throws InvalidArgumentException in any of the following cases:<br>
     *                                  1) The command array or any of its
     *                                     elements is {@code null}<br>
     *                                  2) The command array has zero elements<br>
     *                                  3) The working directory (if not
     *                                     {@code null}) does not exist or does
     *                                     not have read access
     */
    public RuntimeExec(String   cmdPath,
                       String[] cmdArgs,
                       String   workingDir)
        throws InvalidArgumentException {
        this(cmdPath,
             cmdArgs,
             workingDir,
             null,   // input
             null,   // env
             true,   // showOutput
             true,   // showError
             false); // labelStreams
    }
    
    /**
     * Constructs a new RuntimeExec
     * 
     * @param cmdPath The absolute path name of the command to execute
     * @param cmdArgs The arguments of the command to execute or {@code null} if
     *                there are no command arguments
     * @param workingDir The working directory in the command to execute or
     *                   {@code null} if the command should inherit the working
     *                   directory of the current process
     * @param input The user input in case the command to execute expects an
     *              input from the user at runtime or {@code null} in case there
     *              is no user input
     * @param env The specified environment in the command to execute or
     *            {@code null} if there is no environment
     * @param showOutput {@code true} to show the command output or
     *                   {@code false} otherwise
     * @param showError {@code true} to show the command error output or
     *                  {@code false} otherwise
     * 
     * @throws InvalidArgumentException in any of the following cases:<br>
     *                                  1) The command array or any of its
     *                                     elements is {@code null}<br>
     *                                  2) The command array has zero elements<br>
     *                                  3) The working directory (if not
     *                                     {@code null}) does not exist or does
     *                                     not have read access
     */
    public RuntimeExec(String   cmdPath,
                       String[] cmdArgs,
                       String   workingDir,
                       String[] input,
                       String[] env,
                       boolean  showOutput,
                       boolean  showError)
        throws InvalidArgumentException {
        this(null,
             cmdPath,
             cmdArgs,
             workingDir,
             input,
             env,
             showOutput,
             showError,
             false); // labelStreams
    }
    
    /**
     * Constructs a new RuntimeExec
     * 
     * @param cmdPath The absolute path name of the command to execute
     * @param cmdArgs The arguments of the command to execute or {@code null} if
     *                there are no command arguments
     * @param workingDir The working directory in the command to execute or
     *                   {@code null} if the command should inherit the working
     *                   directory of the current process
     * @param input The user input in case the command to execute expects an
     *              input from the user at runtime or {@code null} in case there
     *              is no user input
     * @param env The specified environment in the command to execute or
     *            {@code null} if there is no environment
     * @param showOutput {@code true} to show the command output or
     *                   {@code false} otherwise
     * @param showError {@code true} to show the command error output or
     *                  {@code false} otherwise
     * @param labelStreams {@code true} to label the output and the error
     *                     streams or {@code false} otherwise
     * 
     * @throws InvalidArgumentException in any of the following cases:<br>
     *                                  1) The command array or any of its
     *                                     elements is {@code null}<br>
     *                                  2) The command array has zero elements<br>
     *                                  3) The working directory (if not
     *                                     {@code null}) does not exist or does
     *                                     not have read access
     */
    public RuntimeExec(String   cmdPath,
                       String[] cmdArgs,
                       String   workingDir,
                       String[] input,
                       String[] env,
                       boolean  showOutput,
                       boolean  showError,
                       boolean  labelStreams)
        throws InvalidArgumentException {
        this(null,
             cmdPath,
             cmdArgs,
             workingDir,
             input,
             env,
             showOutput,
             showError,
             labelStreams);
    }
    
    /**
     * Constructs a new RuntimeExec
     * 
     * @param processID The unique ID that can be used for blocking execution
     *                  (i.e., do not allow two processes with the same ID run
     *                  in parallel) or {@code null} for a non-blocking process 
     * @param cmdPath The absolute path name of the command to execute
     * @param cmdArgs The arguments of the command to execute or {@code null} if
     *                there are no command arguments
     * @param workingDir The working directory in the command to execute or
     *                   {@code null} if the command should inherit the working
     *                   directory of the current process
     * @param input The user input in case the command to execute expects an
     *              input from the user at runtime or {@code null} in case there
     *              is no user input
     * @param env The specified environment in the command to execute or
     *            {@code null} if there is no environment
     * @param showOutput {@code true} to show the command output or
     *                   {@code false} otherwise
     * @param showError {@code true} to show the command error output or
     *                  {@code false} otherwise
     * @param labelStreams {@code true} to label the output and the error
     *                     streams or {@code false} otherwise
     * 
     * @throws InvalidArgumentException in any of the following cases:<br>
     *                                  1) The command array or any of its
     *                                     elements is {@code null}<br>
     *                                  2) The command array has zero elements<br>
     *                                  3) The working directory (if not
     *                                     {@code null}) does not exist or does
     *                                     not have read access
     */
    public RuntimeExec(String   processID,
                       String   cmdPath,
                       String[] cmdArgs,
                       String   workingDir,
                       String[] input,
                       String[] env,
                       boolean  showOutput,
                       boolean  showError,
                       boolean  labelStreams)
           throws InvalidArgumentException {
        // Assert command
        if (cmdPath == null || cmdPath.trim().length() == 0) {
            throw new InvalidArgumentException("No command path");
        }
        if (cmdArgs != null) {
            for (int i = 0; i < cmdArgs.length; i++) {
                if (cmdArgs[i] == null) {
                    throw new InvalidArgumentException("Command argument "
                                                     + " 'cmdArgs[" + i
                                                     + "]' is null");
               }
            }
        }
        
        if (workingDir != null) {
            try {
                DirectoryUtil.validateDirToRead(workingDir);
            }
            catch (ErrorException error) {
                throw new InvalidArgumentException(error.getMessage());
            }
            
            this.workingDir = new File(workingDir);
        }
        else {
            this.workingDir = null;
        }
        
        this.showOutput = showOutput;
        this.showError  = showError;
        this.labelStreams = labelStreams;
        
        // Build the command (i.e., combine command path + command arguments
        if (cmdArgs != null) {
           cmd = new String[cmdArgs.length + 1];
           cmd[0] = cmdPath;
           for (int i = 0; i < cmdArgs.length; i++) {
               cmd[i + 1] = cmdArgs[i];
           }
        }
        else {
            cmd = new String[1];
            cmd[0] = cmdPath;
        }
        
        this.processID = processID;
        this.input     = input;
        this.env       = env;
        
        if(processID != null && !executionSemaphoreMap.containsKey(processID)) {
            executionSemaphoreMap.put(processID, new Semaphore(1));
        }
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Clears all entries from the map with the node execution semaphores
     * (that prevents parallel executions of a given process specified by its
     * unique ID).
     */
    public static void resetExecutionSemaphoreLookupMap() {
        if (!executionSemaphoreMap.isEmpty()) {
            executionSemaphoreMap.clear();
        }
    }
    
    /**
     * Initializes the map with the node executions semaphores using the given
     * process ID as keys as long as the map is empty.<br>
     * Otherwise, it just adds any node names that do not already exist in the
     * map.<br>
     * To enforce initialization the caller must call explicitly 
     * resetExecutionSemaphoreLookupMap() and then this method.
     * 
     * @param processIDs The process IDs to initialize the map with
     * 
     * @throws InvalidArgumentException in case the array with the node names is
     *                                  {@code null}<br>
     *                                  Note: Although a zero-size non-null
     *                                        array does not make sense it is
     *                                        not an error (it is the caller's
     *                                        responsibility to check this)
     */
    public static void updateNodeSemaphoreLookupMap(List<String> processIDs)
           throws InvalidArgumentException {
        if (processIDs == null) {
            throw new InvalidArgumentException("The list with the process IDs "
                                             + "is null");
        }
        
        String key;
        for (String processID : processIDs) {
            key = processID.trim().toLowerCase();
            // Update map by adding the process IDs that do not already exist
            // in the map keys
            if (!executionSemaphoreMap.containsKey(key)) {
                executionSemaphoreMap.put(key, new Semaphore(1));
            }
            else {
                System.out.println("There already exists an execution semaphore"
                                 + "for process ID \"" + processID + "\"");
            }
        }
    }
    
    /**
     * Runs the given command locally and returns the live process without
     * waiting for completion
     *
     * @return the live process without waiting for completion
     * 
     * @throws ErrorException if an I/O error occurs during the command
     *                        execution
     */
    public RuntimeProcess runCommandNoWait()
           throws ErrorException {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        
        if (DEBUG) {
            System.out.println(getCmdDescription());
        }
        
        try {
            process = runtime.exec(cmd, env, workingDir);
        }
        catch (IOException ioe) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error executing '");
            errorMsg.append(cmd[0]);
            for (int i = 1; i < cmd.length; i++) {
                errorMsg.append(SPACE + cmd[i]);
            }
            errorMsg.append("'. Details: " + NEW_LINE + ioe.getMessage());
            
            throw new ErrorException(errorMsg.toString());
        }
        
        return new RuntimeProcess(process);
    }
    
    /**
     * Runs the given command locally, waits for its termination and returns
     * the command execution return value.<br>
     * If a timeout is provided and that time expires, it terminates the process
     * running the command.
     * 
     * @param timeoutInSec The timeout in seconds. Non-positive or {@code null}
     *                     value means no timeout
     * 
     * @return the command return value
     * 
     * @throws ErrorException in case of an I/O error in the command execution
     *                        or in case of an internal error
     */
    public int runCommand(Double timeoutInSec)
           throws ErrorException { 
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        
        boolean checkForTimeout = timeoutInSec != null && timeoutInSec > 0;
        
        
        // Synchronization barrier to control parallel executions of the process
        // with the same process ID
        // It blocks only if 1) processID is not null and
        //                   2) processID is found in the execution table
        //                      (case-insensitive lookup) and is already in use
        //                      (i.e., acquired by another thread, but not yet
        //                      released)
        Semaphore executionSemaphore = null;
        if (processID != null) {
            executionSemaphore = RuntimeExec.getSemaphore(processID);
            
            if (executionSemaphore != null) {
                long startTime = System.currentTimeMillis();
                try {
                    executionSemaphore.acquire();
                    if (DEBUG) {
                        long secondsPassed =
                             (System.currentTimeMillis() - startTime) / 1000;
                        System.out.println("Acquired semaphore after "
                                         + secondsPassed + " seconds");
                    }
                }
                catch (InterruptedException impossible) {
                    // Release the semaphore that controls parallel execution
                    if (executionSemaphore != null) {
                        executionSemaphore.release();
                    }
                    
                    exitValue = EXCEPTION;
                    throw new ErrorException("Internal error");
                }
            }
        }
        
        if (DEBUG) {
            System.out.println(getCmdDescription());
        }
        
        try {
            process = runtime.exec(cmd, env, workingDir);
        }
        catch (IOException ioe) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error executing '");
            errorMsg.append(cmd[0]);
            for (int i = 1; i < cmd.length; i++) {
                errorMsg.append(SPACE + cmd[i]);
            }
            errorMsg.append("'. Details: " + NEW_LINE + ioe.getMessage());
            
            // Release the semaphore that controls parallel execution
            if (executionSemaphore != null) {
                executionSemaphore.release();
            }
            
            exitValue = EXCEPTION;
            throw new ErrorException(errorMsg.toString());
        }
        
        // Write input to the process
        if (input != null) {
            OutputStream       outStream          = process.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                                                    outStream);
            BufferedWriter     bufferedWriter     = new BufferedWriter(
                                                        outputStreamWriter);
            
            try {
                if (DEBUG){
                    // Never trace input as it can contain credentials
                    System.out.println("Input length=" + input.length);
                }
                for (int line = 0; line < input.length; line++) {
                    bufferedWriter.write(input[line]);
                    bufferedWriter.flush();
                }
            }
            catch (IOException ioe) {
                // Release the semaphore that controls parallel execution
                if (executionSemaphore != null) {
                    executionSemaphore.release();
                }
                
                exitValue = EXCEPTION;
                throw new ErrorException("Error processing the input. Details: "
                                       + NEW_LINE + ioe.getMessage());
            }
            finally {
                try {
                    outStream.close(); //close the input of the process
                    if (DEBUG) {
                        System.out.println ("Closed input for process");
                    }
                 }
                 catch (IOException ioe) {
                     System.err.println("Error closing the process input. "
                                      + "Details:" + NEW_LINE
                                      + ioe.getMessage());
                 }
            }
            
            if (DEBUG) {
                System.out.println("Processed successfully the input");
            }
        }
        
        StreamReader outputReader = null;
        StreamReader errorReader  = null;
        
        if (showOutput) {
            String label = labelStreams? "OUTPUT" : null;
            outputReader = new StreamReader(process.getInputStream(), label);
            // Read error stream
            outputReader.start();
        }
        
        if (showError) {
            String label = labelStreams? "ERROR" : null;
            errorReader = new StreamReader(process.getErrorStream(), label);
            // Read output stream
            errorReader.start();
        }
        
        int retValue;
        if (!checkForTimeout) { // Timeout mechanism: disabled
            // Await the ending of the process
            try {
                if (DEBUG) {
                    System.out.println("Waiting for the process to terminate");
                }
                retValue = process.waitFor();
                if (DEBUG) {
                    System.out.println("runCommand: process returns "
                                     + retValue);
                }
            }
            catch (InterruptedException ie) {
                // Release the semaphore that controls parallel execution
                if (executionSemaphore != null) {
                    executionSemaphore.release();
                }
                
                exitValue = EXCEPTION;
                throw new ErrorException("Interrupted while the process was "
                                       + "still running");
            }
        }
        else { // Timeout mechanism: enabled
            RuntimeExecWorker worker = new RuntimeExecWorker(process);
            worker.start();
            try {
                worker.join((long)(timeoutInSec * 1000));
                if (worker.getExitValue() == null) {
                    if (DEBUG) {
                        System.out.println("runCommand: process timed out");
                    }
                    retValue = TIMEOUT;
                }
                else {
                    retValue = worker.getExitValue();
                }
            }
            catch(InterruptedException ie) {
                worker.interrupt();
                Thread.currentThread().interrupt();
                exitValue = EXCEPTION;
                throw new ErrorException("Runtime process got interrupted. "
                                       + "Details:" + NEW_LINE
                                       + ie.getMessage());
            }
        }
        
        try {
            if (showOutput && retValue != TIMEOUT) {
                outputReader.join();
            }
            if (showError && retValue != TIMEOUT) {
                errorReader.join();
            }
        }
        catch (InterruptedException impossible) {
            // Release the semaphore that controls parallel execution
            if (executionSemaphore != null) {
                executionSemaphore.release();
            }
            
            exitValue = EXCEPTION;
            throw new ErrorException("Internal error");
        }
        
        // Output
        if (showOutput) {
            output = outputReader.getBuffer();
            if (DEBUG) {
                if (output != null && output.length > 0) {
                    String outputLabel = "RunTimeExec: output>";
                    System.out.print(outputLabel);
                    StringBuilder prefixSpaces = new StringBuilder();
                    for (int i = 0; i < outputLabel.length() - 1; i++) {
                        prefixSpaces.append(SPACE);
                    }
                    prefixSpaces.append(">");
                    
                    System.out.println(output[0]);
                    for (int i = 1; i < output.length; i++) {
                        System.out.println(prefixSpaces.toString() + output[i]);
                    }
                }
                else {
                    System.out.println ("No output");
                }
            }
        }
        
        // Errors
        if (showError) {
            errors = errorReader.getBuffer();
            if (DEBUG) {
                if (errors != null && errors.length > 0) {
                    String errorLabel = "RunTimeExec: error>";
                    System.out.print(errorLabel);
                    StringBuilder prefixSpaces = new StringBuilder();
                    for (int i = 0; i < errorLabel.length() - 1; i++) {
                        prefixSpaces.append(SPACE);
                    }
                    prefixSpaces.append(">");
                    
                    System.out.println(errors[0]);
                    for (int i = 1; i < errors.length; i++) {
                        System.out.println(prefixSpaces.toString() + errors[i]);
                    }
                }
                else {
                    System.out.println ("No errors");
                }
            }
        }
        
        // Get the exit Value of the process
        if (!checkForTimeout) {
            try {
                exitValue = process.exitValue();
            }
            catch(IllegalThreadStateException itse) {
                // Release the semaphore that controls parallel execution
                if (executionSemaphore != null) {
                    executionSemaphore.release();
                }
                 
                exitValue = EXCEPTION;
                throw new ErrorException("The process has not yet terminated. "
                                       + "Details:" + NEW_LINE
                                       + itse.getMessage());
            }
            process.destroy();
        }
        else {
            process.destroyForcibly();
        }
         
        // Release the semaphore that controls parallel execution
        if (executionSemaphore != null) {
            executionSemaphore.release();
        }
        
        return retValue;
    }
    
    @Override
    public String toString() {
        return getCmdDescription();
    }
    
    /**
     * @return the command exit value 
     */
    public int getExitValue() {
      return exitValue;
    }
    
    /**
     * @return an array with the command output (each element accounts for a
     *         standard output line) or an empty array of size 0 if there is no
     *         standard output
     */
    public String[] getOutput() {
        if(output != null) {
            return output;
        }
        else {
            return new String[0];
        }
    }
    
    /**
     * @return an array with the command error (each element accounts for a
     *         standard error line) or an empty array of size 0 if there are no
     *         standard errors
     */
    public String[] getError() {
        if(errors != null) {
            return errors;
        }
        else {
            return new String[0];
        }
    }
    
    /**
     * @return all errors as a single string
     */
    public String getErrorString() {
        StringBuilder allErrors = new StringBuilder();
        
        if (errors != null) {
            for (int i = 0; i < errors.length; i++) {
                allErrors.append(errors[i]);
                allErrors.append(NEW_LINE);
            }
        }
        
        return allErrors.toString();
    }
    
    /**
     * @return the command details command and env
     */
    public String getCmdDescription() {
        StringBuilder cmdDescription = new StringBuilder();
        
        // Cmd
        cmdDescription.append("Cmd -->" + cmd[0]);
        for (int i = 1; i < cmd.length; i++) {
            cmdDescription.append(SPACE + cmd[i]);
        }
        
        // Env
        if (env != null && env.length > 0) {
            cmdDescription.append(NEW_LINE + "Env -->" + env[0]);
            for (int i = 1; i < env.length; i++) {
                cmdDescription.append(SPACE + env[i]);
                System.out.println(" " + i + ":" + env[i]);
            }
        }
        
        return cmdDescription.toString();
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Given a process ID it looks up in the map with the execution semaphores
     * (that prevents parallel connections to this node) and returns a
     * reference to the semaphore that must be acquired before the execution.<br>
     * In case the process ID is not found in the lookup table it returns
     * {@code null} which means that the execution does not block.
     *
     * @param processID The unique ID that can be used for blocking execution
     *                  (i.e., do not allow two processes with the same ID run
     *                  in parallel) or {@code null} for a non-blocking process 
     *                  a non-blocking process
     * 
     * @return the connection blocking semaphore for the given node or 
     *         {@code null} if there is no entry for the node with the given
     *         name
     */
    private static Semaphore getSemaphore(String processID) {
        processID = processID.trim().toLowerCase(); // Case insensitive lookup
        
        return executionSemaphoreMap.get(processID);
    }
 }
