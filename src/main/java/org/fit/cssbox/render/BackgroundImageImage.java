/*
 * BackgroundImageImage.java
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
 * Created on 2. 4. 2020, 13:09:02 by burgetr
 */
package org.fit.cssbox.render;

import java.net.URL;

import org.fit.cssbox.layout.BackgroundImage;
import org.fit.cssbox.layout.ContentImage;
import org.fit.cssbox.layout.ElementBox;

import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundOrigin;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;
import cz.vutbr.web.css.CSSProperty.BackgroundSize;
import cz.vutbr.web.css.TermList;

/**
 * A background image that is really created with an image.
 * 
 * @author burgetr
 */
public class BackgroundImageImage extends BackgroundImage
{
    /** Default image width used when there is no image data available */
    public static final int DEFAULT_IMAGE_WIDTH = 20;
    /** Default image height used when there is no image data available */
    public static final int DEFAULT_IMAGE_HEIGHT = 20;
    
    private URL url; //image url
    private ContentImage image; //contained image
    

    public BackgroundImageImage(ElementBox owner, URL url, BackgroundPosition position, TermList positionValues,
            BackgroundRepeat repeat, BackgroundAttachment attachment, BackgroundOrigin origin,
            BackgroundSize size, TermList sizeValues)
    {
        super(owner, url, position, positionValues, repeat, attachment, origin, size, sizeValues);
        this.url = url;
    }
    
    /**
     * Obtains the source URL of the image.
     * @return the source URL
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * Obtains the actual image used for creating this background image.
     * @return an image representation
     */
    public ContentImage getImage()
    {
        return image;
    }

    public void setImage(ContentImage image)
    {
        this.image = image;
    }

    public float getIntrinsicWidth()
    {
        if (getImage() != null)
            return getImage().getWidth();
        else
            return DEFAULT_IMAGE_WIDTH;
    }

    public float getIntrinsicHeight()
    {
        if (getImage() != null)
            return getImage().getHeight();
        else
            return DEFAULT_IMAGE_HEIGHT;
    }

    public float getIntrinsicRatio()
    {
        return getIntrinsicWidth() / getIntrinsicHeight();
    }

    @Override
    public boolean hasIntrinsicWidth()
    {
        return true;
    }

    @Override
    public boolean hasIntrinsicHeight()
    {
        return true;
    }

    @Override
    public boolean hasIntrinsicRatio()
    {
        return true;
    }

}
