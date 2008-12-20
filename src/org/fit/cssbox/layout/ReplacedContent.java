/**
 * ReplacedContent.java
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
 * Created on 27.9.2006, 21:15:03 by radek
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

/**
 * A class that represents the contents of a replace box
 * 
 * @author radek
 */
public abstract class ReplacedContent
{
	/** The owner box */
	protected ElementBox owner;
    
    /** Defined width (CSS syntax, e.g. "120px" or "auto") */
    protected String def_width;

    /** Defined height (CSS syntax, e.g. "120px" or "auto") */
    protected String def_height;
	
	//============================================================
	
	public ReplacedContent(ElementBox owner)
	{
		this.owner = owner;
	}
	
	/**
	 * @return the owner
	 */
	public ElementBox getOwner()
	{
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(ElementBox owner)
	{
		this.owner = owner;
	}

    /**
     * Loads the size definitions from appropriate node attributes or CSS properties
     */
    public void loadSizeDefs()
    {
        //default behaviour - set 'auto' only
        def_width = "auto";
        def_height = "auto";
    }
    
	/**
	 * Draw the contents of the element.
	 * @param g graphics context
	 * @param width the required width of the result 
	 * @param height the required height of the result 
	 */
    abstract public void draw(Graphics2D g, int width, int height);
    
    /**
     * @return the intrinsic width of the contents
     */
    abstract public int getIntrinsicWidth();

    /**
     * @return the intrinsic height of the contents
     */
    abstract public int getIntrinsicHeight();

    /**
     * @return the intrinsic width/height ratio. If the object has no ratio, 0 is returned
     */
    abstract public float getIntrinsicRatio();
    
    
}
