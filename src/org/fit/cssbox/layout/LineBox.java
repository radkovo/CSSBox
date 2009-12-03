/**
 * LineBox.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * CSSBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * CSSBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 4.7.2006, 11:20:34 by burgetr
 */
package org.fit.cssbox.layout;

/**
 * This class describes a single line of the content in a block box.
 * @author burgetr
 */
public class LineBox
{
    /** The BlockBox containing the lines */
    private BlockBox parent;
    
    /** Index of the first box at this line (from all the subboxes of the block) */
    private int start;
    
    /** The Y position of this line */
    private int y;
    
    /** Index of the last box at this line (excl.) */
    private int end;
    
    /** Total text width in pixels (for horizontal alignment) */
    private int width;
    
    /** Left offset caused by floating boxes */
    private int left;
    
    /** Right offset caused by floating boxes */
    private int right;
    
    /** Maximal height of the content boxes */
    private int maxh;
    
    /** Maximal baseline offset for the line */
    private int baseline;

    public LineBox(BlockBox parent, int start, int y)
    {
        this.parent = parent;
        this.start = start;
        this.y = y;
        width = 0;
        left = 0;
        right = 0;
        maxh = 0;
    }

    @Override
    public String toString()
    {
        return "LineBox " + start + ".." + end + " y=" + y +  " width=" + width + " maxh=" + maxh;
    }

    public BlockBox getParent()
    {
        return parent;
    }
    
    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    public int getStart()
    {
        return start;
    }

    public int getY()
    {
        return y;
    }
    
    public int getAbsoluteY()
    {
        return parent.getAbsoluteContentY() + y;
    }
    
    public void setStart(int start)
    {
        this.start = start;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
    
    public int getLeftLimit()
    {
        return left;
    }

    public int getRightLimit()
    {
        return right;
    }
    
    /**
     * Computes the sum of the left and right limiting widths
     * @return The sum of the widths
     */
    public int getLimits()
    {
        return left + right;
    }
    
    /**
     * Sets the left and right width limits formed by the floating blocks (if any)
     * @param left the left limit
     * @param right the right limit
     */
    public void setLimits(int left, int right)
    {
        this.left = left;
        this.right = right;
    }

    public void setMaxHeight(int maxh)
    {
        this.maxh = maxh;
    }

    public int getMaxHeight()
    {
        return maxh;
    }
    
    /**
     * Updates the baseline value if the new value is greater than the current one
     * @param baseline the new baseline value
     */
    public void considerBaseline(int baseline)
    {
    	if (this.baseline < baseline)
    		this.baseline = baseline;
    }
    
    public int getBaselineOffset()
    {
    	return baseline;
    }
    
}
