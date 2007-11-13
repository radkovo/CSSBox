/*
 * CSSUnits.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Created on 31. leden 2005, 15:49
 */

package org.fit.cssbox.css;

import java.text.*;
import java.util.*;
import org.w3c.dom.css.*;

/**
 * CSS unit conversion functions.
 * 
 * @author  burgetr
 */
public class CSSUnits 
{
    public static final double dpi = 100;  //100 DPI display
    private static final double font_step = 1.2;
    private static final double medium_font = 12;
    private static final String default_font_size = "12pt";
    
    /** Converts points to pixels according to the DPI set */
    public static int pixels(double pt)
    {
        return (int) ((pt * 72) / dpi);
    }

    /** Converts pixels to points according to the DPI set */
    public static double points(int px)
    {
        return (px * dpi) / (double) 72;
    }
    
    /** Converts a length to absolute units (points) */
    public static double convertLength(String src, double em, double ex)
    {
        if (src.length() < 3) //this cannot be a number with a unit, most probably it's just 0
            return 0;
        String val = src.substring(0, src.length()-2);
        String unit = src.substring(src.length()-2);
        double ret = 0;
        try {
            double nval = Double.parseDouble(val);
            if (unit.equals("pt"))
            {
                ret = nval;
            }
            else if (unit.equals("in"))
            {
                ret = nval * 72; //1pt = 1/72"
            }
            else if (unit.equals("cm"))
            {
                ret = (nval * 72) / 2.54;
            }
            else if (unit.equals("mm"))
            {
                ret = (nval * 72) / 25.4;
            }
            else if (unit.equals("pc"))
            {
                ret = nval * 12;
            }
            else if (unit.equals("px"))
            {
                ret = (nval * 72) / dpi;
            }
            else if (unit.equals("em"))
            {
                ret = em * nval;
            }
            else if (unit.equals("ex"))
            {
                ret = ex * nval;
            }
            return ret;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
    
    /** 
     * Converts the font size to absolute length in points 
     *@param parent Parent font size CSS specification
     *@param current The size specification to be converted 
     */
    public static String convertFontSize(String parent, String current)
    {
        try {
            String pval = parent.substring(0, parent.length()-2);
            double em = Double.parseDouble(pval);
            double ret = em;
            if (current.equals("medium"))
                ret = medium_font;
            else if (current.equals("small"))
                ret = medium_font / font_step;
            else if (current.equals("x-small"))
                ret = medium_font / font_step / font_step;
            else if (current.equals("xx-small"))
                ret = medium_font / font_step / font_step / font_step;
            else if (current.equals("large"))
                ret = medium_font * font_step;
            else if (current.equals("x-large"))
                ret = medium_font * font_step * font_step;
            else if (current.equals("xx-large"))
                ret = medium_font * font_step * font_step * font_step;
            else if (current.equals("smaller"))
                ret = em / font_step;
            else if (current.equals("larger"))
                ret = em * font_step;
            else if (current.endsWith("%"))
            {
                double perc = Double.parseDouble(current.substring(0, current.length()-1));
                ret = (perc * em) / 100;
            }
            else if (current.endsWith("pt") || current.endsWith("in") || current.endsWith("cm") ||
                     current.endsWith("mm") || current.endsWith("pc") || current.endsWith("px") ||
                     current.endsWith("em") || current.endsWith("ex"))
                ret = convertLength(current, em, 0.6*em);
            
            NumberFormat f = new DecimalFormat("#.#", new DecimalFormatSymbols(new Locale("en")));
            return f.format(ret) + "pt";
        } catch (NumberFormatException e) {
            return current; //cannot convert
        }
    }
    
    //========================================================================
    
    public static void normalizeUnits(CSSStyleDeclaration parent, CSSStyleDeclaration decl)
    {
        RGBColor rgb = new RGBColor();
        for (int i = 0; i < decl.getLength(); i++)
        {
            String name = decl.item(i);
            String value = decl.getPropertyValue(name);
            String prio = decl.getPropertyPriority(name);
            if (name.equals("font-size"))
            {
                String psize = "";
                if (parent != null)
                    psize = parent.getPropertyValue("font-size");
                if (psize.length() == 0)
                    psize = default_font_size;
                decl.setProperty(name, convertFontSize(psize, value), prio);
            }
            if (name.equals("color") || name.indexOf("-color") > 0)
            {
                if (!value.startsWith("#") && !value.startsWith("rgb("))
                {
                    String ncolor = rgb.getColorString(value);
                    if (ncolor != null)
                        decl.setProperty(name, ncolor, prio);
                }
            }
            if (name.startsWith("border-") && name.endsWith("-width"))
            {
            		if (value.equals("thin"))
            			decl.setProperty(name, "1px", prio);
            		else if (value.equals("medium"))
            			decl.setProperty(name, "3px", prio);
            		else if (value.equals("thick"))
            			decl.setProperty(name, "5px", prio);
            }
        }
    }
    
}
