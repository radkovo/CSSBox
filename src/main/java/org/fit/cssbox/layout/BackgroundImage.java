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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;
import cz.vutbr.web.css.CSSProperty.BackgroundSize;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;

/**
 * An image placed at the element background together with its position and repeating.
 * 
 * @author burgetr
 */
public class BackgroundImage extends ContentImage
{
    private static Logger log = LoggerFactory.getLogger(BackgroundImage.class);
    
    private CSSProperty.BackgroundPosition position;
    private CSSProperty.BackgroundRepeat repeat;
    private CSSProperty.BackgroundAttachment attachment;
    private CSSProperty.BackgroundSize size;
    private TermList positionValues;
    private TermList sizeValues;

    //the coordinates of the image within the element
    private int imgx;
    private int imgy;
    private int imgw;
    private int imgh;
    private boolean repeatx;
    private boolean repeaty;
    
    
    public BackgroundImage(ElementBox owner, URL url, BackgroundPosition position, TermList positionValues, 
                            BackgroundRepeat repeat, BackgroundAttachment attachment,
                            BackgroundSize size, TermList sizeValues)
    {
        super(owner);
        this.loadImages = owner.getViewport().getConfig().getLoadBackgroundImages();
        this.url = url;
        this.position = position;
        this.positionValues = positionValues;
        this.size = size;
        this.sizeValues = sizeValues;
        this.repeat = repeat;
        this.attachment = attachment;
        if (loadImages)
            image = loadImage(caching);
        repeatx = (repeat == BackgroundRepeat.REPEAT || repeat == BackgroundRepeat.REPEAT_X);
        repeaty = (repeat == BackgroundRepeat.REPEAT || repeat == BackgroundRepeat.REPEAT_Y);
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
    
    public CSSProperty.BackgroundSize getSize()
    {
        return size;
    }

    //===========================================================================
    
    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        Rectangle bounds = getOwner().getAbsoluteBackgroundBounds();
        computeCoordinates(bounds);
        drawScaledImage(g, image, bounds.x + imgx, bounds.y + imgy, observer);
    }
    
    @Override
    public BufferedImage getBufferedImage()
    {
        if (image == null || abort)
            return null;

        //image = new ImageIcon(image).getImage();
        image = loadImage(caching);
        // no container that would repaint -- wait for the complete image
        if (container == null)
            waitForLoad();
        
        Rectangle bounds = getOwner().getAbsoluteBackgroundBounds();
        Rectangle clipped = getOwner().getClippedBounds();
        if (getOwner() instanceof Viewport)
            bounds = clipped;  //for the root box (Viewport), use the whole clipped content (not only the visible part)
        clipped = new Rectangle(bounds.x - clipped.x, bounds.y - clipped.y, clipped.width, clipped.height); //make the clip relative to the background bounds
        if (bounds.width > 0 && bounds.height > 0)
        {
            computeCoordinates(bounds);
            BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            
            if (repeatx && repeaty)
                drawRepeatBoth(g, imgx, imgy, bounds.width, bounds.height, clipped);
            else if (repeatx)
                drawRepeatX(g, imgx, imgy, bounds.width, clipped);
            else if (repeaty)
                drawRepeatY(g, imgx, imgy, bounds.height, clipped);
            else
                drawScaledImage(g, image, imgx, imgy, observer);
            
            g.dispose();
    
            return img;
        }
        else
            return null;
    }
    
    private void drawRepeatX(Graphics2D g, int sx, int sy, int limit, Rectangle clip)
    {
        int width = imgw;
        int height = imgh;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (width > 0)
        {
            for (int x = sx; x < limit; x += width)
            {
                r.setLocation(x, sy);
                if (r.intersects(clip))
                    drawScaledImage(g, image, x, sy, observer);
            }
            for (int x = sx - width; x + width - 1 >= 0; x -= width)
            {
                r.setLocation(x, sy);
                if (r.intersects(clip))
                    drawScaledImage(g, image, x, sy, observer);
            }
        }
        
    }
    
    private void drawRepeatY(Graphics2D g, int sx, int sy, int limit, Rectangle clip)
    {
        int width = imgw;
        int height = imgh;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (height > 0)
        {
            for (int y = sy; y < limit; y += height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawScaledImage(g, image, sx, y, observer);
            }
            for (int y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawScaledImage(g, image, sx, y, observer);
            }
        }
        
    }
    
    private void drawRepeatBoth(Graphics2D g, int sx, int sy, int limitx, int limity, Rectangle clip)
    {
        int width = imgw;
        int height = imgh;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (height > 0)
        {
            for (int y = sy; y < limity; y += height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawRepeatX(g, sx, y, limitx, clip);
            }
            for (int y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawRepeatX(g, sx, y, limitx, clip);
            }
        }
    }
    
    private void drawScaledImage(Graphics2D g, Image image, int x, int y, ImageObserver observer)
    {
        g.drawImage(image,
                    x, y, x + imgw, y + imgh,
                    0, 0, getIntrinsicWidth(), getIntrinsicHeight(),
                    observer);
    }
    
    //===========================================================================
    
    /**
     * @return <code>true</code> if the image is repeated in the X direction
     */
    public boolean isRepeatX()
    {
        return repeatx;
    }

    /**
     * @return <code>true</code> if the image is repeated in the Y direction
     */
    public boolean isRepeatY()
    {
        return repeaty;
    }

    /**
     * @return the imgx
     */
    public int getImgX()
    {
        return imgx;
    }

    /**
     * @return the imgy
     */
    public int getImgY()
    {
        return imgy;
    }
    
    public int getImgWidth()
    {
        return imgw;
    }

    public int getImgHeight()
    {
        return imgh;
    }

    /**
     * Computes the image coordinates within the padding box of the element.
     * After this, the coordinates may be obtained using {@link #getImgX()} and {@link #getImgY()}.
     */
    public void computeCoordinates()
    {
        computeCoordinates(getOwner().getAbsoluteBackgroundBounds());
    }
    
    protected void computeCoordinates(Rectangle bounds)
    {
        computeSize(bounds);
        CSSDecoder dec = new CSSDecoder(getOwner().getVisualContext());
        
        //X position
        if (position == BackgroundPosition.LEFT)
            imgx = 0;
        else if (position == BackgroundPosition.RIGHT)
            imgx = bounds.width - imgw;
        else if (position == BackgroundPosition.CENTER)
            imgx = (bounds.width - imgw) / 2;
        else if (position == BackgroundPosition.list_values)
        {
            imgx = dec.getLength((TermLengthOrPercent) positionValues.get(0), false, 0, 0, bounds.width - imgw);
        }
        else
            imgx = 0;
        
        //Y position
        if (position == BackgroundPosition.TOP)
            imgy = 0;
        else if (position == BackgroundPosition.BOTTOM)
            imgy = bounds.height - imgh;
        else if (position == BackgroundPosition.CENTER)
            imgy = (bounds.height - imgh) / 2;
        else if (position == BackgroundPosition.list_values)
        {
            int i = positionValues.size() > 1 ? 1 : 0;
            imgy = dec.getLength((TermLengthOrPercent) positionValues.get(i), false, 0, 0, bounds.height - imgh);
        }
        else
            imgy = 0;
        
        //System.out.println(url + ": x=" + imgx + " y=" + imgy);
    }

    protected void computeSize(Rectangle bounds)
    {
        CSSDecoder dec = new CSSDecoder(getOwner().getVisualContext());
        
        final float ir = getIntrinsicRatio();
        
        if (size == BackgroundSize.COVER)
        {
            int w1 = bounds.width;
            int h1 = Math.round(w1 / ir);
            int h2 = bounds.height;
            int w2 = Math.round(h2 * ir);
            if (h1 - bounds.height > w2 - bounds.width)
            {
                imgw = w1; imgh = h1;
            }
            else
            {
                imgw = w2; imgh = h2;
            }
        }
        else if (size == BackgroundSize.CONTAIN)
        {
            int w1 = bounds.width;
            int h1 = Math.round(w1 / ir);
            int h2 = bounds.height;
            int w2 = Math.round(h2 * ir);
            if (h1 - bounds.height < w2 - bounds.width)
            {
                imgw = w1; imgh = h1;
            }
            else
            {
                imgw = w2; imgh = h2;
            }
        }
        else if (size == BackgroundSize.list_values)
        {
            if (sizeValues == null) //no values provided: auto,auto is the default
            {
                imgw = getIntrinsicWidth();
                imgh = getIntrinsicHeight();
            }
            else if (sizeValues.size() == 2) //two values should be provided by jStyleParser
            {
                Term<?> w = sizeValues.get(0);
                Term<?> h = sizeValues.get(1);
                if (w instanceof TermLengthOrPercent && h instanceof TermLengthOrPercent)
                {
                    imgw = dec.getLength((TermLengthOrPercent) w, false, 0, 0, bounds.width);                    
                    imgh = dec.getLength((TermLengthOrPercent) h, false, 0, 0, bounds.height);                    
                }
                else if (w instanceof TermLengthOrPercent)
                {
                    imgw = dec.getLength((TermLengthOrPercent) w, false, 0, 0, bounds.width);                    
                    imgh = Math.round(imgw / ir);
                }
                else if (h instanceof TermLengthOrPercent)
                {
                    imgh = dec.getLength((TermLengthOrPercent) h, false, 0, 0, bounds.height);                    
                    imgw = Math.round(imgh * ir);
                }
                else
                {
                    imgw = getIntrinsicWidth();
                    imgh = getIntrinsicHeight();
                }
            }
            else //this should not happen
            {
                log.error("Invalid number BackgroundSize values: {}", sizeValues);
                imgw = getIntrinsicWidth();
                imgh = getIntrinsicHeight();
            }
        }
        
    }
    
    
}
