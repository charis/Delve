/*
 * File          : WarningException.java
 * Author        : Charis Charitsis
 * Creation Date : 15 June 2013
 * Last Modified : 18 August 2018
 */
package edu.stanford.exception;

/**
 * WarningException is raised every time an operation cannot be completed
 * successfully, but is not critical to the overall operation that is performed.
 */
public class WarningException extends RuntimeException
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -5884382545980389306L;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructor of class WarningException.
     * 
     * @param message The exception message
     */
    public WarningException(String message) {
        super(message);
    }
    
    /**
     * Constructor of class WarningException.
     * 
     * @param message The exception message
     * @param cause The cause which is saved for later retrieval by the
     *              Throwable.getCause()  method.<br>
     *              Note: A {@code null} value is permitted, and indicates that
     *                    the cause is nonexistent or unknown.
     */
    public WarningException(String message, Throwable cause) {
        super(message, cause);
    }
}
