/*
 * InlineBox.java
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
    /** vertical box alignment specified by the style */
    private CSSProperty.VerticalAlign valign;
    
    /** maximal line height of the contained boxes */
    private int maxLineHeight;
    
    /** parent LineBox assigned during layout */
    private LineBox linebox;
    
    //========================================================================
    
    /** Creates a new instance of InlineBox */
    public InlineBox(Element n, Graphics2D g, VisualContext ctx) 
    {
        super(n, g, ctx);
    }
    
    public void copyValues(InlineBox src)
    {
        super.copyValues(src);
        valign = src.valign;
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
    public void setStyle(NodeData s)
    {
        super.setStyle(s);
        loadInlineStyle();
    }
    
    public CSSProperty.VerticalAlign getVerticalAlign()
    {
        return valign;
    }
    
    /**
     * Assigns the line box assigned to this inline box and all the inline sub-boxes.
     * @param linebox The assigned linebox.
     */
    public void setLineBox(LineBox linebox)
    {
        this.linebox = linebox;
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            if (sub instanceof InlineBox)
                ((InlineBox) sub).setLineBox(linebox);
        }
    }
    
    /**
     * Returns the line box used for positioning this element.
     */
    public LineBox getLineBox()
    {
        return linebox;
    }
    
    //========================================================================
    
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
        alignBoxes();
        
        content.width = x;
        content.height = lineHeight;
        
        setSize(totalWidth(), totalHeight());
        
        return ret;
    }
    
    @Override
    public void absolutePositions()
    {
        if (isDisplayed())
        {
            //x coordinate is taken from the content edge
            absbounds.x = getParent().getAbsoluteContentX() + bounds.x;
            //y coordinate -- depends on the vertical alignment
            if (valign == CSSProperty.VerticalAlign.TOP)
            {
                absbounds.y = linebox.getAbsoluteY() - getContentOffsetY();
            }
            else if (valign == CSSProperty.VerticalAlign.BOTTOM)
            {
                absbounds.y = linebox.getAbsoluteY() + linebox.getMaxHeight() - getContentHeight() - getContentOffsetY();
            }
            else //other positions -- set during the layout. Relative to the parent content edge.
            {
                absbounds.y = getParent().getAbsoluteContentY() + bounds.y;
            }

            //update the width and height according to overflow of the parent
            absbounds.width = bounds.width;
            absbounds.height = bounds.height;
            
            //repeat for all valid subboxes
            for (int i = startChild; i < endChild; i++)
                getSubBox(i).absolutePositions();
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
    
    /**
     * Returns the height of the box or the highest subbox.
     */
    public int getMaximalHeight()
    {
        int ret = getHeight();
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            int h = 0;
            if (sub instanceof InlineBox)
                h = ((InlineBox) sub).getMaximalHeight();
            else
                h = sub.getHeight();
            
            if (h > ret) ret = h;
        }
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
    
    @Override
    public int totalHeight()
    {
        //for inline boxes, the top and bottom margins don't apply
        return border.top + padding.top + content.height + padding.bottom + border.bottom;
    }
    
    //=======================================================================
    
    /**
     * Loads the basic style properties related to the inline elements.
     */
    protected void loadInlineStyle()
    {
        valign = style.getProperty("vertical-align");
        if (valign == null) valign = CSSProperty.VerticalAlign.BASELINE;
    }
    
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
    
    @Override
    public void computeEfficientMargins()
    {
        emargin.top = margin.top; //no collapsing is applied to inline boxes
        emargin.bottom = margin.bottom;
    }

    @Override
	public boolean marginsAdjoin()
	{
    	if (padding.top > 0 || padding.bottom > 0 ||
    		border.top > 0 || border.bottom > 0)
    	{
    		//margins are separated by padding or border
    		return false;
    	}
    	else
    	{
    		//margins can be separated by contents
	        for (int i = startChild; i < endChild; i++)
	        {
	        	Box box = getSubBox(i);
	        	if (box instanceof ElementBox) //all child boxes must have adjoining margins
	        	{
	        		if (!((ElementBox) box).marginsAdjoin())
	        			return false;
	        	}
	        	else
	        	{
	        		if (!box.isWhitespace()) //text boxes must be whitespace
	        			return false;
	        	}
	        }
	        return true;
    	}
	}

	/**
     * Vertically aligns the boxes placed relatively to the line box (vertical-align:top or bottom).
     * @param top the top y coordinate relatively to this box
     * @param bottom the bottom y coordinate relatively to this box
     */
    public void alignLineBoxes(int top, int bottom)
    {
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            if (sub instanceof InlineBox)
            {
                CSSProperty.VerticalAlign va = ((InlineBox) sub).getVerticalAlign();
                if (va == CSSProperty.VerticalAlign.TOP)
                    sub.moveDown(top);
                else if (va == CSSProperty.VerticalAlign.BOTTOM)
                    sub.moveDown(bottom);
            }
        }
    }
    
    //=====================================================================================================
    
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
    
    /**
     * Vertically aligns the contained boxes according to their vertical-align properties.
     */
    private void alignBoxes()
    {
        int base = getBaselineOffset();
        for (int i = startChild; i < endChild; i++)
        {
            int dif = 0;
            Box sub = getSubBox(i);
            int baseshift = base - sub.getBaselineOffset(); 
            if (sub instanceof InlineBox)
            {
                CSSProperty.VerticalAlign va = ((InlineBox) sub).getVerticalAlign();
                if (va == CSSProperty.VerticalAlign.BASELINE)
                    dif = baseshift;
                else if (va == CSSProperty.VerticalAlign.MIDDLE)
                    dif = baseshift - (int) (ctx.getEx() / 2);
                else if (va == CSSProperty.VerticalAlign.SUB)
                    dif = baseshift + (int) (0.3 * getLineHeight());  
                else if (va == CSSProperty.VerticalAlign.SUPER)
                    dif = baseshift - (int) (0.3 * getLineHeight());  
                else if (va == CSSProperty.VerticalAlign.TEXT_TOP)
                    dif = 0;
                else if (va == CSSProperty.VerticalAlign.TEXT_BOTTOM)
                    dif = getLineHeight() - sub.getLineHeight();
                else if (va == CSSProperty.VerticalAlign.length || va == CSSProperty.VerticalAlign.percentage)
                {
                    CSSDecoder dec = new CSSDecoder(sub.getVisualContext());
                    int len = dec.getLength(sub.getLengthValue("vertical-align"), false, 0, 0, sub.getLineHeight());
                    dif = baseshift - len;
                }
                //Now, dif is the difference of the content boxes. Recompute to the whole boxes.
                dif = dif - ((InlineBox) sub).getContentOffsetY(); 
            }
            else if (sub instanceof TextBox) //use baseline
            {
                dif = baseshift;
            }
            
            
            if (dif != 0)
            {
                sub.moveDown(dif);
            }
        }
    }
    
}
