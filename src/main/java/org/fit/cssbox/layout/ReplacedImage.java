/*
 * ReplacedImage.java
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
 * Created on 4. prosinec 2005, 21:01
 */

package org.fit.cssbox.layout;

import java.net.MalformedURLException;
import java.net.URL;

import org.fit.net.DataURLHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an image as the contents of a replaced element.
 * 
 * @author radek
 */
public class ReplacedImage extends ReplacedContent
{
    private static Logger log = LoggerFactory.getLogger(ReplacedImage.class);

    /** Default image width used when there is no image data available */
    public static final int DEFAULT_IMAGE_WIDTH = 20;
    /** Default image height used when there is no image data available */
    public static final int DEFAULT_IMAGE_HEIGHT = 20;
    
    private URL base; //base url
    private URL url; //image url
    private VisualContext ctx; //visual context
    private ContentImage image; //contained image

    /**
     * Creates a new instance of ReplacedImage. 
     * 
     * @param owner
     *            the owning Box.
     * @param ctx
     *            the visual context applied during rendering.
     * @param baseurl
     *            the base url used for loading images from.
     * @param src
     *            the source URL
     * 
     * @see ElementBox
     * @see VisualContext
     * @see URL
     */
    public ReplacedImage(ElementBox owner, VisualContext ctx, URL baseurl, String src)
    {
        super(owner);
        this.ctx = ctx;
        base = baseurl;
        try
        {
            url = DataURLHandler.createURL(base, src);
        } catch (MalformedURLException e) {
            url = null;
            log.error("URL: " + e.getMessage());
        }
    }

    public VisualContext getVisualContext()
    {
        return ctx;
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

    @Override
    public float getIntrinsicWidth()
    {
        if (getImage() != null)
            return getImage().getWidth();
        else
            return DEFAULT_IMAGE_WIDTH;
    }

    @Override
    public float getIntrinsicHeight()
    {
        if (getImage() != null)
            return getImage().getHeight();
        else
            return DEFAULT_IMAGE_HEIGHT;
    }

    @Override
    public float getIntrinsicRatio()
    {
        return getIntrinsicWidth() / getIntrinsicHeight();
    }

    @Override
    public String toString()
    {
        return "ReplacedImage [url=" + url + "]";
    }

    
}
