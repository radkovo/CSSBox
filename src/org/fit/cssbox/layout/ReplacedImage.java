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
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class represents an image as the contents of a replaced element.
 * 
 * @author radek
 * @author petsof
 */
public class ReplacedImage extends ReplacedContent implements ImageObserver
{
    /** Default image width of 20 px, used when there are no image data */
    public static final int DEFAULT_IMAGE_WIDTH = 20;
    /** Default image height of 20 px, used when there are no image data */
    public static final int DEFAULT_IMAGE_HEIGHT = 20;
    /** Indicates whether to load images automatically */
    private static boolean LOAD_IMAGES = true;

    private URL base; // document base url
    private URL url; // image url
    private VisualContext ctx; // visual context
    private Image image;// the loaded image
    private ImageObserver observer;
    private int width = -1;
    private int height = -1;

    private boolean abort; // error or abort flag during loading in image
                           // observer
    private boolean caching; // use picture caching ?
    private Container container; // Component's container, for repaint
    private Rectangle tmpRectangle; // do not allocate, reuse...
    private Toolkit toolkit; // system default toolkit

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
        this.base = baseurl;
        this.observer = this;
        this.toolkit = Toolkit.getDefaultToolkit();
        this.caching = true;
        this.container = null;
        this.abort = false;

        try {
            String src = getOwner().getElement().getAttribute("src");
            url = new URL(base, src);
            if (LOAD_IMAGES)
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
     * Sets the component's container used by internal {@link ImageObserver}
     * when there is need to repaint (e.g. animated gif). If not set, rendering
     * may not work correctly. Null does not cause {@link NullPointerException}
     * 
     * @param container
     *            the new container
     * @see ImageObserver
     * @see Container
     */
    public void setContainer(Container container)
    {
        this.container = container;
    }

    /**
     * Gets the container. May return null.
     * 
     * @return the container
     */
    public Container getContainer()
    {
        return this.container;
    }

    /**
     * Fires repaint event within t milliseconds.
     * 
     * @param t
     *            the time to repaint within.
     * 
     * @see java.awt.Component#repaint(long, int, int, int, int)
     */
    protected void repaint(int t)
    {
        if (container == null) return;
        tmpRectangle = owner.getAbsoluteBounds();
        container.repaint(t, tmpRectangle.x, tmpRectangle.y, tmpRectangle.width, tmpRectangle.height);
    }

    private Image loadImage(boolean cache)
    {
        // TODO doplnit sem parameter URL url, aby to pracovalo podla parametra
        // !!!
        // supported : gif, jpg, png

        // SunToolkit
        // ToolkitImage

        Image img;
        if (url != null)
        {
            if (cache)
            {
                // get image and cache
                img = toolkit.getImage(url);
            }
            else
            {
                // do not cache, just get image
                img = toolkit.createImage(url);
            }

            // start loading and preparation
            toolkit.prepareImage(img, -1, -1, observer);
            return img;
        }
        return null;
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

    /**
     * Checks if is caching.
     * 
     * @return true, if is caching
     * 
     * @see ReplacedImage#setCaching(boolean)
     */
    public boolean isCaching()
    {
        return caching;
    }

    /**
     * Sets the caching. If set to true, image is checked, if present in cache.
     * If not, it is loaded and placed to cache. If set to False image is just
     * loaded, any cache and caching is avoided.
     * 
     * @param val
     *            indicates, whether use cache. Default is true.
     * @see Toolkit#getImage(URL)
     * @see Toolkit#createImage(URL)
     */
    public void setCaching(boolean val)
    {
        caching = val;
    }

    /**
     * Gets the url.
     * 
     * @return the url of the image.
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * Gets the image.
     * 
     * @return the image.
     */
    public Image getImage()
    {
        return image;
    }

    /**
     * Gets the loaded image as BufferedImage object. May return null if no data
     * available.
     * 
     * @return the buffered image.
     */
    public BufferedImage getBufferedImage()
    {
        if (image == null || abort) return null;

        BufferedImage img = new BufferedImage(getIntrinsicWidth(), getIntrinsicHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(image, null, null);
        g.dispose();

        return img;
    }

    /**
     * Sets the image observer.
     * 
     * @param observer
     *            the new image observer.
     * @see ImageObserver
     */
    public void setImageObserver(ImageObserver observer)
    {
        this.observer = observer;
    }

    /**
     * Gets the image observer.
     * 
     * @return the image observer
     * @see ImageObserver
     */
    public ImageObserver getImageObserver()
    {
        return this.observer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fit.cssbox.layout.ReplacedContent#draw(java.awt.Graphics2D, int,
     * int)
     */
    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        tmpRectangle = getOwner().getAbsoluteContentBounds();

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
            g.drawImage(image, tmpRectangle.x, tmpRectangle.y, width, height,
                    observer);
        }
        else
        {
            ctx.updateGraphics(g);
            g.setStroke(new BasicStroke(1));
            g.drawRect(tmpRectangle.x, tmpRectangle.y, tmpRectangle.width - 1, tmpRectangle.height - 1);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fit.cssbox.layout.ReplacedContent#getIntrinsicHeight()
     */
    @Override
    public int getIntrinsicHeight()
    {
        if (LOAD_IMAGES)
        {
            if (image != null)
            {
                if (height > -1)
                {
                    return height;
                }
                else
                {
                    // wait for the width...
                    abort = false;
                    while (!abort && image != null && (height = image.getHeight(observer)) == -1)
                    {
                        try
                        {
                            Thread.sleep(25);
                        } catch (Exception e)
                        {
                            image = null;
                            abort = true;
                        }
                    }

                    if (height == -1) height = DEFAULT_IMAGE_HEIGHT;
                    return height;
                }
            }
            else
            {
                image = loadImage(caching);
                abort = false;
                while (!abort && image != null && (height = image.getHeight(observer)) == -1)
                {
                    try
                    {
                        Thread.sleep(25);
                    } catch (Exception e)
                    {
                        image = null;
                        abort = true;
                    }
                }
                if (height == -1) height = DEFAULT_IMAGE_HEIGHT;
                return height;// if there was an error, height == DEFAULT_IMAGE_HEIGHT
            }
        }
        else
        {
            return DEFAULT_IMAGE_HEIGHT;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fit.cssbox.layout.ReplacedContent#getIntrinsicWidth()
     */
    @Override
    public int getIntrinsicWidth()
    {
        if (LOAD_IMAGES)
        {
            if (image != null)
            {
                if (width > -1)
                {
                    return width;
                }
                else
                {
                    // wait for the width...
                    abort = false;
                    while (!abort && image != null && (width = image.getWidth(observer)) == -1)
                    {
                        try
                        {
                            Thread.sleep(25);
                        } catch (Exception e)
                        {
                            image = null;
                            abort = true;
                        }
                    }
                    if (width == -1) width = DEFAULT_IMAGE_WIDTH;
                    return width;
                }
            }
            else
            {
                image = loadImage(caching);
                abort = false;
                while (!abort && image != null
                        && (width = image.getWidth(observer)) == -1)
                {
                    try
                    {
                        Thread.sleep(25);
                    } catch (Exception e)
                    {
                        image = null;
                        abort = true;
                    }
                }
                if (width == -1) width = DEFAULT_IMAGE_WIDTH;
                return width;// if there was an error, height == DEFAULT_IMAGE_HEIGHT
            }
        }
        else
        {
            return DEFAULT_IMAGE_WIDTH;
        }

    }

    @Override
    public float getIntrinsicRatio()
    {
        return (float) getIntrinsicWidth() / (float) getIntrinsicHeight();
    }

    /**
     * Releases the image resources.
     */
    public void dispose()
    {
        if (image != null) image.flush();
        image = null;
    }

    /**
     * Resets all data to default state, releases the image resources.
     */
    public void reset()
    {
        abort = false;
        width = -1;
        height = -1;
        if (image != null) image.flush();
        image = null;
    }

    /**
     * Aborts waiting for image data to determine width and/or height. This
     * method is called from ImageObserver. If custom ImageObserver is provided
     * and ABORT or ERROR flags are set during loading, call this method to
     * abort waiting. Otherwise infinite loop will be created, resulting in
     * dead-lock.
     * 
     * @see ImageObserver
     * @see ImageObserver#ABORT
     * @see ImageObserver#ERROR
     */
    public synchronized void abort()
    {
        abort = true;
        image = null;
    }

    public boolean imageUpdate(Image img, int flags, int x, int y, int newWidth, int newHeight)
    {
        // http://www.permadi.com/tutorial/javaImgObserverAndAnimGif/
        if (image == null || image != img) { return false; }

        // error
        if ((flags & (ABORT | ERROR)) != 0)
        {
            synchronized (this)
            {
                if (image == img)
                {
                    abort();
                }
            }

            repaint(0);
            return false;
        }

        // Repaint when done or when new pixels arrive:
        // FRAMEBITS - another complete frame of a multi-frame image which was
        // previously drawn is now available to be drawn again.
        // ALLBITS - static image which was previously drawn is now complete and
        // can be drawn again in its final form.
        if ((flags & (FRAMEBITS | ALLBITS)) != 0)
        {
            repaint(0);
        }

        // hint : provide some "Loading..." animation...
        // else if ((flags & SOMEBITS) != 0) {}
        return ((flags & ALLBITS) == 0) /* && owner.isVisible() */;
    }

}
