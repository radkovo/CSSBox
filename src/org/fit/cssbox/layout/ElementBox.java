/*
 * ElementBox.java
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
 * Created on 5. únor 2006, 21:32
 */

package org.fit.cssbox.layout;

import java.util.*;
import java.awt.*;

import cz.vutbr.web.css.*;
import org.w3c.dom.*;

/**
 * An abstract class representing a box formed by a DOM element. There are two
 * possible subclases: an inline box and a block box. The element box can contain
 * an arbitrary number of sub-boxes. Since the box can be split to several parts,
 * only a continuous part of the list is considered for rendering.
 * @author  radek
 */
abstract public class ElementBox extends Box
{
    public static final CSSProperty.Display DISPLAY_ANY = null;
    public static final CSSProperty.Display DISPLAY_NONE = CSSProperty.Display.NONE;
    public static final CSSProperty.Display DISPLAY_INLINE = CSSProperty.Display.INLINE;
    public static final CSSProperty.Display DISPLAY_BLOCK = CSSProperty.Display.BLOCK;
    public static final CSSProperty.Display DISPLAY_LIST_ITEM = CSSProperty.Display.LIST_ITEM;
    public static final CSSProperty.Display DISPLAY_RUN_IN = CSSProperty.Display.RUN_IN;
    public static final CSSProperty.Display DISPLAY_INLINE_BLOCK = CSSProperty.Display.INLINE_BLOCK;
    public static final CSSProperty.Display DISPLAY_TABLE = CSSProperty.Display.TABLE;
    public static final CSSProperty.Display DISPLAY_INLINE_TABLE = CSSProperty.Display.INLINE_TABLE;
    public static final CSSProperty.Display DISPLAY_TABLE_ROW_GROUP = CSSProperty.Display.TABLE_ROW_GROUP;
    public static final CSSProperty.Display DISPLAY_TABLE_HEADER_GROUP = CSSProperty.Display.TABLE_HEADER_GROUP;
    public static final CSSProperty.Display DISPLAY_TABLE_FOOTER_GROUP = CSSProperty.Display.TABLE_FOOTER_GROUP;
    public static final CSSProperty.Display DISPLAY_TABLE_ROW = CSSProperty.Display.TABLE_ROW;
    public static final CSSProperty.Display DISPLAY_TABLE_COLUMN_GROUP = CSSProperty.Display.TABLE_COLUMN_GROUP;
    public static final CSSProperty.Display DISPLAY_TABLE_COLUMN = CSSProperty.Display.TABLE_COLUMN;
    public static final CSSProperty.Display DISPLAY_TABLE_CELL = CSSProperty.Display.TABLE_CELL;
    public static final CSSProperty.Display DISPLAY_TABLE_CAPTION = CSSProperty.Display.TABLE_CAPTION;
    
    /** Default line height if nothing or 'normal' is specified */
    private static final float DEFAULT_LINE_HEIGHT = 1.12f;
    
    /** Assigned element */
    protected Element el;

    /** The display property value */
    protected CSSProperty.Display display;
    
    /** Background color or null when transparent */
    protected Color bgcolor;
    
    /** A list of nested boxes (possibly empty). The box can contain either 
     * only block boxes or only inline boxes. The inline boxes can only
     * contain inline boxes */
    protected Vector<Box> nested;
    
    /** Margin widths */
    protected LengthSet margin;
    
    /** Effective margins (after collapsing) */
    protected LengthSet emargin;
    
    /** Padding widths */
    protected LengthSet border;
    
    /** Border widths */
    protected LengthSet padding;
    
    /** Content sizes */
    protected Dimension content;
    
    /** Minimal absolute bounds. */
    protected Rectangle minAbsBounds;
    
    /** the computed value of line-height */
    protected int lineHeight;
    
    /** First valid child */
    protected int startChild;
    
    /** Last valid child (excl) */
    protected int endChild;

    //=======================================================================
    
    /**
     * Creates a new element box from a DOM element
     * @param n the DOM element
     * @param g current graphics context
     * @param ctx current visual context
     */
    public ElementBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        minAbsBounds = null;
        if (n != null)
        {
	        el = n;
	        
	        nested = new Vector<Box>();
	        startChild = 0;
	        endChild = 0;
	        isblock = false;
        }
    }
    
    /**
     * Copy the values from another element box.
     * @param src source element box
     */ 
    public void copyValues(ElementBox src)
    {
        super.copyValues(src);
        nested.addAll(src.nested);
        startChild = src.startChild;
        endChild = src.endChild;
        isblock = src.isblock;
        style = src.style;
        display = src.display;
        lineHeight = src.lineHeight;
        
        margin = new LengthSet(src.margin);
        emargin = new LengthSet(src.emargin);
        border = new LengthSet(src.border);
        padding = new LengthSet(src.padding);
        content = new Dimension(src.content);
    }
    
    //=======================================================================
    
    /**
     * @return the corresponding DOM element
     */
    public Element getElement()
    {
        return el;
    }
    
    /**
     * Set the new element style
     */
    public void setStyle(NodeData s)
    {
    	super.setStyle(s);
    	loadBasicStyle();
    }
    
    /**
     * Returns the value of the display property
     * @return One of the ElementBox.DISPLAY_XXX constants
     */
    public CSSProperty.Display getDisplay()
    {
    	return display;
    }
    
    public String getDisplayString()
    {
        if (display != null)
            return display.toString();
        else
            return "";
    }
    
    /**
     * @return the background color or null when transparent
     */
    public Color getBgcolor()
    {
        return bgcolor;
    }

    /**
     * @param bgcolor the background color
     */
    public void setBgcolor(Color bgcolor)
    {
        this.bgcolor = bgcolor;
    }

    /**
     * @return the number of subboxes in this box
     */
    public int getSubBoxNumber()
    {
        return nested.size();
    }
    
    /**
     * @param index the sub box index in the range from 0 to n-1 
     * @return the appropriate sub box
     */
    public Box getSubBox(int index)
    {
        return nested.elementAt(index);
    }
    
    /**
     * Adds a new sub box to the end of the sub box list.
     * @param box the new sub box to add
     */
    public void addSubBox(Box box)
    {
        nested.add(box);
        endChild++;
    }
    
    /**
     * Inserts a new sub box after a specified sub box
     * @param where the box already existing in the list
     * @param what the new box to add
     */
    public void insertSubBox(Box where, Box what)
    {
        int pos = nested.indexOf(where);
        nested.insertElementAt(what,  pos+1);
        endChild++;
    }

    /**
     * Inserts a new sub box at a specified index
     * @param index the index where the new box will be placed
     * @param what the new box to add
     */
    public void insertSubBox(int index, Box what)
    {
        nested.insertElementAt(what, index);
        endChild++;
    }
    
    /**
     * @return the width of the content without any margins and borders
     */
    public int getContentWidth()
    {
    	return content.width;
    }
    
    /**
     * @return the height of the content without any margins and borders
     */
    public int getContentHeight()
    {
    	return content.height;
    }
    
    @Override
    public int getLineHeight()
    {
        return lineHeight;
    }

    /**
     * @return the margin sizes
     */
    public LengthSet getMargin()
    {
        return margin;
    }
    
    /**
     * @return the effective margin sizes (after collapsing)
     */
    public LengthSet getEMargin()
    {
        return emargin;
    }
    
    /**
     * @return the border sizes (0 when no border is displayed)
     */
    public LengthSet getBorder()
    {
        return border;
    }
    
    /**
     * @return the padding sizes
     */
    public LengthSet getPadding()
    {
        return padding;
    }
    
    /**
     * @return the content sizes
     */
    public Dimension getContent()
    {
        return content;
    }
    
    /**
     * @return the first child from the list that is considered for rendering
     */
    public int getStartChild()
    {
        return startChild;
    }
    
    /**
     * @param index the index of the first child from the list that is considered for rendering
     */
    public void setStartChild(int index)
    {
        startChild = index;
    }
    
    /**
     * @return the last child from the list that is considered for rendering (not included)
     */
    public int getEndChild()
    {
        return endChild;
    }
    
    /**
     * @param index the index of the last child from the list that is considered for rendering
     */
    public void setEndChild(int index)
    {
        endChild = index;
    }
    
    /**
     * Sets the parent of the valid children to this (used while splitting the boxes)
     */
    public void adoptChildren()
    {
        for (int i = startChild; i < endChild; i++)
            nested.elementAt(i).setParent(this);
    }
    
    /**
     * This method is called for all the element boxes once the box tree is finished.
     * It is the right place for internal object initializing, content organization, etc. 
     */
    public void initBox()
    {
    }
    
    //=======================================================================

    public int getContentX()
    {
        return bounds.x + emargin.left + border.left + padding.left;
    }
    
    public int getContentY()
    {
        return bounds.y + emargin.top + border.top + padding.top;
    }
    
    public int getAbsoluteContentX()
    {
        return absbounds.x + emargin.left + border.left + padding.left;
    }
    
    public int getAbsoluteContentY()
    {
        return absbounds.y + emargin.top + border.top + padding.top;
    }
    
    public int totalWidth()
    {
        return emargin.left + border.left + padding.left + content.width +
            padding.right + border.right + emargin.right;
    }
    
    public int totalHeight()
    {
        return emargin.top + border.top + padding.top + content.height +
            padding.bottom + border.bottom + emargin.bottom;
    }
    
    public int getAvailableContentWidth()
    {
        return availwidth - emargin.left - border.left - padding.left 
                  - padding.right - border.right - emargin.right;
    }
    
    @Override
    public Rectangle getMinimalAbsoluteBounds()
    {
    	if (minAbsBounds == null)
    		minAbsBounds = computeMinimalAbsoluteBounds();
    	return minAbsBounds;
    }
    
    private Rectangle computeMinimalAbsoluteBounds()
    {
    	int rx1 = 0, ry1 = 0, rx2 = 0, ry2 = 0;
    	boolean valid = false;
    	for (int i = startChild; i < endChild; i++)
		{
			Box sub = getSubBox(i);
			Rectangle sb = sub.getMinimalAbsoluteBounds();
			if (sub.isDisplayed() && sub.isVisible() && sb.width > 0 && sb.height > 0)
			{
				if (sb.x < rx1 || !valid) rx1 = sb.x;
				if (sb.y < ry1 || !valid) ry1 = sb.y;
				if (sb.x + sb.width > rx2 || !valid) rx2 = sb.x + sb.width;
				if (sb.y + sb.height > ry2 || !valid) ry2 = sb.y + sb.height;
				valid = true;
			}
		}
    	return new Rectangle(rx1, ry1, rx2 - rx1, ry2 - ry1);
    }
    
    @Override
    public boolean affectsDisplay()
    {
        boolean ret = !isEmpty();
        //non-zero top or bottom border
        if (border.top > 0 || border.bottom > 0)
            ret = true;
        //the same with padding
        if (padding.top > 0 || padding.bottom > 0)
            ret = true;
        
        return ret;
    }
    
    /**
     * @return the bounds of the background - the content and padding
     */
    public Rectangle getAbsoluteBackgroundBounds()
    {
        return new Rectangle(absbounds.x + emargin.left + border.left,
                             absbounds.y + emargin.top + border.top,
                             content.width + padding.left + padding.right,
                             content.height + padding.top + padding.bottom);
    }

    /**
     * @return the bounds of the border - the content, padding and border
     */
    public Rectangle getAbsoluteBorderBounds()
    {
        return new Rectangle(absbounds.x + emargin.left,
                             absbounds.y + emargin.top,
                             content.width + padding.left + padding.right + border.left + border.right,
                             content.height + padding.top + padding.bottom + border.top + border.bottom);
    }

    @Override
    public String getText()
    {
        String ret = "";
        for (int i = startChild; i < endChild; i++)
        	ret += getSubBox(i).getText();
        return ret;
    }
    
    @Override
    public boolean isWhitespace()
    {
        boolean ret = true;
        for (int i = startChild; i < endChild; i++)
            if (!getSubBox(i).isWhitespace())
                ret = false;
        return ret;
    }
        
    //=======================================================================
    
    /** 
     * Draw the background and border of this box (no subboxes)
     * @param g the graphics context used for drawing 
     */
    protected void drawBackground(Graphics2D g)
    {
        Color color = g.getColor(); //original color

        //top left corner
        int x = absbounds.x;
        int y = absbounds.y;
        int x2 = absbounds.x + absbounds.width - 1;
        int y2 = absbounds.y + absbounds.height - 1;

        //draw the margin
        int mx = x + emargin.left;
        int my = y + emargin.top;
        int mw = bounds.width - emargin.left - emargin.right;
        int mh = bounds.height - emargin.top - emargin.bottom;

        //draw the border
        if (border.top > 0)
            drawBorder(g, mx, my, mx + mw, my, border.top, "top");
        if (border.right > 0)
            drawBorder(g, mx + mw, my, mx + mw, my + mh, border.right, "right"); 
        if (border.bottom > 0)
            drawBorder(g, mx, my + mh, mx + mw, my + mh, border.bottom, "bottom"); 
        if (border.left > 0)
            drawBorder(g, mx, my, mx, my + mh, border.left, "left"); 

        //Background
        int bgx = x + emargin.left + border.left;
        int bgy = y + emargin.top + border.top;
        int bgw = padding.left + content.width + padding.right;
        int bgh = padding.top + content.height + padding.bottom;
        //clip to computed absolute size
        if (bgx + bgw - 1 > x2) bgw = x2 - bgx + 1;
        if (bgy + bgh - 1 > y2) bgh = y2 - bgy + 1;
        //draw the color
        if (bgcolor != null)
        {
            g.setColor(bgcolor);
            g.fillRect(bgx, bgy, bgw, bgh);
        }

        g.setColor(color); //restore original color
    }
    
    private void drawBorder(Graphics2D g, int x1, int y1, int x2, int y2, int width,
                            String side)
    {
        TermColor tclr = style.getValue(TermColor.class, "border-"+side+"-color");
        if (tclr != null)
        {
            Color clr = tclr.getValue();
            float dash[] = {10.0f}; 
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, width, dash, 0));
            if (clr == null) clr = Color.BLACK;
            g.setColor(clr);
            g.drawRect(x1, y1, x2 - x1, y2 - y1);            
        }
        
    }

    @Override
    public void drawExtent(Graphics2D g)
    {
    	//draw the full box
        g.setColor(Color.RED);
        g.drawRect(absbounds.x, absbounds.y, bounds.width, bounds.height);
    	
    	//draw the content box
        /*g.setColor(Color.ORANGE);
        g.drawRect(getContentX(), getContentY(), getContentWidth(), getContentHeight());*/
        
        //draw the real content box
        /*g.setColor(Color.GREEN);
        Rectangle r = getMinimalBounds();
        g.drawRect(r.x, r.y, r.width, r.height);*/
    }
    
    //=======================================================================
    
    /**
     * Load the box sizes from the CSS properties.
     */
    abstract protected void loadSizes();
    
    /**
     * Update the box sizes according to the new parent size. Should be
     * called when the parent has been resized.
     */
    abstract public void updateSizes(); 

    /**
     * Load the basic style from the CSS properties. This includes the display
     * properties, floating, positioning, color and font properties.
     */
    private void loadBasicStyle()
    {
        ctx.updateForGraphics(style, g);
        
        display = style.getProperty("display");
        if (display == null) display = CSSProperty.Display.INLINE;
        
        isblock = (display == DISPLAY_BLOCK);
        displayed = (display != DISPLAY_NONE && display != DISPLAY_TABLE_COLUMN);
        visible = (style.getProperty("visibility") != CSSProperty.Visibility.HIDDEN); 

        CSSProperty.Float fp = style.getProperty("float");
        CSSProperty.Position pp = style.getProperty("position");
        if (fp == CSSProperty.Float.RIGHT || fp == CSSProperty.Float.LEFT ||
            pp == CSSProperty.Position.ABSOLUTE || pp == CSSProperty.Position.FIXED)
                isblock = true;
        
        //line height
        CSSProperty.LineHeight lh = style.getProperty("line-height");
        if (lh == null || lh == CSSProperty.LineHeight.NORMAL)
            lineHeight = Math.round(DEFAULT_LINE_HEIGHT * ctx.getFontHeight());
        else if (lh == CSSProperty.LineHeight.length)
        {
            TermLength len = style.getValue(TermLength.class, "line-height");
            lineHeight = (int) ctx.pxLength(len);
        }
        else if (lh == CSSProperty.LineHeight.percentage)
        {
            TermPercent len = style.getValue(TermPercent.class, "line-height");
            lineHeight = (int) ctx.pxLength(len, ctx.getEm()); 
        }
        else //must be NUMBER
        {
            TermNumber len = style.getValue(TermNumber.class, "line-height");
            float r = len.getValue(); 
            lineHeight = (int) Math.round(r * ctx.getEm());
        }
        
        //background
        CSSProperty.BackgroundColor bg = style.getProperty("background-color");
        if (bg == CSSProperty.BackgroundColor.color)
        {
            TermColor bgc = style.getValue(TermColor.class, "background-color");
            bgcolor = bgc.getValue();
        }
        else
            bgcolor = null;
        
    }
    
}
