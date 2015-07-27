/*
 * ReplacedImage.java
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
 * Created on 4. prosinec 2005, 21:01
 */

package org.fit.cssbox.layout;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import org.fit.net.DataURLHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an image as the contents of a replaced element.
 * 
 * @author radek
 * @author petsof
 */
public class ReplacedImage extends ContentImage
{
    private static Logger log = LoggerFactory.getLogger(ReplacedImage.class);

    /** Default image width of 20 px, used when there are no image data */
    protected final int DEFAULT_IMAGE_WIDTH = 20;
    /** Default image height of 20 px, used when there are no image data */
    protected final int DEFAULT_IMAGE_HEIGHT = 20;
    
    protected URL base;
    protected VisualContext ctx; //visual context

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
        this.loadImages = owner.getViewport().getConfig().getLoadImages();
        this.base = baseurl;

        try {
            url = DataURLHandler.createURL(base, src);
            if (loadImages && !src.trim().isEmpty())
            {
                // get image object (may not have picture data)
                image = loadImage(caching);
            }
        } catch (MalformedURLException e) {
            log.error("URL: " + e.getMessage());
            image = null;
            url = null;
        } catch (IllegalArgumentException e) {
            log.error("Format error: " + e.getMessage());
            image = null;
        }

    }

    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        Rectangle bounds = getOwner().getAbsoluteContentBounds();

        if (image != null)
        {
            // *_SOME_ transparent animated gifs*,
            // may not erase previous frame of animation
            // well, repainting with a parent's graphics is not working...
            // seems to be java-related, repaint-animated-gif problem.

            // workaround:
            // owner has already set clipping, so we can render parent's
            // background
            // owner.getVisualContext().getParentContext().updateGraphics(g);
            // owner.getParent().drawBackground(g);

            // now update our configuration
            ctx.updateGraphics(g);

            // no container that would repaint -- wait for the complete image
            if (container == null)
                waitForLoad();
            // draw image
            g.drawImage(image, bounds.x, bounds.y, width, height, observer);
        }
        else
        {
            ctx.updateGraphics(g);
            g.setStroke(new BasicStroke(1));
            g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
        }

    }

    @Override
    public String toString()
    {
        return "ReplacedImage [url=" + url + "]";
    }
    
}
