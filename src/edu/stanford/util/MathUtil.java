/*
 * File          : MathUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 18 July 2014
 * Last Modified : 28 August 2018
 */
package edu.stanford.util;

// Import Java SE classes
import java.math.RoundingMode;
import java.text.DecimalFormat;
// Import constants
import static edu.stanford.constants.Literals.SPACE;

/**
 * Utility class for math related operations
 */
public class MathUtil
{
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * It rounds the provided double precision value using the given number of
     * decimal digits.<br>
     * It rounds towards the "nearest neighbor" unless both neighbors are
     * equidistant, in which case, round towards the even neighbor.
     * 
     * @param value The double-precision value
     * @param decimalDigits The number of decimal digits to use. 
     *                      If {@code null} or negative, the method returns
     *                      the provided value unaltered. 
     * 
     * @return the rounded value using the specified number of decimal digits
     *         or the value unaltered in case the decimal digits is a either
     *         {@code null} or negative or {@code null} 
     */
    public static Double round(Double value, Integer decimalDigits) {
        if (value == null) {
            return null;
        }
        
        if ((decimalDigits == null) || (decimalDigits < 0)) {
            return value;
        }
        
        return Double.valueOf(MathUtil.doubleToString(value,
                                                      decimalDigits,
                                                      0)); // No minimum length
    }
    
    /**
     * It rounds the provided double precision value and returns its string
     * representation using the given number of decimal digits.<br>
     * It rounds towards the "nearest neighbor" unless both neighbors are
     * equidistant, in which case, round towards the even neighbor.
     * 
     * @param value The double-precision value
     * @param decimalDigits The number of decimal digits to use in the string
     *                      representation of the value
     * @param minLength The minimum number of characters that the returned
     *                  string representation should have.<br>
     *                  If the string representation has not that many
     *                  characters, whitespaces are prepended.
     * 
     * @return the string representation of the value using the specified number
     *         of decimal digits or {@code null} in case the given value is
     *         {@code null} or the decimal digits is a negative
     */
    public static String doubleToString(Double value,
                                        int    decimalDigits,
                                        int    minLength) {
        if ((value == null) || (decimalDigits < 0)) {
            return null;
        }
        
        StringBuilder pattern = new StringBuilder();
        pattern.append("0.");
        for (int i = 0; i < decimalDigits; i++) {
            pattern.append("#");
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        
        String strRepresentation = decimalFormat.format(value);
        int indexOfDot = strRepresentation.indexOf(".");
        int decimalDigitsInValue;
        if (indexOfDot != -1) {
            decimalDigitsInValue =
                   strRepresentation.substring(indexOfDot + 1).length();
        }
        else {
           decimalDigitsInValue = 0;
        }
        if ((decimalDigitsInValue == 0) && (decimalDigits > 0)) {
           strRepresentation += ".";
        }
        for (int i = 0; i < decimalDigits - decimalDigitsInValue; i++) {
           strRepresentation += "0";
        }
        
        int numOfSpacesInFront = minLength - strRepresentation.length();
        if (numOfSpacesInFront > 0) {
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < numOfSpacesInFront; i++) {
                spaces.append(SPACE);
            }
            strRepresentation = spaces.toString() + strRepresentation;
        }
        
        return strRepresentation;
    }
    
    /**
     * Given a base-10 integer number it returns the number of its digits.<br>
     * The sign is not taken into account (e.g. the number of digits for both
     * 120 and -120 is 3).
     *  
     * @param number The base-10 integer number to get back its number of digits
     * 
     * @return the number of digits for the given number
     */
    public static int numberOfDigits(int number) {
        if (number < 0) { // Covert negative to positive number
           number = -number;
        }
        
        return (int)Math.log10((double)number) + 1;
    }
}
