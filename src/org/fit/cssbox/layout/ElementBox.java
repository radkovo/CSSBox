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

import org.w3c.dom.*;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.fit.cssbox.css.CSSNorm;

/**
 * An abstract class representing a box formed by a DOM element. There are two
 * possible subclases: an inline box and a block box. The element box can contain
 * an arbitrary number of sub-boxes. Since the box can be split to several parts,
 * only a continuous part of the list is considered for rendering.
 * @author  radek
 */
abstract public class ElementBox extends Box
{
    public static final short DISPLAY_ANY = -1;
    public static final short DISPLAY_NONE = 0;
    public static final short DISPLAY_INLINE = 1;
    public static final short DISPLAY_BLOCK = 2;
    public static final short DISPLAY_LIST_ITEM = 3;
    public static final short DISPLAY_RUN_IN = 4;
    public static final short DISPLAY_INLINE_BLOCK = 5;
    public static final short DISPLAY_TABLE = 6;
    public static final short DISPLAY_INLINE_TABLE = 7;
    public static final short DISPLAY_TABLE_ROW_GROUP = 8;
    public static final short DISPLAY_TABLE_HEADER_GROUP = 9;
    public static final short DISPLAY_TABLE_FOOTER_GROUP = 10;
    public static final short DISPLAY_TABLE_ROW = 11;
    public static final short DISPLAY_TABLE_COLUMN_GROUP = 12;
    public static final short DISPLAY_TABLE_COLUMN = 13;
    public static final short DISPLAY_TABLE_CELL = 14;
    public static final short DISPLAY_TABLE_CAPTION = 15;
    
    /** Default line height if nothing or 'normal' is specified */
    private static final float DEFAULT_LINE_HEIGHT = 1.1f;
    
    /** Assigned element */
    protected Element el;

    /** The display property value */
    protected short display;
    
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
    public ElementBox(Element n, Graphics g, VisualContext ctx)
    {
        super(n, g, ctx);
        if (n != null)
        {
	        el = n;
	        
	        nested = new Vector<Box>();
	        startChild = 0;
	        endChild = 0;
	        isblock = false;
	        //style = org.burgetr.transformer.DOMAnalyzer.getStyleDeclaration(el);
	        //loadBasicStyle();
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
    public void setStyle(CSSStyleDeclaration s)
    {
    	super.setStyle(s);
    	loadBasicStyle();
    }
    
    /**
     * Returns the value of the display property
     * @return One of the ElementBox.DISPLAY_XXX constants
     */
    public short getDisplay()
    {
    	return display;
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
    
    public Rectangle getMinimalBounds()
    {
    	int rx1 = 0, ry1 = 0, rx2 = 0, ry2 = 0;
    	boolean valid = false;
    	for (Iterator<Box> it = nested.iterator(); it.hasNext();)
		{
			Box sub = it.next();
			Rectangle sb = sub.getMinimalBounds();
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
    public Rectangle getBackgroundBounds()
    {
        return new Rectangle(bounds.x + emargin.left + border.left,
                             bounds.y + emargin.top + border.top,
                             content.width + padding.left + padding.right,
                             content.height + padding.top + padding.bottom);
    }

    /**
     * @return the bounds of the border - the content, padding and border
     */
    public Rectangle getBorderBounds()
    {
        return new Rectangle(bounds.x + emargin.left,
                             bounds.y + emargin.top,
                             content.width + padding.left + padding.right + border.left + border.right,
                             content.height + padding.top + padding.bottom + border.top + border.bottom);
    }

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
    protected void drawBackground(Graphics g)
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
            drawBorder(g, mx, my, mx + mw, my + border.top, "top");
        if (border.right > 0)
            drawBorder(g, mx + mw - border.right, my, mx + mw, my + mh, "right"); 
        if (border.bottom > 0)
            drawBorder(g, mx, my + mh - border.bottom, mx + mw, my + mh, "bottom"); 
        if (border.left > 0)
            drawBorder(g, mx, my, mx + border.left, my + mh, "left"); 

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
    
    private void drawBorder(Graphics g, int x1, int y1, int x2, int y2,
                            String side)
    {
        String sclr = getStyleProperty("border-"+side+"-color");
        if (!sclr.equals("") && !sclr.equals("none"))
        {
            Color clr = ctx.getColor(sclr);
            if (clr == null) clr = Color.BLACK;
            g.setColor(clr);
            g.fillRect(x1, y1, x2 - x1, y2 - y1);            
        }
        
    }

    public void drawExtent(Graphics g)
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
        
        String d = getStyleProperty("display");
        if (d.equals("none")) display = DISPLAY_NONE;
        else if (d.equals("inline")) display = DISPLAY_INLINE;
        else if (d.equals("block")) display = DISPLAY_BLOCK;
        else if (d.equals("list-item")) display = DISPLAY_LIST_ITEM;
        else if (d.equals("run-in")) display = DISPLAY_RUN_IN;
        else if (d.equals("inline-block")) display = DISPLAY_INLINE_BLOCK;
        else if (d.equals("table")) display = DISPLAY_TABLE;
        else if (d.equals("inline-table")) display = DISPLAY_INLINE_TABLE;
        else if (d.equals("table-row-group")) display = DISPLAY_TABLE_ROW_GROUP;
        else if (d.equals("table-header-group")) display = DISPLAY_TABLE_HEADER_GROUP;
        else if (d.equals("table-footer-group")) display = DISPLAY_TABLE_FOOTER_GROUP;
        else if (d.equals("table-row")) display = DISPLAY_TABLE_ROW;
        else if (d.equals("table-column-group")) display = DISPLAY_TABLE_COLUMN_GROUP;
        else if (d.equals("table-column")) display = DISPLAY_TABLE_COLUMN;
        else if (d.equals("table-cell")) display = DISPLAY_TABLE_CELL;
        else if (d.equals("table-caption")) display = DISPLAY_TABLE_CAPTION;
        else display = DISPLAY_INLINE;
        
        isblock = (display == DISPLAY_BLOCK);
        displayed = (display != DISPLAY_NONE && display != DISPLAY_TABLE_COLUMN);
        visible = !getStyleProperty("visibility").equals("hidden");
        
        String fp = getStyleProperty("float");
        String pp = getStyleProperty("position");
        if (fp.equals("right") || fp.equals("left") ||
            pp.equals("absolute") || pp.equals("fixed"))
                isblock = true;
        
        //line height
        String lh = getStyleProperty("line-height");
        if (lh.equals("") || lh.equals("normal"))
            lineHeight = Math.round(DEFAULT_LINE_HEIGHT * ctx.getFontHeight());
        else if (CSSNorm.isLength(lh))
            lineHeight = ctx.getLength(lh);
        else if (CSSNorm.isPercent(lh))
        {
            String lhp = lh.substring(0, lh.length()-1);
            float r = DEFAULT_LINE_HEIGHT * 100; 
            try {
                r = Float.valueOf(lhp);
            } catch (NumberFormatException e) {
            }
            lineHeight = Math.round(r * ctx.getFontHeight() / 100.0f);
        }
        else
        {
            float r = DEFAULT_LINE_HEIGHT; 
            try {
                r = Float.valueOf(lh);
            } catch (NumberFormatException e) {
            }
            lineHeight = Math.round(r * ctx.getFontHeight());
        }
        
        //background
        String bg = getStyleProperty("background-color");
        if (!bg.equals("") && !bg.equals("transparent"))
        {
            bgcolor = ctx.getColor(bg);
            if (bgcolor == null) bgcolor = Color.white; //couldn't parse, use white
        }
        else
            bgcolor = null;
        
    }
    
}
