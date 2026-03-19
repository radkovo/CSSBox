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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.web.css.CSSProperty;

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
    private static Logger log = LoggerFactory.getLogger(InlineLayoutManager.class);

    public InlineLayoutManager(ElementBox owner)
    {
        super(owner);
    }

    @Override
    public boolean layout(float availw, boolean force, boolean linestart)
    {
        if (owner instanceof InlineBox)
        {
            return performInlineLayout((InlineBox) owner, availw, force, linestart);
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
    // Inline element layout (moved from InlineBox.doLayoutInline + alignBoxes)
    // -----------------------------------------------------------------------

    /**
     * Lays out the children of an inline element box.
     * Moved from {@code InlineBox.doLayoutInline()}.
     */
    private boolean performInlineLayout(InlineBox box, float availw, boolean force, boolean linestart)
    {
        if (!box.displayed)
        {
            box.content.setSize(0, 0);
            box.bounds.setSize(0, 0);
            return true;
        }

        box.setAvailableWidth(availw);

        LineBox curline = new LineBox(box, box.startChild, 0);
        float wlimit = box.getAvailableContentWidth();
        float x = 0;
        boolean ret = true;
        box.rest = null;

        int lastbreak = box.startChild;
        box.collapsedCompletely = true;

        for (int i = box.startChild; i < box.endChild; i++)
        {
            Box subbox = box.getSubBox(i);
            if (subbox.canSplitBefore())
                lastbreak = i;
            boolean f = force && (i == box.startChild || lastbreak == box.startChild);
            boolean fit = subbox.doLayout(wlimit - x, f, linestart && (i == box.startChild));
            if (fit)
            {
                if (subbox instanceof Inline)
                {
                    subbox.setPosition(x, 0);
                    x += subbox.getWidth();
                    curline.considerBox((Inline) subbox);
                    if (((Inline) subbox).finishedByLineBreak())
                        box.lineBreakStop = true;
                    if (!((Inline) subbox).collapsedCompletely())
                        box.collapsedCompletely = false;
                }
                else
                    log.debug("Warning: performInlineLayout(): subbox is not inline: " + subbox);
                if (subbox.getRest() != null)
                {
                    InlineBox rbox = box.copyBox();
                    rbox.splitted = true;
                    rbox.splitid = box.splitid + 1;
                    rbox.setStartChild(i);
                    rbox.nested.set(i, subbox.getRest());
                    rbox.adoptChildren();
                    box.setEndChild(i + 1);
                    box.rest = rbox;
                    break;
                }
                else if (box.lineBreakStop)
                {
                    if (i + 1 < box.endChild)
                    {
                        InlineBox rbox = box.copyBox();
                        rbox.splitted = true;
                        rbox.splitid = box.splitid + 1;
                        rbox.setStartChild(i + 1);
                        rbox.adoptChildren();
                        box.setEndChild(i + 1);
                        box.rest = rbox;
                    }
                    break;
                }
            }
            else
            {
                if (lastbreak == box.startChild)
                {
                    ret = false;
                    break;
                }
                else
                {
                    InlineBox rbox = box.copyBox();
                    rbox.splitted = true;
                    rbox.splitid = box.splitid + 1;
                    rbox.setStartChild(lastbreak);
                    rbox.adoptChildren();
                    box.setEndChild(lastbreak);
                    box.rest = rbox;
                    break;
                }
            }

            if (subbox.canSplitAfter())
                lastbreak = i + 1;
        }

        box.content.width = x;
        box.content.height = box.ctx.getFontHeight();
        box.setHalfLead((box.content.height - box.ctx.getFontHeight()) / 2);
        alignInlineBoxes(box, curline);
        box.setCurLine(curline);
        box.setSize(box.totalWidth(), box.totalHeight());

        return ret;
    }

    /**
     * Vertically aligns the children of an inline box within its line box.
     * Moved from {@code InlineBox.alignBoxes()}.
     */
    private void alignInlineBoxes(InlineBox box, LineBox curline)
    {
        float minDY = box.getMinDescendantY();
        float maxDY = box.getMaxDescendantY();
        for (int i = box.startChild; i < box.endChild; i++)
        {
            Box sub = box.getSubBox(i);
            if (!sub.isBlock())
            {
                float dif = curline.alignBox((Inline) sub);
                dif = dif - box.getLineboxOffset();
                if (sub instanceof InlineBox)
                    dif = dif - ((ElementBox) sub).getContentOffsetY();
                if (dif != 0)
                    sub.moveDown(dif);
                float y1 = sub.getContentY();
                if (sub instanceof InlineBox)
                {
                    final float dy = ((InlineBox) sub).getMinDescendantY();
                    if (dy < 0)
                        y1 += dy;
                }
                minDY = Math.min(minDY, y1);
                float y2 = sub.getContentY() + sub.getContentHeight() - 1;
                if (sub instanceof InlineBox)
                {
                    final float dy = ((InlineBox) sub).getMaxDescendantY();
                    if (dy > sub.getContentHeight())
                        y2 += dy;
                }
                maxDY = Math.max(maxDY, y2);
            }
        }
        box.setMinDescendantY(minDY);
        box.setMaxDescendantY(maxDY);
    }

    // -----------------------------------------------------------------------
    // Block-with-inline-children layout (moved from BlockBox.layoutInline)
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

        x += block.getIndent();

        List<LineBox> lines = new ArrayList<LineBox>();
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
            alignLineHorizontally(block, line, !it.hasNext());
            alignLineVertically(block, line);
        }
    }

    // -----------------------------------------------------------------------
    // Line alignment (moved from BlockBox.alignLineHorizontally/Vertically)
    // -----------------------------------------------------------------------

    /**
     * Aligns the subboxes in a line according to the text-align setting.
     * Moved from {@code BlockBox.alignLineHorizontally()}.
     */
    private void alignLineHorizontally(BlockBox block, LineBox line, boolean isLast)
    {
        final float dif = block.getContentWidth() - line.getLimits() - line.getWidth();
        if (dif > 0)
        {
            CSSProperty.TextAlign align = block.getTextAlign();
            if (align == BlockBox.ALIGN_JUSTIFY)
            {
                if (!isLast)
                    block.extendInlineChildWidths(dif, line.getStart(), line.getEnd(), true, true);
            }
            else if (align != BlockBox.ALIGN_LEFT)
            {
                for (int i = line.getStart(); i < line.getEnd(); i++)
                {
                    Box subbox = block.getSubBox(i);
                    if (subbox instanceof Inline)
                    {
                        if (align == BlockBox.ALIGN_RIGHT)
                            subbox.moveRight(dif);
                        else if (align == BlockBox.ALIGN_CENTER)
                            subbox.moveRight(dif / 2);
                    }
                }
            }
        }
    }

    /**
     * Vertically aligns the subboxes within a line box.
     * Moved from {@code BlockBox.alignLineVertically()}.
     */
    private void alignLineVertically(BlockBox block, LineBox line)
    {
        for (int i = line.getStart(); i < line.getEnd(); i++)
        {
            Box subbox = block.getSubBox(i);
            if (!subbox.isBlock())
            {
                float dif = line.alignBox((Inline) subbox);
                if (subbox instanceof InlineBox)
                    dif = dif - ((ElementBox) subbox).getContentOffsetY();
                if (subbox instanceof InlineElement)
                    ((InlineElement) subbox).setLineBox(line);
                float y = line.getY() + line.getTopOffset() + (line.getLead() / 2) + dif;
                subbox.moveDown(y);
            }
        }
    }
}
