/*
 * BackgroundImage.java
 * Copyright (c) 2005-2012 Radek Burget
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
 * Created on 11.6.2012, 14:47:16 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.net.URL;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;

/**
 * An image placed at the element background together with its position and repeating.
 * 
 * @author burgetr
 */
public class BackgroundImage extends ContentImage
{
    /** Indicates whether to load images automatically */
    private static boolean LOAD_IMAGES = true;

    private CSSProperty.BackgroundPosition position;
    
    private CSSProperty.BackgroundRepeat repeat;
    
    private CSSProperty.BackgroundAttachment attachment;

    
    
    public BackgroundImage(ElementBox owner, URL url, BackgroundPosition position, BackgroundRepeat repeat, BackgroundAttachment attachment)
    {
        super(owner);
        this.loadImages = LOAD_IMAGES;
        this.url = url;
        this.position = position;
        this.repeat = repeat;
        this.attachment = attachment;
    }

    /**
     * Switches automatic image data downloading on or off.
     * 
     * @param b
     *            when set to <code>true</code>, the images are automatically
     *            loaded from the server. When set to <code>false</code>, the
     *            images are not loaded and the corresponding box is displayed
     *            empty. When the image loading is switched off, the box size
     *            can be only determined from the element attributes or style.
     *            The default value is on.
     */
    public static void setLoadImages(boolean b)
    {
        LOAD_IMAGES = b;
    }

    /**
     * Gets the load images.
     * 
     * @return the load images
     */
    public static boolean getLoadImages()
    {
        return LOAD_IMAGES;
    }
    
    public CSSProperty.BackgroundPosition getPosition()
    {
        return position;
    }

    public CSSProperty.BackgroundRepeat getRepeat()
    {
        return repeat;
    }

    public CSSProperty.BackgroundAttachment getAttachment()
    {
        return attachment;
    }

    //===========================================================================
    
    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        Rectangle bounds = getOwner().getAbsoluteBackgroundBounds();
        g.drawImage(image, bounds.x, bounds.y, getIntrinsicWidth(), getIntrinsicHeight(), observer);
    }
    
    
}
