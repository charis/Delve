/*
 * File          : StatsUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 20 January 2015
 * Last Modified : 1 January 2021
 */
package edu.stanford.util;

// Import Java SE classes
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for statistical computations
 */
public class StatsUtil
{
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    // ------------------------ //
    //        M  E  A  N        //
    // ------------------------ //
    /**
     * Calculates and returns the average/mean of the given set of numeric
     * values.
     * 
     * @param <T> The number type
     * @param values The values to calculate the average for
     * @param decimalDigits The number of decimal digits to use for rounding
     *                      towards the "nearest neighbor" or {@code null} for
     *                      no rounding
     * 
     * @return the average/mean value
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}<br>
     *                                  2) The list of values is empty
     */
    public static <T extends Number>
           double getMean(Collection<T> values,
                          Integer       decimalDigits)
           throws IllegalArgumentException {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values");
        }
        
        double sum         = 0.0; // Total sum of all values
        for (Number value : values) {
            sum += value.doubleValue();
        }
        
        // Calculate and return the mean/average value
        double avg = sum / values.size();
        if (decimalDigits != null && decimalDigits >= 0) {
            avg = MathUtil.round(avg, decimalDigits);
        }
        return avg;
    }
    
    // ------------------------ //
    //     M  E  D  I  A  N     //
    // ------------------------ //
    /**
     * Calculates and returns the median of the given set of numeric values.
     * The values in the provided list must be either integer, long or double.
     * 
     * @param <T> The number type
     * @param values The values to calculate the median for
     * 
     * @return the median value
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}
     *                                     <br>
     *                                  2) The list of values is empty<br>
     *                                  3) The values in the list are not
     *                                     integer or long or double values
     */
    public static <T extends Comparable<T>>
                  double getMedian(Collection<T> values)
           throws IllegalArgumentException {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values");
        }
        
        if (values.size() == 1) {
            Iterator<T> itr = values.iterator();
            T value = itr.next();
            if (!(value instanceof Number)) {
                throw new IllegalArgumentException("The value in the provided "
                                                 + "list is not an integer, "
                                                 + "long or double");
            }
            
            return ((Number)value).doubleValue();
        }
        
        List<T> sortedValues = new ArrayList<T>(values.size());
        sortedValues.addAll(values);
        Collections.sort(sortedValues);
        
        int middle = sortedValues.size() / 2;
        
        T term1 = sortedValues.get(middle);
        if (term1 instanceof Integer) {
            if (middle % 2 == 1) {
                return (Integer)term1;
            }
            else {
                return
                 ((Integer)sortedValues.get(middle - 1) + (Integer)term1) / 2.0;
            }
        }
        if (term1 instanceof Long) {
            if (middle % 2 == 1) {
                return (Long)term1;
            }
            else {
                return
                 ((Long)sortedValues.get(middle - 1) + (Long)term1) / 2.0;
            }
        }
        if (term1 instanceof Double) {
            if (middle % 2 == 1) {
                return (Double)term1;
            }
            else {
                return
                 ((Double)sortedValues.get(middle - 1) + (Double)term1) / 2.0;
            }
        }
        
        throw new IllegalArgumentException("The values in the provided list "
                                         + "are not integer, long or double");
    }
    
    /**
     * Calculates and returns the median of the given set of numeric values.
     * The values in the provided list must be either integer, long or double.
     * 
     * @param <T> The number type
     * @param values The values to calculate the median for
     * 
     * @return the median value
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}
     *                                     <br>
     *                                  2) The list of values is empty<br>
     *                                  3) The values in the list are not
     *                                     integer or long or double values
     */
    public static <T extends Number>
                  double getMedianValue(Collection<T> values)
           throws IllegalArgumentException {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values");
        }
        
        if (values.size() == 1) {
            Iterator<T> itr = values.iterator();
            T value = itr.next();
            if (!(value instanceof Number)) {
                throw new IllegalArgumentException("The value in the provided "
                                                 + "list is not an integer, "
                                                 + "long or double");
            }
            
            return ((Number)value).doubleValue();
        }
        
        List<T> sortedValues = new ArrayList<T>(values.size());
        sortedValues.addAll(values);
        Collections.sort(sortedValues,
                         new Comparator<Number>() {
                             @Override
                             public int compare(Number number1, Number number2){
                                 double diff =
                                  number1.doubleValue() - number2.doubleValue();
                                 if (diff < 0) {
                                     return -1;
                                 }
                                 if (diff > 0) {
                                     return 1;
                                 }
                                 return 0;
                             }
                         });
        
        int middle = sortedValues.size() / 2;
        
        T term1 = sortedValues.get(middle);
        if (term1 instanceof Integer) {
            if (middle % 2 == 1) {
                return (Integer)term1;
            }
            else {
                return
                 ((Integer)sortedValues.get(middle - 1) + (Integer)term1) / 2.0;
            }
        }
        if (term1 instanceof Long) {
            if (middle % 2 == 1) {
                return (Long)term1;
            }
            else {
                return
                 ((Long)sortedValues.get(middle - 1) + (Long)term1) / 2.0;
            }
        }
        if (term1 instanceof Double) {
            if (middle % 2 == 1) {
                return (Double)term1;
            }
            else {
                return
                 ((Double)sortedValues.get(middle - 1) + (Double)term1) / 2.0;
            }
        }
        
        throw new IllegalArgumentException("The values in the provided list "
                                         + "are not integer, long or double");
    }
    // ----------------------------------------------------------- //
    //     S  T  A  N  D  A  R  D    D  E  V  I  A  T  I  O  N     //
    // ----------------------------------------------------------- //
    /**
     * Calculates and returns the standard deviation of the given set of numeric
     * values.<br>
     * The formula for the sample standard deviation  is:<br>
     * <pre>
     *                    -------------------------
     *                   /  Sum[(x_i - X_avg)^2]
     *    Std Dev (X) = /   ---------------------
     *                 v            N - 1
     * </pre>
     * or Std Dev(X)= sqrt{Sum[(x_i - X_avg)^2] / (num of values - 1)}<br>
     * <br>
     * The standard deviation is the amount of variation or dispersion from the
     * average. A low standard deviation indicates that the data points tend to
     * be very close to the mean (also called expected value or average); a high
     * standard deviation indicates that the data points are spread out over a
     * large range of values.
     * 
     * @param <T> The number type
     * @param values The values to calculate the standard deviation for
     * @param decimalDigits The number of decimal digits to use for rounding
     *                      towards the "nearest neighbor" or {@code null} for
     *                      no rounding
     * 
     * @return the standard deviation 
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}<br>
     *                                  2) The list of values is empty
     */
    public static <T extends Number>
                  double getStdDeviation(Collection<T> values,
                                         Integer       decimalDigits)
           throws IllegalArgumentException {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values");
        }
        
        if (values.size() == 1) {
            return 0.0;
        }
        
        double sum = 0.0; // Total sum of all between nodes
        for (Number value : values) {
            sum += value.doubleValue();
        }
        
        // Calculate  the mean/average value
        double average = sum / values.size();
        
        double deviationFromMean;
        double sumOfDeviationsFromMeanSquare = 0.0;
        for (Number value : values) {
            deviationFromMean = value.doubleValue() - average;
            sumOfDeviationsFromMeanSquare +=
                                   deviationFromMean * deviationFromMean;
        }
        
        // Std deviation = sqrt(sumOfDeviationsFromMeanSquare / num of values)
        int numOfValues = values.size();
        double stdDev =
                  Math.sqrt(sumOfDeviationsFromMeanSquare / (numOfValues - 1));
        if (decimalDigits != null && decimalDigits >= 0) {
            stdDev = MathUtil.round(stdDev, decimalDigits);
        }
        return stdDev;
    }
    
    // ----------------------------------------------------------- //
    //           S  T  A  N  D  A  R  D    E  R  R  O  R           //
    //   ( i. e.,   s t a n d a r d    d e v i a t i o n    o f    //
    //      t h e    s a m p l i n g    d i s t r i b u t i o n)   // 
    // ----------------------------------------------------------- //
    /**
     * Calculates and returns the standard error (i.e., standard deviation of
     * the sampling distribution) given a sample of values.<br>
     * It uses the sample standard deviation to approximate the population
     * standard deviation.<br>
     * Therefore, the standard error is calculated by the following formula:<br>
     * <pre>
     *      standard error = sample standard deviation / sqrt(sample size)
     * </pre>
     * 
     * @param <T> The number type
     * @param sampleValues The sample (values) to calculate the standard error
     *                     for
     * 
     * @return the standard error of the sampling distribution (i.e., the
     *         standard deviation of the means of all samples (of the same size)
     *         in a population
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}<br>
     *                                  2) The list of values is empty<br>
     *                                  3) The values in the list are not
     *                                     integer or long or double values
     */
    public static <T extends Number>
                  double getStdError(Collection<T> sampleValues)
           throws IllegalArgumentException {
        double sampleStdSev = StatsUtil.getStdDeviation(sampleValues, null);
        return sampleStdSev / Math.sqrt(sampleValues.size());
    }
    
    /**
     * Calculates and returns the standard error (i.e., standard deviation of
     * the sampling distribution) given a sample size and a population standard
     * deviation.<br>
     * The standard error is calculated by the following formula:<br>
     * <pre>
     *      standard error = population standard deviation / sqrt(sample size)
     * </pre>
     * 
     * @param sampleSize The common sample size for all samples in the sampling
     *                   distribution
     * @param populationStdDev The population standard deviation
     * 
     * @return the standard error of the sampling distribution (i.e., the
     *         standard deviation of the means of all samples (of the same size)
     *         in a population
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The sample size is less than 1<br>
     *                                  2) The population standard deviation is
     *                                     negative
     */
    public static double getStdError(int    sampleSize,
                                     double populationStdDev)
           throws IllegalArgumentException {
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Invalid (non-positive) sample "
                                             + " size: " + sampleSize);
        }
        
        if (populationStdDev < 0) {
            throw new IllegalArgumentException("Invalid (negative) population "
                                             + "standard deviation: "
                                             + populationStdDev);
        }
        
        return populationStdDev / Math.sqrt(sampleSize);
    }
    
    // ------------------------------------------------------- //
    //                 P  E  A  R  S  O  N ' S                 //
    //   S  A  M  P  L  E    C  O  R  R  E  L  A  T  I  O  N   //
    // ------------------------------------------------------- //
    /**
     * Calculates and returns the sample correlation coefficient given a set of
     * numeric values for a variable X and a set or numeric values for a
     * variable Y.<br>
     * The two sets must contain the same number of elements.<br>
     * The formula for the sample correlation is:<br>
     * <pre>
     *                  Sum[(x_i - X_avg) * (y_i - Y_avg)]
     * r = --------------------------------------------------------------------
     *                sqrt[Sum[x_i - X_avg)^2] * sqrt[Sum[y_i - Y_avg]^2]
     * </pre>
     * 
     * @param <T> The number type
     * @param valuesX The values for the first variable
     * @param valuesY The values for the second variable
     * @param decimalDigits The number of decimal digits to use for rounding
     *                      towards the "nearest neighbor" or {@code null} for
     *                      no rounding
     * 
     * @return the sample correlation coefficient
     * 
     * @throws IllegalArgumentException in any of the following cases:<br>
     *                                  1) The list of values is {@code null}<br>
     *                                  2) The list of values is empty<br>
     *                                  3) The two list have different size
     */
    public static <T extends Number>
           double getCorrelation(Collection<T> valuesX,
                                 Collection<T> valuesY,
                                 Integer       decimalDigits)
           throws IllegalArgumentException {
        // ---  A  r  g  u  m  e  n  t      V  a  l  i  d  a  t  i  o  n  --- //
        if (valuesX == null || valuesX.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values "
                                             + "for the first variable (X)");
        }
        if (valuesY == null || valuesY.isEmpty()) {
            throw new IllegalArgumentException("Null or empty list of values "
                                             + "for the first variable (Y)");
        }
        if (valuesX.size() != valuesY.size()) {
            throw new IllegalArgumentException("The two variable lists have "
                                             + "different size");
        }
        
        if (valuesX.size() == 1) {
            throw new IllegalArgumentException("The variable lists have a "
                                             + "single value");
        }
        
        // Make sure that the x values are not the same (they vary)
        T prevValue = null;
        boolean sameValues = true;
        for (T val : valuesX) {
            if (prevValue == null) {
                prevValue = val;
            }
            else {
                if (val != prevValue) {
                    sameValues = false;
                    break;
                }
            }
        }
        if (sameValues) {
             throw new IllegalArgumentException("All values in X are the same");
        }
        
        // Make sure that the y values are not the same (they vary)
        prevValue = null;
        sameValues = true;
        for (T val : valuesY) {
            if (prevValue == null) {
                prevValue = val;
            }
            else {
                if (val != prevValue) {
                    sameValues = false;
                    break;
                }
            }
        }
        if (sameValues) {
             throw new IllegalArgumentException("All values in Y are the same");
        }
        
        double meanX = getMean(valuesX, null);
        double meanY = getMean(valuesY, null);
        
        Iterator<T> itrX = valuesX.iterator();
        Iterator<T> itrY = valuesY.iterator();
        
        double num  = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;
        while (itrX.hasNext()) {
            double x = itrX.next().doubleValue();
            double y = itrY.next().doubleValue();
            
            double diffX = (x - meanX);
            double diffY = (y - meanY);
            // The numerator is the following sum
            num += diffX * diffY;
            
            // Calculate the following two sums to used them for the denominator
            sum1 += diffX * diffX;
            sum2 += diffY * diffY;
        }
        double den = Math.sqrt(sum1) *  Math.sqrt(sum2);
        
        double correlation = num / den;
        if (decimalDigits != null && decimalDigits >= 0) {
            correlation = MathUtil.round(correlation, decimalDigits);
        }
        
        return correlation;
    }
}
