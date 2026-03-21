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

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;

import org.w3c.dom.Element;

/**
 * This class represents the anonymous box created for a block-level table.
 * @author burgetr
 */
public class BlockTableBox extends TableWrapperBox
{
    public BlockTableBox(Element n, VisualContext ctx)
    {
        super(n, ctx);
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

    //======================================================================================================

    @Override
    public void initBox()
    {
        organizeContent();
        loadCaptionStyle();
    }

    @Override
    public boolean doLayout(float availw, boolean force, boolean linestart)
    {
        setAvailableWidth(availw);
        float x1 = fleft.getWidth(floatY) - floatXl;
        float x2 = fright.getWidth(floatY) - floatXr;
        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        float wlimit = getAvailableContentWidth() - x1 - x2;
        doTableWrapperLayout(wlimit, x1);
        return true;
    }

    @Override
    protected void computeWidths(TermLengthOrPercent width, boolean auto, boolean exact, boolean update)
    {
        // anonymous table box has always an 'auto' width in the beginning. After the layout, the width is updated
        // according to the resulting table (and caption) width
        if (!widthComputed)
            super.computeWidths(null, true, exact, update);
        else
            super.computeWidths(CSSFactory.getTermFactory().createLength(content.width, TermLength.Unit.px), false, exact, update);
    }

}
