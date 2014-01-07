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
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.web.css.CSSProperty;

/**
 * Stroke for drawing the CSS borders.
 * 
 * @author burgetr
 */
public class CSSStroke implements Stroke
{
    private static Logger log = LoggerFactory.getLogger(CSSStroke.class);

    private int width;
    private CSSProperty.BorderStyle style;
    private boolean reverse;
    
    /**
     * Creates a new CSS stroke.
     * @param width Border width
     * @param style Border css style
     * @param reverse Should be true for right and bottom border - used for reversing the shape of 'double' style border.
     */
    public CSSStroke(int width, CSSProperty.BorderStyle style, boolean reverse)
    {
    	this.width = width;
    	this.style = style;
    	this.reverse = reverse;
    }
    
    public Shape createStrokedShape(Shape s)
    {
    	if (s instanceof Line2D)
    	{
    		Line2D l = (Line2D) s;
    		int x1 = (int) l.getX1();
    		int y1 = (int) l.getY1();
    		int x2 = (int) l.getX2();
    		int y2 = (int) l.getY2();
    		if (y1 == y2 && x2 > x1)
    			return sideShape(x1, y1, x2 - x1 + 1 , width, false);
    		else if (x1 == x2 && y2 > y1)
    			return sideShape(x1, y1, y2 - y1 + 1, width, true);
    		else
    			return basicStrokeShape(s);
    	}
    	else
    		return basicStrokeShape(s);
    }

    private GeneralPath sideShape(int x, int y, int len, int width, boolean vert)
    {
    	GeneralPath ret;
    	if (!vert)
    	{
    		if (style == CSSProperty.BorderStyle.DASHED || style == CSSProperty.BorderStyle.DOTTED)
    		{
    			int r = (style == CSSProperty.BorderStyle.DASHED) ? 3 : 1;
    			ret = null;
    			int i = 0;
    			while (i < len)
    			{
    				int l = width * r;
    				if (i + l >= len) l = len - i;
    				ret = append(ret, new Rectangle(x + i, y, l, width));
    				i += width * (r + 1);
    			}
    		}
    		else if (style == CSSProperty.BorderStyle.DOUBLE && width >= 3)
    		{
    			int w = (width + 2) / 3;
    			int space = width - 2 * w;
    			if (!reverse)
    			{
	    			ret = new GeneralPath(new Rectangle(x, y, len, w));
	    			ret.append(new Rectangle(x + w + space, y + w + space, len - 2 * (w + space), w), false);
    			}
    			else
    			{
	    			ret = new GeneralPath(new Rectangle(x + w + space, y, len - 2 * (w + space), w));
	    			ret.append(new Rectangle(x, y + w + space, len, w), false);
    			}
    		}
    		else
    			ret = new GeneralPath(new Rectangle(x, y, len, width));
    	}
    	else
    	{
    		if (style == CSSProperty.BorderStyle.DASHED || style == CSSProperty.BorderStyle.DOTTED)
    		{
    			int r = (style == CSSProperty.BorderStyle.DASHED) ? 3 : 1;
    			ret = null;
    			int i = 0;
    			while (i < len)
    			{
    				int l = width * r;
    				if (i + l >= len) l = len - i;
    				ret = append(ret, new Rectangle(x, y + i, width, l));
    				i += width * (r + 1);
    			}
    		}
    		else if (style == CSSProperty.BorderStyle.DOUBLE && width >= 3)
    		{
    			int w = (width + 2) / 3;
    			int space = width - 2 * w;
    			if (!reverse)
    			{
	    			ret = new GeneralPath(new Rectangle(x, y, w, len));
	    			ret.append(new Rectangle(x + w + space, y + w + space, w, len - 2 * (w + space)), false);
    			}
    			else
    			{
	    			ret = new GeneralPath(new Rectangle(x, y + w + space, w, len - 2 * (w + space)));
	    			ret.append(new Rectangle(x + w + space, y, w, len), false);
    			}
    		}
    		else
    			ret = new GeneralPath(new Rectangle(x, y, width, len));
    	}
    	
		return ret;
    }
    
    private GeneralPath append(GeneralPath src, Shape s)
    {
    	if (src == null)
    		return new GeneralPath(s);
    	else
    	{
    		src.append(s, false);
    		return src;
    	}
    }
    
    private Shape basicStrokeShape(Shape s)
    {
    	log.debug("Warning: CSSStroke: fallback to BasicStroke");
		BasicStroke bas = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, width);
		return bas.createStrokedShape(s);
    }
    
}
