/*
 * CSSUnits.java
 * Copyright (c) 2005-2007 Radek Burget
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
 * Created on 31. leden 2005, 15:49
 */

package org.fit.cssbox.css;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import cz.vutbr.web.css.CSSProperty;

/**
 * CSS unit conversion functions.
 * 
 * @author  burgetr
 */
public class CSSUnits 
{
    //public static final double dpi = 100;  //100 DPI display
    public static final double dpi = GraphicsEnvironment.isHeadless() ? 100 : Toolkit.getDefaultToolkit().getScreenResolution();
    public static final double medium_font = 16; //16px
    private static final double font_step = 1.2;
    public static final int THIN_BORDER = 1;
    public static final int MEDIUM_BORDER = 3;
    public static final int THICK_BORDER = 5;
    
    /** Converts points to pixels according to the DPI set */
    public static double pixels(double pt)
    {
        return pt * dpi / 72.0; 
    }

    /** Converts pixels to points according to the DPI set */
    public static double points(double px)
    {
        return px * 72.0 / dpi;
    }
    
    /** 
     * Converts the font size given by an identifier to absolute length in pixels.
     * @param parent Parent font size (taken as 1em)
     * @param value The size specification to be converted
     * @return absolute font size in px
     */
    public static double convertFontSize(double parent, CSSProperty.FontSize value)
    {
        double em = parent;
        double ret = em;
        if (value == CSSProperty.FontSize.MEDIUM)
            ret = medium_font;
        else if (value == CSSProperty.FontSize.SMALL)
            ret = medium_font / font_step;
        else if (value == CSSProperty.FontSize.X_SMALL)
            ret = medium_font / font_step / font_step;
        else if (value == CSSProperty.FontSize.XX_SMALL)
            ret = medium_font / font_step / font_step / font_step;
        else if (value == CSSProperty.FontSize.LARGE)
            ret = medium_font * font_step;
        else if (value == CSSProperty.FontSize.X_LARGE)
            ret = medium_font * font_step * font_step;
        else if (value == CSSProperty.FontSize.XX_LARGE)
            ret = medium_font * font_step * font_step * font_step;
        else if (value == CSSProperty.FontSize.SMALLER)
            ret = em / font_step;
        else if (value == CSSProperty.FontSize.LARGER)
            ret = em * font_step;
        return ret;
    }
    
    /**
     * Converts the border size given by an identifier to an absolute value.
     * @param width the border-width identifier
     * @return absolute length in pixels
     */
    public static int convertBorderWidth(CSSProperty.BorderWidth width)
    {
    	if (width == CSSProperty.BorderWidth.THIN)
    		return THIN_BORDER;
    	else if (width == CSSProperty.BorderWidth.MEDIUM)
    		return MEDIUM_BORDER;
    	else
    		return THICK_BORDER;
    }
    
}
