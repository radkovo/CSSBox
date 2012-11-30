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
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;

/**
 * An image placed at the element background together with its position and repeating.
 * 
 * @author burgetr
 */
public class BackgroundImage extends ContentImage
{
    private CSSProperty.BackgroundPosition position;
    private CSSProperty.BackgroundRepeat repeat;
    private CSSProperty.BackgroundAttachment attachment;
    private TermList positionValues;

    //the coordinates of the image within the element
    private int imgx;
    private int imgy;
    private boolean repeatx;
    private boolean repeaty;
    
    
    public BackgroundImage(ElementBox owner, URL url, BackgroundPosition position, TermList positionValues, BackgroundRepeat repeat, BackgroundAttachment attachment)
    {
        super(owner);
        this.loadImages = owner.getViewport().getConfig().getLoadBackgroundImages();
        this.url = url;
        this.position = position;
        this.positionValues = positionValues;
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

    //===========================================================================
    
    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        Rectangle bounds = getOwner().getAbsoluteBackgroundBounds();
        computeCoordinates(bounds);
        g.drawImage(image, bounds.x + imgx, bounds.y + imgy, observer);
    }
    
    @Override
    public BufferedImage getBufferedImage()
    {
        if (image == null || abort)
            return null;

        image = new ImageIcon(image).getImage();
        
        Rectangle bounds = getOwner().getAbsoluteBackgroundBounds();
        if (bounds.width > 0 && bounds.height > 0)
        {
            computeCoordinates(bounds);
            BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            
            if (repeatx && repeaty)
                drawRepeatBoth(g, imgx, imgy, bounds.width, bounds.height);
            else if (repeatx)
                drawRepeatX(g, imgx, imgy, bounds.width);
            else if (repeaty)
                drawRepeatY(g, imgx, imgy, bounds.height);
            else
                g.drawImage(image, imgx, imgy, observer);
            
            g.dispose();
    
            return img;
        }
        else
            return null;
    }
    
    private void drawRepeatX(Graphics2D g, int sx, int sy, int limit)
    {
        int width = getIntrinsicWidth();
        if (width == 0) width = 1;
        for (int x = sx; x < limit; x += width)
            g.drawImage(image, x, sy, observer);
        for (int x = sx - width; x + width - 1 >= 0; x -= width)
            g.drawImage(image, x, sy, observer);
        
    }
    
    private void drawRepeatY(Graphics2D g, int sx, int sy, int limit)
    {
        int height = getIntrinsicHeight();
        if (height == 0) height = 1;
        for (int y = sy; y < limit; y += height)
            g.drawImage(image, sx, y, observer);
        for (int y = sy - height; y + height - 1 >= 0; y -= height)
            g.drawImage(image, sx, y, observer);
        
    }
    
    private void drawRepeatBoth(Graphics2D g, int sx, int sy, int limitx, int limity)
    {
        int height = getIntrinsicHeight();
        for (int y = sy; y < limity; y += height)
            drawRepeatX(g, sx, y, limitx);
        for (int y = sy - height; y + height - 1 >= 0; y -= height)
            drawRepeatX(g, sx, y, limitx);
        
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
        CSSDecoder dec = new CSSDecoder(getOwner().getVisualContext());
        
        //X position
        if (position == BackgroundPosition.LEFT)
            imgx = 0;
        else if (position == BackgroundPosition.RIGHT)
            imgx = bounds.width - getIntrinsicWidth();
        else if (position == BackgroundPosition.CENTER)
            imgx = (bounds.width - getIntrinsicWidth()) / 2;
        else if (position == BackgroundPosition.list_values)
        {
            imgx = dec.getLength((TermLengthOrPercent) positionValues.get(0), false, 0, 0, bounds.width - getIntrinsicWidth());
        }
        else
            imgx = 0;
        
        //Y position
        if (position == BackgroundPosition.TOP)
            imgy = 0;
        else if (position == BackgroundPosition.BOTTOM)
            imgy = bounds.height - getIntrinsicHeight();
        else if (position == BackgroundPosition.CENTER)
            imgy = (bounds.height - getIntrinsicHeight()) / 2;
        else if (position == BackgroundPosition.list_values)
        {
            int i = positionValues.size() > 1 ? 1 : 0;
            imgy = dec.getLength((TermLengthOrPercent) positionValues.get(i), false, 0, 0, bounds.height - getIntrinsicHeight());
        }
        else
            imgy = 0;
        
        //System.out.println(url + ": x=" + imgx + " y=" + imgy);
    }

    
    
    
}
