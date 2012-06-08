/*
 * LengthSet.java
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
 * Created on 7. zari 2005, 20:06
 */

package org.fit.cssbox.layout;

/**
 * This class represents a set of four lengths - top, right bottom and left. It
 * is used for representing the margin, padding and border width.
 * 
 * @author  radek
 */
public class LengthSet 
{
    /** top length */
    public int top = 0;
    
    /** right length */
    public int right = 0;
    
    /** bottom length */
    public int bottom = 0;
    
    /** left length */
    public int left = 0;
    
    /**
     * Creates a new length set with all the lengths initialized to zero.
     */
    public LengthSet()
    {
    }
    
    /**
     * Creates a new length set with the specified lengths.
     * @param t top length
     * @param r right length
     * @param b bottom length
     * @param l left length
     */
    public LengthSet(int t, int r, int b, int l)
    {
        top = t;
        right = r;
        bottom = b;
        left = l;
    }
    
    /**
     * Creates a new length set from an existing one.
     * @param src the source length set
     */
    public LengthSet(LengthSet src)
    {
        top = src.top;
        right = src.right;
        bottom = src.bottom;
        left = src.left;
    }
    
    /**
     * Copies all the values from an existing set.
     * @param src the source length set
     */
    public void copy(LengthSet src)
    {
        top = src.top;
        right = src.right;
        bottom = src.bottom;
        left = src.left;
    }
    
    /**
     * Returns a string representation of the length set
     * @return A string in the [top, right, bottom, left] format
     */
    public String toString()
    {
    	return "[" + top + ", " + right + ", " + bottom + ", " + left + "]";
    }
}
