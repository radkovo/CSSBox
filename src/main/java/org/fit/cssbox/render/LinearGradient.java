/*
 * LinearGradient.java
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
 */
package org.fit.cssbox.render;

/**
 * A linear gradient.
 *
 * @author Martin Safar
 * @author Nguyen Hoang Duong
 * @author burgetr
 */
public class LinearGradient extends Gradient
{
    //gradient area width and height
    private float width;
    private float height;
    
    //gradient starting and ending points in percentages of the width/height
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public LinearGradient()
    {
        super();
    }
    
    /**
     * Computes the gradient length from its rectangular width and height.
     * @param w
     * @param h
     * @return
     */
    public float getLength()
    {
        final double dx = Math.abs(x2 - x1);
        final double dy = Math.abs(y2 - y1);
        return (float) Math.hypot(dx, dy);
    }
    
    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public float getX1()
    {
        return x1;
    }

    public float getY1()
    {
        return y1;
    }

    public float getX2()
    {
        return x2;
    }

    public float getY2()
    {
        return y2;
    }

    /**
     * Sets the gradient angle and computes the coordinates.
     * 
     * @param deg the gradient angle
     * @param w containing element width
     * @param h containing element height
     */
    public void setAngleDeg(double deg, float w, float h)
    {
        this.width = w;
        this.height = h;
        
        final double procDeg = (deg % 360 + 360) % 360;
        final double normDeg = 90 - procDeg;
        final double wRatio = (double) w / h;

        x1 = x2 = y1 = y2 = 0;

        final float sx = w / 2;
        final float sy = h / 2;

        // calculating coordinates of corners of the element
        final float ax = 0;
        final float ay = h;

        final float bx = w;
        final float by = h;

        final float cx = w;
        final float cy = 0;

        final float dx = 0;
        final float dy = 0;

        if (procDeg == 0)
        {
            x1 = w / 2;
            y1 = 0;
            x2 = w / 2;
            y2 = h;
        }
        else if (procDeg == 90)
        {
            x1 = 0;
            y1 = h / 2;
            x2 = w;
            y2 = h / 2;
        }
        else if (procDeg == 180)
        {
            x1 = w / 2;
            y1 = h;
            x2 = w / 2;
            y2 = 0;
        }
        else if (procDeg == 270)
        {
            x1 = w;
            y1 = h / 2;
            x2 = 0;
            y2 = h / 2;
        }
        else
        {
            final double tan = Math.tan((normDeg / 180) * Math.PI);

            double qqq, kkk;
            double qqq1, pdir;
            double qqq2;

            //compute the direction of the gradient axis
            kkk = -tan / wRatio;
            qqq = sy - kkk * sx;

            //direction of the perpendiculars
            pdir = 1 / (tan / wRatio);

            if (procDeg > 0 && procDeg <= 90)
            {
                qqq1 = dy - pdir * dx;
                qqq2 = by - pdir * bx;

                x2 = (float) ((qqq2 - qqq) / (kkk - pdir));
                y2 = (float) (kkk * x2 + qqq);
                x1 = (float) ((qqq1 - qqq) / (kkk - pdir));
                y1 = (float) (kkk * x1 + qqq);

            }
            else if (procDeg > 90 && procDeg < 180)
            {
                qqq1 = ay - pdir * ax;
                qqq2 = cy - pdir * cx;

                x2 = (float) ((qqq2 - qqq) / (kkk - pdir));
                y2 = (float) (kkk * x2 + qqq);

                x1 = (float) ((qqq1 - qqq) / (kkk - pdir));
                y1 = (float) (kkk * x1 + qqq);

            }
            else if (procDeg > 180 && procDeg < 270)
            {
                qqq1 = by - pdir * bx;
                qqq2 = dy - pdir * dx;

                x2 = (float) ((qqq2 - qqq) / (kkk - pdir));
                y2 = (float) (kkk * x2 + qqq);

                x1 = (float) ((qqq1 - qqq) / (kkk - pdir));
                y1 = (float) (kkk * x1 + qqq);
            }
            else if (procDeg > 270 && procDeg < 360)
            {
                qqq1 = cy - pdir * cx;
                qqq2 = ay - pdir * ax;

                x2 = (float) ((qqq2 - qqq) / (kkk - pdir));
                y2 = (float) (kkk * x2 + qqq);

                x1 = (float) ((qqq1 - qqq) / (kkk - pdir));
                y1 = (float) (kkk * x1 + qqq);
            }
        }
    }
    
}
