/*
 * File          : Argument.java
 * Author        : Charis Charitsis
 * Creation Date : 5 April 2014
 * Last Modified : 28 February 2021
 */
package edu.stanford.javaparser.body;

// Import com.github.javaparser classes
import com.github.javaparser.ast.body.Parameter;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Represents an argument in a method, interface or constructor declaration
 */
public class Argument extends Variable
{
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new Argument
     * 
     * @param parameter The module with the information about the parameter that
     *                  is declared in a constructor or method
     * 
     * @throws ErrorException in case of an error processing the type to
     *                        determine if it is numeric, text, boolean or
     *                        none of those
     */
    public Argument(Parameter parameter)
           throws ErrorException {
        super(parameter);
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * @return {@code true} if this variable is also an argument in a method
     *         or constructor or {@code false} otherwise
     */
    @Override
    public boolean isArgument() {
        return true;
    }
}
