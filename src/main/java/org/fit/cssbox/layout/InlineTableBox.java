/*
 * InlineTableBox.java
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
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermLengthOrPercent;

/**
 * A box corresponding to an inline-table element (display: inline-table).
 * It is inline-level on the outside (participates in a line box) but
 * table on the inside (columns, rows, cells, captions).
 *
 * @author burgetr
 */
public class InlineTableBox extends BlockBox implements InlineElement
{
    // from BlockTableBox
    private TableBox table;
    private TableCaptionBox caption;
    private boolean captionbottom;

    // from InlineBlockBox
    private CSSProperty.VerticalAlign valign;
    private LineBox linebox;
    protected float baseline;
    private float availw;

    public InlineTableBox(InlineBox src)
    {
        super(src);
        isblock = false;
    }

    //======================================================================================================

    @Override
    public void initBox()
    {
        setFloats(new FloatList(this), new FloatList(this), 0, 0, 0);
        organizeContent();
        loadCaptionStyle();
    }

    @Override
    public void initLayoutManager()
    {
        layoutManager = new TableLayoutManager(this);
    }

    @Override
    public void setStyle(NodeData s)
    {
        super.setStyle(s);
        loadInlineStyle();
    }

    //======================================================================================================
    // InlineElement interface

    @Override
    public CSSProperty.VerticalAlign getVerticalAlign()
    {
        return valign;
    }

    @Override
    public void setLineBox(LineBox linebox)
    {
        this.linebox = linebox;
    }

    @Override
    public LineBox getLineBox()
    {
        return linebox;
    }

    @Override
    public float getLineboxOffset()
    {
        return 0;
    }

    //======================================================================================================
    // Inline interface

    @Override
    public float getMaxLineHeight()
    {
        return getHeight();
    }

    @Override
    public float getBaselineOffset()
    {
        return baseline;
    }

    @Override
    public float getBelowBaseline()
    {
        return getHeight() - baseline;
    }

    @Override
    public float getTotalLineHeight()
    {
        return getHeight();
    }

    @Override
    public float getHalfLead()
    {
        return 0;
    }

    @Override
    public float getFirstLineLength()
    {
        return getMaximalContentWidth();
    }

    @Override
    public float getLastLineLength()
    {
        return getMaximalContentWidth();
    }

    @Override
    public boolean containsLineBreak()
    {
        return false;
    }

    @Override
    public boolean finishedByLineBreak()
    {
        return false;
    }

    @Override
    public boolean collapsedCompletely()
    {
        return false;
    }

    @Override
    public int getWidthExpansionPoints(boolean atLineStart, boolean atLineEnd)
    {
        return 0;
    }

    @Override
    public void extendWidth(float dif, boolean atLineStart, boolean atLineEnd)
    {
        // not applicable for inline-table boxes
    }

    @Override
    public void setIgnoreInitialWhitespace(boolean b)
    {
        if (endChild > startChild)
            getSubBox(startChild).setIgnoreInitialWhitespace(b);
    }

    //======================================================================================================

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
    public boolean hasFixedWidth()
    {
        return wset; // only if explicitly set
    }

    @Override
    public float getMinimalContentWidthLimit()
    {
        float ret;
        if (wset)
            ret = content.width;
        else if (min_size.width != -1)
            ret = min_size.width;
        else
            ret = 0;
        return ret;
    }

    @Override
    public boolean doLayout(float availw, boolean force, boolean linestart)
    {
        this.availw = availw;
        setAvailableWidth(availw);
        float wlimit = getAvailableContentWidth();
        float tabwidth = 0;
        float tabheight = 0;
        float capheight = 0;
        float capwidth = 0;

        // lay out table
        BlockLayoutStatus stat = new BlockLayoutStatus();
        table.setAvailableWidth(wlimit);
        table.updateSizes();
        layoutManager.layoutBlockInFlow(table, wlimit, stat);
        tabwidth = stat.maxw;
        tabheight = stat.y;

        // the caption width is not known yet, use the tab width for now 
        setContentWidth(tabwidth);
        
        // lay out caption
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
                table.setPosition(0, 0);
                caption.setPosition(0, tabheight);
            }
            else
            {
                caption.setPosition(0, 0);
                table.setPosition(0, capheight);
            }
        }
        else
            table.setPosition(0, 0);

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

        if (force || fitsSpace())
        {
            // baseline: last in-flow line, fallback bottom edge
            baseline = getLastInlineBoxBaseline(this);
            if (baseline == -1)
                baseline = getHeight();
            else
            {
                baseline += getContentOffsetY();
                if (baseline > getHeight()) baseline = getHeight();
            }
            return true;
        }
        else
            return false;
    }

    /**
     * Checks whether the block fits the available space.
     * @return {@code true} when there is enough space to fit the block
     */
    private boolean fitsSpace()
    {
        return availw >= totalWidth();
    }

    @Override
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, float contw, boolean update)
    {
        // Shrink-to-fit (same as InlineBlockBox)
        CSSDecoder dec = new CSSDecoder(ctx);

        if (width == null) auto = true;

        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");

        if (!widthComputed) update = false;

        if (auto)
        {
            if (exact) wset = false;
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, contw);
        }
        else
        {
            if (exact)
            {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
        }

        // auto margins are treated as zero
        margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
        margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
    }

    @Override
    protected void computeAbsolutePosition()
    {
        absbounds.x = getParent().getAbsoluteContentX() + bounds.x;
        if (valign == CSSProperty.VerticalAlign.TOP)
        {
            absbounds.y = linebox.getAbsoluteY();
        }
        else if (valign == CSSProperty.VerticalAlign.BOTTOM)
        {
            absbounds.y = linebox.getAbsoluteY() + linebox.getMaxBoxHeight() - getHeight();
        }
        else
        {
            absbounds.y = getParent().getAbsoluteContentY() + bounds.y;
        }

        if (position == POS_RELATIVE)
        {
            absbounds.x += leftset ? coords.left : (-coords.right);
            absbounds.y += topset ? coords.top : (-coords.bottom);
        }

        absbounds.width = bounds.width;
        absbounds.height = bounds.height;
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
        // anonymous wrapper box has no border
        border = new LengthSet();
    }

    @Override
    protected void loadPadding(CSSDecoder dec, float contw)
    {
        // anonymous wrapper box has no padding
        padding = new LengthSet();
    }

    @Override
    public void draw(DrawStage turn)
    {
        if (displayed)
        {
            if (!this.formsStackingContext())
            {
                switch (turn)
                {
                    case DRAW_NONINLINE:
                    case DRAW_FLOAT:
                        // everything is drawn in the DRAW_INLINE phase
                        break;
                    case DRAW_INLINE:
                        if (isVisible())
                            getViewport().getRenderer().renderElementBackground(this);
                        drawStackingContext(true);
                        break;
                }
            }
        }
    }

    //======================================================================================================

    /**
     * Loads the basic style properties related to inline elements.
     */
    protected void loadInlineStyle()
    {
        valign = style.getProperty("vertical-align");
        if (valign == null) valign = CSSProperty.VerticalAlign.BASELINE;
    }

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
     * table content, etc.
     */
    private void organizeContent()
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
     * Recursively finds the baseline of the last in-flow box.
     * @param root the element to start search in
     * @return The baseline offset in the element content or -1 if there are no in-flow boxes.
     */
    private float getLastInlineBoxBaseline(ElementBox root)
    {
        Box box = null;
        for (int i = root.getSubBoxNumber() - 1; i >= 0; i--)
        {
            box = root.getSubBox(i);
            if (box.isInFlow())
                break;
            else
                box = null;
        }

        if (box != null)
        {
            if (box instanceof Inline)
            {
                return box.getContentY() + ((Inline) box).getBaselineOffset();
            }
            else
            {
                return box.getContentY() + getLastInlineBoxBaseline((ElementBox) box);
            }
        }
        else
            return -1;
    }

}
