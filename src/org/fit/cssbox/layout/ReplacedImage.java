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

/**
 * This class represents an image as the contents of a replaced element.
 * 
 * @author radek
 * @author petsof
 */
public class ReplacedImage extends ContentImage
{
    /** Default image width of 20 px, used when there are no image data */
    protected final int DEFAULT_IMAGE_WIDTH = 20;
    /** Default image height of 20 px, used when there are no image data */
    protected final int DEFAULT_IMAGE_HEIGHT = 20;
    
    /** Indicates whether to load images automatically */
    private static boolean LOAD_IMAGES = true;

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
     *            the url to load image from.
     * 
     * @see ElementBox
     * @see VisualContext
     * @see URL
     */
    public ReplacedImage(ElementBox owner, VisualContext ctx, URL baseurl)
    {
        super(owner);
        this.ctx = ctx;
        this.loadImages = LOAD_IMAGES;
        this.base = baseurl;

        try {
            String src = getOwner().getElement().getAttribute("src");
            url = new URL(base, src);
            if (loadImages)
            {
                // get image object (may not have picture data)
                image = loadImage(caching);
            }
        } catch (MalformedURLException e) {
            System.err.println("ImgBox: URL: " + e.getMessage());
            image = null;
            url = null;
        } catch (IllegalArgumentException e) {
            System.err.println("ImgBox: Format error: " + e.getMessage());
            image = null;
        }

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

}
