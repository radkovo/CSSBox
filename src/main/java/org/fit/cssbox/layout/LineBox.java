/**
 * LineBox.java
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
 * Created on 4.7.2006, 11:20:34 by burgetr
 */
package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.VerticalAlign;

/**
 * This class describes a single line of the content in a block box.
 * @author burgetr
 */
public class LineBox
{
    /** The BlockBox containing the lines */
    private ElementBox parent;
    
    /** Index of the first box at this line (from all the subboxes of the block) */
    private int start;
    
    /** The Y position of this line top */
    private float y;
    
    /** Index of the last box at this line (excl.) */
    private int end;
    
    /** Total text width in pixels (for horizontal alignment) */
    private float width;
    
    /** Left offset caused by floating boxes */
    private float left;
    
    /** Right offset caused by floating boxes */
    private float right;
    
    /** Maximal height above the baseline (including baseline) */
    private float above;
    
    /** Maximal height below the baseline (excluding baseline) */
    private float below;
    
    /** Maximal aligned height of the boxes on the line */
    private float maxAlignedHeight;
    
    /** Maximal height of both aligned and unaligned boxes */
    private float maxBoxHeight;
    
    /** An additional top Y offset used for alligning the boxes with vertical-align: bottom with the baseline-aligned boxes */
    private float heightFromBottom;
    
    
    public LineBox(ElementBox parent, int start, float y)
    {
        this.parent = parent;
        this.start = start;
        this.y = y;
        width = 0;
        left = 0;
        right = 0;
        above = 0;
        below = 0;
    }

    @Override
    public String toString()
    {
        return "LineBox " + start + ".." + end + " y=" + y +  " width=" + width + " above=" + above + " below=" + below + " total=" + (above+below) + " alignedHeight=" + maxAlignedHeight + " lead=" + getLead();
    }

    public ElementBox getParent()
    {
        return parent;
    }
    
    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    public int getStart()
    {
        return start;
    }

    public void setY(float y)
    {
        this.y = y;
    }
    
    public float getY()
    {
        return y;
    }
    
    public float getAbsoluteY()
    {
        return parent.getAbsoluteContentY() + y;
    }
    
    public void setStart(int start)
    {
        this.start = start;
    }

    public float getWidth()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }
    
    public float getLeftLimit()
    {
        return left;
    }

    public float getRightLimit()
    {
        return right;
    }
    
    /**
     * Computes the sum of the left and right limiting widths
     * @return The sum of the widths
     */
    public float getLimits()
    {
        return left + right;
    }
    
    /**
     * Sets the left and right width limits formed by the floating blocks (if any)
     * @param left the left limit
     * @param right the right limit
     */
    public void setLimits(float left, float right)
    {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the total line box height which is the same as {@code getBaselineOffset() + getBelowBaseline}.
     * @return the line box height
     */
    public float getTotalLineHeight()
    {
        return above + below;
    }
    
    public float getTopOffset()
    {
        float ofs = heightFromBottom - getMaxAlignedHeight();
        return ofs >= 0 ? ofs : 0;
    }

    /**
     * Returns the maximal height of the aligned boxes (that have vertical-align different from top and bottom).
     * @return the maximal aligned height.
     */
    public float getMaxAlignedHeight()
    {
        return maxAlignedHeight;
    }
    
    /**
     * Returns the maximal inline height of both the aligned and unaligned boxes.
     * @return the maximal height.
     */
    public float getMaxBoxHeight()
    {
        return maxBoxHeight;
    }

    /**
     * Returns the line box height above the baseline.
     * @return the baseline Y offset
     */
    public float getBaselineOffset()
    {
    	return above;
    }

    /**
     * Returns the line box height below the baseline.
     * @return the bottom Y offset
     */
    public float getBelowBaseline()
    {
    	return below;
    }
    
    /**
     * Obtains the lead - the difference between the declared line height and the actual line box height.
     * @return the lead value
     */
    public float getLead()
    {
    	return maxAlignedHeight - (above + below);
    }
    
    /**
     * Updates the line box sizes according to a new inline box.
     * @param box the new box
     */
    public void considerBox(Inline box)
    {
        if (((Box) box).isDisplayed() && !box.collapsedCompletely())
        {
            float a = box.getBaselineOffset();
            float b = box.getBelowBaseline();
        	if (box instanceof InlineElement)
        	{
    	        VerticalAlign va = ((InlineElement) box).getVerticalAlign();
    	        if (va != VerticalAlign.TOP && va != VerticalAlign.BOTTOM) //the box influences 'a' and 'b'
    	        {
    	            float dif = computeBaselineDifference((InlineElement) box);
    	            a -= dif; //what from the box is above our baseline
    	            b += dif; //what from the box is below
    	            above = Math.max(above, a);
    	            below = Math.max(below, b);
    	            maxAlignedHeight = Math.max(maxAlignedHeight, box.getMaxLineHeight());
    	        }
    	        else if (va == VerticalAlign.BOTTOM)
    	            heightFromBottom = Math.max(heightFromBottom, box.getMaxLineHeight());
        	}
        	else
        	{
    	        above = Math.max(above, a);
    	        below = Math.max(below, b);
                maxAlignedHeight = Math.max(maxAlignedHeight, box.getMaxLineHeight());
        	}
        	
            maxBoxHeight = Math.max(maxBoxHeight, box.getMaxLineHeight());
        }
    }
    
    /**
     * Initializes the above and below offsets based on the font properties of the given box.
     * @param box The box whose font properties should be used for initialization.
     */
    public void considerBoxProperties(ElementBox box)
    {
        VisualContext ctx = box.getVisualContext();
        float a = ctx.getBaselineOffset();
        float b = ctx.getFontHeight() - ctx.getBaselineOffset();
        above = Math.max(above, a);
        below = Math.max(below, b);

        maxAlignedHeight = Math.max(maxAlignedHeight, box.getLineHeight());
    }
    
    /**
     * Aligns a new box and updates the line metrics. 
     * @param box the box to be placed on the line
     * @return the Y distance of the box top content edge from top of this line box
     */
    public float alignBox(Inline box)
    {
    	if (box instanceof InlineElement)
    	{
	        VerticalAlign va = ((InlineElement) box).getVerticalAlign();
	        if (va == VerticalAlign.TOP || va == VerticalAlign.BOTTOM)
	        {
	            return 0; //top and bottom boxes are aligned to the line box in their absolutePositions()
	        }
	        else
	        {
	            return above + computeBaselineDifference((InlineElement) box) - box.getBaselineOffset() + ((InlineElement) box).getLineboxOffset();
	        }
    	}
    	else
    		return above - box.getBaselineOffset();
    }
    
    /** 
     * Computes the difference between the box baseline and our baseline according to the vertical alignment of the box.
     * @param box The box whose baseline should be considered
     * @return the vertical difference betweein baselines, positive dif means the box baseline is below our baseline 
     */
    private float computeBaselineDifference(InlineElement box)
    {
        float a = box.getBaselineOffset();
        float b = box.getBelowBaseline();
        CSSProperty.VerticalAlign va = box.getVerticalAlign();
        
        float dif = 0;

        if (va == CSSProperty.VerticalAlign.BASELINE)
            dif = 0; //just sits on the baseline
        else if (va == CSSProperty.VerticalAlign.MIDDLE)
        {
            float midbox = box.getLineHeight() / 2.0f;
            float halfex = parent.getVisualContext().getEx() / 2.0f;
            dif = midbox - halfex;
        }
        else if (va == CSSProperty.VerticalAlign.SUB)
            dif = 0.3f * parent.getLineHeight();
        else if (va == CSSProperty.VerticalAlign.SUPER)
            dif = -0.3f * parent.getLineHeight();  
        else if (va == CSSProperty.VerticalAlign.TEXT_TOP)
        {
            float na = parent.getVisualContext().getBaselineOffset();
            dif = a - na;
        }
        else if (va == CSSProperty.VerticalAlign.TEXT_BOTTOM)
        {
            float nb = parent.getVisualContext().getFontHeight() - parent.getVisualContext().getBaselineOffset();
            dif = nb - b;
        }
        else if (va == CSSProperty.VerticalAlign.length || va == CSSProperty.VerticalAlign.percentage)
        {
            CSSDecoder dec = new CSSDecoder(((ElementBox) box).getVisualContext());
            float len = dec.getLength(((ElementBox) box).getLengthValue("vertical-align"), false, 0, 0, box.getLineHeight());
            dif = -len;
        }
        return dif;
    }
    
}
