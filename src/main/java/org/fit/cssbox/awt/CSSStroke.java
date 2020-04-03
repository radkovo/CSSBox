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
package org.fit.cssbox.awt;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.fit.cssbox.misc.Coords;
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

    private float width;
    private CSSProperty.BorderStyle style;
    private boolean reverse;
    
    /**
     * Creates a new CSS stroke.
     * @param width Border width
     * @param style Border css style
     * @param reverse Should be true for right and bottom border - used for reversing the shape of 'double' style border.
     */
    public CSSStroke(float width, CSSProperty.BorderStyle style, boolean reverse)
    {
    	this.width = width;
    	this.style = style;
    	this.reverse = reverse;
    }
    
    public Shape createStrokedShape(Shape s)
    {
    	if (s instanceof Line2D.Float)
    	{
    		Line2D.Float l = (Line2D.Float) s;
    		float x1 = l.x1;
    		float y1 = l.y1;
    		float x2 = l.x2;
    		float y2 = l.y2;
    		if (Coords.eq(y1, y2) && x2 > x1)
    			return sideShape(x1, y1, x2 - x1 + 1 , width, false);
    		else if (Coords.eq(x1, x2) && y2 > y1)
    			return sideShape(x1, y1, y2 - y1 + 1, width, true);
    		else
    			return basicStrokeShape(s, "not orthogonal");
    	}
    	else
    		return basicStrokeShape(s, "not a line");
    }

    private GeneralPath sideShape(float x, float y, float len, float width, boolean vert)
    {
    	GeneralPath ret;
    	if (!vert)
    	{
    		if (style == CSSProperty.BorderStyle.DASHED || style == CSSProperty.BorderStyle.DOTTED)
    		{
    			float r = (style == CSSProperty.BorderStyle.DASHED) ? 3 : 1;
    			ret = null;
    			float i = 0;
    			while (i < len)
    			{
    				float l = width * r;
    				if (i + l >= len) l = len - i;
    				ret = append(ret, new Rectangle2D.Float(x + i, y, l, width));
    				i += width * (r + 1);
    			}
    		}
    		else if (style == CSSProperty.BorderStyle.DOUBLE && width >= 3)
    		{
    			float w = (width + 2) / 3;
    			float space = width - 2 * w;
    			if (!reverse)
    			{
	    			ret = new GeneralPath(new Rectangle2D.Float(x, y, len, w));
	    			ret.append(new Rectangle2D.Float(x + w + space, y + w + space, len - 2 * (w + space), w), false);
    			}
    			else
    			{
	    			ret = new GeneralPath(new Rectangle2D.Float(x + w + space, y, len - 2 * (w + space), w));
	    			ret.append(new Rectangle2D.Float(x, y + w + space, len, w), false);
    			}
    		}
    		else
    			ret = new GeneralPath(new Rectangle2D.Float(x, y, len, width));
    	}
    	else
    	{
    		if (style == CSSProperty.BorderStyle.DASHED || style == CSSProperty.BorderStyle.DOTTED)
    		{
    			float r = (style == CSSProperty.BorderStyle.DASHED) ? 3 : 1;
    			ret = null;
    			float i = 0;
    			while (i < len)
    			{
    				float l = width * r;
    				if (i + l >= len) l = len - i;
    				ret = append(ret, new Rectangle2D.Float(x, y + i, width, l));
    				i += width * (r + 1);
    			}
    		}
    		else if (style == CSSProperty.BorderStyle.DOUBLE && width >= 3)
    		{
    			float w = (width + 2) / 3;
    			float space = width - 2 * w;
    			if (!reverse)
    			{
	    			ret = new GeneralPath(new Rectangle2D.Float(x, y, w, len));
	    			ret.append(new Rectangle2D.Float(x + w + space, y + w + space, w, len - 2 * (w + space)), false);
    			}
    			else
    			{
	    			ret = new GeneralPath(new Rectangle2D.Float(x, y + w + space, w, len - 2 * (w + space)));
	    			ret.append(new Rectangle2D.Float(x + w + space, y, w, len), false);
    			}
    		}
    		else
    			ret = new GeneralPath(new Rectangle2D.Float(x, y, width, len));
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
    
    private Shape basicStrokeShape(Shape s, String reason)
    {
    	log.debug("Warning: CSSStroke: fallback to BasicStroke ({})", reason);
		BasicStroke bas = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, width);
		return bas.createStrokedShape(s);
    }
    
}
