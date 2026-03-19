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

import java.util.List;

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

        calculateColumns(table);

        List<TableColumn> columns = table.getColumns();
        if (table.getHeader() != null)
        {
            table.getHeader().doLayout(wlimit, columns);
            table.getHeader().setPosition(0, y);
            if (table.getHeader().getWidth() > maxw)
                maxw = table.getHeader().getWidth();
            y += table.getHeader().getHeight();
        }
        for (TableBodyBox body : table.getBodies())
        {
            body.doLayout(wlimit, columns);
            body.setPosition(0, y);
            if (body.getWidth() > maxw)
                maxw = body.getWidth();
            y += body.getHeight();
        }
        if (table.getFooter() != null)
        {
            table.getFooter().doLayout(wlimit, columns);
            table.getFooter().setPosition(0, y);
            if (table.getFooter().getWidth() > maxw)
                maxw = table.getFooter().getWidth();
            y += table.getFooter().getHeight();
        }
        table.setContentWidth(maxw);
        table.setContentHeight(y);
        table.setSize(table.totalWidth(), table.totalHeight());
    }

    // -----------------------------------------------------------------------
    // Column width calculation (moved from TableBox.calculateColumns)
    // -----------------------------------------------------------------------

    /**
     * Propagates column parameters from a body's cells into the column objects.
     * Moved from {@code TableBox.updateColumns()}.
     */
    private void updateColumns(TableBodyBox body, List<TableColumn> columns)
    {
        for (int i = 0; i < columns.size(); i++)
            if (i < body.getColumnCount())
                body.updateColumn(i, columns.get(i));
    }

    /**
     * Calculates the widths of all table columns.
     * Moved from {@code TableBox.calculateColumns()}.
     */
    private void calculateColumns(TableBox table)
    {
        List<TableColumn> columns = table.getColumns();
        float wlimit = table.getAvailableContentWidth();

        //create columns that haven't been specified explicitly
        table.determineColumnCount();
        while (columns.size() < table.getColumnCount())
            columns.add(new TableColumn(TableColumn.createAnonymousColumn(
                    table.getParent().getElement().getOwnerDocument()), table.getVisualContext()));

        //load the parameters and ensure the minimal column widths
        if (table.getHeader() != null)
            updateColumns(table.getHeader(), columns);
        if (table.getFooter() != null)
            updateColumns(table.getFooter(), columns);
        for (TableBodyBox body : table.getBodies())
            updateColumns(body, columns);

        //now, the columns are at minimal widths
        //gather column statistics
        float sumabs = 0; //total length of absolute columns
        float sumperc = 0; //total percentage
        float mintotalw = 0;  //total minimal length of all the columns
        float sumnonemin = 0; //total minimal length of the columns with no width specified
        float sumnonemax = 0; //total maximal length of the columns with no width specified
        float totalwperc = 0; //total table width computed from percentage columns
        for (TableColumn col : columns) //compute the sums
        {
            mintotalw += col.getMinimalWidth();
            if (col.wrelative)
            {
                sumperc += col.percent;
                float maxw = col.getMaximalWidth();
                float newtotal = maxw * 100 / col.percent;
                if (newtotal > totalwperc) totalwperc = newtotal;
            }
            else
            {
                if (col.wset)
                    sumabs += Math.max(col.abswidth, col.getMinimalWidth());
                else
                {
                    sumnonemin += col.getWidth();
                    sumnonemax += col.getMaximalWidth();
                }
            }
        }

        //guess the total width available for columns (not including spacing now)
        if (totalwperc > wlimit) totalwperc = wlimit;
        float totalwabs = 0; //from absolute fields
        if (sumabs + sumnonemax > 0)
        {
            float abspart = 100 - sumperc; //the absolute part is how many percent
            totalwabs = (abspart == 0) ? wlimit : (sumabs + sumnonemax) * 100 / abspart; //what is 100%
        }
        float totalw = Math.max(totalwperc, totalwabs); //desired width taken from the columns

        //apply the table limits
        if (table.hasFixedWidth())
        {
            totalw = table.getContentWidth() - (columns.size() + 1) * table.getSpacing(); //total space obtained from definition
        }
        else
        {
            if (totalw > wlimit)
                totalw = wlimit; //we would not like to exceed the limit
        }
        if (totalw < mintotalw) totalw = mintotalw; //we cannot be below the minimal width

        //available for further allocation
        float remain = totalw - mintotalw;

        //set the percentage columns to their values, if possible
        if (remain > 0 && sumperc > 0)
        {
            for (TableColumn col : columns) //set the column sizes
            {
                if (col.wrelative)
                {
                    float mincw = col.getMinimalWidth();
                    float neww = col.percent * totalw / 100;
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= (neww - mincw);
                }
            }
        }

        //set the absolute columns
        if (remain > 0 && sumabs > 0)
        {
            for (TableColumn col : columns) //set the column sizes
            {
                if (col.wset && !col.wrelative)
                {
                    float mincw = col.getMinimalWidth();
                    float neww = col.abswidth;
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= (neww - mincw);
                }
            }
        }

        //set the remaining columns
        if (remain > 0 && sumnonemin > 0 && sumnonemax > 0)
        {
            float remainmax = sumnonemax;
            remain += sumnonemin;
            for (TableColumn col : columns) //set the column sizes
            {
                if (!col.wset)
                {
                    float mincw = col.getMinimalWidth();
                    float neww = remain * col.getMaximalWidth() / remainmax;
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= neww;
                    remainmax -= col.getMaximalWidth();
                    if (remainmax <= 0 || remain <= 0) //the remaining columns have zero width
                        break;
                }
            }
        }

        //if something still remains, use it for fixed columns
        if (remain > 0 && sumabs > 0)
        {
            float remainabs = sumabs;
            for (TableColumn col : columns)
            {
                if (col.wset && !col.wrelative)
                {
                    float addw = remain * col.getMaximalWidth() / remainabs;
                    col.setColumnWidth(col.getWidth() + addw);
                    remain -= addw;
                    remainabs -= col.getMaximalWidth();
                }
            }
        }

        //if something still remains, use it for percentage columns
        if (remain > 0 && sumperc > 0 && sumperc < 100)
        {
            float remainperc = sumperc;
            for (TableColumn col : columns)
            {
                if (col.wrelative)
                {
                    float addw = remain * col.percent / remainperc;
                    col.setColumnWidth(col.getWidth() + addw);
                    remain -= addw;
                    remainperc -= col.getMaximalWidth();
                    if (remainperc <= 0 || remain <= 0)
                        break;
                }
            }
        }

        //if something still remains, use it for all columns
        if (remain > 0)
        {
            float remaincols = columns.size();
            for (int i = columns.size() - 1; i >= 0; i--)
            {
                TableColumn col = columns.get(i);
                float addw = remain / remaincols;
                col.setColumnWidth(col.getWidth() + addw);
                remain -= addw;
                remaincols--;
            }
        }

        //we are wider than we should be, reduce the widths
        if (remain < 0)
        {
            //non-fixed columns
            if (remain < 0 && sumnonemin > 0)
            {
                float totaldif = 0;
                for (TableColumn col : columns)
                    if (!col.wset)
                        totaldif += col.getWidth() - col.getMinimalWidth();

                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.get(i);
                    if (!col.wset)
                    {
                        float dif = col.getWidth() - col.getMinimalWidth();
                        float addw = remain * dif / totaldif;
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
            //fixed columns
            if (remain < 0 && sumabs > 0)
            {
                float totaldif = 0;
                for (TableColumn col : columns)
                    if (col.wset && !col.wrelative)
                        totaldif += col.getWidth() - col.getMinimalWidth();

                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.get(i);
                    if (col.wset && !col.wrelative)
                    {
                        float dif = col.getWidth() - col.getMinimalWidth();
                        float addw = remain * dif / totaldif;
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
            //percentage columns
            if (remain < 0 && sumperc > 0)
            {
                float totaldif = 0;
                for (TableColumn col : columns)
                    if (col.wrelative)
                        totaldif += col.getWidth() - col.getMinimalWidth();

                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.get(i);
                    if (col.wrelative)
                    {
                        float dif = col.getWidth() - col.getMinimalWidth();
                        float addw = remain * dif / totaldif;
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
        }

        table.markColumnsCalculated();
    }

    // -----------------------------------------------------------------------
    // Table body layout algorithm (moved from TableBodyBox.doLayout(float, Vector))
    // -----------------------------------------------------------------------

    /**
     * Lays out the rows inside a table body section.
     * Moved from {@code TableBodyBox.doLayout(float, Vector)}.
     */
    protected boolean performBodyLayout(TableBodyBox body, float widthlimit, List<TableColumn> columns)
    {
        body.setAvailableWidth(widthlimit);

        float sp = body.getSpacing();
        float y = sp;
        float x = sp;
        float maxw = 0;
        float maxh = 0;
        float wlimit = body.getAvailableContentWidth();

        float rowY[] = new float[body.getRowCount()];

        for (int r = 0; r < body.getRowCount(); r++)
        {
            TableRowBox row = body.getRow(r);

            x = sp;
            maxh = 0;
            int c = 0;
            while (c < body.getColumnCount())
            {
                TableCellBox cell = body.getCell(c, r);
                if (cell != null)
                {
                    int firstrow = cell.getRow();
                    int lastrow = cell.getRow() + cell.getRowspan() - 1;
                    float cw = columns.get(c).getWidth();
                    for (int i = 1; i < cell.getColspan(); i++)
                        cw += sp + columns.get(c + i).getWidth();
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
                    x += cw + sp;
                    c += cell.getColspan();
                }
                else
                    c++;
            }

            float baseline = 0;
            c = 0;
            while (c < body.getColumnCount())
            {
                TableCellBox cell = body.getCell(c, r);
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
                TableCellBox cell = body.getCell(c, r);
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
            y += maxh + sp;
        }
        body.content.width = maxw;
        body.content.height = y;
        body.setSize(body.totalWidth(), body.totalHeight());
        return true;
    }
}
