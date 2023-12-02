/*
 * File          : PrePostConditionPair.java
 * Author        : Charis Charitsis
 * Creation Date : 13 February 2020
 * Last Modified : 1 January 2021
 */
package edu.stanford.java.state;

// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Represents a pre-condition and a post-condition pair that stores the states
 * when entering and exiting a method.
 */
public class PrePostConditionPair implements Comparable<PrePostConditionPair>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The pre-condition which is the state when entering a method */
    private final State preCondition;
    /** The pre-condition which is the state when exiting a method */
    private final State postCondition;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a PrePostConditionPair.
     * 
     * @param preCondition The pre-condition which is the state when entering a
     *                      method
     * @param postCondition The pre-condition which is the state when exiting a
     *                      method
     */
    public PrePostConditionPair(State preCondition,
                                State postCondition) {
        this.preCondition  = preCondition;
        this.postCondition = postCondition;
    }
    
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
     * For any non-null object (obj) to compare to this reference object (this),
     * this method returns true if and only if:<br>
     * <pre>
     *   1) 'this' and 'obj' refer to the same object (this == obj has the value
     *      true)
     *   OR
     *   2) 'this' and 'obj' refer to different objects, but:
     *      i)   'obj' is not null
     *      ii)  'obj' is an instance of the same class as 'this' 
     *      iii) 'obj' has the same preCondition and postCondition as 'this'
     * </pre>
     * 
     * @param obj The reference object to compare against
     * 
     * @return {@code true} if this object is the same as the obj argument or
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        // 'this' and 'obj' are pointing to the same object => return true
        if (this == obj) {
            return true;
        }
        
        // 'obj' is null and not pointing to the same object as 'this'
        // => return false
        if (obj == null) {
            return false;
        }
        
        // 'obj' is pointing to a different object than 'this'
        // If that object (obj) is not a PrePostConditionPair object return false
        if (!(obj instanceof PrePostConditionPair)) {
            return false;
        }
        
        // Now can cast safely
        PrePostConditionPair other = (PrePostConditionPair) obj;
        
        // Make sure that in both PrePostConditionPair have the same
        // pre-condition and the same post-condition
        if (!preCondition.equals(other.preCondition) ||
            !postCondition.equals(other.postCondition)) {
            return false;
        }
        
        // If we reach here => the two objects are considered equal
        return true;
    }
    
    // ------------------------------------------ //
    //    h   a   s   h   C   o   d   e   (  )    //
    // ------------------------------------------ //
    /**
     * {@inheritDoc}
     * 
     * To calculate the hash code it takes into consideration the pre-condition
     * and the post-condition.
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + preCondition.hashCode();
        result = prime * result + postCondition.hashCode();
        
        return result;
    }
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this PrePostConditionPair with another PrePostConditionPair
     * object for order.<br>
     * The comparison between the two takes place by:<br>
     * 1) The pre-condition<br>
     * 2) The post-condition<br>
     *  
     * This PrePostConditionPair is 'less than' the provided
     * PrePostConditionPair, if and only if, either
     * {@code this.preCondition.compareTo(other.preCondition) < 0} or if
     * {@code this.preCondition.compareTo(other.preCondition) == 0} and
     * {@code this.postCondition.compareTo(other.postCondition) > 0}
     * 
     * @param other The other PrePostConditionPair to be compared with this one
     * 
     * @return the value {@code 0}/{@code -1}/{@code 1} if this
     *         PrePostConditionPair is 'equal'/'less than'/'greater than' the
     *         other PrePostConditionPair respectively
     */
    @Override
    public int compareTo(PrePostConditionPair other) {
        if (preCondition.compareTo(other.preCondition) < 0) {
            return -1;
        }
        
        if (preCondition.compareTo(other.preCondition) > 0) {
            return 1;
        }
        
        // this.preCondition.compareTo(other.preCondition) == 0
        if (postCondition.compareTo(other.postCondition) < 0) {
            return -1;
        }
        
        if (postCondition.compareTo(other.postCondition) > 0) {
            return 1;
        }
        
        return 0;
    }
    
    /**
     * @return the pre-condition which is the state when entering a method
     */
    public State getPrecondition() {
        return preCondition;
    }
    
    /**
     * @return the post-condition which is the state when exiting a method
     */
    public State getPostcondition() {
        return postCondition;
    }
    
    /**
     * Compresses the pre-condition/post-condition pair as a String.<br>
     * The two states are separated by a new line
     * 
     * @return pre-condition/post-condition pair as a single String
     */
    @Override
    public String toString() {
        return preCondition.toString() + NEW_LINE + preCondition.toString();
    }
}
