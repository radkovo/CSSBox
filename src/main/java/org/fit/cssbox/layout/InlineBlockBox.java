/*
 * InlineBlockBox.java
 * Copyright (c) 2005-2011 Radek Burget
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
 * Created on 16.10.2011, 22:51:58 by radek
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermLengthOrPercent;

import org.w3c.dom.Element;

/**
 * A box corresponding to an inline-block element.
 * 
 * @author radek
 */
public class InlineBlockBox extends BlockBox implements InlineElement
{
    /** vertical box alignment specified by the style */
    private CSSProperty.VerticalAlign valign;
    
    /** parent LineBox assigned during layout */
    private LineBox linebox;
    
    /** The baseline offset of the contents */
    protected int baseline;
    
    /* current layout parametres */
    private int availw;
    private boolean force;
    
    
	public InlineBlockBox(Element n, Graphics2D g, VisualContext ctx)
	{
		super(n, g, ctx);
		isblock = false;
	}

	public InlineBlockBox(InlineBox src)
	{
		super(src);
		isblock = false;
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
	
    public void setLineBox(LineBox linebox)
    {
        this.linebox = linebox;
    }
    
    public LineBox getLineBox()
    {
        return linebox;
    }

    public int getLineboxOffset()
    {
        return 0;
    }
    
    //========================================================================
    
    public int getMaxLineHeight()
    {
        return getHeight();
    }

    public int getBaselineOffset()
    {
        return baseline;
    }

    public int getBelowBaseline()
    {
        return getHeight() - baseline;
    }

    public int getTotalLineHeight()
    {
        return getHeight();
    }

    public int getHalfLead()
    {
        return 0;
    }
    
    public int getFirstLineLength()
    {
        return getMaximalContentWidth();
    }

    public int getLastLineLength()
    {
        return getMaximalContentWidth();
    }

    public boolean containsLineBreak()
    {
        return false;
    }

    public boolean finishedByLineBreak()
    {
        return false;
    }
    
	//========================================================================
	
    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        this.availw = availw;
        this.force = force;
        super.doLayout(availw, force, linestart);
        if (force || fitsSpace())
        {
            baseline = getLastInlineBoxBaseline(this);
            if (baseline == -1)
                baseline = getHeight();
            else
            {
                baseline += getContentOffsetY();
                if (baseline > getHeight()) baseline = getHeight();
            }
            return true;
        }
        else
            return false;
    }

    @Override
    protected void layoutInline()
    {
        if (force || fitsSpace()) //do not layout if we don't fit the available space
            super.layoutInline();
    }

    @Override
    protected void layoutBlocks()
    {
        if (force || fitsSpace()) //do not layout if we don't fit the available space
            super.layoutBlocks();
    }
    
    /**
     * Checks wheter the block fits the available space
     * @return <code>true</code> when there is enough space to fit the block
     */
    private boolean fitsSpace()
    {
        return availw >= totalWidth();
    }

    @Override
    public boolean hasFixedWidth()
    {
        return wset; //the width should not be computed from the parent
    }

    @Override
    public int getMinimalContentWidthLimit()
    {
        int ret;
        if (wset)
            ret = content.width;
        else if (min_size.width != -1)
            ret = min_size.width;
        else
            ret = 0;
            
        return ret;
    }
    
    @Override
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update)
    {
        //The same as for absolutely positioned boxes (shrink-to-fit or explicitely set)
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (width == null) auto = true; //no value behaves as "auto"

        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");
        preferredWidth = -1;
        
        if (!widthComputed) update = false;
        
        if (auto)
        {
            if (exact) wset = false;
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, contw);
            preferredWidth = -1; //we don't prefer anything (auto width)
        }
        else
        {
            if (exact) 
            {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
        }

        //auto margins are treated as zero
        margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
        margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
        
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
                //absbounds.y = linebox.getAbsoluteY() + (linebox.getLead() / 2) - getContentOffsetY();
                absbounds.y = linebox.getAbsoluteY() - getContentOffsetY();
            }
            else if (valign == CSSProperty.VerticalAlign.BOTTOM)
            {
                absbounds.y = linebox.getAbsoluteY() + linebox.getTotalLineHeight() - getContentHeight() - getContentOffsetY();
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
    
    /**
     * Loads the basic style properties related to the inline elements.
     */
    protected void loadInlineStyle()
    {
        valign = style.getProperty("vertical-align");
        if (valign == null) valign = CSSProperty.VerticalAlign.BASELINE;
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
                        //everything is drawn in the DRAW_INLINE phase as a new stacking context
                        break;
                    case DRAW_INLINE:
                        if (isVisible())
                            getViewport().getRenderer().renderElementBackground(this);
                        drawStackingContext(true);
                        break;
                }
            }
        }
    }
    
    //========================================================================
	
	/**
	 * Recursively finds the baseline of the last in-flow box.
	 * @param root the element to start search in
	 * @return The baseline offset in the element content or -1 if there are no in-flow boxes.
	 */
    private int getLastInlineBoxBaseline(ElementBox root)
	{
	    //find last in-flow box
	    Box box = null;
        for (int i = root.getSubBoxNumber() - 1; i >= 0; i--)
        {
            box = root.getSubBox(i);
            if (box.isInFlow())
                break;
            else
                box = null;
        }
        
        if (box != null)
        {
	        if (box instanceof Inline)
	        {
                //System.out.println(box + ":I: " + (box.getContentY() + ((Inline) box).getBaselineOffset()));
                return box.getContentY() + ((Inline) box).getBaselineOffset();
	        }
            else
            {
                //System.out.println(box + ":B: " + (box.getContentY() + getLastInlineBoxBaseline((ElementBox) box)));
	            return box.getContentY() + getLastInlineBoxBaseline((ElementBox) box);
            }
	    }
	    else
	        return -1; //no inline box found
	}
	

}
