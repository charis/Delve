/*
 * File          : State.java
 * Author        : Charis Charitsis
 * Creation Date : 18 September 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.state;

/**
 * Encapsulates the state of the program
 */
public abstract class State implements Comparable<State>
{
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    // ---------------------------------- //
    //    e   q   u   a   l   s   (  )    //
    // ---------------------------------- //
    /**
     * Indicates whether some other object is "equal to" this one.<br>
     * The equals method implements an equivalence relation on non-null object
     * references.<br>
     * 
     * @param obj The reference object to compare against
     * 
     * @return {@code true} if this object is the same as the obj argument or
     *         {@code false} otherwise
     */
    @Override
    public abstract boolean equals(Object obj);
    
    // ------------------------------------------ //
    //    h   a   s   h   C   o   d   e   (  )    //
    // ------------------------------------------ //
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this State with another State object for order.<br>
     * 
     * @param other The other State to be compared with this one
     * 
     * @return the value {@code 0}/{@code -1}/{@code 1} if this State is 
     *         'equal'/'less than'/'greater than' the other State respectively
     */
    @Override
    public abstract int compareTo(State other);
}
