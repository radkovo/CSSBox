/*
 * InlineElement.java
 * Copyright (c) 2005-2011 Radek Burget
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
 * Created on 19.10.2011, 11:25:26 by burgetr
 */
package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;

/**
 * This interface defines the properties common for all the inline-level element boxes
 * 
 * @author burgetr
 */
public interface InlineElement extends Inline
{

    /**
     * Obtains the vertical alignment of the element as specified in the style.
     * @return a vertical alignment value
     */
    public CSSProperty.VerticalAlign getVerticalAlign();

    /**
     * Assigns the line box assigned to this inline box and all the inline sub-boxes.
     * @param linebox The assigned linebox.
     */
    public void setLineBox(LineBox linebox);
    
    /**
     * Returns the line box used for positioning this element.
     */
    public LineBox getLineBox();
    
    /**
     * Obtains the offset of the content edge from the line box top
     * @return the difference between the content edge and the top of the line box in pixels. Positive numbers mean the content box is inside the line box.  
     */
    public int getLineboxOffset();
    
}
