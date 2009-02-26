/*
 * InlineBox.java
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
 * Created on 5. únor 2006, 13:38
 */

package org.fit.cssbox.layout;

import java.awt.*;

import org.w3c.dom.*;
import cz.vutbr.web.css.*;

/**
 * An inline element box.
 *
 * @author  radek
 */
public class InlineBox extends ElementBox
{
    /** maximal line height of the contained boxes */
    private int maxLineHeight;
    
    //========================================================================
    
    /** Creates a new instance of InlineBox */
    public InlineBox(Element n, Graphics2D g, VisualContext ctx) 
    {
        super(n, g, ctx);
    }
    
    public void copyValues(InlineBox src)
    {
        super.copyValues(src);
    }
    
    /** Create a new box from the same DOM node in the same context */
    public InlineBox copyInlineBox()
    {
        InlineBox ret = new InlineBox(el, g, ctx);
        ret.copyValues(this);
        return ret;
    }
    
    //========================================================================
    
    @Override
    public String toString()
    {
        return "<" + el.getTagName() + " id=\"" + el.getAttribute("id") + 
               "\" class=\""  + el.getAttribute("class") + "\">";
    }
    
	@Override
    public boolean isInFlow()
	{
		return true;
	}
	
	@Override
    public boolean containsFlow()
	{
		return !isempty;
	}
    
    //========================================================================
    
    /** Compute the width and height of this element. Layout the sub-elements.
     * @param availw Maximal width available to the child elements
     * @param force Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return True if the box has been succesfully placed
     */
    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        //Skip if not displayed
        if (!displayed)
        {
            content.setSize(0, 0);
            bounds.setSize(0, 0);
            return true;
        }

        setAvailableWidth(availw);
        
        int wlimit = getAvailableContentWidth();
        int x = 0; //current x
        int maxh = 0;
        boolean ret = true;
        rest = null;

        int lastbreak = startChild; //last possible position of a line break
        
        for (int i = startChild; i < endChild; i++)
        {
            Box subbox = getSubBox(i);
            if (subbox.canSplitBefore())
            	lastbreak = i;
            //when forcing, force the first child only and the children before
            //the first possible break
            boolean f = force && (i == startChild || lastbreak == startChild);
            boolean fit = subbox.doLayout(wlimit - x, f, linestart && (i == startChild));
            if (fit) //something has been placed
            {
                if (subbox.isInFlow())
                {
                    subbox.setPosition(x,  0); //the y position will be updated later
                    x += subbox.getWidth();
                    if (subbox.getHeight() > maxh)
                        maxh = subbox.getHeight();
                }
                if (subbox.getRest() != null) //is there anything remaining?
                {
                    InlineBox rbox = copyInlineBox();
                    rbox.splitted = true;
                    rbox.setStartChild(i); //next starts with me...
                    rbox.nested.setElementAt(subbox.getRest(), i); //..but only with the rest
                    rbox.adoptChildren();
                    setEndChild(i+1); //...and this box stops with this element
                    rest = rbox;
                    break;
                }
            }
            else //nothing from the child has been placed
            {
                if (lastbreak == startChild) //no children have been placed, give up
                {
                    ret = false; 
                    break; 
                }
                else //some children have been placed, contintue the next time
                {
                    InlineBox rbox = copyInlineBox();
                    rbox.splitted = true;
                    rbox.setStartChild(lastbreak); //next time start from the last break
                    rbox.adoptChildren();
                    setEndChild(lastbreak); //this box stops here
                    rest = rbox;
                    break;
                }
            }
            
            if (subbox.canSplitAfter())
            	lastbreak = i+1;
        }
        
        //compute the vertical positions of the boxes
        computeMaxLineHeight();
        //TODO: vertical-align should be considered here
        //(at this point all the boxes have y=0 (see above))
        
        content.width = x;
        if (maxh == 0)
            content.height = 0; //no content
        else
            content.height = Math.max(lineHeight, maxh); //according to CSS spec. section 10.6.1
        setSize(totalWidth(), totalHeight());
        
        return ret;
    }
    
    @Override
    public void absolutePositions()
    {
        if (isDisplayed())
        {
            //my top left corner
            absbounds.x = getParent().getAbsoluteContentX() + bounds.x;
            absbounds.y = getParent().getAbsoluteContentY() + bounds.y;

            //update the width and height according to overflow of the parent
            absbounds.width = bounds.width;
            absbounds.height = bounds.height;
            
            //repeat for all valid subboxes
            if (isDisplayed())
            {
                for (int i = startChild; i < endChild; i++)
                    getSubBox(i).absolutePositions();
            }
        }
    }

    @Override
    public int getMinimalWidth()
    {
        //return the maximum of the nested minimal widths that are separated
        int ret = 0;
        for (int i = startChild; i < endChild; i++)
        {
            int w = getSubBox(i).getMinimalWidth();
            if (w > ret) ret = w;
        }
        //increase by margin, padding, border
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }
    
    @Override
    public int getMaximalWidth()
    {
        //return the sum of all the elements inside
        int ret = 0;
        for (int i = startChild; i < endChild; i++)
            ret += getSubBox(i).getMaximalWidth();
        //increase by margin, padding, border
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }
    
    @Override
    public boolean canSplitInside()
    {
        for (int i = startChild; i < endChild; i++)
            if (getSubBox(i).canSplitInside())
                return true;
        return false;
    }
    
    @Override
    public boolean canSplitBefore()
    {
        return (endChild > startChild) && getSubBox(startChild).canSplitBefore();
    }
    
    @Override
    public boolean canSplitAfter()
    {
        return (endChild > startChild) && getSubBox(endChild-1).canSplitAfter();
    }
    
    /** Draw the specified stage (DRAW_*) */
    @Override
    public void draw(Graphics2D g, int turn, int mode)
    {
        ctx.updateGraphics(g);
        if (displayed)
        {
            Shape oldclip = g.getClip();
            g.setClip(clipblock.getAbsoluteContentBounds());
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
            }
            
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                for (int i = startChild; i < endChild; i++)
                    getSubBox(i).draw(g, turn, mode);
            }
            g.setClip(oldclip);
        }
    }
    
    /**
     * @return the maximal line height of the contained sub-boxes
     */
    public int getMaxLineHeight()
    {
        return maxLineHeight;
    }
    
    //=======================================================================
    
    @Override
    protected void loadSizes()
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (cblock == null)
            System.err.println(this + " has no cblock");
        
        //containing box sizes
        int contw = cblock.getContentWidth();
        
        //top and bottom margins take no effect for inline boxes
        // http://www.w3.org/TR/CSS21/box.html#propdef-margin-top
        margin = new LengthSet();
        margin.right = dec.getLength(getLengthValue("margin-right"), style.getProperty("margin-right") == CSSProperty.Margin.AUTO, 0, 0, contw);
        margin.left = dec.getLength(getLengthValue("margin-left"), style.getProperty("margin-left") == CSSProperty.Margin.AUTO, 0, 0, contw);
        emargin = new LengthSet(margin);
        
        border = new LengthSet();
        if (borderVisible("top"))
        		border.top = getBorderWidth(dec, "border-top-width");
        else
        		border.top = 0;
        if (borderVisible("right"))
        		border.right = getBorderWidth(dec, "border-right-width");
	    else
	    		border.right = 0;
	    if (borderVisible("bottom"))
        		border.bottom = getBorderWidth(dec, "border-bottom-width");
	    else
	    		border.bottom = 0;
	    if (borderVisible("left"))
        		border.left = getBorderWidth(dec, "border-left-width");
	    else
	    		border.left = 0;
        
        padding = new LengthSet();
        padding.top = dec.getLength(getLengthValue("padding-top"), false, null, null, contw);
        padding.right = dec.getLength(getLengthValue("padding-right"), false, null, null, contw);
        padding.bottom = dec.getLength(getLengthValue("padding-bottom"), false, null, null, contw);
        padding.left = dec.getLength(getLengthValue("padding-left"), false, null, null, contw);
        
        content = new Dimension(0, 0);
    }
    
    @Override
    public void updateSizes()
    {
    	//no update needed - inline box size depends on the contents only
    }
   
    @Override
    public boolean hasFixedWidth()
    {
    	return false; //depends on the contents
    }
    
    @Override
    public boolean hasFixedHeight()
    {
    	return false; //depends on the contents
    }
    
    protected boolean borderVisible(String dir)
    {
            CSSProperty.BorderStyle st = style.getProperty("border-"+dir+"-style");
            return (st != null && st != CSSProperty.BorderStyle.NONE  && st != CSSProperty.BorderStyle.HIDDEN); 
    }

    private void computeMaxLineHeight()
    {
        int max = lineHeight; //shouldn't be smaller than our own height
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            int h;
            if (sub instanceof InlineBox)
                h = ((InlineBox) sub).getMaxLineHeight();
            else
                h = sub.getLineHeight();
            if (h > max) max = h;
        }
        maxLineHeight = max;
    }
}
