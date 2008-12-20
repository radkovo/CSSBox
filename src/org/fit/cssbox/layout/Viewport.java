/*
 * Viewport.java
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
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.util.Vector;

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

    
    public Viewport(Graphics2D g, VisualContext ctx, int width, int height)
	{
		super(null, g, ctx);
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
	protected void loadSizes(boolean update)
	{
		margin = new LengthSet();
		emargin = new LengthSet();
		border = new LengthSet();
		padding = new LengthSet(1, 1, 1, 1);
		content = new Dimension(0, 0);
		min_size = new Dimension(width, height);
		max_size = new Dimension(-1, -1);
		position = BlockBox.POS_ABSOLUTE;
		computeWidths(CSSFactory.getTermFactory().createLength((float) width, Unit.px), false, false, 0, update); 
		computeHeights(CSSFactory.getTermFactory().createLength((float) height, Unit.px), false, false, 0, update); 
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
			getSubBox(i).absolutePositions(null);
		//update the size
		if (width < maxx) width = maxx;
		if (height < maxy) height = maxy;
		loadSizes();
	}
	
	@Override
    public void absolutePositions(Rectangle clip)
    {
	    absbounds = new Rectangle(bounds);
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).absolutePositions(clip);
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
	
    //========================================================================

	/**
	 * Compute the effective margins from the specified.
	 */
	public void collapseMargins()
	{
		for (int i = 0; i < getSubBoxNumber(); i++)
			recursiveCollapseMargins((ElementBox) getSubBox(i));
	}
	
    protected void recursiveCollapseMargins(ElementBox box)
    {
        //Do not count with empty in-flow elements that do not affect display
        if (box.isInFlow()
            && box.isEmpty()
            && !box.affectsDisplay()
            && !(box.isBlock() && (((BlockBox) box).getClearing() != CLEAR_NONE)))
            box.displayed = false;
    	
        if (box.isDisplayed())
        {
            //Solve collapsing with preceding boxes or parents
            if (box.affectsDisplay())
            {
                if (box.isBlock())
                {
                    if (lastparent != null) //if there is some parent
                    {
                        //collapse if it is not separated by border or padding
                        if (lastparent.isInFlow() && !separatedFromTop(lastparent))
                        {
                        	if (box.isInFlow())
                        	{
                        		collapseNestedTopMargins(lastparent, box);
                        		lastparent = null; //following siblings don't collapse
                        	}
                        }
                    }
                    //no parent to collapse but some preceding box
                    else if (lastbox != null)
                    {
                    	if (box.isInFlow())
                    		collapseSubsequentMargins(lastbox, box);
                    	else //apply the correction to floating boxes
                    		uncollapseSubsequentMargins(lastbox, box);
                    }
                }
                else
                {
                    lastbox = null;
                    lastparent = null;
                }
            }

            //----------------- collapse the elements inside -----------------------------
            ElementBox mylastbox = lastbox;
            ElementBox myparent = lastparent;
            
            if (!box.isInFlow() ||
            	separatedFromTop(box))
                lastbox = null; //do not collapse anything inside with anything before 
            //get a new parent box
            if (box.isBlock() && box.affectsDisplay())
            	lastparent = box;
            //process the children
            for (int i = box.getStartChild(); i < box.getEndChild(); i++)
            {
                Box subbox = box.getSubBox(i);
                if (subbox instanceof ElementBox)
                {
                    recursiveCollapseMargins((ElementBox) subbox);
                }
                else
                {
                    if (subbox.isDisplayed()) //displayed content separates
                    {
                        lastbox = null;
                        lastparent = null;
                    }
                }
            }

            //Collapse bottom margins with nested content if not separated
            if (lastbox != null && lastbox != mylastbox &&
                !separatedFromBottom(box))
                collapseNestedBottomMargins(box, lastbox);
            
    		lastbox = mylastbox;
    		lastparent = myparent;

            //set this box as the last box for following boxes
            if (box.affectsDisplay() && box.isBlock() && box.isInFlow())
                lastbox = box;
        }
    }
    
    /** 
     * Compute efective margins by applying the margin collapsing agorithms
     * on two subsequent adjoining boxes.
     */
    protected void collapseSubsequentMargins(ElementBox b1, ElementBox b2)
    {
        int max = Math.max(b1.emargin.bottom, b2.emargin.top);
        //System.out.println("Seq collapsing "+ b1 + " with " + b2 + " by " + max);
        b1.emargin.bottom = 0;
        b2.emargin.top = max;
    }
    
    /**
     * Applies a top margin correction to a box not in flow when a previous in-flow box lost
     * its bottom margin.  
     */
    protected void uncollapseSubsequentMargins(ElementBox b1, ElementBox b2)
    {
        //System.out.println("Seq uncollapsing "+ b1 + " with " + b2 + " by " + b1.margin.bottom);
        b1.emargin.bottom = 0;
        b2.emargin.top += b1.margin.bottom;
    }
    
    /** 
     * Compute efective margins by applying the margin collapsing agorithms
     * on two the top margins of two nested blocks
     */
    protected void collapseNestedTopMargins(ElementBox parent, ElementBox child)
    {
        int max = Math.max(parent.emargin.top, child.emargin.top);
        //System.out.println("Top collapsing "+ parent + " with " + child);
        parent.emargin.top = max;
        child.emargin.top = 0;
    }
    
    /** 
     * Compute efective margins by applying the margin collapsing agorithms
     * on two the bottom margins of two nested blocks
     */
    protected void collapseNestedBottomMargins(ElementBox parent, ElementBox child)
    {
        int max = Math.max(parent.emargin.bottom, child.emargin.bottom);
        if (child.isInFlow())
        {
	        //System.out.println("Bottom collapsing "+ parent + " with " + child);
	        parent.emargin.bottom = max;
	        child.emargin.bottom = 0;
        }
    }
    
    //===================================================================================
    
    private boolean separatedFromTop(ElementBox box)
    {
        return (box.border.top > 0 || box.padding.top > 0);
    }
    
    private boolean separatedFromBottom(ElementBox box)
    {
        return (box.border.bottom > 0 || box.padding.bottom > 0);
    }
    
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

