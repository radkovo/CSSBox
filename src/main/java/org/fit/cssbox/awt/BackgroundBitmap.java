/*
 * BackgroundBitmap.java
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
 * Created on 3. 4. 2020, 11:42:20 by burgetr
 */
package org.fit.cssbox.awt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.fit.cssbox.layout.BackgroundImageImage;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.render.ElementBackground;

/**
 * A bitmap representing a complete rendered background of an element. It provides functions
 * for creating the resulting bitmap by adding background images and other artifacts.
 * 
 * @author burgetr
 */
public class BackgroundBitmap extends ElementBackground
{
    private BufferedImage bgimage;
    private Graphics2D g;
    
    
    public BackgroundBitmap(ElementBox owner)
    {
        super(owner);
        if (!isZeroSize())
        {
            bgimage = new BufferedImage(Math.round(getBounds().width), Math.round(getBounds().height), BufferedImage.TYPE_INT_ARGB);
            g = bgimage.createGraphics();
        }
    }
    
    public BufferedImage getBufferedImage()
    {
        return bgimage;
    }
    
    public void addBackgroundImage(BackgroundImageImage img)
    {
        if (bgimage != null && img.getImage() != null)
        {
            if (img.getImage() instanceof BitmapImage)
            {
                final Rectangle pos = img.getComputedPosition();
                final BufferedImage image = ((BitmapImage) img.getImage()).getBufferedImage();
                final float origw = img.getIntrinsicWidth();
                final float origh = img.getIntrinsicHeight();
                
                if (img.isRepeatX() && img.isRepeatY())
                    drawRepeatBoth(g, image, pos, getBounds().width, getBounds().height, origw, origh, getClipped());
                else if (img.isRepeatX())
                    drawRepeatX(g, image, pos, getBounds().width, origw, origh, getClipped());
                else if (img.isRepeatY())
                    drawRepeatY(g, image, pos, getBounds().height, origw, origh, getClipped());
                else
                    drawScaledImage(g, image, pos.x, pos.y, pos.width, pos.height, origw, origh, null);
            }
        }
    }
    
    private void drawRepeatX(Graphics2D g, BufferedImage image, Rectangle pos,
            float limit, float origw, float origh, Rectangle clip)
    {
        final float sx = pos.x;
        final float sy = pos.y;
        final float width = pos.width;
        final float height = pos.height;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (width > 0)
        {
            for (float x = sx; x < limit; x += width)
            {
                r.setLocation(x, sy);
                if (r.intersects(clip))
                    drawScaledImage(g, image, x, sy, width, height, origw, origh, null);
            }
            for (float x = sx - width; x + width - 1 >= 0; x -= width)
            {
                r.setLocation(x, sy);
                if (r.intersects(clip))
                    drawScaledImage(g, image, x, sy, width, height, origw, origh, null);
            }
        }
        
    }
    
    private void drawRepeatY(Graphics2D g, BufferedImage image, Rectangle pos,
            float limit, float origw, float origh, Rectangle clip)
    {
        final float sx = pos.x;
        final float sy = pos.y;
        final float width = pos.width;
        final float height = pos.height;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (height > 0)
        {
            for (float y = sy; y < limit; y += height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawScaledImage(g, image, sx, y, width, height, origw, origh, null);
            }
            for (float y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawScaledImage(g, image, sx, y, width, height, origw, origh, null);
            }
        }
        
    }
    
    private void drawRepeatBoth(Graphics2D g, BufferedImage image, Rectangle pos,
            float limitx, float limity, float origw, float origh,
            Rectangle clip)
    {
        final float sx = pos.x;
        final float sy = pos.y;
        final float width = pos.width;
        final float height = pos.height;
        Rectangle r = new Rectangle(0, 0, width, height);
        if (height > 0)
        {
            for (float y = sy; y < limity; y += height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawRepeatX(g, image, new Rectangle(sx, y, width, height),
                            limitx, origw, origh, clip);
            }
            for (float y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawRepeatX(g, image, new Rectangle(sx, y, width, height),
                            limitx, origw, origh, clip);
            }
        }
    }
    
    private void drawScaledImage(Graphics2D g, BufferedImage image,
            float x, float y, float w, float h,
            float origw, float origh, ImageObserver observer)
    {
        g.drawImage(image,
                    Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h),
                    0, 0, Math.round(origw), Math.round(origh),
                    observer);
    }

    

}
