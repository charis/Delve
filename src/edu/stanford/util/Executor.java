/*
 * File          : Executor.java
 * Author        : Charis Charitsis
 * Creation Date : 8 April 2019
 * Last Modified : 1 January 2021
 */
package edu.stanford.util;

// Import Java SE classes
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
// Import custom classes
import edu.stanford.exception.ErrorException;
// Import constants
import static edu.stanford.constants.Literals.NEW_LINE;

/**
 * Thread that executes a runnable for a given duration before it times out.
 */
public class Executor
{
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** The initial sleep time, in milliseconds */
    private static final long SLEEP_TIME_IN_MILLIS = 10;
    
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * Runs a given thread and waits (if necessary) up to the specified amount
     * of time till it signals a timeout.
     * 
     * @param thread The worker thread with the task to run
     * @param timeout The timeout in milliseconds
     * 
     * @throws TimeoutException in case the task execution times out
     * @throws ErrorException in case of an error during the task execution.
     */
    public static void runWithTimeout(Thread thread,
                                      long   timeout)
           throws TimeoutException, ErrorException {
        thread.start();
        
        try {
            thread.join(timeout);
        }
        catch (Throwable t) {
            throw new ErrorException("Execution error. Details:" + NEW_LINE
                                   + t.getMessage(),
                                     t);
        }
        
        if (thread.isAlive()) {
            thread.interrupt();
            throw new TimeoutException("Task run by '" + thread.getName()
                                     + "' timed out after " + timeout + " ms");
        }
    }
    
    /**
     * Runs a given thread.<br>
     * It checks after {@code sleepTime = 10 ms} if the task has completed and
     * if not it checks again after {@code sleepFactor x sleepTime} and it
     * repeats this (thus the next time it checks after
     * {@code sleepFactor^2 x sleepTime})
     * till either the task has completed or a timeout occurred.
     * 
     * @param thread The worker thread with the task to run
     * @param timeout The timeout in milliseconds
     * @param sleepFactor The multiplier for the timeout in every iteration
     * 
     * @throws TimeoutException in case the task execution times out
     * @throws ErrorException in case of an error during the task execution.
     */
    public static void runWithTimeout(Thread thread,
                                      long   timeout,
                                      double sleepFactor)
           throws TimeoutException, ErrorException {
        long sleepTimeInMillis = SLEEP_TIME_IN_MILLIS;
        try {
            thread.start();
            
            while (sleepTimeInMillis <= timeout) {
                try {
                    Thread.sleep(sleepTimeInMillis);
                }
                catch (InterruptedException ignored) {
                    thread.interrupt();
                }
                if (!thread.isAlive()) {
                    break;
                }
                sleepTimeInMillis = Math.round(sleepTimeInMillis * sleepFactor);
            }
        }
        catch (Throwable t) {
            throw new ErrorException("Execution error. Details:" + NEW_LINE
                                   + t.getMessage(),
                                     t);
        }
        
        if (thread.isAlive()) {
            thread.interrupt();
            throw new TimeoutException("Task run by '" + thread.getName()
                                     + "' timed out after " + timeout + " ms");
        }
    }
    
    /**
     * Runs a given runnable task.
     * Note: In case of a timeout, the thread that executes the runnable gets
     *       interrupted. For e.g. if there is a Thread.sleep in method run() of
     *       the passed runnable, it will throw an InterruptedException as it
     *       gets interrupted when the timeout occurs.
     * 
     * @param runnable The task to run
     * @param timeout The timeout in milliseconds
     * 
     * @throws TimeoutException in case the task execution times out
     * @throws ErrorException in case of an error or in case the execution got
     *                        interrupted
     */
    public static void runWithTimeout(final Runnable runnable,
                                      long           timeout)
           throws TimeoutException, ErrorException {
        Callable<Object> callable = new Callable<Object>() {
            @Override
            public Object call() {
                runnable.run();
                
                return null;
            }
        };
        
        runWithTimeout(callable, timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Runs the callable task for up to the specified duration before a timeout
     * occurs.<br>
     * Note: In case of a timeout, the thread that executes the callable gets
     *       interrupted. For e.g. if there is a Thread.sleep in method run() of
     *       the passed callable, it will throw an InterruptedException as it
     *       gets interrupted when the timeout occurs.
     * 
     * @param <T> The result type of the callable task
     * @param callable The callable task
     * @param timeout The number of time units before the task execution times
     *                out
     * @param timeUnit The time unit of granularity
     * 
     * @return the object of type {@code T} as the result of the callable
     *         execution
     * 
     * @throws TimeoutException in case the task execution times out
     * @throws ErrorException in case of an error or in case the execution got
     *                        interrupted
     */
    public static <T> T runWithTimeout(Callable<T> callable,
                                       long        timeout,
                                       TimeUnit    timeUnit)
           throws TimeoutException, ErrorException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(callable);
        executor.shutdown(); // This does not cancel the already-scheduled task
        
        try {
            // Waits for at most the given time (i.e., 'timeout') for the task
            // to complete, and then return its result, if available
            return future.get(timeout, timeUnit);
        }
        catch (TimeoutException te) {
            // Remove this if you do not want to cancel the job in progress or
            // set the argument to 'false' if you do not want to interrupt the
            // thread
            future.cancel(true);
            throw te;
        }
        catch (ExecutionException ee) {
            // Unwrap the root cause
            Throwable throwable = ee.getCause();
            if (throwable instanceof Error) {
                throw new ErrorException("Execution abnormal error. Details:"
                                       + NEW_LINE + throwable.getMessage(),
                                         throwable);
            }
            else if (throwable instanceof Exception) {
                throw new ErrorException("Execution exception. Details:"
                                       + NEW_LINE + throwable.getMessage(),
                                         throwable);
            }
            else {
                throw new ErrorException("Execution illegal state. Details:"
                                       + NEW_LINE + throwable.getMessage(),
                                         throwable);
            }
        }
        catch (InterruptedException ie) {
            throw new ErrorException("The execution got interrupted. Details:"
                                   + NEW_LINE + ie.getMessage(),
                                     ie);
        }
    }
}
