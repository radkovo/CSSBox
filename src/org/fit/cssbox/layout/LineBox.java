/**
 * LineBox.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
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
    

    public LineBox(BlockBox parent, int start)
    {
        this.parent = parent;
        this.start = start;
        width = 0;
        left = 0;
        right = 0;
        maxh = 0;
    }

    @Override
    public String toString()
    {
        return "LineBox " + start + ".." + end + " width=" + width + " maxh=" + maxh;
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
    
}
