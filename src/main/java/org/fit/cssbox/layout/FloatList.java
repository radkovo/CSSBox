/*
 * FloatList.java
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
 * Created on 27. zari 2005, 23:00
 */

package org.fit.cssbox.layout;

import java.util.*;

/**
 * A list of floating boxes
 *
 * @author  radek
 */
public class FloatList 
{
	private BlockBox owner;
    private Vector<BlockBox> floats;
    //private int maxY;
    //private int lastY; //Y coordinate of the last box. New boxes shouldn't be placed above this limit
    private BlockBox bottomBox = null; //Bottom-most box.
    private BlockBox lastBox = null; //last box inserted. New boxes shouldn't be placed above this box.
    
    /**
     * Creates a list of floating boxes for some owner block.
     * @param ownerBox the owner block box
     */
    public FloatList(BlockBox ownerBox) 
    {
    	owner = ownerBox;
        floats = new Vector<BlockBox>();
    }
    
    /**
     * @return the owning block box of the float list
     */
    public BlockBox getOwner()
	{
		return owner;
	}
    
	/**
	 * Adds a new floating box to the list
	 * @param box Floating block box to be added 
	 */
    public void add(BlockBox box)
    {
		box.setOwnerFloatList(this);
        floats.add(box);
        if (box.getBounds().y + box.getBounds().height > getMaxY())
            bottomBox = box;
        if (box.getBounds().y > getLastY())
            lastBox = box;
    }
    
    /**
     * @return the number of boxes in the list
     */
    public int size()
    {
        return floats.size();
    }
    
    /**
     * Finds an n-th box in the list
     * @param index the position of the box in the list
     * @return the box on the index position
     */
    public BlockBox getBox(int index)
    {
        return floats.elementAt(index);
    }
    
    /**
     * Returns the the Y coordinate of the lowest bottom edge
     * of the boxes.
     * @return the maximal Y coordinate
     */
    public int getMaxY()
    {
        if (bottomBox == null)
            return 0;
        else
            return bottomBox.getBounds().y + bottomBox.getBounds().height;
    }
    
    /**
     * Returns the Y coordinate of the last box. New boxes shouldn't be placed above this limit.
     * @return Y coordinate
     */
    public int getLastY()
    {
        if (lastBox == null)
            return 0;
        else
            return lastBox.getBounds().y;
    }

    /** 
     * Gets the total width of the floating boxes in some point.
     * @param y the Y coordinate of the point
     * @return the total width of the floating boxes on that Y coordinate  
     */
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
    
    /** 
     * Gets the first Y coordinate where the floats are narrower than in the specified Y
     * @param y the starting y coordinate
     * @return the next Y coordinate where the total width of the floating boxes is narrower
     * than at the starting coordinate. When there is no such Y coordinate, -1 is returned.
     */
    public int getNextY(int y)
    {
        int maxx = 0;
        int nexty = -1;
        for (int i = 0; i < size(); i++) //find the bottom of the rightmost box at this Y coordinate
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
     * of the lowest box. Only the boxes with the 'owner' containing block are taken
     * into account.
     * @param owner the owning block
     * @return the maximal Y coordinate
     */
    public int getMaxYForOwner(BlockBox owner, boolean requireVisible)
    {
        int maxy = 0;
        for (int i = 0; i < size(); i++)
        {
            Box box = getBox(i);
            if ((!requireVisible || box.isDeclaredVisible()) && box.getContainingBlockBox() == owner)
            {
                int ny = box.bounds.y + box.bounds.height; //TODO: -1 here?
                if (ny > maxy) maxy = ny;
            }
        }
        return maxy;
    }
    
}
