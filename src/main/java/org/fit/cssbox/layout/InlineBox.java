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
 * Created on 5. �nor 2006, 13:38
 */

package org.fit.cssbox.layout;

import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import cz.vutbr.web.css.*;

/**
 * An inline element box.
 *
 * @author  radek
 */
public class InlineBox extends ElementBox implements InlineElement
{
    private static Logger log = LoggerFactory.getLogger(InlineBox.class);
    
    /** vertical box alignment specified by the style */
    private CSSProperty.VerticalAlign valign;
    
    /** parent LineBox assigned during layout */
    private LineBox linebox;
    
    /** line box describing the children layout */
    protected LineBox curline;
    
    /** half-lead after layout */
    private float halflead;
    
    /** minimal relative Y coordinate of the descendants (computed during the layout) */
    protected float minDescendantY;
    
    /** maximal relative Y coordinate of the descendants (computed during the layout) */
    protected float maxDescendantY;
    
    /** Layout finished with a line break? */
    protected boolean lineBreakStop;
    
    /** Collapsed to an empty box by ws collapsing during the layout? */
    protected boolean collapsedCompletely;
    
    //========================================================================
    
    /** Creates a new instance of InlineBox */
    public InlineBox(Element n, VisualContext ctx) 
    {
        super(n, ctx);
        halflead = 0;
        lineBreakStop = false;
        collapsedCompletely = false;

        setLayoutManager(new InlineBoxLayoutManager(this));
    }
    
    public void copyValues(InlineBox src)
    {
        super.copyValues(src);
        valign = src.valign;
    }
    
    @Override
    public InlineBox copyBox()
    {
        InlineBox ret = new InlineBox(el, ctx);
        ret.copyValues(this);
        return ret;
    }
    
    //========================================================================
    
    @Override
    public String toString()
    {
        return "<" + el.getTagName() + " id=\"" + HTMLNorm.getAttribute(el, "id") + 
               "\" class=\""  + HTMLNorm.getAttribute(el, "class") + "\">";
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
            if (sub instanceof InlineElement)
                ((InlineElement) sub).setLineBox(linebox);
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
    public float getBaselineOffset()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getBaselineOffset();
    }
    
    @Override
    public float getBelowBaseline()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getBelowBaseline();
    }
    
    @Override
    public float getTotalLineHeight()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getTotalLineHeight();
    }
    
    @Override
    public float getMaxLineHeight()
    {
        if (curline == null)
            return lineHeight;
        else
            return Math.max(lineHeight, curline.getMaxBoxHeight());
    }
    
    @Override
    public float getLineboxOffset()
    {
        if (curline == null)
            return 0;
        else
            return curline.getBaselineOffset() - ctx.getBaselineOffset() - halflead;
    }
    /**
     * Returns the half-lead value used for positioning the nested boxes within this inline box
     * @return half-lead value in pixels
     */
    @Override
    public float getHalfLead()
    {
        return halflead;
    }
    
    @Override
    public float getFirstLineLength()
    {
        if (preservesLineBreaks())
        {
            if (endChild > startChild)
                return ((Inline) getSubBox(startChild)).getFirstLineLength();
            else
                return 0;
        }
        else
        {
            float ret = 0;
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
            return ret;
        }
    }

    @Override
    public float getLastLineLength()
    {
        if (preservesLineBreaks())
        {
            if (endChild > startChild)
                return ((Inline) getSubBox(endChild - 1)).getLastLineLength();
            else
                return 0;
        }
        else
        {
            float ret = 0;
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
            return ret;
        }
    }
    
    @Override
    public boolean containsLineBreak()
    {
        for (int i = startChild; i < endChild; i++)
            if (((Inline) getSubBox(i)).containsLineBreak())
                return true;
        return false;
        
    }

    @Override
    public boolean finishedByLineBreak()
    {
        return lineBreakStop;
    }
    
    @Override
    public boolean collapsedCompletely()
    {
        return collapsedCompletely;
    }
    
    @Override
    public int getWidthExpansionPoints(boolean atLineStart, boolean atLineEnd)
    {
        return countInlineExpansionPoints(startChild, endChild, atLineStart, atLineEnd);
    }

    @Override
    public void extendWidth(float dif, boolean atLineStart, boolean atLineEnd)
    {
        extendInlineChildWidths(dif, startChild, endChild, atLineStart, atLineEnd);
        bounds.width += dif;
        content.width += dif;
    }
    
    /**
     * After performing the layout, this method obtains the minimal relative Y coordinate of the aligned descendants. 
     * @return The minimal relative Y value.
     */
    public float getMinDescendantY()
    {
        return minDescendantY;
    }

    /**
     * After performing the layout, this method obtains the maximal relative Y coordinate of the aligned descendants. 
     * @return The minimal relative Y value.
     */
    public float getMaxDescendantY()
    {
        return minDescendantY;
    }

    public LineBox getCurline() {
        return curline;
    }

    public void setCurline(LineBox curline) {
        this.curline = curline;
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        InlineBox.log = log;
    }

    public float getHalflead() {
        return halflead;
    }

    public void setHalflead(float halflead) {
        this.halflead = halflead;
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
	
    @Override
    public boolean mayContainBlocks()
    {
    	return false;
    }

    @Override
    public void absolutePositions()
    {
        updateStackingContexts();
        if (isDisplayed())
        {
            //x coordinate is taken from the content edge
            absbounds.x = getParent().getAbsoluteContentX() + bounds.x;
            //y coordinate -- depends on the vertical alignment
            if (valign == CSSProperty.VerticalAlign.TOP)
            {
                final float topOfs = minDescendantY < 0 ? minDescendantY : 0; //negative minDescendantY means we have to make space for higher descendant boxes
                absbounds.y = linebox.getAbsoluteY() - getContentOffsetY() - topOfs;
            }
            else if (valign == CSSProperty.VerticalAlign.BOTTOM)
            {
                final float bottomOfs = maxDescendantY >= getContentHeight() ? maxDescendantY - getContentHeight() + 1 : 0;
                absbounds.y = linebox.getAbsoluteY() + linebox.getMaxBoxHeight() - getContentHeight() - getContentOffsetY() - bottomOfs;
            }
            else //other positions -- set during the layout. Relative to the parent content edge.
            {
                absbounds.y = getParent().getAbsoluteContentY() + bounds.y;
            }

            //consider the relative position
            if (position == POS_RELATIVE)
            {
                absbounds.x += leftset ? coords.left : (-coords.right);
                absbounds.y += topset ? coords.top : (-coords.bottom);
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
    public float getMinimalWidth()
    {
        float ret = 0;
        if (allowsWrapping())
        {
            //return the maximum of the nested minimal widths that are separated
            for (int i = startChild; i < endChild; i++)
            {
                float w = getSubBox(i).getMinimalWidth();
                if (w > ret) ret = w;
            }
        }
        else if (preservesLineBreaks())
        {
            //return the maximum of the nested minimal widths and try to sum the siblings sharing the same line
            float total = 0;
            for (int i = startChild; i < endChild; i++)
            {
                Box cur = getSubBox(i);
                float w = cur.getMinimalWidth();
                if (w > ret) ret = w;
                
                total += ((Inline) cur).getFirstLineLength();
                if (total > ret) ret = total;
                if (((Inline) cur).containsLineBreak())
                    total = 0;
                total += ((Inline) cur).getLastLineLength();
            }
        }
        else
        {
            //no wrapping allowed, no preserved line breaks, return the sum
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
        }
        //increase by margin, padding, border
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }
    
    @Override
    public float getMaximalWidth()
    {
        float ret = 0;
        if (!preservesLineBreaks())
        {
            //return the sum of all the elements inside
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
        }
        else
        {
            //return the maximum of the nested minimal widths and try to sum the siblings sharing the same line
            float total = 0;
            for (int i = startChild; i < endChild; i++)
            {
                Box cur = getSubBox(i);
                float w = cur.getMaximalWidth();
                if (w > ret) ret = w;
                
                total += ((Inline) cur).getFirstLineLength();
                if (total > ret) ret = total;
                if (((Inline) cur).containsLineBreak())
                    total = 0;
                total += ((Inline) cur).getLastLineLength();
            }
        }
        //increase by margin, padding, border
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }
    
    /**
     * Returns the height of the box or the highest subbox.
     */
    public float getMaximalHeight()
    {
        float ret = getHeight();
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            float h = 0;
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
    
    @Override
    public boolean startsWithWhitespace()
    {
        return (endChild > startChild) && getSubBox(startChild).startsWithWhitespace();
    }
    
    @Override
    public boolean endsWithWhitespace()
    {
        return (endChild > startChild) && getSubBox(endChild - 1).endsWithWhitespace();
    }
    
    @Override
    public void setIgnoreInitialWhitespace(boolean b)
    {
        if (endChild > startChild)
            getSubBox(startChild).setIgnoreInitialWhitespace(b);
    }
    
    @Override
    public void draw(DrawStage turn)
    {
        if (displayed)
        {
            if (!this.formsStackingContext())
            {
                switch (turn)
                {
                    case DRAW_NONINLINE:
                    case DRAW_FLOAT:
                        //there should be no block-level or floating children here -- we do nothing
                        break;
                    case DRAW_INLINE:
                        if (isVisible())
                        {
                            getViewport().getRenderer().renderElementBackground(this);
                            getViewport().getRenderer().startElementContents(this);
                            drawChildren(turn);
                            getViewport().getRenderer().finishElementContents(this);
                        }
                        break;
                }
            }
        }
    }
    
    @Override
    public float totalHeight()
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
        
        //containing box sizes
        float contw = getContainingBlock().width;
        
        //top and bottom margins take no effect for inline boxes
        // http://www.w3.org/TR/CSS21/box.html#propdef-margin-top
        margin = new LengthSet();
        margin.right = dec.getLength(getLengthValue("margin-right"), style.getProperty("margin-right") == CSSProperty.Margin.AUTO, 0, 0, contw);
        margin.left = dec.getLength(getLengthValue("margin-left"), style.getProperty("margin-left") == CSSProperty.Margin.AUTO, 0, 0, contw);
        emargin = new LengthSet(margin);

        loadBorders(dec, contw);
        
        padding = new LengthSet();
        padding.top = dec.getLength(getLengthValue("padding-top"), false, null, null, contw);
        padding.right = dec.getLength(getLengthValue("padding-right"), false, null, null, contw);
        padding.bottom = dec.getLength(getLengthValue("padding-bottom"), false, null, null, contw);
        padding.left = dec.getLength(getLengthValue("padding-left"), false, null, null, contw);
        
        content = new Dimension(0, 0);
        
        loadPosition();
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
}
