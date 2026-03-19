/*
 * LayoutManager.java
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
 * An abstract base class for layout managers that compute the arrangement of
 * a box's children. Each box is assigned a specific {@code LayoutManager}
 * instance (via {@link Box#initLayoutManager()}) after the box tree is fully
 * built and before the layout pass begins.
 *
 * <p>The split of responsibility between box and manager is:
 * <ul>
 *   <li>The <em>box</em> owns CSS property values, its own geometry
 *       (margin, padding, border, content size), rendering hooks, and
 *       DOM/style references.</li>
 *   <li>The <em>manager</em> is responsible for arranging the box's
 *       <em>children</em>: the specific algorithm used, float lists,
 *       line box construction, and absolute child positioning.</li>
 * </ul>
 *
 * @author radek
 */
public abstract class LayoutManager
{
    /** The box whose children this manager arranges. */
    protected ElementBox owner;

    protected LayoutManager(ElementBox owner)
    {
        this.owner = owner;
    }

    /**
     * Arranges the owner box's children within the available width.
     * This mirrors the contract of {@link Box#doLayout(float, boolean, boolean)}.
     *
     * @param availw    available width from the containing block
     * @param force     force placement even when the box does not fit
     * @param linestart {@code true} if this box starts a new line
     * @return {@code true} if the box was successfully placed
     */
    public abstract boolean layout(float availw, boolean force, boolean linestart);

    /**
     * Returns the minimum content width needed by the children (used for
     * shrink-to-fit width computation).
     *
     * @return the minimal content width
     */
    public abstract float getMinimalContentWidth();

    /**
     * Returns the maximum content width the children could use (used for
     * table column width computation).
     *
     * @return the maximal content width
     */
    public abstract float getMaximalContentWidth();

    /**
     * Second layout pass: resolves absolute and fixed child positions after
     * the main layout pass has completed.
     * The default implementation is a no-op; override in managers that place
     * absolutely or fixed-positioned children.
     */
    public void absolutePositions()
    {
    }

    /**
     * Returns the owner box.
     *
     * @return the owner box
     */
    public ElementBox getOwner()
    {
        return owner;
    }

    // -----------------------------------------------------------------------
    // Shared helpers used by both block and inline layout
    // -----------------------------------------------------------------------

    /**
     * Calculates the position for a floating child box in the context of the
     * owner block.
     *
     * @param subbox the floating box to place
     * @param wlimit available width of the owner's content area
     * @param stat   current layout status (updated in place)
     */
    protected void layoutBlockFloating(BlockBox subbox, float wlimit, BlockLayoutStatus stat)
    {
        BlockBox block = (BlockBox) owner;
        subbox.setFloats(new FloatList(subbox), new FloatList(subbox), 0, 0, 0);
        subbox.doLayout(wlimit, true, true);
        FloatList f  = (subbox.getFloating() == BlockBox.FLOAT_LEFT) ? block.fleft : block.fright;
        FloatList of = (subbox.getFloating() == BlockBox.FLOAT_LEFT) ? block.fright : block.fleft;
        float  floatX  = (subbox.getFloating() == BlockBox.FLOAT_LEFT) ? block.floatXl : block.floatXr;
        float oFloatX  = (subbox.getFloating() == BlockBox.FLOAT_LEFT) ? block.floatXr : block.floatXl;

        float fy = stat.y + block.floatY;
        if (fy < f.getLastY()) fy = f.getLastY();

        float fx = f.getWidth(fy);
        if (fx < floatX) fx = floatX;
        if (fx == 0 && floatX < 0) fx = floatX;

        float ofx = of.getWidth(fy);
        if (ofx < oFloatX) ofx = oFloatX;
        if (ofx == 0 && oFloatX < 0) ofx = oFloatX;

        while ((fx > floatX || ofx > oFloatX || stat.inlineWidth > 0)
               && (stat.inlineWidth + fx - floatX + ofx - oFloatX + subbox.getWidth() > wlimit))
        {
            float nexty = FloatList.getNextY(block.fleft, block.fright, fy);
            if (nexty == -1)
                fy += Math.max(stat.maxh, block.getLineHeight());
            else
                fy = nexty;
            fx = f.getWidth(fy);
            if (fx < floatX) fx = floatX;
            if (fx == 0 && floatX < 0) fx = floatX;
            ofx = of.getWidth(fy);
            if (ofx < oFloatX) ofx = oFloatX;
            if (ofx == 0 && oFloatX < 0) ofx = oFloatX;
            stat.inlineWidth = 0;
        }

        subbox.setPosition(fx, fy);
        f.add(subbox);
        float floatw = block.maxFloatWidth(fy, fy + subbox.getHeight());
        if (floatw > stat.maxw) stat.maxw = floatw;
        if (stat.maxw > wlimit) stat.maxw = wlimit;
    }

    /**
     * Positions an absolutely or fixed-positioned child box.
     *
     * @param subbox the positioned box to lay out
     * @param stat   current layout status (read-only for this call)
     */
    protected void layoutBlockPositioned(BlockBox subbox, BlockLayoutStatus stat)
    {
        BlockBox block = (BlockBox) owner;
        float wlimit = block.availwidth;
        if (block.leftset) wlimit -= block.coords.left;
        if (block.rightset) wlimit -= block.coords.right;
        subbox.setFloats(new FloatList(subbox), new FloatList(subbox), 0, 0, 0);
        subbox.doLayout(wlimit, true, true);
    }

    /**
     * Positions a normally-flowing block child in the context of the owner block.
     * Passes the owner block's float lists down so the child can arrange its own
     * children relative to any surrounding floats.
     *
     * @param subbox the in-flow block child to lay out
     * @param wlimit available content width
     * @param stat   current layout status (updated in place)
     */
    protected void layoutBlockInFlow(BlockBox subbox, float wlimit, BlockLayoutStatus stat)
    {
        BlockBox block = (BlockBox) owner;
        float newfloatXl = block.floatXl + subbox.margin.left
                            + subbox.border.left + subbox.padding.left;
        float newfloatXr = block.floatXr + subbox.margin.right
                            + subbox.border.right + subbox.padding.right;
        float newfloatY  = block.floatY  + subbox.emargin.top
                            + subbox.border.top + subbox.padding.top;
        if (subbox.position == ElementBox.POS_RELATIVE)
        {
            float dx = subbox.leftset ? subbox.coords.left : (-subbox.coords.right);
            float dy = subbox.topset  ? subbox.coords.top  : (-subbox.coords.bottom);
            newfloatXl += dx;
            newfloatXr -= dx;
            newfloatY  += dy;
        }
        if (newfloatXl < 0) newfloatXl = 0;
        if (newfloatXr < 0) newfloatXr = 0;
        subbox.setFloats(block.fleft, block.fright, newfloatXl, newfloatXr, stat.y + newfloatY);
        subbox.setPosition(0, stat.y);
        subbox.doLayout(wlimit, true, true);
        stat.y += subbox.getHeight();
        if (subbox.getWidth() > stat.maxw)
            stat.maxw = subbox.getWidth();
    }

    /**
     * Positions a normally-flowing block child, moving it down past floats if
     * needed so it is not obscured by them.
     * See http://www.w3.org/TR/CSS22/visuren.html#bfc-next-to-float
     *
     * @param subbox the in-flow block child to lay out
     * @param wlimit available content width
     * @param stat   current layout status (updated in place)
     */
    protected void layoutBlockInFlowAvoidFloats(BlockBox subbox, float wlimit, BlockLayoutStatus stat)
    {
        BlockBox block = (BlockBox) owner;
        final float minw = subbox.getMinimalDecorationWidth();
        float yoffset = stat.y + block.floatY;
        float availw = 0;
        do
        {
            float fy  = yoffset;
            float flx = block.fleft.getWidth(fy)  - block.floatXl;
            if (flx < 0) flx = 0;
            float frx = block.fright.getWidth(fy) - block.floatXr;
            if (frx < 0) frx = 0;
            float avail = wlimit - flx - frx;

            final float startfy = fy;
            while ((flx > block.floatXl || frx > block.floatXr) && (minw > avail))
            {
                float nexty = FloatList.getNextY(block.fleft, block.fright, fy);
                if (nexty == -1)
                    fy += Math.max(stat.maxh, block.getLineHeight());
                else
                    fy = nexty;
                flx = block.fleft.getWidth(fy)  - block.floatXl;
                if (flx < 0) flx = 0;
                frx = block.fright.getWidth(fy) - block.floatXr;
                if (frx < 0) frx = 0;
                avail = wlimit - flx - frx;
            }
            if (fy > startfy && subbox.margin.top != 0)
            {
                fy -= subbox.margin.top;
                if (fy < startfy) fy = startfy;
            }
            stat.y = fy - block.floatY;

            subbox.setFloats(new FloatList(subbox), new FloatList(subbox), 0, 0, 0);
            subbox.setPosition(flx, stat.y);
            subbox.setWidthAdjust(-flx - frx);
            subbox.doLayout(avail, true, true);

            float xlimit[] = block.computeFloatLimits(fy, fy + subbox.getBounds().height, new float[]{flx, frx});
            availw = wlimit - xlimit[0] - xlimit[1];
            if (minw > availw)
                yoffset = FloatList.getNextY(block.fleft, block.fright, fy);
        } while (minw > availw && yoffset != -1);

        stat.y += subbox.getHeight();
        if (subbox.getWidth() > stat.maxw)
            stat.maxw = subbox.getWidth();
    }
}
