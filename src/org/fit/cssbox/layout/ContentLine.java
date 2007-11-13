/**
 * ContentLine.java
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
 * This class describes a single line of the content in a block box. It
 * contains the start and end index of the subboxes that form the line,
 * the total text width and the additional width limit that can be caused
 * by floating boxes. 
 * @author burgetr
 */
public class ContentLine
{
    private int start;
    private int end;
    private int width;
    private int limits;

    public ContentLine(int start)
    {
        this.start = start;
    }

    /**
     * @return Returns the end.
     */
    public int getEnd()
    {
        return end;
    }

    /**
     * @param end The end to set.
     */
    public void setEnd(int end)
    {
        this.end = end;
    }

    /**
     * @return Returns the start.
     */
    public int getStart()
    {
        return start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(int start)
    {
        this.start = start;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    /**
     * @return Returns the limits.
     */
    public int getLimits()
    {
        return limits;
    }

    /**
     * @param limits The limits to set.
     */
    public void setLimits(int limits)
    {
        this.limits = limits;
    }

}
