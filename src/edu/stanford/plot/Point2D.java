/*
 * File          : Point2D.java
 * Author        : Charis Charitsis
 * Creation Date : 21 December 2018
 * Last Modified : 22 December 2018
 */
package edu.stanford.plot;

// Import Java SE classes
import java.util.Comparator;

/**
 *  Immutable data type to encapsulate a 2D point with x-y real-value
 *  coordinates.
 */
public final class Point2D implements Comparable<Point2D>
{
    // ----------------------------------------------------------------- //
    //   P   U   B   L   I   C       C   O   N   S   T   A   N   T   S   //
    // ----------------------------------------------------------------- //
    /**
     * Compares two points by x-coordinate.
     */
    public static final Comparator<Point2D> X_ORDER = new Comparator<Point2D>(){
        /**
         * Compares two points for order. It uses only the x-coordinate to
         * determine the order.
         * 
         * @param point1 The first point
         * @param point2 The second point
         * 
         * @return one of -1, 0, or 1 according to whether the value
         *         of the x-coordinate of the first point is greater, equal or
         *         less than the x-coordinate of the second point, respectively
         */
        @Override
        public int compare(Point2D point1, Point2D point2) {
            if (point1.x < point2.x) {
                return -1;
            }
            else if (point1.x > point2.x) {
                return 1;
            }
            else {
                return 0;
            }
        }
    };
    
    /**
     * Compares two points by y-coordinate.
     */
    public static final Comparator<Point2D> Y_ORDER = new Comparator<Point2D>(){
        /**
         * Compares two points for order. It uses only the y-coordinate to
         * determine the order.
         * 
         * @param point1 The first point
         * @param point2 The second point
         * 
         * @return one of -1, 0, or 1 according to whether the value
         *         of the y-coordinate of the first point is greater, equal or
         *         less than the y-coordinate of the second point, respectively
         */
        @Override
        public int compare(Point2D point1, Point2D point2) {
            if (point1.y < point2.y) {
                return -1;
            }
            else if (point1.y > point2.y) {
                return 1;
            }
            else {
                return 0;
            }
        }
    };
    
    /**
     * Compares two points by polar radius.
     */
    public static final Comparator<Point2D> POLAR_RADIUS_ORDER =
                                            new Comparator<Point2D>() {
        /**
         * Compares two points for order. It uses only the y-coordinate to
         * determine the order.
         * 
         * @param point1 The first point
         * @param point2 The second point
         * 
         * @return one of -1, 0, or 1 according to whether the value
         *         of the y-coordinate of the first point is greater, equal or
         *         less than the y-coordinate of the second point, respectively
         */
        @Override
        public int compare(Point2D point1, Point2D point2) {
            double delta = ((point1.x * point1.x) + (point1.y * point1.y)) -
                           ((point2.x * point2.x) + (point2.y * point2.y));
            if (delta < 0) {
                return -1;
            }
            else if (delta > 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
    };
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The x-coordinate */
    private final double x;
    /** The y-coordinate */
    private final double y;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a new Point2D.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * 
     * @throws IllegalArgumentException if either {@code x} or {@code y} is
     *                                  {@code Double.NaN}, 
     *                                  {@code Double.POSITIVE_INFINITY} or
     *                                  {@code Double.NEGATIVE_INFINITY}
     */
    public Point2D(double x,
                   double y) 
           throws IllegalArgumentException {
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException("Coordinates cannot be NaN");
        }
        
        // To deal with the difference behavior of double and Double with
        // respect to -0.0 and +0.0, convert any coordinates that are -0.0 to
        // +0.0.
        if (x == 0.0) {
            this.x = 0.0;  // convert -0.0 to +0.0
        }
        else {
            this.x = x;
        }
        
        if (y == 0.0) {
            this.y = 0.0;  // convert -0.0 to +0.0
        }
        else {
            this.y = y;
        }
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Returns the x-coordinate.
     * 
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * Returns the y-coordinate.
     * 
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }
    
    /**
     * Returns the polar radius of this point in polar coordinates.<br>
     * In other words, it returns {@literal sqrt(x*x + y*y)}.
     * 
     * @return the polar radius of this point 
     */
    public double getPolarRadius() {
        return Math.sqrt((x * x) + (y * y));
    }
    
    /**
     * Returns the angle, in radians. of this point in polar coordinates (i.e.,
     * between -pi and pi).
     * 
     * @return the angle of this point in polar coordinates
     */
    public double getPolarAngle() {
        return Math.atan2(y, x);
    }
    
    /**
     * Returns the Euclidean distance between this point and the provided point.
     * 
     * @param other The other point
     * 
     * @return the Euclidean distance between this point and the provided point
     */
    public double getDistance(Point2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        
        return Math.sqrt((dx * dx) + (dy * dy));
    }
    
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
     *      iii) 'obj' has the same base filename as 'this'
     *           (i.e., 'this.baseFilename.equalsIgnoreCase(other.baseFilename)'
     *      iv)  'obj' has the same x-y coordinates ID as 'this'
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
        // If that object (obj) is not a Point2D object return false
        if (!(obj instanceof Point2D)) {
            return false;
        }
        
        Point2D other = (Point2D) obj; // Now can cast safely
        
        // The points are "equal" if they have the same x-y coordinates
        return (this.x == other.x) && (this.y == other.y);
    }
    
    // ------------------------------------------ //
    //    h   a   s   h   C   o   d   e   (  )    //
    // ------------------------------------------ //
    /**
     * {@inheritDoc}
     * 
     * To calculate the hash code it takes into consideration the x and y
     * coordinates
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Double) x).hashCode();
        result = prime * result + ((Double) y).hashCode();
        
        return result;
    }
    
    // ---------------------------------------------- //
    //    c   o   m   p   a   r   e   T   o   (  )    //
    // ---------------------------------------------- //
    /**
     * Compares this 2D point with another 2D point object for order.<br>
     * The comparison between the two points takes place by y-coordinate,
     * breaking ties by x-coordinate.<br>
     * This point (x0, y0) is less than the provided point (x1, y1), if and only
     * if, either {@code y0 < y1} or if {@code y0 == y1} and {@code x0 < x1}.
     * 
     * @param other The other point to be compared with this point
     * 
     * @return the value {@code 0} if this point is equal to the other point,
     *         a negative integer if this point is less than the other point and
     *         a positive integer if this point is greater than the other point
     */
    @Override
    public int compareTo(Point2D other) {
        if (this.y < other.y) {
            return -1;
        }
        if (this.y > other.y) {
            return 1;
        }
        
        // this.y == other.y
        if (this.x < other.x) {
            return -1;
        }
        if (this.x > other.x) {
            return 1;
        }
        
        // this.y == other.y && this.x == other.x
        return 0;
    }
    
    /**
     * Return a string representation of this point.<br>
     * The string representation uses the following format: {@code (x, y)}
     * 
     * @return a string representation of this point in the format (x, y)
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

