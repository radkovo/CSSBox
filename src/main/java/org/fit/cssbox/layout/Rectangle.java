/*
 * Rectangle.java
 * Copyright (c) 2005-2019 Radek Burget
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
 * Created on 1. 11. 2019, 22:20:08 by burgetr
 */
package org.fit.cssbox.layout;

/**
 * A generic rectangle. It provides a basically compatible (but float-based) 
 * replacement for java.awt.Rectangle.
 * 
 * @author burgetr
 */
public class Rectangle
{
    public float x;
    public float y;
    public float width;
    public float height;
    
    protected Rectangle(float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected Rectangle(float width, float height)
    {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
    }

    protected Rectangle()
    {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }
    
    protected Rectangle(Rectangle src)
    {
        this.x = src.x;
        this.y = src.y;
        this.width = src.width;
        this.height = src.height;
    }
    
    protected Rectangle(Dimension src)
    {
        this.x = 0;
        this.y = 0;
        this.width = src.width;
        this.height = src.height;
    }
    
    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public float getWidth()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }

    public float getHeight()
    {
        return height;
    }

    public void setHeight(float height)
    {
        this.height = height;
    }
    
    public void setLocation(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
    }
    
    public Dimension getSize()
    {
        return new Dimension(width, height);
    }
    
    public boolean contains(float x, float y)
    {
        if (width <= 0 || height <= 0)
            return false;
        return (x >= this.x && x < this.x + this.width &&
                y >= this.y && y < this.y + this.height);
    }
    
    public boolean intersects(Rectangle r)
    {
        float tw = this.width;
        float th = this.height;
        float rw = r.width;
        float rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        float tx = this.x;
        float ty = this.y;
        float rx = r.x;
        float ry = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      overflow || intersect
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));
    }
    
    public Rectangle intersection(Rectangle r) 
    {
        float tx1 = this.x;
        float ty1 = this.y;
        float rx1 = r.x;
        float ry1 = r.y;
        float tx2 = tx1; tx2 += this.width;
        float ty2 = ty1; ty2 += this.height;
        float rx2 = rx1; rx2 += r.width;
        float ry2 = ry1; ry2 += r.height;
        if (tx1 < rx1) tx1 = rx1;
        if (ty1 < ry1) ty1 = ry1;
        if (tx2 > rx2) tx2 = rx2;
        if (ty2 > ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        return new Rectangle(tx1, ty1, tx2, ty2);
    }

    @Override
    public String toString()
    {
        return "Rectangle[x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + "]";
    }
}
