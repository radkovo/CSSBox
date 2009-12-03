/*
 * ElementBox.java
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
    
    /** Effective top and bottom margins (after collapsing with the contained boxes) */
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
    
    /** baseline offset */
    private int baseline;
    
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
        baseline = src.baseline;
        
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
     * Inserts a new sub box before a specified sub box
     * @param where the box already existing in the list
     * @param what the new box to add
     */
    public void insertSubBoxBefore(Box where, Box what)
    {
        int pos = nested.indexOf(where);
        nested.insertElementAt(what, pos);
        endChild++;
    }

    /**
     * Inserts a new sub box after a specified sub box
     * @param where the box already existing in the list
     * @param what the new box to add
     */
    public void insertSubBoxAfter(Box where, Box what)
    {
        int pos = nested.indexOf(where);
        nested.insertElementAt(what, pos+1);
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
    
    //=======================================================================

    /**
     * This method is called for all the element boxes once the box tree is finished.
     * It is the right place for internal object initializing, content organization, etc. 
     */
    public void initBox()
    {
    }
    
    /**
     * Computes the distance of the content from the left edge of the whole box
     * (a convenience function for margin + border + padding).
     * @return the distance 
     */
    public int getContentOffsetX()
    {
        return margin.left + border.left + padding.left;
    }
    
    /**
     * Computes the distance of the content from the top edge of the whole box. 
     * (a convenience function for margin + border + padding).
     * @return the distance
     */
    public int getContentOffsetY()
    {
        return margin.top + border.top + padding.top;
    }
    
    public int getContentX()
    {
        return bounds.x + margin.left + border.left + padding.left;
    }
    
    public int getContentY()
    {
        return bounds.y + margin.top + border.top + padding.top;
    }
    
    public int getAbsoluteContentX()
    {
        return absbounds.x + margin.left + border.left + padding.left;
    }
    
    public int getAbsoluteContentY()
    {
        return absbounds.y + margin.top + border.top + padding.top;
    }
    
    public int totalWidth()
    {
        return margin.left + border.left + padding.left + content.width +
            padding.right + border.right + margin.right;
    }
    
    //totalHeight() differs for inline and block boxes
    
    public int getAvailableContentWidth()
    {
        return availwidth - margin.left - border.left - padding.left 
                  - padding.right - border.right - margin.right;
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
        return new Rectangle(absbounds.x + margin.left + border.left,
                             absbounds.y + margin.top + border.top,
                             content.width + padding.left + padding.right,
                             content.height + padding.top + padding.bottom);
    }

    /**
     * @return the bounds of the border - the content, padding and border
     */
    public Rectangle getAbsoluteBorderBounds()
    {
        return new Rectangle(absbounds.x + margin.left,
                             absbounds.y + margin.top,
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
        for (int i = startChild; i < endChild; i++)
            if (!getSubBox(i).isWhitespace())
                return false;
        return true;
    }
        
    @Override
    public int getBaselineOffset()
    {
        return baseline;
    }
    
    @Override
    public int getMaxBaselineOffset()
    {
        int max = baseline; //current box offset is the minimum
        //find the maximum of the baseline offsets of the subboxes
        for (int i = startChild; i < endChild; i++)
            if (getSubBox(i).getMaxBaselineOffset() > max)
                max = getSubBox(i).getMaxBaselineOffset();
        return max;
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

        //border bounds
        int bx1 = x + margin.left;
        int by1 = y + margin.top;
        int bx2 = x2 - margin.right;
        int by2 = y2 - margin.bottom;
        
        //draw the border
        if (border.top > 0)
            drawBorder(g, bx1, by1, bx2, by1, border.top, 0, border.top/2, "top");
        if (border.right > 0)
            drawBorder(g, bx2, by1, bx2, by2, border.right, -border.right/2 + 1, 0, "right"); 
        if (border.bottom > 0)
            drawBorder(g, bx1, by2, bx2, by2, border.bottom, 0, -border.bottom/2 + 1, "bottom"); 
        if (border.left > 0)
            drawBorder(g, bx1, by1, bx1, by2, border.left, border.left/2, 0, "left"); 
        
        //Background
        int bgx = x + margin.left + border.left;
        int bgy = y + margin.top + border.top;
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
                            int right, int down, String side)
    {
        TermColor tclr = style.getValue(TermColor.class, "border-"+side+"-color");
        CSSProperty.BorderStyle bst = style.getProperty("border-"+side+"-style");
        if (tclr != null && bst != CSSProperty.BorderStyle.HIDDEN)
        {
            Color clr = tclr.getValue();
            if (clr == null) clr = Color.BLACK;
            g.setColor(clr);
            
            if (bst == CSSProperty.BorderStyle.SOLID)
            {
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1));
                g.drawLine(x1 + right, y1 + down, x2 + right, y2 + down);
            }
            else if (bst == CSSProperty.BorderStyle.DOTTED)
            {
                float dash[] = {width, width};
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
                g.drawLine(x1 + right, y1 + down, x2 + right, y2 + down);
            }
            else if (bst == CSSProperty.BorderStyle.DASHED)
            {
                float dash[] = {3*width, width};
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
                g.drawLine(x1 + right, y1 + down, x2 + right, y2 + down);
            }
            else if (bst == CSSProperty.BorderStyle.DOUBLE)
            {
                int sw = (width + 2) / 3;
                int gw = width - 2 * sw;
                int gwr = (right == 0) ? 0 : (gw+1) / 2 + (sw+1) / 2;
                int gwd = (down == 0) ? 0 : (gw+1) / 2 + (sw+1) / 2;
                g.setStroke(new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1));
                g.drawLine(x1 + right - gwr, y1 + down - gwd, x2 + right - gwr, y2 + down - gwd);
                g.drawLine(x1 + right + gwr, y1 + down + gwd, x2 + right + gwr, y2 + down + gwd);
            }
            else //default or unsupported - draw a solid line
            {
                g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1));
                g.drawLine(x1 + right, y1 + down, x2 + right, y2 + down);
            }
        }
    }

    @Override
    public void drawExtent(Graphics2D g)
    {
    	//draw the full box
        g.setColor(Color.RED);
        g.drawRect(absbounds.x, absbounds.y, bounds.width, bounds.height);
    	
    	//draw the content box
        g.setColor(Color.ORANGE);
        g.drawRect(getAbsoluteContentX(), getAbsoluteContentY(), getContentWidth(), getContentHeight());
        
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
     * Computes efficient top and bottom margins for collapsing.
     */
    abstract public void computeEfficientMargins();
    
    /**
     * Checks if the element's own top and bottom margins are adjoining 
     * according to the CSS specifiaction.
     * @return <code>true</code> if the margins are adjoining
     */
    abstract public boolean marginsAdjoin();
    
    /**
     * Load the basic style from the CSS properties. This includes the display
     * properties, floating, positioning, color and font properties.
     */
    protected void loadBasicStyle()
    {
        ctx.updateForGraphics(style, g);
        
        display = style.getProperty("display");
        if (display == null) display = CSSProperty.Display.INLINE;
        
        CSSProperty.Float floating = style.getProperty("float");
        if (floating == null) floating = BlockBox.FLOAT_NONE;
        CSSProperty.Position position = style.getProperty("position");
        if (position == null) position = BlockBox.POS_STATIC;
        
        //apply combination rules
        //http://www.w3.org/TR/CSS21/visuren.html#dis-pos-flo
        if (display == ElementBox.DISPLAY_NONE)
        {
            position = BlockBox.POS_STATIC;
            floating = BlockBox.FLOAT_NONE;
        }
        else if (position == BlockBox.POS_ABSOLUTE || position == BlockBox.POS_FIXED)
        {
            floating = BlockBox.FLOAT_NONE;
        }
        //compute the display computed value
        if (floating != BlockBox.FLOAT_NONE || position == BlockBox.POS_ABSOLUTE || isRootElement())
        {
            if (display == DISPLAY_INLINE_TABLE)
                display = DISPLAY_TABLE;
            else if (display == DISPLAY_INLINE ||
                     display == DISPLAY_RUN_IN ||
                     display == DISPLAY_TABLE_ROW_GROUP ||
                     display == DISPLAY_TABLE_COLUMN ||
                     display == DISPLAY_TABLE_COLUMN_GROUP ||
                     display == DISPLAY_TABLE_HEADER_GROUP ||
                     display == DISPLAY_TABLE_FOOTER_GROUP ||
                     display == DISPLAY_TABLE_ROW ||
                     display == DISPLAY_TABLE_CELL ||
                     display == DISPLAY_TABLE_CAPTION ||
                     display == DISPLAY_INLINE_BLOCK)
                display = DISPLAY_BLOCK;
        }

        isblock = (display == DISPLAY_BLOCK);
        displayed = (display != DISPLAY_NONE && display != DISPLAY_TABLE_COLUMN);
        visible = (style.getProperty("visibility") != CSSProperty.Visibility.HIDDEN); 
        
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
            lineHeight = (int) ctx.pxLength(len, ctx.getFontHeight()); 
        }
        else //must be INTEGER or NUMBER
        {
            Term<?> len = style.getValue("line-height", true);
            float r;
            if (len instanceof TermInteger)
                r = ((TermInteger) len).getValue();
            else
                r = ((TermNumber) len).getValue();
            lineHeight = (int) Math.round(r * ctx.getFontHeight());
        }
        baseline = ctx.getBaselineOffset() + ((lineHeight - ctx.getFontHeight()) / 2);  //add half-leading to the baseline

        //background
        loadBackground();
    }
    
    /**
     * Loads the background information from the style
     */
    protected void loadBackground()
    {
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
