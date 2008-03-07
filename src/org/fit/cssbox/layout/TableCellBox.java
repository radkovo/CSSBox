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
import java.awt.Graphics;

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
    public TableCellBox(Element n, Graphics g, VisualContext ctx)
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
        
        //Borders
        if (!update) //borders needn't be updated
        {
            String medium = "3px";
            border = new LengthSet();
            if (borderVisible("top"))
                    border.top = dec.getLength(getStyleProperty("border-top-width"), medium, "0", 0);
            else
                    border.top = 0;
            if (borderVisible("right"))
                    border.right = dec.getLength(getStyleProperty("border-right-width"), medium, "0", 0);
            else
                    border.right = 0;
            if (borderVisible("bottom"))
                    border.bottom = dec.getLength(getStyleProperty("border-bottom-width"), medium, "0", 0);
            else
                    border.bottom = 0;
            if (borderVisible("left"))
                    border.left = dec.getLength(getStyleProperty("border-left-width"), medium, "0", 0);
            else
                    border.left = 0;
        }
        
        //Padding
        padding = new LengthSet();
        padding.top = dec.getLength(getStyleProperty("padding-top"), "0", "0", contw);
        padding.right = dec.getLength(getStyleProperty("padding-right"), "0", "0", contw);
        padding.bottom = dec.getLength(getStyleProperty("padding-bottom"), "0", "0", contw);
        padding.left = dec.getLength(getStyleProperty("padding-left"), "0", "0", contw);
        
        //Content and margins
        if (!update)
        {
            content = new Dimension(0, 0);
            margin = new LengthSet();
            emargin = new LengthSet();
            min_size = new Dimension(-1, -1);
            max_size = new Dimension(-1, -1);
        }
        
        //Load the width if set
        String width = getElement().getAttribute("width").trim(); //try to load from attribute
        if (!width.equals(""))
        {
            if (!width.endsWith("%"))
                width = width + "px";
        }
        else
            width = getStyleProperty("width"); //no attribute set - use the style
        if (width.equals("") || width.equals("auto"))
        {
            wset = false;
        }
        else
        {
            wset = true;
            if (!update)
                content.width = dec.getLength(width, "0", "0", contw);
            if (width.endsWith("%"))
            {
            	wrelative = true;
            	try {
            		percent = Integer.parseInt(width.substring(0, width.length()-1));
            	} catch (NumberFormatException e) {
            		wrelative = false;
            		percent = 0;
            	}
            }
        }
        
        //Load the height if set
        String height = getElement().getAttribute("height").trim(); //try to load from attribute
        if (!height.equals(""))
        {
            if (!height.endsWith("%"))
                width = width + "px";
        }
        else
            height = getStyleProperty("height");
        if (cblock.hset)
        {
            hset = (!height.equals("auto") && !height.equals(""));
            if (!update)
                content.height = dec.getLength(height, "0", "0", cblock.getContentHeight());
        }
        else
        {
            hset = (!height.equals("auto") && !height.equals("") && !height.endsWith("%"));
            if (!update)
                content.height = dec.getLength(height, "0", "0", 0);
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
