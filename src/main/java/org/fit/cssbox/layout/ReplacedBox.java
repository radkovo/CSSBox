/**
 * ReplacedBox.java
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
 * Created on 6.2.2009, 16:48:52 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

/**
 * Common interface for both the inline and block replaced boxes
 * 
 * @author burgetr
 */
public interface ReplacedBox
{
    
    /**
     * @return the content object
     */
    public ReplacedContent getContentObj();

    /**
     * @param obj the obj to set
     */
    public void setContentObj(ReplacedContent obj);
    
    /**
     * Draws the box replaced content.
     * @param g
     */
    public void drawContent(Graphics2D g);

}
