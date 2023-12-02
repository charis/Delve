/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
package edu.stanford.gui;

// Import Java SE classes
import java.awt.Component;
import java.awt.Container;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;

/**
 * A 1.4 file that provides utility methods for
 * creating form- or grid-style layouts with SpringLayout.
 * These utilities are used by several programs, such as
 * SpringBox and SpringCompactGrid.
 */
public class SpringUtilities
{
    // ------------------------------------------------------ //
    //  P   U   B   L   I   C      M   E   T   H   O   D   S  //
    // ------------------------------------------------------ //
    /**
     * A debugging utility that prints to stdout the component's
     * minimum, preferred, and maximum sizes.
     * 
     * @param component The component to print its minimum, preferred, and
     *                  maximum sizes
     */
    public static void printSizes(Component component) {
        System.out.println("minimumSize   = " + component.getMinimumSize());
        System.out.println("preferredSize = " + component.getPreferredSize());
        System.out.println("maximumSize   = " + component.getMaximumSize());
    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component is as big as the maximum
     * preferred width and height of the components.
     * The parent is made just big enough to fit them all.
     *
     * @param parent The parent container
     * @param numOfRows The number of rows
     * @param numOfCols The number of columns
     * @param initialX The x location to start the grid at
     * @param initialY The y location to start the grid at
     * @param xPadding The x padding between cells
     * @param yPadding The y padding between cells
     */
    public static void makeGrid(Container parent,
                                int       numOfRows,
                                int       numOfCols,
                                int       initialX,
                                int       initialY,
                                int       xPadding,
                                int       yPadding) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        }
        catch (ClassCastException exc) {
            System.err.println(
                       "The first argument to makeGrid must use SpringLayout.");
            return;
        }
        
        Spring xPadSpring = Spring.constant(xPadding);
        Spring yPadSpring = Spring.constant(yPadding);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = numOfRows * numOfCols;
        
        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.
        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0))
                                      .getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0))
                                       .getHeight();
        for (int i = 1; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                                                        parent.getComponent(i));
            
            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }
        
        //Apply the new width/height Spring. This forces all the
        //components to have the same size.
        for (int i = 0; i < max; i++) {
            Constraints cons = layout.getConstraints(parent.getComponent(i));
            
            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }
        
        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.
        Constraints lastCons    = null;
        Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            Constraints cons = layout.getConstraints(parent.getComponent(i));
            if (i % numOfCols == 0) { //start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            }
            else { //x position depends on previous component
                cons.setX(Spring.sum(
                               lastCons.getConstraint(SpringLayout.EAST),
                               xPadSpring));
            }
            
            if (i / numOfCols == 0) { //first row
                cons.setY(initialYSpring);
            }
            else { //y position depends on previous row
                cons.setY(
                     Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH),
                                yPadSpring));
            }
            lastCons = cons;
        }
        
        //Set the parent's size.
        SpringLayout.Constraints parentCons = layout.getConstraints(parent);
        parentCons.setConstraint(SpringLayout.SOUTH,
                                 Spring.sum(
                                   Spring.constant(yPadding),
                                   lastCons.getConstraint(SpringLayout.SOUTH)));
        parentCons.setConstraint(SpringLayout.EAST,
                                 Spring.sum(
                                   Spring.constant(xPadding),
                                   lastCons.getConstraint(SpringLayout.EAST)));
    }
    
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param parent The parent container
     * @param numOfRows The number of rows
     * @param numOfColumns The number of columns
     * @param initialX The x location to start the grid at
     * @param initialY The y location to start the grid at
     * @param xPadding The x padding between cells
     * @param yPadding The y padding between cells
     */
    public static void makeCompactGrid(Container parent,
                                       int       numOfRows,
                                       int       numOfColumns,
                                       int       initialX,
                                       int       initialY,
                                       int       xPadding,
                                       int       yPadding) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        }
        catch (ClassCastException cce) {
            System.err.println("The first argument to makeCompactGrid must use "
                              + "SpringLayout.");
            return;
        }
        
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int col = 0; col < numOfColumns; col++) {
            Spring width = Spring.constant(0);
            
            for (int row = 0; row < numOfRows; row++) {
                Constraints constraints = getConstraintsForCell(row,
                                                                col,
                                                                parent,
                                                                numOfColumns);
                width = Spring.max(width, constraints.getWidth());
            }
            
            for (int row = 0; row < numOfRows; row++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(row, col, parent, numOfColumns);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPadding)));
        }
        
        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int row = 0; row < numOfRows; row++) {
            Spring height = Spring.constant(0);
            
            for (int col = 0; col < numOfColumns; col++) {
                Constraints constraints = getConstraintsForCell(row,
                                                                col,
                                                                parent,
                                                                numOfColumns);
                height = Spring.max(height, constraints.getHeight());
            }
            
            for (int col = 0; col < numOfColumns; col++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(row, col, parent, numOfColumns);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPadding)));
        }
        
        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Calculates and returns the constrains for a grid cell.
     * 
     * @param row The cell row
     * @param col The cell column
     * @param parent The parent container
     * @param numOfColumns The number of columns in the grid
     * 
     * @return the cell constrains
     */
    private static Constraints getConstraintsForCell(int       row,
                                                     int       col,
                                                     Container parent,
                                                     int       numOfColumns) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component component = parent.getComponent(row * numOfColumns + col);
        return layout.getConstraints(component);
    }
}