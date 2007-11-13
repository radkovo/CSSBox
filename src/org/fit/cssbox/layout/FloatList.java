/*
 * FloatList.java
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
 * Created on 27. zברם 2005, 23:00
 */

package org.fit.cssbox.layout;

import java.util.*;

/**
 * A list of floating elements
 *
 * @author  radek
 */
public class FloatList 
{
	private BlockBox owner;
    private Vector<BlockBox> floats;
    
    public FloatList(BlockBox ownerBox) 
    {
    	owner = ownerBox;
        floats = new Vector<BlockBox>();
    }
    
    public BlockBox getOwner()
	{
		return owner;
	}
    
	public void add(BlockBox box)
    {
		box.setOwnerFloatList(this);
        floats.add(box);
    }
    
    public int size()
    {
        return floats.size();
    }
    
    public BlockBox getBox(int index)
    {
        return floats.elementAt(index);
    }

    /** Get the width of the floating boxes in some point */
    public int getWidth(int y)
    {
        int maxx = 0;
        for (int i = 0; i < size(); i++)
        {
            Box box = getBox(i);
            if (box.getBounds().y <= y &&
                box.getBounds().y + box.getBounds().height > y)
            {
                int wx = box.getBounds().x + box.getBounds().width;
                if (wx > maxx) maxx = wx;
            }
        }
        return maxx;
    }
    
    /** Get the first Y where the floats are narrower than in the specified Y */
    public int getNextY(int y)
    {
        int maxx = 0;
        int nexty = y+1;
        for (int i = 0; i < size(); i++)
        {
            Box box = getBox(i);
            if (box.getBounds().y <= y &&
                box.getBounds().y + box.getBounds().height > y)
            {
                int wx = box.getBounds().x + box.getBounds().width;
                if (wx > maxx) 
                {
                    maxx = wx;
                    nexty = box.getBounds().y + box.getBounds().height;
                }
            }
        }
        return nexty;
    }

    /**
     * Goes through all the boxes and computes the Y coordinate of the bottom edge
     * of the lowest box.
     * @return the maximal Y coordinate
     */
    public int getMaxY()
    {
        int maxy = 0;
        for (int i = 0; i < size(); i++)
        {
            Box box = getBox(i);
            int ny = box.getBounds().y + box.getBounds().height;
            if (ny > maxy) maxy = ny;
        }
        return maxy;
    }
    
    /**
     * Goes through all the boxes and computes the Y coordinate of the bottom edge
     * of the lowest box. Only the boxes with the 'owner' containing block are taken
     * into account.
     * @param owner the owning block
     * @return the maximal Y coordinate
     */
    public int getMaxYForOwner(BlockBox owner)
    {
        int maxy = 0;
        for (int i = 0; i < size(); i++)
        {
            Box box = getBox(i);
            if (box.getContainingBlock() == owner)
            {
                int ny = box.bounds.y + box.bounds.height;
                if (ny > maxy) maxy = ny;
            }
        }
        return maxy;
    }
    
}
