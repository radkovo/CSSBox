/*
 * VisualContext.java
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
 * Created on 7. zברם 2005, 15:33
 */

package org.fit.cssbox.layout;

import java.awt.*;
import org.w3c.dom.css.*;

/**
 * Trida reprezentujici vizualni kontext elementu:
 *  - velikost pisma - slouzi k vypoctu 1em
 *
 * @author  burgetr
 */
public class VisualContext 
{
    private CSSStyleDeclaration style;
    
    private Font font; //current font
    private FontMetrics fm; //current font metrics
    private String fontWeight;
    private String fontStyle;
    private String fontVariant;
    private String textDecoration;
    public int em; //number of pixels in 1em
    public int ex; //number of pixels in 1ex
    public double dpi; //number of pixels in 1 inch
    
    public Color color; //current text color
    
    public VisualContext()
    {
        font = new Font("Serif", Font.PLAIN, 12);
        fontWeight = "normal";
        fontStyle = "normal";
        em = 12;
        ex = 10;
        dpi = org.fit.cssbox.css.CSSUnits.dpi;
        color = Color.BLACK;
    }
    
    public VisualContext create()
    {
        VisualContext ret = new VisualContext();
        ret.em = em;
        ret.ex = ex;
        ret.dpi = dpi;
        ret.font = font;
        ret.fontWeight = fontWeight;
        ret.fontStyle = fontStyle;
        ret.fontVariant = fontVariant;
        ret.textDecoration = textDecoration;
        ret.color = color;
        return ret;
    }
   
    //=========================================================================
    
    /**
     * The font used for the box.
     * @return current font
     */
    public Font getFont()
    {
        return font;
    }
    
    /**
     * The font variant used for the box.
     * @return <code>normal</code> or <code>small-caps</code>
     */
    public String getFontVariant()
    {
        return fontVariant;
    }
    
    /**
     * Returns the text decoration used for the box.
     * @return <code>none</code>, <code>underline</code>, <code>overline</code>, <code>line-through</code> or <code>blink</code>
     */
    public String getTextDecoration()
    {
        return textDecoration;
    }
    
    /**
     * The text color used for the box.
     * @return color specification
     */
    public Color getColor()
    {
        return color;
    }
    
    //=========================================================================
    
    /** Update the context according to the current style */
    public void update(CSSStyleDeclaration style)
    {
        this.style = style;

        //setup the font
        String family;
        String fmlspec = getStyleProperty("font-family").trim();
        if (fmlspec.equals(""))
            family = font.getFamily();
        else
            family = getFontName(fmlspec);
        int size = getFontSize(getStyleProperty("font-size"));
        if (size == -1) size = font.getSize();
        String wspec = getStyleProperty("font-weight");
        if (!wspec.equals("")) fontWeight = wspec;
        String sspec = getStyleProperty("font-style");
        if (!sspec.equals("")) fontStyle = sspec;
        int fs = Font.PLAIN;
        if (!fontWeight.equals("normal") && !fontWeight.equals("100"))
            fs = Font.BOLD;
        if (fontStyle.equals("italic") || fontStyle.equals("oblique"))
            fs = fs | Font.ITALIC;
        
        font = new Font(family, fs, size);
        
        String vspec = getStyleProperty("font-variant");
        if (vspec.equals("small-caps"))
            fontVariant = vspec;
        else
            fontVariant = "normal";
        
        String dspec = getStyleProperty("text-decoration");
        if (dspec.equals("underline") || dspec.equals("overline") || dspec.equals("line-through"))
            textDecoration = dspec;
        else
            textDecoration = "none";
        
        //color
        Color clr = getColor(getStyleProperty("color"));
        if (clr != null) color = clr;
    }
    
    /** Update the Graphics according to this context */
    public void updateGraphics(Graphics g)
    {
        g.setFont(font);
        g.setColor(color);
    }
     
    /** Update the context according to the current style */
    public void updateForGraphics(CSSStyleDeclaration style, Graphics g)
    {
        if (style != null)
            update(style);
        updateGraphics(g);
        fm = g.getFontMetrics();
        //isn't this a bit naive?
        em = fm.charWidth('M');
        ex = (int) (fm.getHeight() * 0.6);
    }
    
    
    //-----------------------------------------------------------------------
    
    private String getStyleProperty(String name)
    {
        if (style != null)
            return style.getPropertyValue(name);
        else
            return "";
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * @return the height of the normal text line in pixels
     */
    public int getFontHeight()
    {
        return fm.getHeight();
    }
    
    /** Returns font size from the CSS declaration in pt */
    public int getFontSize(String spec)
    {
        if (spec.length() < 3) return -1;
        String s = spec.substring(0, spec.length()-2); //strip trailing 'pt'
        try {
            return (int) (Float.parseFloat(s) * 1.2);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Returns a particular length in pixels */
    public int getLength(String src)
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
                ret = (nval * dpi) / 72;
            }
            else if (unit.equals("in"))
            {
                ret = nval * dpi;
            }
            else if (unit.equals("cm"))
            {
                ret = (nval * dpi) / 2.54;
            }
            else if (unit.equals("mm"))
            {
                ret = (nval * dpi) / 25.4;
            }
            else if (unit.equals("pc"))
            {
                ret = (nval * 12 * dpi) / 72;
            }
            else if (unit.equals("px"))
            {
                ret = nval;
            }
            else if (unit.equals("em"))
            {
                ret = em * nval;
            }
            else if (unit.equals("ex"))
            {
                ret = ex * nval;
            }
            return (int) Math.round(ret);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /** Scan a list of font names and choose the first one that is available */
    public String getFontName(String list)
    {
        String avail[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String lst[] = list.split(",");
        for (int i = 0; i < lst.length; i++)
        {
            String name = lst[i].trim().toLowerCase();
            if (name.equals("serif")) return "Serif";
            else if (name.equals("sans-serif")) return "SansSerif";
            else if (name.equals("monospace")) return "Monospaced";
            else
            {
                String avf = fontAvailable(name, avail);
                if (avf != null) return avf;
            }
        }
        //nothing found, use Serif
        return "Serif";
    }
    
    /** Returns true if the font family is available.
     * @return The exact name of the font family or null if it's not available
     */
    private String fontAvailable(String family, String[] avail)
    {
        for (int i = 0; i < avail.length; i++)
            if (avail[i].equalsIgnoreCase(family)) return avail[i];
        return null;
    }
    
    /**
     * Creates a new java Color instance according to a CSS specification rgb(r,g,b)
     * @param spec the CSS color specification
     * @return the Color instance
     */
    public Color getColor(String spec)
    {
        if (spec.startsWith("rgb("))
        {
            String s = spec.substring(4, spec.length() - 1);
            String[] lst = s.split(",");
            try {
                int r = Integer.parseInt(lst[0].trim());
                int g = Integer.parseInt(lst[1].trim());
                int b = Integer.parseInt(lst[2].trim());
                return new Color(r, g, b);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else
            return null;
    }
}
