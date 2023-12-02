/*
 * File          : TimeUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 30 September 2014
 * Last Modified : 2 January 2021
 */
package edu.stanford.util;

// Import Java SE classes
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
// Import custom classes
import edu.stanford.exception.ErrorException;

/**
 * Class that gets an input from the user
 */
public class TimeUtil
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /**
     * The pattern use to format the date considering just the year, the month
     * and the day
     */
    private   static final String           DAYS_PATTERN                =
                                            "yyyy-MM-dd";
    /** The pattern use to format the date */
    private   static final String           DATE_PATTERN                =
                                            "yyyy-MM-dd HH:mm:ss";
    /** The pattern use to format the date. Includes the time zone. */
    private   static final String           DATE_PATTERN_WITH_TIME_ZONE =
                                            "yyyy-MM-dd HH:mm:ss ZZZZ";
    /** Object for formatting and parsing dates */
    protected static final SimpleDateFormat DATE_FORMAT                 =
                                            new SimpleDateFormat(DATE_PATTERN);
    /** Object for formatting and parsing dates. Includes the time zone. */
    protected static final SimpleDateFormat DATE_FORMAT_WITH_TIME_ZONE  =
                                            new SimpleDateFormat(
                                                   DATE_PATTERN_WITH_TIME_ZONE);
    /**
     * Object for formatting and parsing dates considering just the year, month
     * and day
     */
    protected static final SimpleDateFormat DATE_FORMAT_DAYS_ONLY       =
                                            new SimpleDateFormat(DAYS_PATTERN);
    /** The number of milliseconds in one day */
    private   static final long             MILLISECONDS_IN_A_DAY       =
                                                         24 * 60 * 60 * 1000L;
    
    // --------------------------------------------------------- //
    //   P   U   B   L   I   C       M   E   T   H   O   D   S   //
    // --------------------------------------------------------- //
    /**
     * Gets this Calendar's time from the given long value formatted as: <br>
     * yyyy-dd-MM HH:mm:ss
     * 
     * @param millis The new time in UTC milliseconds from the epoch
     * 
     * @return the time in the following format:<br>
     *         yyyy-dd-MM HH:mm:ss
     */
    public static String getTime(long millis) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(millis);
        
        return DATE_FORMAT.format(calendar.getTime());
    }
    
    /**
     * @return the current time in the following format:<br>
     *         yyyy-dd-MM HH:mm:ss
     */
    public static String getCurrentTime() {
        return DATE_FORMAT.format(GregorianCalendar.getInstance().getTime());
    }
    
    /**
     * Given a date in the following format:<br>
     *    yyyy-dd-MM HH:mm:ss ZZZZ where ZZZZ denotes the time zone (e.g. -0700) 
     * or yyyy-dd-MM HH:mm:ss<br>
     * it returns the Date object which represents this instant in time, with
     * millisecond precision.
     * 
     * @param formattedDate The date in the following textual format:<br>
     *                      yyyy-dd-MM HH:mm:ss ZZZZ or yyyy-dd-MM HH:mm:ss
     * 
     * @return the Date object which represents this instant in time, with
     *         millisecond precision.
     * 
     * @throws ErrorException in case the date string has not the expected
     *                        format (i.e., 'yyyy-dd-MM HH:mm:ss ZZZZ' or
     *                        'yyyy-dd-MM HH:mm:ss')
     */
    public static Date getDate(String formattedDate)
           throws ErrorException {
        try {
            return DATE_FORMAT_WITH_TIME_ZONE.parse(formattedDate);
        }
        catch (ParseException pe1) {
            try {
                return DATE_FORMAT.parse(formattedDate);
            }
            catch (ParseException pe2) {
                throw new ErrorException(pe2.getMessage());
            }
        }
    }
    
    /**
     * Given two dates in the following format:<br>
     *    yyyy-dd-MM HH:mm:ss ZZZZ where ZZZZ denotes the time zone (e.g. -0700) 
     * or yyyy-dd-MM HH:mm:ss<br>
     * it returns the difference in seconds after subtracting the first date
     * from the second.
     * 
     * @param formattedDate1 The first date in the following textual format:<br>
     *                       yyyy-dd-MM HH:mm:ss ZZZZ  or  yyyy-dd-MM HH:mm:ss<br>
     *                       In other words this is the 'before' date.
     * @param formattedDate2 The second date in the following textual format:<br>
     *                       yyyy-dd-MM HH:mm:ss ZZZZ  or  yyyy-dd-MM HH:mm:ss<br>
     *                       In other words this is the 'after' date.
     *
     * @return the difference in seconds between the two dates after subtracting
     *         the first date from the second
     * 
     * @throws ErrorException in case either date string has not the expected
     *                        format (i.e., 'yyyy-dd-MM HH:mm:ss ZZZZ' or
     *                        'yyyy-dd-MM HH:mm:ss')
     */
    public static long subtractDates(String formattedDate1,
                                     String formattedDate2)
           throws ErrorException {
        Date date1   = TimeUtil.getDate(formattedDate1);
        Date date2   = TimeUtil.getDate(formattedDate2);
        long seconds = (date2.getTime() - date1.getTime()) / 1000;
        
        return seconds;
    }
    
    /**
     * Given two dates in the following format:<br>
     *    yyyy-dd-MM HH:mm:ss ZZZZ where ZZZZ denotes the time zone (e.g. -0700) 
     * or yyyy-dd-MM HH:mm:ss<br>
     * it considers only the yyyy-dd-MM and returns the difference in days after
     * subtracting the first date from the second.
     * 
     * @param formattedDate1 The first date in the following textual format:<br>
     *                       yyyy-dd-MM  or  yyyy-dd-MM HH:mm:ss ZZZZ  or
     *                       yyyy-dd-MM HH:mm:ss<br>
     *                       In other words this is the 'before' date.
     * @param formattedDate2 The second date in the following textual format:<br>
     *                       yyyy-dd-MM  or  yyyy-dd-MM HH:mm:ss ZZZZ  or
     *                       yyyy-dd-MM HH:mm:ss<br>
     *                       In other words this is the 'after' date.
     *
     * @return the difference in days between the two dates after subtracting
     *         the first date from the second
     * 
     * @throws ErrorException in case either date string has not the expected
     *                        format (i.e., 'yyyy-dd-MM'  or
     *                        'yyyy-dd-MM HH:mm:ss ZZZZ'  or
     *                        'yyyy-dd-MM HH:mm:ss')
     */
    public static int subtractDatesInDays(String formattedDate1,
                                          String formattedDate2)
           throws ErrorException {
        Date date1;
        Date date2;
        try {
            date1 = DATE_FORMAT_DAYS_ONLY.parse(formattedDate1);
            date2 = DATE_FORMAT_DAYS_ONLY.parse(formattedDate2);
        }
        catch (ParseException pe) {
            throw new ErrorException(pe.getMessage());
        }
        
        long days = (date2.getTime() - date1.getTime()) / MILLISECONDS_IN_A_DAY;
        
        return (int)days;
    }
    
    /**
     * Given a date in the following format:<br>
     *    yyyy-dd-MM HH:mm:ss ZZZZ where ZZZZ denotes the time zone (e.g. -0700) 
     * or yyyy-dd-MM HH:mm:ss<br>
     * and an amount of seconds it subtracts this time from the date and returns
     * the result.
     * 
     * @param formattedDate The date in the following textual format:<br>
     *                      yyyy-dd-MM HH:mm:ss ZZZZ  or  yyyy-dd-MM HH:mm:ss
     * @param seconds The amount of seconds to subtract from the given date
     *
     * @return the new date in the same format as provided date
     * 
     * @throws ErrorException in case the date string has not the expected
     *                        format (i.e., 'yyyy-dd-MM HH:mm:ss ZZZZ' or
     *                        'yyyy-dd-MM HH:mm:ss') or in case the amount of
     *                        seconds to subtract is negative
     */
    public static String subtractTime(String formattedDate,
                                      long   seconds)
           throws ErrorException {
        Date date;
        boolean hasTimeZone;
        try {
            date = DATE_FORMAT_WITH_TIME_ZONE.parse(formattedDate);
            hasTimeZone = true;
        }
        catch (ParseException pe1) {
            try {
                date = DATE_FORMAT.parse(formattedDate);
                hasTimeZone = false;
            }
            catch (ParseException pe2) {
                throw new ErrorException(pe2.getMessage());
            }
        }
        
        long resultInMillis = date.getTime() - (1000 * seconds);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(resultInMillis);
        
        if (hasTimeZone) {
            return DATE_FORMAT_WITH_TIME_ZONE.format(calendar.getTime());
        }
        else {
            return DATE_FORMAT.format(calendar.getTime());
        }
    }
    
    /**
     * Given a date in the following format:<br>
     *    yyyy-dd-MM HH:mm:ss ZZZZ where ZZZZ denotes the time zone (e.g. -0700) 
     * or yyyy-dd-MM HH:mm:ss<br>
     * and an amount of seconds it subtracts this time from the date and returns
     * the result.
     * 
     * @param formattedDate The date in the following textual format:<br>
     *                      yyyy-dd-MM  or  yyyy-dd-MM HH:mm:ss ZZZZ  or
     *                      yyyy-dd-MM HH:mm:ss
     * @param days The number of days to subtract from the given date
     * @param useDaysAsPecision {@code true} to use only year, month and day in
     *                          the returned result (i.e., use 'yyyy-dd-MM' as
     *                          the result format) or {@code false} to use the
     *                          same format as the given date
     * 
     * @return the new date after subtracting the specified number of days The
     *         returned string is formatted as yyyy-dd-MM if 'useDaysAsPecision'
     *         is {@code true} or else it is in the same format as provided date 
     * 
     * @throws ErrorException in case the date string has not the expected
     *                        format (i.e., 'yyyy-dd-MM ' or
     *                        'yyyy-dd-MM HH:mm:ss ZZZZ' or
     *                        'yyyy-dd-MM HH:mm:ss') or in case the amount of
     *                        days to subtract is negative
     */
    public static String subtractDaysFromDate(String  formattedDate,
                                              boolean useDaysAsPecision,
                                              long    days)
           throws ErrorException {
        Date date;
        boolean hasTimeZone = false;
        boolean hasDaysOnly = false;
        try {
            date = DATE_FORMAT_WITH_TIME_ZONE.parse(formattedDate);
            hasTimeZone = true;
        }
        catch (ParseException pe1) {
            try {
                date = DATE_FORMAT.parse(formattedDate);
            }
            catch (ParseException pe2) {
                try {
                    date = DATE_FORMAT_DAYS_ONLY.parse(formattedDate);
                    hasDaysOnly = true;
                }
                catch (ParseException pe3) {
                    throw new ErrorException(pe3.getMessage());
                }
            }
        }
        
        long resultInMillis = date.getTime() - (days * MILLISECONDS_IN_A_DAY);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(resultInMillis);
        
        if (useDaysAsPecision || hasDaysOnly) {
            return DATE_FORMAT_DAYS_ONLY.format(calendar.getTime());
        }
        
        if (hasTimeZone) {
            return DATE_FORMAT_WITH_TIME_ZONE.format(calendar.getTime());
        }
        
        return DATE_FORMAT.format(calendar.getTime());
    }
}