/*
 * File          : ErrorException.java
 * Author        : Charis Charitsis
 * Creation Date : 15 June 2013
 * Last Modified : 2 April 2019
 */
package edu.stanford.exception;

/**
 * ErrorException is raised every time an operation cannot be completed
 * successfully and is usually critical to the operation that is performed.
 */
public class ErrorException extends Exception
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -5707156809453721497L;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructor of class ErrorException.
     * 
     * @param message The exception message
     */
    public ErrorException(String message) {
        super(message);
    }
    
    /**
     * Constructor of class ErrorException.
     * 
     * @param message The exception message
     * @param cause The cause which is saved for later retrieval by the
     *              Throwable.getCause()  method.<br>
     *              Note: A {@code null} value is permitted, and indicates that
     *                    the cause is nonexistent or unknown.
     */
    public ErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}