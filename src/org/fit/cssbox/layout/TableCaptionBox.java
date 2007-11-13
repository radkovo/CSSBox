/**
 * TableCaptionBox.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Created on 29.9.2006, 14:12:05 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics;

import org.w3c.dom.Element;

/**
 * A box that contains the table caption. 
 * @author burgetr
 */
public class TableCaptionBox extends BlockBox
{

    /**
     * Create a new table caption
     */
    public TableCaptionBox(Element n, Graphics g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
    }

    /**
     * Create a new table caption from an inline box
     */
    public TableCaptionBox(InlineBox src)
    {
        super(src);
        isblock = true;
    }
    
}
