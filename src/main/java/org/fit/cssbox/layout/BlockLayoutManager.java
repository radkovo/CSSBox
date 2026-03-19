/*
 * BlockLayoutManager.java
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

/**
 * A layout manager for block boxes whose children include block-level boxes
 * ({@link BlockBox#containsBlocks()} returns {@code true}).
 *
 * <p>Contains the block formatting context algorithm previously in
 * {@code BlockBox.layoutBlocks()} and its helper methods.
 * Float lists and related geometry fields remain in {@link BlockBox} and are
 * accessed through the {@link #owner} reference.
 *
 * @author radek
 */
public class BlockLayoutManager extends LayoutManager
{
    public BlockLayoutManager(BlockBox owner)
    {
        super(owner);
    }

    // -----------------------------------------------------------------------
    // LayoutManager contract
    // -----------------------------------------------------------------------

    @Override
    public boolean layout(float availw, boolean force, boolean linestart)
    {
        layoutBlocks();
        return true;
    }

    @Override
    public float getMinimalContentWidth()
    {
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

    @Override
    public float getMaximalContentWidth()
    {
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
    // Block layout algorithm (moved from BlockBox.layoutBlocks())
    // -----------------------------------------------------------------------

    /**
     * Lays out the block-level children of the owner box.
     * Moved from {@code BlockBox.layoutBlocks()}.
     */
    protected void layoutBlocks()
    {
        BlockBox block = (BlockBox) owner;
        float wlimit = block.getAvailableContentWidth();
        BlockLayoutStatus stat = new BlockLayoutStatus();
        float mtop = 0;
        float mbottom = 0;

        for (int i = 0; i < owner.getSubBoxNumber(); i++)
        {
            float nexty = stat.y;
            BlockBox subbox = (BlockBox) owner.getSubBox(i);

            if (subbox.isDisplayed())
            {
                boolean clearance = false;

                if (subbox.getClearing() != BlockBox.CLEAR_NONE)
                {
                    float ny = stat.y;
                    if (subbox.getClearing() == BlockBox.CLEAR_LEFT)
                        ny = block.fleft.getMaxY() - block.floatY;
                    else if (subbox.getClearing() == BlockBox.CLEAR_RIGHT)
                        ny = block.fright.getMaxY() - block.floatY;
                    else if (subbox.getClearing() == BlockBox.CLEAR_BOTH)
                        ny = Math.max(block.fleft.getMaxY(), block.fright.getMaxY()) - block.floatY;
                    if (stat.y < ny)
                    {
                        stat.y = ny;
                        clearance = true;
                    }
                }

                if (subbox.isInFlow())
                {
                    boolean boxempty = subbox.marginsAdjoin();

                    float borderY = stat.y;
                    if (stat.lastinflow != null)
                        borderY -= stat.lastinflow.emargin.bottom;

                    if (subbox.emargin.top > mtop)
                        mtop = subbox.emargin.top;

                    if (stat.firstseparated == null && block.separatedFromTop(block))
                        borderY += mtop;

                    if (stat.firstseparated != null)
                    {
                        if (clearance)
                            borderY += mtop + mbottom;
                        else
                            borderY += block.collapsedMarginHeight(mtop, mbottom);
                    }

                    stat.lastinflow = subbox;
                    if (!boxempty && stat.firstseparated == null)
                        stat.firstseparated = subbox;
                    if (!boxempty)
                    {
                        stat.lastseparated = subbox;
                        mtop = 0;
                        mbottom = subbox.emargin.bottom;
                    }

                    if (stat.lastseparated != null)
                    {
                        if (subbox.emargin.bottom > mbottom)
                            mbottom = subbox.emargin.bottom;
                    }

                    if (subbox.emargin.top > 0)
                        stat.y = borderY - subbox.emargin.top;

                    if (subbox.mayOverlapFloats())
                        layoutBlockInFlow(subbox, wlimit, stat);
                    else
                        layoutBlockInFlowAvoidFloats(subbox, wlimit, stat);

                    if (subbox.getRest() != null)
                        owner.insertSubBox(i + 1, subbox.getRest());
                    nexty = stat.y;
                }
                else if (subbox.getFloating() == BlockBox.FLOAT_LEFT
                        || subbox.getFloating() == BlockBox.FLOAT_RIGHT)
                {
                    layoutBlockFloating(subbox, wlimit, stat);
                }
                else
                {
                    layoutBlockPositioned(subbox, stat);
                }
                stat.y = nexty;
            }
        }

        if (!block.separatedFromBottom(block))
            stat.y -= mbottom;

        if (!block.hasFixedHeight())
        {
            if (block.encloseFloats())
            {
                float mfy = block.getFloatHeight() - block.floatY;
                if (mfy > stat.y) stat.y = mfy;
            }
            block.setContentHeight(stat.y);
            block.updateSizes();
            block.updateChildSizes();
        }
        block.setSize(block.totalWidth(), block.totalHeight());
    }

}
