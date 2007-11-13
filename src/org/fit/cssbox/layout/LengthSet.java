/*
 * LengthSet.java
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
 * Created on 7. zברם 2005, 20:06
 */

package org.fit.cssbox.layout;

/**
 *
 * @author  radek
 */
public class LengthSet 
{
    public int top = 0;
    public int right = 0;
    public int bottom = 0;
    public int left = 0;
    
    public LengthSet()
    {
    }
    
    public LengthSet(int t, int r, int b, int l)
    {
        top = t;
        right = r;
        bottom = b;
        left = l;
    }
    
    public LengthSet(LengthSet src)
    {
        top = src.top;
        right = src.right;
        bottom = src.bottom;
        left = src.left;
    }
    
    public String toString()
    {
    	return "[" + top + ", " + left + ", " + bottom + ", " + right + "]";
    }
}
