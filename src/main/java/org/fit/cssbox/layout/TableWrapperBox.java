/*
 * TableWrapperBox.java
 * Copyright (c) 2005-2024 Radek Burget
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

import cz.vutbr.web.css.CSSProperty;

import org.w3c.dom.Element;

/**
 * Abstract base class for table wrapper boxes. Holds the shared fields
 * ({@code table}, {@code caption}, {@code captionbottom}) and all methods that
 * are identical between {@link BlockTableBox} and {@link InlineTableBox}.
 *
 * @author burgetr
 */
public abstract class TableWrapperBox extends BlockBox
{
    private TableBox table;
    private TableCaptionBox caption;
    private boolean captionbottom; // true when caption should appear below the table

    protected TableWrapperBox(Element n, VisualContext ctx)
    {
        super(n, ctx);
    }

    protected TableWrapperBox(InlineBox src)
    {
        super(src);
    }

    public TableBox getTable()
    {
        return table;
    }

    public void setTable(TableBox table)
    {
        this.table = table;
    }

    public TableCaptionBox getCaption()
    {
        return caption;
    }

    public void setCaption(TableCaptionBox caption)
    {
        this.caption = caption;
    }

    //======================================================================================================

    @Override
    public void initLayoutManager()
    {
        layoutManager = new TableLayoutManager(this);
    }

    @Override
    public boolean canIncreaseWidth()
    {
        return true;
    }

    @Override
    protected boolean mayOverlapFloats()
    {
        return false; // tables may not overlap floats
    }

    @Override
    public float getMaximalWidth()
    {
        if (caption == null)
            return table.getMaximalWidth();
        else
            return Math.max(table.getMaximalWidth(), caption.getMaximalWidth());
    }

    @Override
    public float getMinimalWidth()
    {
        if (caption == null)
            return table.getMinimalWidth();
        else
            return Math.max(table.getMinimalWidth(), caption.getMinimalWidth());
    }

    @Override
    protected float getMaximalContentWidth()
    {
        if (caption == null)
            return table.getMaximalContentWidth();
        else
            return Math.max(table.getMaximalContentWidth(), caption.getMaximalContentWidth());
    }

    @Override
    protected float getMinimalContentWidth()
    {
        if (caption == null)
            return table.getMinimalContentWidth();
        else
            return Math.max(table.getMinimalContentWidth(), caption.getMinimalContentWidth());
    }

    @Override
    protected float getMinimalDecorationWidth()
    {
        if (caption == null)
            return table.getMinimalDecorationWidth();
        else
            return Math.max(table.getMinimalDecorationWidth(), caption.getMinimalDecorationWidth());
    }

    @Override
    protected void loadBorders(CSSDecoder dec, float contw)
    {
        // anonymous table wrapper box has never a border
        border = new LengthSet();
    }

    @Override
    protected void loadPadding(CSSDecoder dec, float contw)
    {
        // anonymous table wrapper box has never a padding
        padding = new LengthSet();
    }

    //======================================================================================================

    protected void loadCaptionStyle()
    {
        if (caption != null)
        {
            CSSProperty.CaptionSide side = caption.getStyle().getProperty("caption-side");
            captionbottom = (side == CSSProperty.CaptionSide.BOTTOM);
        }
        else
            captionbottom = false;
    }

    /**
     * Goes through the list of child boxes and organizes them into captions,
     * header, footer, etc.
     */
    protected void organizeContent()
    {
        table = new TableBox(el, ctx);
        table.adoptParent(this);
        table.setStyle(style);

        for (Iterator<Box> it = nested.iterator(); it.hasNext(); )
        {
            Box box = it.next();
            if (box instanceof TableCaptionBox)
            {
                caption = (TableCaptionBox) box;
            }
            else if (box instanceof BlockBox && ((BlockBox) box).isPositioned())
            {
                // positioned boxes are ignored
            }
            else // other elements belong to the table itself
            {
                table.addSubBox(box);
                box.setContainingBlockBox(table);
                box.setParent(table);
                it.remove();
                endChild--;
            }
        }

        addSubBox(table);
    }

    /**
     * Shared core of {@code doLayout()} for both block-level and inline-level
     * table wrapper boxes.
     *
     * @param wlimit  available content width for table and caption layout
     * @param xoffset x position to assign to the table and caption within this box
     */
    protected void doTableWrapperLayout(float wlimit, float xoffset)
    {
        float tabwidth = 0;
        float tabheight = 0;
        float capheight = 0;
        float capwidth = 0;

        // format the table
        BlockLayoutStatus stat = new BlockLayoutStatus();
        table.setAvailableWidth(wlimit);
        table.updateSizes();
        layoutManager.layoutBlockInFlow(table, wlimit, stat);
        tabwidth = stat.maxw;
        tabheight = stat.y;

        // the caption width is not known yet, use the tab width for now
        setContentWidth(tabwidth);

        // format the caption
        if (caption != null)
        {
            stat.y = 0;
            caption.setAvailableWidth(tabwidth);
            caption.updateSizes();
            layoutManager.layoutBlockInFlow(caption, stat.maxw, stat);
            capwidth = stat.maxw;
            capheight = stat.y;
            if (captionbottom)
            {
                table.setPosition(xoffset, 0);
                caption.setPosition(xoffset, tabheight);
            }
            else
            {
                caption.setPosition(xoffset, 0);
                table.setPosition(xoffset, capheight);
            }
        }
        else
            table.setPosition(xoffset, 0);

        setContentWidth(Math.max(tabwidth, capwidth));
        setContentHeight(tabheight + capheight);
        widthComputed = true;
        updateSizes();
        setSize(totalWidth(), totalHeight());

        // layout positioned boxes
        for (Box box : nested)
        {
            if (box instanceof BlockBox && ((BlockBox) box).isPositioned())
            {
                ((BlockBox) box).updateSizes();
                layoutManager.layoutBlockPositioned((BlockBox) box, stat);
            }
        }
    }

}
