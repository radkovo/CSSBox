/*
 * RadialGradient.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fit.cssbox.layout.Rectangle;

import cz.vutbr.web.css.TermIdent;

/**
 * A radial gradient representation.
 *
 * @author safar
 * @author burgetr
 */
public class RadialGradient extends Gradient
{
    // an elipse/circle switch
    private boolean circle;

    // center point and radiuses
    private float cx;
    private float cy;
    private float rx;
    private float ry;

    /** Position and size of the gradient in the background area */
    private Rectangle bgRect;

    /** Total gradient area width and height */
    private float gradWidth;
    private float gradHeight;

    /** Gradient size keywords */
    public enum GradientSize
    {
        CLOSEST_CORNER, CLOSEST_SIDE, FARTHEST_CORNER, FARTHEST_SIDE
    }

    /**
     * Creates a radial gradient with the given size.
     *
     * @param bgRect
     */
    public RadialGradient(Rectangle bgRect)
    {
        super();
        circle = true;
        this.bgRect = bgRect;
    }

    /**
     * Checks whether this is a circle gradient.
     * @return {@code true} for circle, {@code false} for ellipse.
     */
    public boolean isCircle()
    {
        return circle;
    }

    public float getCx()
    {
        return cx;
    }

    public float getCy()
    {
        return cy;
    }

    public float getRx()
    {
        return rx;
    }

    public float getRy()
    {
        return ry;
    }

    public float getLength()
    {
        return Math.max(rx, ry);
    }
    
    public Rectangle getBgRect()
    {
        return bgRect;
    }

    public float getGradWidth()
    {
        return gradWidth;
    }

    public float getGradHeight()
    {
        return gradHeight;
    }

    //==============================================================================
    
    /**
     * Creates an ellipse gradient from the radius and center point in absolute values.
     *
     * @param rx x radius
     * @param ry y radius
     * @param sx center point x
     * @param sy center point y
     */
    public void setEllipse(float rx, float ry, float sx, float sy)
    {
        circle = false;
        this.cx = sx;
        this.cy = sy;
        this.rx = rx;
        this.ry = ry;

        // compute the width and height of the gradient area
        if (ry > rx)
        {
            gradHeight = bgRect.width * ry / rx;
            gradWidth = bgRect.width;
        }
        else
        {
            gradWidth = bgRect.height * rx / ry;
            gradHeight = bgRect.height;
        }
    }

    /**
     * Creates an ellipse gradient from a size keyword and center point in absolute values.
     *
     * @param size
     * @param sx center point x
     * @param sy center point y
     */
    public void setEllipse(GradientSize size, float sx, float sy)
    {
        float r[];
        switch (size)
        {
            case CLOSEST_CORNER:
                r = computeAxesFromCorner(sx, sy, getIndexOfMinCornerDistance(sx, sy));
                break;
            case FARTHEST_CORNER:
                r = computeAxesFromCorner(sx, sy, getIndexOfMaxCornerDistance(sx, sy));
                break;
            case CLOSEST_SIDE:
                r = new float[2];
                r[0] = closestSideDistanceX(sx);
                r[1] = closestSideDistanceY(sy);
                break;
            case FARTHEST_SIDE:
                r = new float[2];
                r[0] = farthestSideDistanceX(sx);
                r[1] = farthestSideDistanceY(sy);
                break;
            default:
                r = new float[2];
                break;
        }
        setEllipse(r[0], r[1], sx, sy);
    }

    /**
     * Creates a circle gradient from the radius and center point in absolute values.
     *
     * @param rxy the radius
     * @param sx center point x
     * @param sy center point y
     */
    public void setCircle(float rxy, float sx, float sy)
    {
        circle = true;

        cx = sx;
        cy = sy;
        rx = ry = rxy;
    }

    /**
     * Creates a circle gradient from the size keyword and center point in absolute values.
     *
     * @param rxy the radius
     * @param sx center point x
     * @param sy center point y
     */
    public void setCircleDataRadLengths(GradientSize rl, float sx, float sy)
    {
        float rxy = 0;
        List<Float> l;
        switch (rl)
        {
            case CLOSEST_CORNER:
                l = getAllCornersDistance(sx, sy);
                rxy = Collections.min(l);
                break;
            case CLOSEST_SIDE:
                l = getAllSidesDistance(sx, sy);
                rxy = Collections.min(l);
                break;
            case FARTHEST_CORNER:
                l = getAllCornersDistance(sx, sy);
                rxy = Collections.max(l);
                break;
            case FARTHEST_SIDE:
                l = getAllSidesDistance(sx, sy);
                rxy = Collections.max(l);
                break;
        }
        setCircle(rxy, sx, sy);
    }

    //==============================================================================
    
    private float closestSideDistanceX(float sx)
    {
        return Math.min(coordinateDistance(sx, 0), coordinateDistance(sx, bgRect.width));
    }
    
    private float farthestSideDistanceX(float sx)
    {
        return Math.max(coordinateDistance(sx, 0), coordinateDistance(sx, bgRect.width));
    }
    
    private float closestSideDistanceY(float sy)
    {
        return Math.min(coordinateDistance(sy, 0), coordinateDistance(sy, bgRect.height));
    }
    
    private float farthestSideDistanceY(float sy)
    {
        return Math.max(coordinateDistance(sy, 0), coordinateDistance(sy, bgRect.height));
    }
    
    private int getIndexOfMinCornerDistance(float sx, float sy)
    {
        List<Float> l = getAllCornersDistance(sx, sy);
        return l.indexOf(Collections.min(l));
    }

    private int getIndexOfMaxCornerDistance(float sx, float sy)
    {
        List<Float> l = getAllCornersDistance(sx, sy);
        return l.indexOf(Collections.max(l));
    }

    private List<Float> getAllCornersDistance(float centerX, float centerY)
    {
        List<Float> l = new ArrayList<Float>(4);
        l.add(pointsDistance(centerX, centerY, 0, 0)); // A
        l.add(pointsDistance(centerX, centerY, 0, bgRect.height)); // D
        l.add(pointsDistance(centerX, centerY, bgRect.width, 0)); // B
        l.add(pointsDistance(centerX, centerY, bgRect.width, bgRect.height)); // C
        return l;
    }

    private List<Float> getAllSidesDistance(float centerX, float centerY)
    {
        List<Float> l = new ArrayList<Float>(4);
        l.add(coordinateDistance(centerX, 0));
        l.add(coordinateDistance(centerX, bgRect.width));
        l.add(coordinateDistance(centerY, 0));
        l.add(coordinateDistance(centerY, bgRect.height));
        return l;
    }

    /**
     * Computes the rx and ry radiuses from the center and selected corner index so that the
     * ellipse end shape exactly meets the given cornet.
     * @param sx ellipse center x coordinate
     * @param sy ellipse center y coordinate
     * @param i the corner index, see {@link #getCornerX(int)}
     * @return
     */
    private float[] computeAxesFromCorner(float sx, float sy, int i)
    {
        float[] r = new float[2];
        
        final float xx = getCornerX(i);
        final float yy = getCornerY(i);
        final float dx = coordinateDistance(sx, xx);
        final float dy = coordinateDistance(sy, yy);
        if (dy == 0)
        {
            r[0] = r[1] = 0;
        }
        else
        {
            float ratio = dx / dy;
            float ry2 = (xx - sx) * (xx - sx) / (ratio * ratio) + (yy - sy) * (yy - sy);
            r[1] = (float) Math.sqrt(ry2);
            r[0] = ratio * r[1];
        }
        return r;
    }
    
    /**
     * Gets the X coordinate of a corner.
     * @param pointIndex 0 for top left, 1 for bottom left, 2 for top right, 3 for bottom right
     * @return the given point X coordinate.
     */
    private float getCornerX(int pointIndex)
    {
        switch (pointIndex)
        {
            case 0:
            case 1:
                return 0;
            case 2:
            case 3:
                return bgRect.width;
            default:
                return 0;
        }
    }
    
    /**
     * Gets the Y coordinate of a corner.
     * @param pointIndex 0 for top left, 1 for bottom left, 2 for top right, 3 for bottom right
     * @return the given point Y coordinate.
     */
    private float getCornerY(int pointIndex)
    {
        switch (pointIndex)
        {
            case 0:
            case 2:
                return 0;
            case 1:
            case 3:
                return bgRect.height;
            default:
                return 0;
        }
    }
    
    private float pointsDistance(float x1, float y1, float x2, float y2)
    {
        final float dx = Math.abs(x1 - x2);
        final float dy = Math.abs(y1 - y2);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float coordinateDistance(float x1, float x2)
    {
        return Math.abs(x1 - x2);
    }

    //==============================================================================
    
    public static GradientSize decodeSizeIdent(TermIdent ident)
    {
        if (ident != null)
        {
            switch (ident.getValue())
            {
                case "closest-side":
                    return GradientSize.CLOSEST_SIDE;
                case "closest-corner":
                    return GradientSize.CLOSEST_CORNER;
                case "farthest-side":
                    return GradientSize.FARTHEST_SIDE;
                case "farthest-corner":
                    return GradientSize.FARTHEST_CORNER;
                default:
                    return null;
            }
        }
        else
            return null;
    }
    
}
