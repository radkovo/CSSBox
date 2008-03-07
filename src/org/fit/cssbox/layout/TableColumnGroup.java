/*
 * TableColumnGroup.java
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
 * Created on 3.10.2006, 10:51:03 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.*;
import java.util.*;

import org.w3c.dom.Element;


/**
 * @author burgetr
 *
 */
public class TableColumnGroup extends TableColumn
{
    private Vector<TableColumn> columns;
    
    //===============================================================================
    
    /**
     * Create a new table column
     */
    public TableColumnGroup(Element n, Graphics g, VisualContext ctx)
    {
        super(n, g, ctx);
    }
    
    /**
     * Create a new table cell from an inline box
     */
    public TableColumnGroup(InlineBox src)
    {
        super(src);
    }
    
    public int getSpan()
    {
        if (columns == null) organizeColumns();
        return columns.size();
    }
    
    public TableColumn getColumn(int index)
    {
        if (columns == null) organizeColumns();
        return columns.elementAt(index);
    }

    //====================================================================================
    
    private void organizeColumns()
    {
        columns = new Vector<TableColumn>();
        for (int bi = 0; bi < nested.size(); bi++)
        {
            Box box = nested.elementAt(bi);
            if (box instanceof TableColumn)
            {
                TableColumn col = (TableColumn) box;
                if (col.getSpecifiedWidth().equals(""))
                    col.setSpecifiedWidth(colwidth); //when column width is not set, use the group width
                for (int i = 0; i < col.getSpan(); i++)
                    columns.add(col);
            }
        }
        //when there are no <col> elements, use the span attribute
        if (columns.isEmpty())
        {
            TableColumn col = new TableColumn(null, g, ctx);
            col.setSpecifiedWidth(colwidth);
            for (int i = 0; i < span; i++)
                columns.add(col);
        }
    }

}
