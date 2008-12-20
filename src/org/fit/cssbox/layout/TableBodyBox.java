/**
 * TableBodyBox.java
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
 * Created on 29.9.2006, 14:13:58 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.util.*;

import org.w3c.dom.Element;

/**
 * A class that represents a group of table lines - the table body, header or footer.
 * @author burgetr
 */
public class TableBodyBox extends BlockBox
{
    /** The row boxes contained inside */
    protected Vector<TableRowBox> rows;
    /** Number of columns inside */
    protected int numCols;
    /** array of cells */
    protected TableCellBox[][] cells;
    /** cell spacing */
    protected int spacing = 2;
    
    //====================================================================================
    
    /**
     * Create a new table body
     */
    public TableBodyBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
        rows = new Vector<TableRowBox>();
    }

    /**
     * Create a new table body from an inline box
     */
    public TableBodyBox(InlineBox src)
    {
        super(src);
        isblock = true;
        rows = new Vector<TableRowBox>();
    }
    
    /**
     * Add a new row to the table body.
     * @param row the new row
     */
    public void addRow(TableRowBox row)
    {
        rows.add(row);
    }
    
    /**
     * @return the number of rows in the body
     */
    public int getRowCount()
    {
        return rows.size();
    }
    
    /**
     * @return the number of columns in the body (the longest row)
     */
    public int getColumnCount()
    {
        return numCols;
    }
    
    /**
     * Returns a particular row by index.
     * @param index row index
     * @return the row
     */
    public TableRowBox getRow(int index)
    {
        return rows.elementAt(index);
    }

    /**
     * Sets the cell spacing
     */
    public void setSpacing(int spacing)
    {
    	this.spacing = spacing;
    }
    
    //====================================================================================
    
    @Override
    public int getMaximalWidth()
    {
        int sum = spacing;
        for (int i = 0; i < numCols; i++)
            sum += getMaximalColumnWidth(i) + spacing;
        return sum;
    }

    @Override
    public int getMinimalWidth()
    {
        int sum = spacing;
        for (int i = 0; i < numCols; i++)
            sum += getMinimalColumnWidth(i) + spacing;
        return sum;
    }
    
    /**
     * Determine the minimal width of the column
     * @param col the column index
     * @return the minimal width of the column
     */ 
    public int getMinimalColumnWidth(int col)
    {
        int ret = 0;
        int r = 0;
        while (r < getRowCount())
        {
            TableCellBox cell = cells[col][r];
            if (cell != null)
            {
                int min = cell.getMinimalWidth() / cell.getColspan();
                if (min > ret) ret = min;
                r += cell.getRowspan();
            }
            else
                r++;
        }
        return ret;
    }

    /**
     * Determine the maximal width of the column
     * @param col the column index
     * @return the minimal width of the column
     */ 
    public int getMaximalColumnWidth(int col)
    {
        int ret = 0;
        int r = 0;
        while (r < getRowCount())
        {
            TableCellBox cell = cells[col][r];
            if (cell != null)
            {
                int max = cell.getMaximalWidth() / cell.getColspan();
                if (max > ret) ret = max;
                r += cell.getRowspan();
            }
            else
                r++;
        }
        return ret;
    }
    
    /**
     * Checks the maximal and minimal width of the column, if the column has fixed width 
     * and if it is relative. Updates the appropriate information in the TableColumn 
     * structure.
     * @param c the column index
     * @param col the column to be updated
     */ 
    public void updateColumn(int c, TableColumn col)
    {
        int r = 0;
        while (r < getRowCount())
        {
            TableCellBox cell = cells[c][r];
            if (cell != null)
            {
                //minimal width
                int min = cell.getMinimalWidth() / cell.getColspan();
                if (min > col.getMinimalWidth())
                    col.setMinimalWidth(min);
                //maximal width
                int max = cell.getMaximalWidth() / cell.getColspan();
                if (max > col.getMaximalWidth())
                    col.setMaximalWidth(max);
                //fixed width and percentages
                if (cell.wset) 
                {
                    col.wset = true;
                    if (cell.isRelative())
                    {
                        col.setRelative(true);
                        if (col.percent < cell.percent)
                            col.percent = cell.percent;
                    }
                    else
                    {
                        if (cell.getContentWidth() > col.abswidth)
                            col.abswidth = cell.getContentWidth();
                    }
                }
                //ensure the minimal width
                if (col.getWidth() < col.getMinimalWidth())
                    col.setColumnWidth(col.getMinimalWidth());
                r += cell.getRowspan();
            }
            else
                r++;
        }
    }
    
    //====================================================================================
    
    @Override
    public void initBox()
    {
        calcOffsets();
    }
    
    @Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        return true;
    }
    
    public boolean doLayout(int widthlimit, Vector<TableColumn> columns)
    {
        setAvailableWidth(widthlimit);

        int y = spacing;
        int x = spacing;
        int maxw = 0;
        int maxh = 0;
        int wlimit = getAvailableContentWidth();
        
        /*System.out.println("Table body " + getColumnCount() + "x" + getRowCount());
        for (int r = 0; r < rows.size(); r++)
        {
            for (int c = 0; c < numCols; c++)
                System.out.print("| " + cells[c][r]);
            System.out.println(" |");
        }*/

        int rowY[] = new int[getRowCount()]; //Y offests of the rows
        
        for (int r = 0; r < getRowCount(); r++)
        {
            TableRowBox row = getRow(r);
            
            x = spacing;
            maxh = 0;
            int c = 0;
            while (c < getColumnCount())
            {
                TableCellBox cell = cells[c][r];
                if (cell != null)
                {
                    int firstrow = cell.getRow();
                    int lastrow = cell.getRow() + cell.getRowspan() - 1;
                    //compute cell width according to span
                    int cw = columns.elementAt(c).getWidth();
                    for (int i = 1; i < cell.getColspan(); i++)
                        cw += spacing + columns.elementAt(c+i).getWidth();
                    cell.setWidth(cw);
                    //compute the position
                    if (r == firstrow)
                    {
                        cell.doLayout(wlimit, true, true);
                        cell.setPosition(x, 0);
                        //int ch = cell.getHeight() / cell.getRowspan();
                        if (cell.getRowspan() == 1)
                        {
                        	int ch = cell.getHeight();
                        	if (ch > maxh) maxh = ch;
                        }
                    }
                    else if (r < lastrow)
                    {
                        //int ch = cell.getHeight() / cell.getRowspan();
                        if (cell.getRowspan() == 1)
                        {
                        	int ch = cell.getHeight();
                        	if (ch > maxh) maxh = ch;
                        }
                    }
                    else if (r == lastrow) 
                    {
                        //use the remaining height of the cell
                        //int rh = y - cell.getContainingBlock().bounds.x;
                        int startY = rowY[cell.getRow()];
                        int remain = cell.getHeight() - (y - startY); 
                        if (remain > maxh) maxh = remain;
                    }
                    x += cw + spacing;
                    c += cell.getColspan();
                }
                else
                    c++;
            }
            
            //enlarge all the cells to the row height (maxh)
            c = 0;
            while (c < getColumnCount())
            {
                TableCellBox cell = cells[c][r];
                if (cell != null)
                {
                    if (cell.getRow()+cell.getRowspan()-1 == r) //if ends on this line
                    {
                        int startY;
                        if (cell.getRowspan() > 1)
                            startY = rowY[cell.getRow()];
                        else
                            startY = y;
                        cell.setHeight(y + maxh - startY);
                    }
                    c += cell.getColspan();
                }
                else
                    c++;
            }
            
            //set the row size
            rowY[r] = y;
            row.setPosition(0, y);
            row.content.width = x;
            row.content.height = maxh;
            row.setSize(row.totalWidth(), row.totalHeight());
            if (x > maxw) maxw = x;
            y += maxh + spacing;
        }
        content.width = maxw;
        content.height = y;
        setSize(totalWidth(), totalHeight());
        return true;
    }
    
    //====================================================================================
    
    /**
     * Goes through the list of child boxes and organizes them into captions, header,
     * footer, etc.
     */
    private void organizeContent()
    {
        for (int i = 0; i < getSubBoxNumber(); i++)
        {
            Box box = getSubBox(i);
            if (box instanceof ElementBox &&
                ((ElementBox) box).getDisplay() == ElementBox.DISPLAY_TABLE_ROW)
                addRow((TableRowBox) box);
        }
    }
                
    /** 
     * Calculates new cell positions regarding the rowspans 
     */
    private void calcOffsets()
    {
        //collect the table rows
        organizeContent();
        
        //Find the longest line
        int rowidx[] = new int[rows.size()];
        int maxCells = 0;
        for (int r = 0; r < rows.size(); r++)
        {
            int count = rows.elementAt(r).getCellCount();
            if (count > maxCells) maxCells = count;
            rowidx[r] = 0;
            rows.elementAt(r).rewind();
        }
        
        //determine the cell positions
        boolean cell_found = true;
        while (cell_found)
        {
            cell_found = false;
            int r = 0;
            while (r < rows.size())
            {
                TableRowBox row = rows.elementAt(r);
                if (row.hasNext())
                {
                    cell_found = true;
                    //get the current element
                    TableCellBox cell = row.next();
                    //set the new position
                    cell.setCellPosition(rowidx[r], r);
                    //move the row indices
                    for (int nr = r; nr < r + cell.getRowspan(); nr++)
                    {
                        if (nr < rows.size())
                        {
                            rowidx[nr] += cell.getColspan();
                            if (rowidx[nr] > numCols) numCols = rowidx[nr]; 
                        }
                        else
                            cell.rowspan--;
                    }
                    r += cell.getRowspan();
                }
                else
                    r++;
            }                
        }    
        //build the cell array
        for (int i = 0; i < rows.size(); i++)
            rows.elementAt(i).rewind();
        cells = new TableCellBox[numCols][rows.size()];
        for (int c = 0; c < maxCells; c++)
        {
            for (int r = 0; r < rows.size(); r++)
            {
                TableRowBox row = rows.elementAt(r);
                if (row.hasNext())
                {
                    TableCellBox cell = row.next();
                    //add it to the mesh
                    for (int nr = cell.getRow(); nr < cell.getRow() + cell.getRowspan(); nr++)
                        for (int nc = cell.getColumn(); nc < cell.getColumn() + cell.getColspan(); nc++)
                            cells[nc][nr] = cell;
                }
            }
        }
    }
    
}
