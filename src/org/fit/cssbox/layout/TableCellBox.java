/**
 * TableCellBox.java
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
 * Created on 29.9.2006, 14:15:23 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Dimension;
import java.awt.Graphics2D;

import cz.vutbr.web.css.*;
import cz.vutbr.web.css.TermNumeric.Unit;

import org.fit.cssbox.css.HTMLNorm;
import org.w3c.dom.Element;

/**
 * A box that represents a single table cell.
 * @author burgetr
 */
public class TableCellBox extends BlockBox
{
    protected int colspan;
    protected int rowspan;
    
    /** first row of this cell in the body */
    protected int row;
    /** first collumn of this cell in the row */
    protected int column; 
    
    /** relative width [%] when used */
    protected int percent;
    
    //====================================================================================
    
    /**
     * Create a new table cell
     */
    public TableCellBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
        loadAttributes();
        fleft = new FloatList(this);
        fright = new FloatList(this);
        overflow = OVERFLOW_HIDDEN; //just for enclosing the contained floating boxes
    }

    /**
     * Create a new table cell from an inline box
     */
    public TableCellBox(InlineBox src)
    {
        super(src);
        isblock = true;
        loadAttributes();
        fleft = new FloatList(this);
        fright = new FloatList(this);
        overflow = OVERFLOW_HIDDEN; //just for enclosing the contained floating boxes
    }
    
    /**
     * @return the column span
     */
    public int getColspan()
    {
        return colspan;
    }

    /**
     * @return the row span
     */
    public int getRowspan()
    {
        return rowspan;
    }
    
    /**
     * @return the row of this cell in the table body
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return the column of this cell in the row
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Set the index of the first row and column of this cell in the row
     * @param column the column index to set
     * @param row the row index to set
     */
    public void setCellPosition(int column, int row)
    {
        this.column = column;
        this.row = row;
    }
    
    /**
     * Set the total width of the cell. The content width is computed automatically.
     * @param width the width to be set
     */
    public void setWidth(int width)
    {
        content.width = width - border.left - padding.left - padding.right - border.right;
        bounds.width = width;
        wset = true;
        updateChildSizes();
    }
    
    /**
     * Set the total width of the cell. The content width is computed automatically.
     * @param height the height to be set
     */
    public void setHeight(int height)
    {
        content.height = height - border.top - padding.top - padding.bottom - border.bottom;
        bounds.height = height;
        hset = true;
    }
    
    /**
     * @return the percentage when the width is specified relatively
     */
    public int getPercent()
    {
        return percent;
    }

    public String toString()
    {
        return super.toString() + "[" + column + "," + row + "]";
    }
    
    //====================================================================================

    public int getMinimalWidth()
    {
        int ret = getMinimalContentWidth();
        /*if (!wrelative && hasFixedWidth() && content.width > ret)
            ret = content.width;*/
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }

    public int getMaximalWidth()
    {
        int ret = getMaximalContentWidth();
        /*if (!wrelative && hasFixedWidth())
            ret = content.width;*/
        //increase by margin, padding, border
        ret += margin.left + padding.left + border.left +
               margin.right + padding.right + border.right;
        return ret;
    }
    
    @Override
    protected void loadSizes(boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        //containing box sizes
        if (cblock == null)
            { System.err.println(toString() + " has no cblock"); return; }
        int contw = cblock.getContentWidth();
        
        TermLength zero = CSSFactory.getTermFactory().createLength(0.0F, Unit.px);
        
        //Borders
        if (!update) //borders needn't be updated
        {
            TermLength medium = CSSFactory.getTermFactory().createLength(3.0F, Unit.px);
            border = new LengthSet();
            if (borderVisible("top"))
                    border.top = dec.getLength(getLengthValue("border-top-width"), false, medium, zero, 0);
            else
                    border.top = 0;
            if (borderVisible("right"))
                    border.right = dec.getLength(getLengthValue("border-right-width"), false, medium, zero, 0);
            else
                    border.right = 0;
            if (borderVisible("bottom"))
                    border.bottom = dec.getLength(getLengthValue("border-bottom-width"), false, medium, zero, 0);
            else
                    border.bottom = 0;
            if (borderVisible("left"))
                    border.left = dec.getLength(getLengthValue("border-left-width"), false, medium, zero, 0);
            else
                    border.left = 0;
        }
        
        //Padding
        padding = new LengthSet();
        padding.top = dec.getLength(getLengthValue("padding-top"), false, zero, zero, contw);
        padding.right = dec.getLength(getLengthValue("padding-right"), false, zero, zero, contw);
        padding.bottom = dec.getLength(getLengthValue("padding-bottom"), false, zero, zero, contw);
        padding.left = dec.getLength(getLengthValue("padding-left"), false, zero, zero, contw);
        
        //Content and margins
        if (!update)
        {
            content = new Dimension(0, 0);
            margin = new LengthSet();
            emargin = new LengthSet();
            min_size = new Dimension(-1, -1);
            max_size = new Dimension(-1, -1);
            coords = new LengthSet();
        }
        
        //Load the width if set
        CSSProperty.Width wprop = null;
        TermLengthOrPercent width = null; 
        String widthattr = getElement().getAttribute("width"); //try to load from attribute
        if (!widthattr.equals(""))
        {
            width = HTMLNorm.createLengthOrPercent(widthattr);
            if (width != null)
                wprop = width.isPercentage() ? CSSProperty.Width.percentage : CSSProperty.Width.length;
        }
        else //no attribute set - use the style
        {
            wprop = style.getProperty("width");
            width = getLengthValue("width");
        }
        
        if (wprop == null || wprop == CSSProperty.Width.AUTO)
        {
            wset = false;
        }
        else
        {
            wset = true;
            if (!update)
                content.width = dec.getLength(width, false, 0, 0, contw);
            if (width.isPercentage())
            {
            	wrelative = true;
            	percent = (int) Math.round(width.getValue());
            }
        }
        
        //Load the height if set
        String heightattr = getElement().getAttribute("height"); //try to load from attribute
        CSSProperty.Height hprop = null;
        TermLengthOrPercent height = null;
        if (!heightattr.equals(""))
        {
            height = HTMLNorm.createLengthOrPercent(widthattr);
            if (height != null)
                hprop = height.isPercentage() ? CSSProperty.Height.percentage : CSSProperty.Height.length;
        }
        else
        {
            hprop = style.getProperty("height");
            height = getLengthValue("height");
        }

        boolean hauto = (hprop == null || hprop == CSSProperty.Height.AUTO); 
        if (cblock.hset)
        {
            hset = !hauto;
            if (!update)
                content.height = dec.getLength(height, hauto, 0, 0, cblock.getContentHeight());
        }
        else
        {
            hset = (!hauto && !height.isPercentage());
            if (!update)
                content.height = dec.getLength(height, hauto, 0, 0, 0);
        }
    }

	@Override
	public boolean hasFixedWidth()
	{
		//return wset && !isRelative(); //for cells, the width cannot be computed from the containing box now
	    //return false; //we can never be sure for table cells, depends on column widths
		return true; //the width is set by the column
	}

	@Override
	public boolean hasFixedHeight()
	{
		return false;
	}
	
	/**
     * Loads the important values from the element attributes.
     */
    protected void loadAttributes()
    {
        try {
            if (!el.getAttribute("colspan").equals(""))
                colspan = Integer.parseInt(el.getAttribute("colspan"));
            else
                colspan = 1;
            if (!el.getAttribute("rowspan").equals(""))
                rowspan = Integer.parseInt(el.getAttribute("rowspan"));
            else
                rowspan = 1;
        } catch (NumberFormatException e) {
            System.err.println("Invalid width value: " + e.getMessage());
        }
    }
    
}
