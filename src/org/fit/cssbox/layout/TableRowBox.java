/*
 * TableRowBox.java
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
 * Created on 29.9.2006, 14:14:48 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.*;
import java.util.*;

import org.w3c.dom.Element;

/**
 * A box that represents a single table row. 
 * @author burgetr
 */
public class TableRowBox extends BlockBox
{
    protected Vector<TableCellBox> cells;
    protected Iterator<TableCellBox> cursor;

    //====================================================================================
    
    /**
     * Create a new table row
     */
    public TableRowBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
    }

    /**
     * Create a new table row from an inline box
     */
    public TableRowBox(InlineBox src)
    {
        super(src);
        isblock = true;
    }
    
    /**
     * Add a new cell to the table row.
     * @param cell the new cell
     */
    public void addCell(TableCellBox cell)
    {
        if (cells == null) 
            organizeContent();
        cells.add(cell);
    }
    
    /**
     * @return the number of cells in the row
     */
    public int getCellCount()
    {
        if (cells == null) organizeContent();
        return cells.size();
    }
    
    /**
     * Returns a particular cell by its index in the row
     * @param index row index
     * @return the row
     */
    public TableCellBox getCell(int index)
    {
        if (cells == null) organizeContent();
        return cells.elementAt(index);
    }

    /**
     * Points the cursor to the begining
     */
    public void rewind()
    {
        cursor = cells.iterator();
    }
    
    /**
     * Reads a next cell and moves the cursor
     * @return the value of the next cell
     */
    public TableCellBox next()
    {
        if (cursor == null) rewind();
        return cursor.next();
    }
    
    /**
     * @return true if there is a next cell to read
     */
    public boolean hasNext()
    {
        if (cursor == null) rewind();
        return cursor.hasNext();
    }
    
    //=====================================================================================
    
    @Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        //do nothing (table line must be laid out other way)
        return true;
    }
    
    @Override
    protected void loadSizes(boolean update)
    {
    	if (!update)
    	{
	        content = new Dimension(0, 0);
	        bounds = new Rectangle(0, 0, 0, 0);
	        margin = new LengthSet();
	        emargin = margin;
	        padding = new LengthSet();
	        border = new LengthSet();
            min_size = new Dimension(-1, -1);
            max_size = new Dimension(-1, -1);
            coords = new LengthSet();
    	}
    }
    
    //=====================================================================================
    
    private void organizeContent()
    {
        cells = new Vector<TableCellBox>();
        for (int i = 0; i < getSubBoxNumber(); i++)
        {
            Box box = getSubBox(i);
            if (box instanceof TableCellBox)
                addCell((TableCellBox) box);
        }
    }

}
