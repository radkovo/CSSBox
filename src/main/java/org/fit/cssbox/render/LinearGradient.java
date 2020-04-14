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
    
    @Override
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

        x1 = x2 = y1 = y2 = 0;

        final float sx = w / 2;
        final float sy = h / 2;

        // calculating coordinates of corners of the element
        final float ax = 0;
        final float ay = 0;

        final float bx = w;
        final float by = 0;

        final float cx = w;
        final float cy = h;

        final float dx = 0;
        final float dy = h;

        if (procDeg == 0)
        {
            x1 = w / 2;
            y1 = h;
            x2 = w / 2;
            y2 = 0;
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
            y1 = 0;
            x2 = w / 2;
            y2 = h;
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

            //compute the direction of the gradient axis
            double k = -tan;
            double q = sy - k * sx;

            //direction of the perpendiculars
            double kn = 1 / tan;

            double q1, q2;
            if (procDeg > 0 && procDeg <= 90)
            {
                q1 = dy - kn * dx;
                q2 = by - kn * bx;

                x2 = (float) ((q2 - q) / (k - kn));
                y2 = (float) (k * x2 + q);
                x1 = (float) ((q1 - q) / (k - kn));
                y1 = (float) (k * x1 + q);

            }
            else if (procDeg > 90 && procDeg < 180)
            {
                q1 = ay - kn * ax;
                q2 = cy - kn * cx;

                x2 = (float) ((q2 - q) / (k - kn));
                y2 = (float) (k * x2 + q);
                x1 = (float) ((q1 - q) / (k - kn));
                y1 = (float) (k * x1 + q);

            }
            else if (procDeg > 180 && procDeg < 270)
            {
                q1 = by - kn * bx;
                q2 = dy - kn * dx;

                x2 = (float) ((q2 - q) / (k - kn));
                y2 = (float) (k * x2 + q);
                x1 = (float) ((q1 - q) / (k - kn));
                y1 = (float) (k * x1 + q);
            }
            else if (procDeg > 270 && procDeg < 360)
            {
                q1 = cy - kn * cx;
                q2 = ay - kn * ax;

                x2 = (float) ((q2 - q) / (k - kn));
                y2 = (float) (k * x2 + q);
                x1 = (float) ((q1 - q) / (k - kn));
                y1 = (float) (k * x1 + q);
            }
        }
    }
    
}
