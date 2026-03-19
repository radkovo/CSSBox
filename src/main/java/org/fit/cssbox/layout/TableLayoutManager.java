/*
 * TableLayoutManager.java
 * Copyright (c) 2005-2025 Radek Burget
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
 */

package org.fit.cssbox.layout;

import java.util.Iterator;
import java.util.Vector;

/**
 * A layout manager for table-related boxes ({@link TableBox},
 * {@link TableBodyBox}, {@link TableRowBox}, {@link TableCaptionBox},
 * {@link BlockTableBox}).
 *
 * <p>Holds the table and table-body layout algorithms moved from
 * {@link TableBox#doLayout} and {@link TableBodyBox#doLayout(float, Vector)}.
 *
 * @author radek
 */
public class TableLayoutManager extends LayoutManager
{
    public TableLayoutManager(BlockBox owner)
    {
        super(owner);
    }

    @Override
    public boolean layout(float availw, boolean force, boolean linestart)
    {
        if (owner instanceof TableBox)
        {
            performTableLayout((TableBox) owner);
        }
        // TableBodyBox layout is driven by TableBox.doLayout via doLayout(float,Vector)
        return true;
    }

    @Override
    public float getMinimalContentWidth()
    {
        return ((BlockBox) owner).getMinimalContentWidth();
    }

    @Override
    public float getMaximalContentWidth()
    {
        return ((BlockBox) owner).getMaximalContentWidth();
    }

    // -----------------------------------------------------------------------
    // Table layout algorithm (moved from TableBox.doLayout)
    // -----------------------------------------------------------------------

    /**
     * Lays out the table structure: calculates column widths, then positions
     * each body (header, bodies, footer).
     * Moved from {@code TableBox.doLayout()}.
     */
    protected void performTableLayout(TableBox table)
    {
        float wlimit = table.getAvailableContentWidth();
        float maxw = 0;
        float y = 0;

        table.calculateColumns();

        if (table.header != null)
        {
            table.header.doLayout(wlimit, table.columns);
            table.header.setPosition(0, y);
            if (table.header.getWidth() > maxw)
                maxw = table.header.getWidth();
            y += table.header.getHeight();
        }
        for (Iterator<TableBodyBox> it = table.bodies.iterator(); it.hasNext(); )
        {
            TableBodyBox body = it.next();
            body.doLayout(wlimit, table.columns);
            body.setPosition(0, y);
            if (body.getWidth() > maxw)
                maxw = body.getWidth();
            y += body.getHeight();
        }
        if (table.footer != null)
        {
            table.footer.doLayout(wlimit, table.columns);
            table.footer.setPosition(0, y);
            if (table.footer.getWidth() > maxw)
                maxw = table.footer.getWidth();
            y += table.footer.getHeight();
        }
        table.content.width = maxw;
        table.content.height = y;
        table.setSize(table.totalWidth(), table.totalHeight());
    }

    // -----------------------------------------------------------------------
    // Table body layout algorithm (moved from TableBodyBox.doLayout(float, Vector))
    // -----------------------------------------------------------------------

    /**
     * Lays out the rows inside a table body section.
     * Moved from {@code TableBodyBox.doLayout(float, Vector)}.
     */
    protected boolean performBodyLayout(TableBodyBox body, float widthlimit, Vector<TableColumn> columns)
    {
        body.setAvailableWidth(widthlimit);

        float y = body.spacing;
        float x = body.spacing;
        float maxw = 0;
        float maxh = 0;
        float wlimit = body.getAvailableContentWidth();

        float rowY[] = new float[body.getRowCount()];

        for (int r = 0; r < body.getRowCount(); r++)
        {
            TableRowBox row = body.getRow(r);

            x = body.spacing;
            maxh = 0;
            int c = 0;
            while (c < body.getColumnCount())
            {
                TableCellBox cell = body.cells[c][r];
                if (cell != null)
                {
                    int firstrow = cell.getRow();
                    int lastrow = cell.getRow() + cell.getRowspan() - 1;
                    float cw = columns.elementAt(c).getWidth();
                    for (int i = 1; i < cell.getColspan(); i++)
                        cw += body.spacing + columns.elementAt(c + i).getWidth();
                    cell.setWidth(cw);
                    if (r == firstrow)
                    {
                        cell.doLayout(wlimit, true, true);
                        cell.setPosition(x, 0);
                        if (cell.getRowspan() == 1)
                        {
                            float ch = cell.getHeight();
                            if (ch > maxh) maxh = ch;
                        }
                    }
                    else if (r < lastrow)
                    {
                        if (cell.getRowspan() == 1)
                        {
                            float ch = cell.getHeight();
                            if (ch > maxh) maxh = ch;
                        }
                    }
                    else if (r == lastrow)
                    {
                        float startY = rowY[cell.getRow()];
                        float remain = cell.getHeight() - (y - startY);
                        if (remain > maxh) maxh = remain;
                    }
                    x += cw + body.spacing;
                    c += cell.getColspan();
                }
                else
                    c++;
            }

            float baseline = 0;
            c = 0;
            while (c < body.getColumnCount())
            {
                TableCellBox cell = body.cells[c][r];
                if (cell != null)
                {
                    if (cell.getRow() == r)
                    {
                        float cbase = cell.getFirstInlineBoxBaseline();
                        if (cbase > baseline)
                            baseline = cbase;
                    }
                    c += cell.getColspan();
                }
                else
                    c++;
            }

            c = 0;
            while (c < body.getColumnCount())
            {
                TableCellBox cell = body.cells[c][r];
                if (cell != null)
                {
                    if (cell.getRow() + cell.getRowspan() - 1 == r)
                    {
                        float startY;
                        if (cell.getRowspan() > 1)
                            startY = rowY[cell.getRow()];
                        else
                            startY = y;
                        float oldheight = cell.getHeight();
                        float newheight = y + maxh - startY;
                        cell.setHeight(newheight);
                        cell.applyVerticalAlign(oldheight, newheight, baseline);
                    }
                    c += cell.getColspan();
                }
                else
                    c++;
            }

            rowY[r] = y;
            row.setPosition(0, y);
            row.content.width = x;
            row.content.height = maxh;
            row.setSize(row.totalWidth(), row.totalHeight());
            if (x > maxw) maxw = x;
            y += maxh + body.spacing;
        }
        body.content.width = maxw;
        body.content.height = y;
        body.setSize(body.totalWidth(), body.totalHeight());
        return true;
    }
}
