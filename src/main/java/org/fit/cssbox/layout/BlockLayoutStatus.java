/*
 * BlockLayoutStatus.java
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
 * Tracks the mutable state accumulated while laying out the children of a
 * block-level box. An instance of this class is created at the start of a
 * block or inline layout pass and updated as each child box is positioned.
 *
 * <p>Previously defined as an inner class of {@link BlockBox}; extracted as a
 * top-level class to make it accessible from layout-manager classes.
 *
 * @author radek
 */
class BlockLayoutStatus
{
    /** Width of inline boxes currently placed on the line. */
    public float inlineWidth;

    /** Current <em>y</em> coordinate relative to the owner's content box. */
    public float y;

    /** Maximal width of the boxes laid out so far. */
    public float maxw;

    /** Maximal height of boxes laid out on the current line. */
    public float maxh;

    /** First placed non-empty in-flow box (for top margin collapsing). */
    public BlockBox firstseparated;

    /** Last placed non-empty in-flow box (for bottom margin collapsing). */
    public BlockBox lastseparated;

    /** Last placed in-flow box (for margin-collapsing bookkeeping). */
    public BlockBox lastinflow;

    /** Creates a new, zeroed-out layout status. */
    public BlockLayoutStatus()
    {
        inlineWidth = 0;
        y = 0;
        maxw = 0;
        maxh = 0;
        firstseparated = null;
        lastseparated = null;
        lastinflow = null;
    }
}
