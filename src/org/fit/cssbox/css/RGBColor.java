/*
 * RGBColor.java
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
 * Created on 15. zברם 2005, 11:58
 */

package org.fit.cssbox.css;

import java.util.*;

/**
 * This class implements the RGB color definition decoder.
 * 
 * @author  burgetr
 */
public class RGBColor 
{
    private HashMap<String, ColorEntry> colors;

    /**
     * Creates a new color decoder
     */
    public RGBColor()
    {
        colors = new HashMap<String, ColorEntry>();
        initColorsCSS();
    }

    private void initColorsCSS()
    {
	    	insert(0x80,0x00,0x00,"maroon");
	    	insert(0xff,0x00,0x00,"red");
	    	insert(0xff,0xA5,0x00,"orange");
	    	insert(0xff,0xff,0x00,"yellow");
	    	insert(0x80,0x80,0x00,"olive");
	    	insert(0x80,0x00,0x80,"purple");
	    	insert(0xff,0x00,0xff,"fuchsia");
	    	insert(0xff,0xff,0xff,"white");
	    	insert(0x00,0xff,0x00,"lime");
	    	insert(0x00,0x80,0x00,"green");
	    	insert(0x00,0x00,0x80,"navy");
	    	insert(0x00,0x00,0xff,"blue");
	    	insert(0x00,0xff,0xff,"aqua");
	    	insert(0x00,0x80,0x80,"teal");
	    	insert(0x00,0x00,0x00,"black");
	    	insert(0xc0,0xc0,0xc0,"silver");
	    	insert(0x80,0x80,0x80,"gray");
    }
    
    /**
     * Finds a color specification corresponding to a name
     * @param name the color name
     * @return the corresponding color entry
     */
    public ColorEntry getColor(String name)
    {
        return (ColorEntry) colors.get(name.toLowerCase());
    }
    
    /**
     * Converts a color name to the RGB string representation
     * @param name the color name
     * @return the corresponding color RGB specification
     */
    public String getColorString(String name)
    {
        ColorEntry c = (ColorEntry) colors.get(name.toLowerCase());
        if (c != null)
            return "rgb("+c.r+","+c.g+","+c.b+")";
        else
            return null;
    }
    
    private void insert(int r, int g, int b, String name)
    {
        ColorEntry ce = new ColorEntry(r, g, b);
        colors.put(name.toLowerCase(), ce);
    }

    //========================================================================================
    
    static class ColorEntry
    {
        public int r, g, b;

        public ColorEntry(int r, int g, int b)
        {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }    
}
