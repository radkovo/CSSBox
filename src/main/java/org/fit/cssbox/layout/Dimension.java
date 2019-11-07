/*
 * Dimension.java
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
 * Created on 1. 11. 2019, 23:09:31 by burgetr
 */
package org.fit.cssbox.layout;

/**
 * A generic dimension. A float-based replacement of java.awt.Dimension.
 * @author burgetr
 */
public class Dimension
{
    public float width;
    public float height;
    
    public Dimension()
    {
        width = height = 0;
    }
    
    public Dimension(Dimension src)
    {
        this.width = src.width;
        this.height = src.height;
    }
    
    public Dimension(float width, float height)
    {
        this.width = width;
        this.height = height;
    }
    
    public void setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString()
    {
        return "(" + width + " x " + height + ")";
    }
}
