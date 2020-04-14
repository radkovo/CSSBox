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
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.render.BackgroundImageGradient;
import org.fit.cssbox.render.BackgroundImageImage;
import org.fit.cssbox.render.ElementBackground;
import org.fit.cssbox.render.Gradient;
import org.fit.cssbox.render.LinearGradient;
import org.fit.cssbox.render.RadialGradient;

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
    
    /**
     * Returns a bitmap containing the entire element background.
     * @return
     */
    public BufferedImage getBufferedImage()
    {
        return bgimage;
    }
    
    /**
     * Adds a new image to the background based on its CSS properties.
     * @param img the image to add
     */
    public void addBackgroundImage(BackgroundImageImage img)
    {
        if (bgimage != null && img.getImage() != null)
        {
            if (img.getImage() instanceof BitmapImage)
            {
                Rectangle pos = computeTargetImagePosition(img);
                BufferedImage image = ((BitmapImage) img.getImage()).getBufferedImage();
                float origw = img.getIntrinsicWidth();
                float origh = img.getIntrinsicHeight();
                applyImage(image, pos, origw, origh, img.isRepeatX(), img.isRepeatY());
            }
        }
    }
    
    /**
     * Adds a new gradient to the background based on its CSS properties
     * @param img the gradient to add
     */
    public void addBackgroundImage(BackgroundImageGradient img)
    {
        if (bgimage != null && img.getGradient() != null)
        {
            if (img.getGradient() instanceof LinearGradient)
            {
                final LinearGradient grad = (LinearGradient) img.getGradient();
                final LinearGradientPaint p = createLinearGradientPaint(grad);
                addsGradientUsingPaint(p, img);
            }
            else if (img.getGradient() instanceof RadialGradient)
            {
                final RadialGradient grad = (RadialGradient) img.getGradient();
                final RadialGradientPaint p = createRadialGradientPaint(grad);
                addsGradientUsingPaint(p, img);
            }
        }
    }

    /**
     * Uses the paint for creating an image of the gradient and adding it to the target bitmap. 
     * @param p the paint to be applied
     * @param img the gradient image to be drawn
     */
    private void addsGradientUsingPaint(Paint p, BackgroundImageGradient img)
    {
        Rectangle pos = computeTargetImagePosition(img);
        BufferedImage gradImg =
                new BufferedImage(Math.round(pos.getWidth()), Math.round(pos.getHeight()), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = gradImg.createGraphics();
        g.setPaint(p);
        g.fill(new Rectangle2D.Float(0, 0, pos.width, pos.height));
        applyImage(gradImg, pos, pos.width, pos.height, img.isRepeatX(), img.isRepeatY());
    }

    /**
     * Adds an image to the target bitmap while applying the repeat directions and scaling when
     * necessary.
     * @param image The image to add.
     * @param pos Target position in the target bitmap.
     * @param origw Original image width
     * @param origh Original image height
     * @param repeatX Should the image be repeated in X direction?
     * @param repeatY Should the image be repeated in Y direction?
     */
    private void applyImage(final BufferedImage image, final Rectangle pos, final float origw, final float origh,
            boolean repeatX, boolean repeatY)
    {
        if (repeatX && repeatY)
            drawRepeatBoth(g, image, pos, getBounds().width, getBounds().height, origw, origh, getClipped());
        else if (repeatX)
            drawRepeatX(g, image, pos, getBounds().width, origw, origh, getClipped());
        else if (repeatY)
            drawRepeatY(g, image, pos, getBounds().height, origw, origh, getClipped());
        else
            drawScaledImage(g, image, pos.x, pos.y, pos.width, pos.height, origw, origh, null);
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

    private LinearGradientPaint createLinearGradientPaint(LinearGradient grad)
    {
        Point2D start = new Point2D.Float(grad.getX1(), grad.getY1());
        Point2D end = new Point2D.Float(grad.getX2(), grad.getY2());
        float[] dists = new float[grad.getStops().size()];
        java.awt.Color[] colors = new java.awt.Color[grad.getStops().size()];
        stopsToPaintValues(grad, dists, colors);
        return new LinearGradientPaint(start, end, dists, colors, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, new AffineTransform());
    }

    private RadialGradientPaint createRadialGradientPaint(RadialGradient grad)
    {
        final float cx = grad.getCx();
        final float cy = grad.getCy();
        
        Point2D center = new Point2D.Float(cx, cy);
        
        AffineTransform gradientTransform = new AffineTransform();
        if (!grad.isCircle())
        {
            // scale to meet the radiuses
            float scaleX = 1.0f;
            float scaleY = 1.0f;
            if (grad.getRy() < grad.getRx())
            {
                if (grad.getRx() > 0)
                    scaleY = grad.getRy() / grad.getRx();
            }
            else
            {
                if (grad.getRy() > 0)
                    scaleX = grad.getRx() / grad.getRy();
            }
            gradientTransform.translate(cx, cy);
            gradientTransform.scale(scaleX, scaleY);
            gradientTransform.translate(-cx, -cy);
        }
        
        // convert stops
        float[] dists = new float[grad.getStops().size()];
        java.awt.Color[] colors = new java.awt.Color[grad.getStops().size()];
        stopsToPaintValues(grad, dists, colors);
        
        float rx = grad.getRx();
        if (rx < 0.1f) rx = 0.1f; //avoid zero radius
        RadialGradientPaint gp =
                new RadialGradientPaint(center, rx, center,
                                        dists, colors,
                                        CycleMethod.NO_CYCLE,
                                        ColorSpaceType.SRGB,
                                        gradientTransform);
        return gp;
    }

    private void stopsToPaintValues(Gradient grad, float[] dists, java.awt.Color[] colors)
    {
        for (int i = 0; i < grad.getStops().size(); i++)
        {
            dists[i] = grad.getStops().get(i).getPercentage() / 100.0f;
            if (dists[i] < 0.0f) dists[i] = 0.0f;
            if (dists[i] > 1.0f) dists[i] = 1.0f;
            if (i > 0 && dists[i] <= dists[i - 1])
            {
                dists[i] = dists[i-1] + 0.001f; //awt does not like equal stops, increase a bit
                if (dists[i] > 1.0f) { dists[i] = 1.0f; dists[i-1] -= 0.001; }
            }
            colors[i] = GraphicsRenderer.convertColor(grad.getStops().get(i).getColor());
        }
    }

}
