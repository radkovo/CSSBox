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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * A box that represents a single table row. 
 * @author burgetr
 */
public class TableRowBox extends BlockBox
{
    protected Vector<TableCellBox> cells;
    protected Iterator<TableCellBox> cursor;
    protected TableBodyBox ownerBody;

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
        cell.setOwnerRow(this);
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
    
    /**
	 * @return the ownerBody
	 */
	public TableBodyBox getOwnerBody()
	{
		return ownerBody;
	}

	/**
	 * @param ownerBody the ownerBody to set
	 */
	public void setOwnerBody(TableBodyBox ownerBody)
	{
		this.ownerBody = ownerBody;
	}

    //=====================================================================================
    
    @Override
    public void computeEfficientMargins()
    {
        //no margin collapsing with the contents for table rows
        emargin = new LengthSet(margin);
    }
	
	@Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        //do nothing (table line must be laid out other way, through the table body)
        return true;
    }
    
    @Override
    public void absolutePositions()
    {
        updateStackingContexts();
        final Rectangle cblock = getAbsoluteContainingBlock();
        int x = cblock.x + bounds.x;
        int y = cblock.y + bounds.y;

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
                child.updateStackingContexts();
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
	        margin = new LengthSet(); //internal table cells do not have margins
	        emargin = margin;
	        declMargin = margin;
	        padding = new LengthSet();
	        border = new LengthSet(); //borders are ignored for rows
            min_size = new Dimension(-1, -1);
            max_size = new Dimension(-1, -1);
            coords = new LengthSet();
    	}
    	//row occupies the whole body width
    	content.width = getContainingBlock().width;
    }

    @Override
    protected boolean separatedFromTop(ElementBox box)
    {
        return true;
    }

    @Override
    protected boolean separatedFromBottom(ElementBox box)
    {
        return true;
    }

    @Override
	public void drawBackground(Graphics2D g)
	{
    	//table body cannot have borders
    	//the background is drawn in the individual cells
	}
    
    //=====================================================================================
    
    /**
     * Goes through the list of child boxes and creates the anonymous rows if necessary.
     */
    private void organizeContent()
    {
        cells = new Vector<TableCellBox>();
        TableCellBox anoncell = null;
        
        int size = nested.size();
        List<Box> toremove = new ArrayList<Box>();
        for (int i = 0; i < size; i++)
        {
            Box box = nested.get(i);
            
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
                    Element anonelem = viewport.getFactory().createAnonymousElement(getParent().getParent().getParent().getElement().getOwnerDocument(), "td", "table-cell");
                    anoncell = new TableCellBox(anonelem, g, ctx);
                    anoncell.adoptParent(this);
                    anoncell.setStyle(viewport.getFactory().createAnonymousStyle("table-cell"));
                    addCell(anoncell);
                }
                anoncell.addSubBox(box);
                anoncell.isempty = false;
                if (box.isBlock()) anoncell.contblock = true;
                box.setContainingBlockBox(anoncell);
                box.setParent(anoncell);
                toremove.add(box);
                endChild--;
            }
        }
        nested.removeAll(toremove);
        
        if (anoncell != null)
        {
            anoncell.endChild = anoncell.nested.size();
            addSubBox(anoncell);
        }
    }
    
}
