/*
 * TableRowBox.java
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
        //do nothing (table line must be laid out other way, through the table body)
        return true;
    }
    
    @Override
    public void absolutePositions()
    {
        int x = cblock.getAbsoluteContentX() + bounds.x;
        int y = cblock.getAbsoluteContentY() + bounds.y;

        if (position == POS_RELATIVE)
        {
            x += leftset ? coords.left : (-coords.right);
            y += topset ? coords.top : (-coords.bottom);
        }
            
        //set the absolute coordinates
        absbounds.x = x;
        absbounds.y = y;
        absbounds.width = bounds.width;
        absbounds.height = bounds.height;
        
        //Compute the absolute positions as for in-flow boxes. Ignore floating.
        if (isDisplayed())
        {
            //for (int i = startChild; i < endChild; i++)
            for (TableCellBox child : cells)
            {
                //BlockBox child = (BlockBox) getSubBox(i);
                x = getAbsoluteContentX() + child.getBounds().x;
                y = getAbsoluteContentY() + child.getBounds().y;

                if (child.position == POS_RELATIVE)
                {
                    x += child.leftset ? child.coords.left : (-child.coords.right);
                    y += child.topset ? child.coords.top : (-child.coords.bottom);
                }
                    
                child.absbounds.x = x;
                child.absbounds.y = y;
                child.absbounds.width = child.bounds.width;
                child.absbounds.height = child.bounds.height;
                
                for (int j = child.getStartChild(); j < child.getEndChild(); j++)
                    child.getSubBox(j).absolutePositions();
            }
        }

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
    
    /**
     * Goes through the list of child boxes and creates the anonymous rows if necessary.
     */
    private void organizeContent()
    {
        cells = new Vector<TableCellBox>();
        TableCellBox anoncell = null;
        
        for (Iterator<Box> it = nested.iterator(); it.hasNext(); )
        {
            Box box = it.next();
            if (box instanceof TableCellBox)
            {
                addCell((TableCellBox) box);
                //finish and add possible previous anonymous cell
                if (anoncell != null)
                {
                    anoncell.endChild = anoncell.nested.size();
                    addSubBox(anoncell);
                }
                anoncell = null;
            }
            else
            {
                if (anoncell == null)
                {
                    Element anonelem = BoxFactory.getInstance().createAnonymousElement(getParent().getParent().getParent().getElement().getOwnerDocument(), "td", "table-cell");
                    anoncell = new TableCellBox(anonelem, g, ctx);
                    anoncell.setStyle(BoxFactory.getInstance().createAnonymousStyle("table-cell"));
                    anoncell.adoptParent(this);
                    addCell(anoncell);
                }
                anoncell.addSubBox(box);
                anoncell.isempty = false;
                if (box.isBlock()) anoncell.contblock = true;
                box.setContainingBlock(anoncell);
                box.setParent(anoncell);
                it.remove();
                endChild--;
            }
        }
        if (anoncell != null)
        {
            anoncell.endChild = anoncell.nested.size();
            addSubBox(anoncell);
        }
    }
    
}
