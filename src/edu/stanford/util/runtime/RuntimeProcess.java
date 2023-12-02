/*
 * File          : RuntimeProcess.java
 * Author        : Charis Charitsis
 * Creation Date : 9 February 2015
 * Last Modified : 19 August 2018
 */
package edu.stanford.util.runtime;

// Import Java SE classes
import java.io.InputStream;
import java.io.OutputStream;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import constants
import static edu.stanford.util.runtime.RuntimeProcess.ProcessState.*;

/**
 * Represents a native process created by Runtime.exec that can be use to<br>
 * <pre>
 * - Obtain obtain information about it
 * - Perform input from the process
 * - Perform output to the process
 * - Wait for the process to complete
 * - Check the exit status of the process
 * - Destroy/kill the process
 * </pre>
 */
public class RuntimeProcess
{
    /**
     * Enumeration with the possible states that a process can be in
     */
    public enum ProcessState {
         /** The process is running */
         RUNNING,
         /** The process is terminated */
         TERMINATED,
         /** The process is terminated by the user */
         TERMINATED_BY_USER;
    }
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * Represents a native process created by Runtime.exec that can be use to<br>
     * <pre>
     * - Obtain obtain information about it
     * - Perform input from the process
     * - Perform output to the process
     * - Wait for the process to complete
     * - Check the exit status of the process
     * - Destroy/kill the process
     * </pre>
     */
    private Process process; 
    /**
     * The current state that the process is in
     */
    private ProcessState processState;
    
    /**
     * Constructs a new
     *
     * @param process Process object
     */
    public RuntimeProcess(Process process) { 
        this.process = process;
        processState = RUNNING;
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Terminates the process if is running
     * 
     * @return the process state which can be either TERMINATED if the process
     *         had already completed or TERMINATED_BY_USER if it is terminated
     *         now
     */
    public synchronized ProcessState terminate() {
        if (process.isAlive()) {
            process.destroyForcibly();
            processState = TERMINATED_BY_USER;
        }
        else {
            processState = TERMINATED;
        }
        
        return processState;
    }
    
    /**
     * Causes the current thread to wait, if necessary, until the process has
     * terminated.<br>
     * This method returns immediately if the subprocess has already terminated.
     * If the process has not yet terminated, the calling thread will be blocked
     * until the subprocess exits.
     *
     * @return the exit value of the process represented. By convention, the
     *         value 0 indicates normal termination
     *
     * @throws ErrorException if the current thread is interrupted by another
     *                        thread while waiting
     */
    public int waitFor()
           throws ErrorException {
        try {
            return process.waitFor();
        }
        catch (InterruptedException ie) {
            throw new ErrorException("The process has been interrupted while "
                                   + "waiting for completion");
        }
    }
    
    /**
     * Detects whether the process is running, has completed or has been
     * terminated by the user
     *
     * @return the process state which can be either RUNNING if the process is
     *         still alive or TERMINATED if the process has already completed or
     *         TERMINATED_BY_USER if it has been terminated by the user
     */
    public synchronized ProcessState getProcessState() {
        if (processState != TERMINATED_BY_USER) {
            if (process.isAlive()) {
                processState = RUNNING;
            }
            else {
                processState = TERMINATED;
            }
        }
        
        return processState;
    }
    
    /**
     * Returns exit value of the process
     *
     * @return the exit value of the process. By convention, the value 0
     *         indicates normal termination
     *
     * @throws ErrorException if the process has not yet terminated
     */
    public int getExitValue()
           throws ErrorException {
        try {
            return process.exitValue();
        }
        catch (IllegalThreadStateException itse) {
            throw new ErrorException("The process has not yet terminated");
        }
    }
    
    /**
     * @return the output stream connected to the normal input of the process
     */
    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }
    
    /**
     * @return the input stream connected to the standard output of the process
     */
    public InputStream getInputStream() {
        return process.getInputStream();
    }
    
    /**
     * @return the input stream connected to the error output of the process
     */
    public InputStream getErrorStream() {
        return process.getErrorStream();
    }
}
