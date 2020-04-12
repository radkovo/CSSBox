/*
 * StructuredRenderer.java
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
 * Created on 11. 4. 2020, 18:02:20 by burgetr
 */
package org.fit.cssbox.render;

import org.fit.cssbox.css.BackgroundDecoder;
import org.fit.cssbox.layout.BlockTableBox;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.TableCellBox;
import org.fit.cssbox.layout.Viewport;

/**
 * A base implementation of a renderer that automates the most common actions.
 * 
 * @author burgetr
 */
public abstract class StructuredRenderer implements BoxRenderer
{
    private Viewport vp;
    private ElementBox bgSource; //viewport background source element
    private BackgroundDecoder vpBackground;
    
    
    @Override
    public void init(Viewport vp)
    {
        this.vp = vp;
        findViewportBackgroundSource();
    }
    
    /**
     * Returns the viewport for which the renderer was initialized.
     * @return The viewport box.
     */
    public Viewport getViewport()
    {
        return vp;
    }
    
    /**
     * Returns the element box used for obtaining the viewport background. It may be the
     * HTML or BODY element for HTML mode, the root element for non-HTML mode.
     * @return The background source element or {@code null} when the box tree is empty.
     */
    public ElementBox getViewportBackgroundSource()
    {
        return bgSource;
    }
    
    /**
     * Returns a representation of the viewport background.
     * @return the corresponding background decoder 
     */
    public BackgroundDecoder getViewportBackground()
    {
        return vpBackground;
    }
    
    //================================================================================================
    
    /**
     * Finds the element box that provides the background for the given element box. Normally,
     * it is the element box itself but there are certain special cases such as the viewport
     * or table elements.
     * @param elem The element to find the background source for.
     * @return the background decoder with the appropriate source or {@code null} when no
     * background should be drawn for the given element.
     */
    protected BackgroundDecoder findBackgroundSource(ElementBox elem)
    {
        if (elem == vp)
        {
            return vpBackground;
        }
        else if (elem == bgSource)
        {
            // do not draw the background for the original viewport background source
            return null;
        }
        else if (elem instanceof BlockTableBox)
        {
            // anonymous table box has never a background
            return null;
        }
        else if (elem instanceof TableCellBox)
        {
            //if no background is specified for the cell, we try to use the row, row group, column, column group
            final TableCellBox cell = (TableCellBox) elem;
            BackgroundDecoder bg = new BackgroundDecoder(elem);
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerRow());
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerColumn());
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerRow().getOwnerBody());
            
            if (bg.isBackgroundEmpty())
                return null; //nothing found
            else
                return bg;
        }
        else
        {
            // other elements - use the element itself
            return new BackgroundDecoder(elem);
        }
    }
    
    /**
     * Finds the background source element according to
     * https://www.w3.org/TR/CSS2/colors.html#background
     */
    protected void findViewportBackgroundSource()
    {
        ElementBox root = getViewport().getRootBox();
        if (root != null)
        {
            BackgroundDecoder rootBg = new BackgroundDecoder(root);
            
            if (rootBg.isBackgroundEmpty() && getViewport().getConfig().getUseHTML())
            {
                // For HTML, try to use the body if there is no background set for the root box
                ElementBox body = getViewport().getElementBoxByName("body", false);
                if (body != null)
                {
                    bgSource = body;
                    vpBackground = new BackgroundDecoder(body);
                }
                else
                {
                    bgSource = root;
                    vpBackground = rootBg;
                }
            }
            else
            {
                bgSource = root;
                vpBackground = rootBg;
            }
        }
    }

    
}
