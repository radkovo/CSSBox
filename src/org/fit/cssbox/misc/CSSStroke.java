/*
 * CSSStroke.java
 * Copyright (c) 2005-2010 Radek Burget
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
 * Created on 9.11.2010, 17:31:58 by burgetr
 */
package org.fit.cssbox.misc;

import java.awt.*;

/**
 *
 * @author burgetr
 */
public class CSSStroke implements Stroke
{
    private BasicStroke bas;

    public CSSStroke(int width)
    {
        bas = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1);
        System.out.println("creating");
    }
    
    public Shape createStrokedShape(Shape s)
    {
        Shape ret = bas.createStrokedShape(s);
        System.out.println("src="+s+" dest="+ret);
        return s;
    }

    
    
}
