/*
 * File          : RuntimeExecWorker.java
 * Author        : Charis Charitsis
 * Creation Date : 20 December 2020
 * Last Modified : 20 December 2020
 */
package edu.stanford.util.runtime;

/**
 * Worker thread to provide timeout mechanism for a runtime exec process
 */
public class RuntimeExecWorker extends Thread
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The runtime exec process to manage
     */
    private final Process process;
    /**
     * The exit value of the process or {@code null} in case of abnormal
     * termination or timeout
     */
    private Integer exitValue;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a new RuntimeExecWorker instance.
     * 
     * @param process The runtime exec process to manage
     */
    public RuntimeExecWorker(Process process) {
        this.process = process;
    }
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    @Override
    public void run() {
        try { 
            exitValue = process.waitFor();
        }
        catch (InterruptedException ignore) {
            return;
        }
    }
    
    /**
     * @return the exit value for the process or {@code null} in case of
     *         abnormal termination or timeout
     */
    public Integer getExitValue() {
        return exitValue;
    }
}
