/*
 * InlineLayoutManager.java
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
 * A layout manager for two closely related cases:
 * <ol>
 *   <li><b>Inline boxes</b> ({@link InlineBox} instances such as {@code <span>})
 *       — arranges inline children horizontally within the current line.</li>
 *   <li><b>Block boxes whose children are all inline</b>
 *       ({@link BlockBox} with {@code containsBlocks() == false}) — builds line
 *       boxes from the inline children and manages text wrapping.</li>
 * </ol>
 *
 * @author radek
 */
public class InlineLayoutManager extends LayoutManager
{
    public InlineLayoutManager(ElementBox owner)
    {
        super(owner);
    }

    @Override
    public boolean layout(float availw, boolean force, boolean linestart)
    {
        if (owner instanceof InlineBox)
        {
            return ((InlineBox) owner).doLayoutInline(availw, force, linestart);
        }
        else
        {
            performBlockInlineLayout((BlockBox) owner);
            return true;
        }
    }

    /**
     * Computes the minimal content width from the inline children.
     * For {@link BlockBox} owners the same algorithm as
     * {@link BlockLayoutManager#getMinimalContentWidth()} applies.
     * For {@link InlineBox} owners the content width is computed during layout.
     */
    @Override
    public float getMinimalContentWidth()
    {
        if (!(owner instanceof BlockBox))
            return 0;
        float ret = 0;
        float max = 0;
        float sum = 0;
        for (int i = owner.startChild; i < owner.endChild; i++)
        {
            Box box = owner.getSubBox(i);
            if (box instanceof Inline)
            {
                if (owner.allowsWrapping() && box.canSplitBefore())
                    sum = 0;
                sum += box.getMinimalWidth();
            }
            else
            {
                BlockBox block = (BlockBox) box;
                if (block.position != ElementBox.POS_ABSOLUTE && block.position != ElementBox.POS_FIXED)
                {
                    float w = box.getMinimalWidth();
                    if (w > max) max = w;
                    sum = 0;
                }
            }
            if (sum > ret) ret = sum;
            if (max > ret) ret = max;
            if (owner.allowsWrapping() && box.canSplitAfter())
                sum = 0;
        }
        return ret;
    }

    /**
     * Computes the maximal content width from the inline children.
     * For {@link BlockBox} owners the same algorithm as
     * {@link BlockLayoutManager#getMaximalContentWidth()} applies.
     * For {@link InlineBox} owners the content width is computed during layout.
     */
    @Override
    public float getMaximalContentWidth()
    {
        if (!(owner instanceof BlockBox))
            return 0;
        float sum = 0;
        float max = 0;
        for (int i = owner.startChild; i < owner.endChild; i++)
        {
            Box subbox = owner.getSubBox(i);
            if (subbox.isBlock())
            {
                BlockBox block = (BlockBox) subbox;
                if (block.getFloating() != BlockBox.FLOAT_NONE)
                {
                    sum += subbox.getMaximalWidth();
                }
                else if (!block.isInFlow())
                {
                    // positioned blocks don't affect maximal width
                }
                else
                {
                    float sm = subbox.getMaximalWidth();
                    if (sm > max) max = sm;
                    if (sum > max) max = sum;
                    sum = 0;
                }
            }
            else
            {
                if (owner.preservesLineBreaks())
                {
                    float sm = subbox.getMaximalWidth();
                    if (sm > max) max = sm;
                }
                else
                    sum += subbox.getMaximalWidth();
            }
        }
        return Math.max(sum, max);
    }

    // -----------------------------------------------------------------------
    // Inline layout algorithm (moved from BlockBox.layoutInline())
    // -----------------------------------------------------------------------

    /**
     * Lays out inline children of a block box, building line boxes.
     * Moved from {@code BlockBox.layoutInline()}.
     */
    protected void performBlockInlineLayout(BlockBox block)
    {
        float x1 = block.fleft.getWidth(block.floatY) - block.floatXl;
        float x2 = block.fright.getWidth(block.floatY) - block.floatXr;
        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        float wlimit = block.getAvailableContentWidth();
        float minx1 = 0 - block.floatXl;
        float minx2 = 0 - block.floatXr;
        if (minx1 < 0) minx1 = 0;
        if (minx2 < 0) minx2 = 0;
        float x = x1;
        float y = 0;
        int lnstr = 0;
        int lastbreak = 0;

        x += block.indent;

        Vector<LineBox> lines = new Vector<LineBox>();
        LineBox curline = block.firstLine;
        if (curline == null)
            curline = new LineBox(block, 0, 0);
        lines.add(curline);

        for (int i = 0; i < block.getSubBoxNumber(); i++)
        {
            Box subbox = block.getSubBox(i);

            if (subbox.isBlock())
            {
                BlockBox sb = (BlockBox) subbox;
                BlockLayoutStatus stat = new BlockLayoutStatus();
                stat.inlineWidth = x - x1;
                stat.y = y;
                stat.maxh = 0;

                boolean atstart = (x <= x1);

                if (sb.getClearing() != BlockBox.CLEAR_NONE)
                {
                    float ny = stat.y;
                    if (sb.getClearing() == BlockBox.CLEAR_LEFT)
                        ny = block.fleft.getMaxY() - block.floatY;
                    else if (sb.getClearing() == BlockBox.CLEAR_RIGHT)
                        ny = block.fright.getMaxY() - block.floatY;
                    else if (sb.getClearing() == BlockBox.CLEAR_BOTH)
                        ny = Math.max(block.fleft.getMaxY(), block.fright.getMaxY()) - block.floatY;
                    if (stat.y < ny) stat.y = ny;
                }

                if (sb.getFloating() == BlockBox.FLOAT_LEFT || sb.getFloating() == BlockBox.FLOAT_RIGHT)
                {
                    layoutBlockFloating(sb, wlimit, stat);
                    if (sb.getFloating() == BlockBox.FLOAT_LEFT && stat.inlineWidth > 0 && curline.getStart() < i)
                    {
                        for (int j = curline.getStart(); j < i; j++)
                        {
                            Box child = block.getSubBox(j);
                            if (!child.isBlock())
                                child.moveRight(sb.getWidth());
                        }
                        x += sb.getWidth();
                    }
                }
                else
                {
                    layoutBlockPositioned(sb, stat);
                }

                x1 = block.fleft.getWidth(y + block.floatY) - block.floatXl;
                x2 = block.fright.getWidth(y + block.floatY) - block.floatXr;
                if (x1 < 0) x1 = 0;
                if (x2 < 0) x2 = 0;
                if (atstart && x < x1)
                    x = x1;
                continue;
            }

            if (subbox.canSplitBefore())
                lastbreak = i;
            boolean split;
            do
            {
                split = false;
                float space = wlimit - x1 - x2;
                boolean narrowed = (x1 > minx1 || x2 > minx2);
                boolean f = (x == x1 || lastbreak == lnstr || !block.allowsWrapping()) && !narrowed;
                boolean fit = false;
                if (space >= BlockBox.INFLOW_SPACE_THRESHOLD || !narrowed)
                    fit = subbox.doLayout(wlimit - x - x2, f, x == x1);
                if (fit)
                {
                    if (subbox.isInFlow())
                    {
                        subbox.setPosition(x, 0);
                        x += subbox.getWidth();
                    }
                    curline.considerBox((Inline) subbox);
                }

                boolean over = (x > wlimit - x2);
                boolean linebreak = (subbox instanceof Inline && ((Inline) subbox).finishedByLineBreak());
                if (!fit && narrowed && (x == x1 || lastbreak == lnstr))
                {
                    if (lnstr < i)
                    {
                        lnstr = i;
                        curline.setEnd(lnstr);
                        curline = new LineBox(block, lnstr, y);
                        lines.add(curline);
                    }
                    y += block.getLineHeight();
                    curline.setY(y);
                    x1 = block.fleft.getWidth(y + block.floatY) - block.floatXl;
                    x2 = block.fright.getWidth(y + block.floatY) - block.floatXr;
                    if (x1 < 0) x1 = 0;
                    if (x2 < 0) x2 = 0;
                    x = x1;
                    if (block.getLineHeight() > 0)
                        split = true;
                }
                else if ((!fit && lastbreak > lnstr)
                           || (fit && (over || linebreak || subbox.getRest() != null)))
                {
                    curline.setWidth(x - x1);
                    curline.setLimits(x1, x2);
                    y += curline.getMaxBoxHeight();
                    x1 = block.fleft.getWidth(y + block.floatY) - block.floatXl;
                    x2 = block.fright.getWidth(y + block.floatY) - block.floatXr;
                    if (x1 < 0) x1 = 0;
                    if (x2 < 0) x2 = 0;
                    x = x1;

                    if (!fit)
                    {
                        lnstr = i;
                        curline.setEnd(lnstr);
                        curline = new LineBox(block, lnstr, y);
                        lines.add(curline);
                        split = true;
                    }
                    else if (over || linebreak || subbox.getRest() != null)
                    {
                        if (subbox.getRest() != null)
                            block.insertSubBox(i + 1, subbox.getRest());
                        lnstr = i + 1;
                        curline.setEnd(lnstr);
                        curline = new LineBox(block, lnstr, y);
                        lines.add(curline);
                    }
                }
            } while (split);

            if (subbox.canSplitAfter())
                lastbreak = i + 1;
        }

        if (!block.hasFixedHeight())
        {
            y += curline.getMaxBoxHeight();
            if (block.encloseFloats())
            {
                float mfy = block.getFloatHeight() - block.floatY;
                if (mfy > y) y = mfy;
            }
            block.setContentHeight(y);
            block.updateSizes();
            block.updateChildSizes();
        }
        block.setSize(block.totalWidth(), block.totalHeight());

        curline.setWidth(x - x1);
        curline.setLimits(x1, x2);
        curline.setEnd(block.getSubBoxNumber());
        for (Iterator<LineBox> it = lines.iterator(); it.hasNext();)
        {
            LineBox line = it.next();
            block.alignLineHorizontally(line, !it.hasNext());
            block.alignLineVertically(line);
        }
    }
}
