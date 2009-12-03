/*
 * Viewport.java
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
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.util.Vector;
import org.w3c.dom.Element;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.TermNumeric.Unit;

/**
 * This class represents a browser viewport which is implemented as a special case of a block
 * box. It differs mainly in the way the sizes are computed. Moreover, it provides the methods
 * for margin collapsing which is done before the layout itself.
 * 
 * @author radek
 */
public class Viewport extends BlockBox
{
	private int width;
	private int height;
    protected ElementBox lastbox = null;
    protected ElementBox lastparent = null;
    private int maxx; //maximal X position of all the content
    private int maxy; //maximal Y position of all the content

    
    public Viewport(Element root, Graphics2D g, VisualContext ctx, int width, int height)
	{
		super(root, g, ctx);
		style = CSSFactory.createNodeData(); //Viewport starts with an empty style
        nested = new Vector<Box>();
        startChild = 0;
        endChild = 0;
		this.width = width;
		this.height = height;
        isblock = true;
        contblock = true;
        setFloats(new FloatList(this), new FloatList(this), 0, 0, 0);
		loadSizes();
	}
    
    public String toString()
    {
        return "Viewport " + width + "x" + height;
    }

    public int getMinimalWidthLimit()
    {
    	return width;
    }

	@Override
	public boolean hasFixedHeight()
	{
		return false;
	}

	@Override
	public boolean hasFixedWidth()
	{
		return false;
	}

    @Override
    public boolean isVisible()
    {
        return true;
    }

	@Override
	public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        content = new Dimension(width, height);
        bounds = new Rectangle(0, 0, totalWidth(), totalHeight());
    }
    
	@Override
	public Viewport getViewport()
	{
		return this;
	}

    @Override
    protected void loadPosition()
    {
        position = BlockBox.POS_ABSOLUTE;
        topset = true;
        leftset = true;
        bottomset = false;
        rightset = false;
        coords = new LengthSet(0, 0, 0, 0);
    }
    
	@Override
	protected void loadSizes(boolean update)
	{
		if (!update)
		{
			margin = new LengthSet();
			emargin = new LengthSet();
			border = new LengthSet();
			padding = new LengthSet(1, 1, 1, 1);
			content = new Dimension(0, 0);
			min_size = new Dimension(width, height);
			max_size = new Dimension(-1, -1);
			loadPosition();
		}
		computeWidths(CSSFactory.getTermFactory().createLength((float) width, Unit.px), false, false, this, update); 
		computeHeights(CSSFactory.getTermFactory().createLength((float) height, Unit.px), false, false, this, update); 
		bounds = new Rectangle(0, 0, totalWidth(), totalHeight());
	}

	/**
	 * Calculates the absolute positions and updates the viewport size
	 * in order to enclose all the boxes.
	 */
	public void updateBounds()
	{
		//first round - compute the viewport size
		maxx = 0;
		maxy = 0;
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).absolutePositions();
		//update the size
		if (width < maxx) width = maxx;
		if (height < maxy) height = maxy;
		loadSizes();
	}
	
	@Override
    public void absolutePositions()
    {
	    absbounds = new Rectangle(bounds);
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).absolutePositions();
    }
	
	@Override
	public void draw(Graphics2D g, int turn, int mode) 
	{
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).draw(g, turn, mode);
	}

	public void initBoxes()
	{
		for (int i = 0; i < getSubBoxNumber(); i++)
		{
			ElementBox box = (ElementBox) getSubBox(i);
			recursiveInitBoxes(box);
			box.computeEfficientMargins();
			if (box instanceof BlockBox)
			    ((BlockBox) box).setFloats(new FloatList((BlockBox) box), new FloatList((BlockBox) box), 0, 0, 0);
		}
	}

	/**
	 * Updates the maximal viewport size according to the element bounds
	 */
	public void updateBoundsFor(Rectangle bounds)
	{
		int x = bounds.x + bounds.width - 1;
		if (maxx < x) maxx = x;
		int y = bounds.y + bounds.height - 1;
		if (maxy < y) maxy = y;
	}
	
    //===================================================================================
    
    private void recursiveInitBoxes(ElementBox box)
    {
        box.initBox();
        box.loadSizes();
        for (int i = 0; i < box.getSubBoxNumber(); i++)
        {
            Box child = box.getSubBox(i);
            if (child instanceof ElementBox)
                recursiveInitBoxes((ElementBox) child);
        }
    }

}

