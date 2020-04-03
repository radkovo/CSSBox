/*
 * ElementBackground.java
 * Copyright (c) 2005-2020 Radek Burget
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
 * Created on 3. 4. 2020, 12:05:06 by burgetr
 */
package org.fit.cssbox.render;

import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.Viewport;

/**
 * A representation of an element background used for rendering.
 * 
 * @author burgetr
 */
public class ElementBackground
{
    private ElementBox owner;
    private boolean viewportOwner;
    private Rectangle bounds;
    private Rectangle clipped;
    
    
    public ElementBackground(ElementBox owner)
    {
        this.owner = owner;
        this.viewportOwner = (owner instanceof Viewport);
        bounds = owner.getAbsoluteBorderBounds();
        clipped = owner.getClippedBounds();
        if (viewportOwner)
            bounds = clipped;  //for the root box (Viewport), use the whole clipped content (not only the visible part)
        clipped = new Rectangle(clipped.x - bounds.x, clipped.y - bounds.y, clipped.width, clipped.height); //make the clip relative to the background bounds
    }
    
    public ElementBox getOwner()
    {
        return owner;
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public Rectangle getClipped()
    {
        return clipped;
    }
    
    public boolean isViewportOwner()
    {
        return viewportOwner;
    }

    public boolean isZeroSize()
    {
        return bounds.getWidth() <= 0 || bounds.getHeight() <= 0;
    }
    
}
