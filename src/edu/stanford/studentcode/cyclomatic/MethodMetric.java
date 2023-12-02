/*
 * File          : ClassMetric.java
 * Author        : Charis Charitsis
 * Creation Date : 18 July 2021
 * Last Modified : 20 September 2021
 */
package edu.stanford.studentcode.cyclomatic;

/**
 * Placeholder that holds the metrics for a particular method.
 */
public class MethodMetric implements Comparable<MethodMetric>
{
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The method name
     */
    private final String name;
    /**
     * The cyclomatic complexity of the method
     */
    private final int    cyclomaticComplexity;
    /**
     * The number of instructions in the method
     */
    private final int    numOfInstructions;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Constructs a new MethodMetric.
     * 
     * @param name The name of the method
     * @param cyclomaticComplexity The cyclomatic complexity of the method
     * @param numOfInstructions The number of instructions in the method
     */
    public MethodMetric(String name,
                        int    cyclomaticComplexity,
                        int    numOfInstructions) {
        this.name                 = name;
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.numOfInstructions    = numOfInstructions;
    }
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this MethodMetric with another MethodMetric object for order.
     * <br>
     * 
     * @param other The other MethodMetric to be compared with this one
     * 
     * @return the value {@code 1} if this method has smaller cycloclomatic
     *         complexity that the other method or if they have the same but
     *         this method has less instructions, the value {@code -1} if this
     *         method has greater cycloclomatic complexity that the other
     *         method or if they have the same but this method has more
     *         instructions, the value {@code 0} if both methods have the same
     *         cycloclomatic complexity and number of instructions
     */
    @Override
    public int compareTo(MethodMetric other) {
        if (cyclomaticComplexity < other.cyclomaticComplexity) {
            return 1;
        }
        
        if (cyclomaticComplexity > other.cyclomaticComplexity) {
            return -1;
        }
        
        // cyclomaticComplexity == other.cyclomaticComplexity
        if(numOfInstructions < other.numOfInstructions) {
            return 1;
        }
        else if(numOfInstructions > other.numOfInstructions) {
            return -1;
        }
        else {
            return 0;
        }
    }
    
    /**
     * @return the method name
     */
    public String getMethodName() {
        return name;
    }
    
    /**
     * @return the cyclomatic complexity of the method
     */
    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
    
    /**
     * @return the number of instructions in the method
     */
    public int getNumOfInstructions() {
        return numOfInstructions;
    }
}
