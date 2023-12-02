/*
 * File          : InvalidArgumentException.java
 * Author        : Charis Charitsis
 * Creation Date : 25 June 2013
 * Last Modified : 18 August 2018
 */
package edu.stanford.exception;

/**
 * InvalidArgumentException is raised every time an argument is invalid.
 */
public class InvalidArgumentException extends Exception
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -9122253179835518302L;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructor of class InvalidArgumentException.
     * 
     * @param message The exception message
     */
    public InvalidArgumentException(String message) {
        super(message);
    }
    
    /**
     * Constructor of class InvalidArgumentException.
     * 
     * @param message The exception message
     * @param cause The cause which is saved for later retrieval by the
     *              Throwable.getCause()  method.<br>
     *              Note: A {@code null} value is permitted, and indicates that
     *                    the cause is nonexistent or unknown.
     */
    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
