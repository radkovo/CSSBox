/*
 * BackgroundRepeater.java
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
 * Created on 16. 4. 2020, 18:47:15 by burgetr
 */
package org.fit.cssbox.render;

import org.fit.cssbox.layout.Rectangle;

/**
 * This class implements the repetition of background images within a background area.
 * 
 * @author burgetr
 */
public class BackgroundRepeater
{

    /**
     * A target function that applies a single copy of the background image to the given
     * absolute coordinates.
     *
     * @author burgetr
     */
    public static interface Target
    {
        void apply(float x, float y);
    }
    
    /**
     * Repeats the image over the background area in specified directions.
     * 
     * @param bb the entire background bounds
     * @param pos the initial absolute position and size of the background image
     * @param clip a clipping box to be applied on the repetitions
     * @param repeatX repeat in X-axis?
     * @param repeatY repeat in Y-axis?
     * @param target the target function to be called for each copy
     */
    public void repeatImage(Rectangle bb, Rectangle pos, Rectangle clip, 
            boolean repeatX, boolean repeatY, Target target)
    {
        if (repeatX && repeatY)
            drawRepeatBoth(pos, bb.width, bb.height, clip, target);
        else if (repeatX)
            drawRepeatX(pos, bb.width, clip, target);
        else if (repeatY)
            drawRepeatY(pos, bb.height, clip, target);
        else
            target.apply(pos.x, pos.y);
    }
    
    private void drawRepeatX(Rectangle pos, float limit, Rectangle clip, Target target)
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
                    target.apply(x, sy);
            }
            for (float x = sx - width; x + width - 1 >= 0; x -= width)
            {
                r.setLocation(x, sy);
                if (r.intersects(clip))
                    target.apply(x, sy);
            }
        }
        
    }
    
    private void drawRepeatY(Rectangle pos, float limit, Rectangle clip, Target target)
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
                    target.apply(sx, y);
            }
            for (float y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    target.apply(sx, y);
            }
        }
        
    }
    
    private void drawRepeatBoth(Rectangle pos, float limitx, float limity, Rectangle clip, Target target)
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
                    drawRepeatX(new Rectangle(sx, y, width, height), limitx, clip, target);
            }
            for (float y = sy - height; y + height - 1 >= 0; y -= height)
            {
                r.setLocation(sx, y);
                if (r.intersects(clip))
                    drawRepeatX(new Rectangle(sx, y, width, height), limitx, clip, target);
            }
        }
    }

    
}
