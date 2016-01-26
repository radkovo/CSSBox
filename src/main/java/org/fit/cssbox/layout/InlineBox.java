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

import java.awt.*;

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
    private LineBox curline;
    
    /** half-lead after layout */
    private int halflead;
    
    /** Layout finished with a line break? */
    protected boolean lineBreakStop;
    
    //========================================================================
    
    /** Creates a new instance of InlineBox */
    public InlineBox(Element n, Graphics2D g, VisualContext ctx) 
    {
        super(n, g, ctx);
        halflead = 0;
        lineBreakStop = false;
    }
    
    public void copyValues(InlineBox src)
    {
        super.copyValues(src);
        valign = src.valign;
    }
    
    @Override
    public InlineBox copyBox()
    {
        InlineBox ret = new InlineBox(el, g, ctx);
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
    
    public int getBaselineOffset()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getBaselineOffset();
    }
    
    public int getBelowBaseline()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getBelowBaseline();
    }
    
    public int getTotalLineHeight()
    {
    	if (curline == null)
    		return 0;
    	else
    		return curline.getTotalLineHeight();
    }
    
    public int getMaxLineHeight()
    {
        if (curline == null)
            return lineHeight;
        else
            return Math.max(lineHeight, curline.getMaxLineHeight());
    }
    
    public int getLineboxOffset()
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
    public int getHalfLead()
    {
        return halflead;
    }
    
    public int getFirstLineLength()
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
            int ret = 0;
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
            return ret;
        }
    }

    public int getLastLineLength()
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
            int ret = 0;
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
            return ret;
        }
    }
    
    public boolean containsLineBreak()
    {
        for (int i = startChild; i < endChild; i++)
            if (((Inline) getSubBox(i)).containsLineBreak())
                return true;
        return false;
        
    }

    public boolean finishedByLineBreak()
    {
        return lineBreakStop;
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
    
    /** Compute the width and height of this element. Layout the sub-elements.
     * @param availw Maximal width available to the child elements
     * @param force Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return True if the box has been succesfully placed
     */
    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        //if (getElement() != null && getElement().getAttribute("id").equals("mojo"))
        //  System.out.println("jo!");
        //Skip if not displayed
        if (!displayed)
        {
            content.setSize(0, 0);
            bounds.setSize(0, 0);
            return true;
        }

        setAvailableWidth(availw);
        
        curline = new LineBox(this, startChild, 0);
        int wlimit = getAvailableContentWidth();
        int x = 0; //current x
        boolean ret = true;
        rest = null;

        int lastbreak = startChild; //last possible position of a line break
        boolean lastwhite = false; //last box ends with a whitespace
        
        for (int i = startChild; i < endChild; i++)
        {
            Box subbox = getSubBox(i);
            if (subbox.canSplitBefore())
            	lastbreak = i;
            //when forcing, force the first child only and the children before
            //the first possible break
            boolean f = force && (i == startChild || lastbreak == startChild);
            if (lastwhite) subbox.setIgnoreInitialWhitespace(true);
            boolean fit = subbox.doLayout(wlimit - x, f, linestart && (i == startChild));
            if (fit) //something has been placed
            {
                if (subbox instanceof Inline)
                {
                    subbox.setPosition(x,  0); //the y position will be updated later
                    x += subbox.getWidth();
                    curline.considerBox((Inline) subbox);
                    if (((Inline) subbox).finishedByLineBreak())
                        lineBreakStop = true;
                }
                else
                	log.debug("Warning: doLayout(): subbox is not inline: " + subbox);
                if (subbox.getRest() != null) //is there anything remaining?
                {
                    InlineBox rbox = copyBox();
                    rbox.splitted = true;
                    rbox.splitid = splitid + 1;
                    rbox.setStartChild(i); //next starts with me...
                    rbox.nested.setElementAt(subbox.getRest(), i); //..but only with the rest
                    rbox.adoptChildren();
                    setEndChild(i+1); //...and this box stops with this element
                    rest = rbox;
                    break;
                }
                else if (lineBreakStop) //nothing remained but there was a line break
                {
                    if (i + 1 < endChild) //some children remaining
                    {
                        InlineBox rbox = copyBox();
                        rbox.splitted = true;
                        rbox.splitid = splitid + 1;
                        rbox.setStartChild(i + 1); //next starts with the next one
                        rbox.adoptChildren();
                        setEndChild(i+1); //...and this box stops with this element
                        rest = rbox;
                    }
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
                    InlineBox rbox = copyBox();
                    rbox.splitted = true;
                    rbox.splitid = splitid + 1;
                    rbox.setStartChild(lastbreak); //next time start from the last break
                    rbox.adoptChildren();
                    setEndChild(lastbreak); //this box stops here
                    rest = rbox;
                    break;
                }
            }
            
            if (!subbox.isEmpty())
                lastwhite = subbox.collapsesSpaces() && subbox.endsWithWhitespace(); 
            if (subbox.canSplitAfter())
            	lastbreak = i+1;
        }
        
        //compute the vertical positions of the boxes
        //updateLineMetrics();
        content.width = x;
        content.height = (int) Math.round(ctx.getFontHeight() * 1.1); //based on browser behaviour observations
        halflead = (content.height - ctx.getFontHeight()) / 2;
        alignBoxes();
        setSize(totalWidth(), totalHeight());
        
        return ret;
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
                absbounds.y = linebox.getAbsoluteY() + linebox.getMaxLineHeight() - getContentHeight() - getContentOffsetY();
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
    public int getMinimalWidth()
    {
        int ret = 0;
        if (allowsWrapping())
        {
            //return the maximum of the nested minimal widths that are separated
            for (int i = startChild; i < endChild; i++)
            {
                int w = getSubBox(i).getMinimalWidth();
                if (w > ret) ret = w;
            }
        }
        else if (preservesLineBreaks())
        {
            //return the maximum of the nested minimal widths and try to sum the siblings sharing the same line
            int total = 0;
            for (int i = startChild; i < endChild; i++)
            {
                Box cur = getSubBox(i);
                int w = cur.getMinimalWidth();
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
    public int getMaximalWidth()
    {
        int ret = 0;
        if (!preservesLineBreaks())
        {
            //return the sum of all the elements inside
            for (int i = startChild; i < endChild; i++)
                ret += getSubBox(i).getMaximalWidth();
        }
        else
        {
            //return the maximum of the nested minimal widths and try to sum the siblings sharing the same line
            int total = 0;
            for (int i = startChild; i < endChild; i++)
            {
                Box cur = getSubBox(i);
                int w = cur.getMaximalWidth();
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
        ctx.updateGraphics(g);
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
    public void drawExtent(Graphics2D g)
    {
        super.drawExtent(g);
        
        /*g.setColor(Color.MAGENTA);
        int y = getAbsoluteContentY() - getLineboxOffset();
        int h = 0;
        if (curline != null)
            h = curline.getTotalLineHeight();
        g.drawRect(getAbsoluteContentX(), y, getContentWidth(), h);
        
        g.setColor(Color.BLUE);
        y = y + getBaselineOffset();
        g.drawRect(getAbsoluteContentX(), y, getContentWidth(), 1);*/
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
        
        //containing box sizes
        int contw = getContainingBlock().width;
        
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
    
    //=====================================================================================================

    /**
     * Vertically aligns the contained boxes according to their vertical-align properties.
     */
    private void alignBoxes()
    {
        for (int i = startChild; i < endChild; i++)
        {
            Box sub = getSubBox(i);
            if (!sub.isBlock())
            {
                //position relative to the line box
                int dif = curline.alignBox((Inline) sub);
                //recompute to the content box
                dif = dif - getLineboxOffset();
                //recompute to the bounding box
                if (sub instanceof InlineBox)
                    dif = dif - ((ElementBox) sub).getContentOffsetY();
                
                if (dif != 0)
                    sub.moveDown(dif);
            }
        }
    }
    
}
