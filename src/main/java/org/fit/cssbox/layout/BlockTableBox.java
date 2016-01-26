/**
 * BlockTableBox.java
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
 * Created on 8.10.2009, 16:33:31 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.util.Iterator;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;

import org.w3c.dom.Element;

/**
 * This class represents the anonymous box created for a block-level table.
 * @author burgetr
 */
public class BlockTableBox extends BlockBox
{
    private TableBox table;
    private TableCaptionBox caption;
    private boolean captionbottom; //set to true, when caption should be in the bottom. Otherwise, caption is at the top.

    public BlockTableBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
    }

    /**
     * Create a new table from an inline box
     */
    public BlockTableBox(InlineBox src)
    {
        super(src);
        isblock = true;
    }
    
    /**
     * @return the caption
     */
    public TableCaptionBox getCaption()
    {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(TableCaptionBox caption)
    {
        this.caption = caption;
    }

    /**
     * @return the table
     */
    public TableBox getTable()
    {
        return table;
    }

    /**
     * @param table the table to set
     */
    public void setTable(TableBox table)
    {
        this.table = table;
    }
    
    //======================================================================================================
    
    @Override
    public void initBox()
    {
        organizeContent(); //organize the child elements according to their display property
        loadCaptionStyle();
    }

    @Override
    public boolean canIncreaseWidth()
    {
        return true;
    }

    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        setAvailableWidth(availw);
        int x1 = fleft.getWidth(floatY) - floatXl;
        int x2 = fright.getWidth(floatY) - floatXr;
        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        int wlimit = getAvailableContentWidth() - x1 - x2;
        int tabwidth = 0;
        int tabheight = 0;
        int capheight = 0;
        int capwidth = 0;
        
        //format the table
        BlockLayoutStatus stat = new BlockLayoutStatus();
        table.setAvailableWidth(wlimit);
        table.updateSizes();
        layoutBlockInFlow(table, wlimit, stat);
        tabwidth = stat.maxw;
        tabheight = stat.y;

        //format the caption
        if (caption != null)
        {
            stat.y = 0;
            caption.setAvailableWidth(stat.maxw);
            caption.updateSizes();
            layoutBlockInFlow(caption, stat.maxw, stat);
            capwidth = stat.maxw;
            capheight = stat.y;
            if (captionbottom) //place the caption below or above
            {
                table.setPosition(x1, 0);
                caption.setPosition(x1, tabheight);
            }
            else
            {
                caption.setPosition(x1, 0);
                table.setPosition(x1, capheight);
            }
        }
        else
            table.setPosition(x1, 0);
        
        setContentWidth(Math.max(tabwidth, capwidth));
        setContentHeight(tabheight + capheight);
        widthComputed = true;
        updateSizes();
        setSize(totalWidth(), totalHeight());
        
        //layout positioned boxes
        for (Box box : nested)
        {
            if (box instanceof BlockBox && ((BlockBox) box).isPositioned())
            {
                ((BlockBox) box).updateSizes();
                layoutBlockPositioned((BlockBox) box, stat);
            }
        }
        
        return true;
    }
    
    @Override
    public int getMaximalWidth()
    {
        if (caption == null)
            return table.getMaximalWidth();
        else
            return Math.max(table.getMaximalWidth(), caption.getMaximalWidth());
    }

    @Override
    public int getMinimalWidth()
    {
        if (caption == null)
            return table.getMinimalWidth();
        else
            return Math.max(table.getMinimalWidth(), caption.getMinimalWidth());
    }

    @Override
    protected int getMaximalContentWidth()
    {
        if (caption == null)
            return table.getMaximalContentWidth();
        else
            return Math.max(table.getMaximalContentWidth(), caption.getMaximalContentWidth());
    }

    @Override
    protected int getMinimalContentWidth()
    {
        if (caption == null)
            return table.getMinimalContentWidth();
        else
            return Math.max(table.getMinimalContentWidth(), caption.getMinimalContentWidth());
    }

    @Override
    protected void loadBackground()
    {
        //anonymous table box has never a background
        bgcolor = null;
    }

    @Override
    protected void loadBorders(CSSDecoder dec, int contw)
    {
        //anonymous table box has never a border
        border = new LengthSet();
    }

    @Override
    protected void loadPadding(CSSDecoder dec, int contw)
    {
        //anonymous table box has never a padding
        padding = new LengthSet();
    }
    
    @Override
    protected void computeWidths(TermLengthOrPercent width, boolean auto, boolean exact, boolean update)
    {
        //anonymous table box has always an 'auto' width in the beginning. After the layout, the width is updated according
        //the resulting table (and caption) width
        if (!widthComputed)
            super.computeWidths(null, true, exact, update);
        else
            super.computeWidths(CSSFactory.getTermFactory().createLength((float) content.width, TermLength.Unit.px), false, exact, update);
    }
    
    
    //======================================================================================================

    protected void loadCaptionStyle()
    {
        CSSProperty.CaptionSide side = style.getProperty("caption-side");
        captionbottom = (side == CSSProperty.CaptionSide.BOTTOM);
    }
    
    /**
     * Goes through the list of child boxes and organizes them into captions, header,
     * footer, etc.
     */
    private void organizeContent()
    {
        table = new TableBox(el, g, ctx);
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
                //positioned boxes are ignored
            }
            else //other elements belong to the table itself
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
    
}
