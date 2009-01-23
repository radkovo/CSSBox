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

import org.fit.cssbox.css.CSSUnits;

import cz.vutbr.web.css.*;

/**
 * The visual context represents the context of the element - the current font properties, EM and EX values,
 * font metrics and color.
 *
 * @author  burgetr
 */
public class VisualContext 
{
    private VisualContext parent;
    private Font font; //current font
    private FontMetrics fm; //current font metrics
    private CSSProperty.FontWeight fontWeight;
    private CSSProperty.FontStyle fontStyle;
    private CSSProperty.FontVariant fontVariant;
    private CSSProperty.TextDecoration textDecoration;
    private double em; //number of pixels in 1em
    private double ex; //number of pixels in 1ex
    private double dpi; //number of pixels in 1 inch
    
    public Color color; //current text color
    
    public VisualContext(VisualContext parent)
    {
        this.parent = parent;
        font = new Font(Font.SERIF, Font.PLAIN, 12);
        fontWeight = CSSProperty.FontWeight.NORMAL;
        fontStyle = CSSProperty.FontStyle.NORMAL;
        fontVariant = CSSProperty.FontVariant.NORMAL;
        textDecoration = CSSProperty.TextDecoration.NONE;
        em = CSSUnits.medium_font;
        ex = 0.8 * em;
        dpi = org.fit.cssbox.css.CSSUnits.dpi;
        color = Color.BLACK;
    }
    
    public VisualContext create()
    {
        VisualContext ret = new VisualContext(this);
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
    
    public VisualContext getParentContext()
    {
        return parent;
    }
    
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
        return fontVariant.toString();
    }
    
    /**
     * Returns the text decoration used for the box.
     * @return <code>none</code>, <code>underline</code>, <code>overline</code>, <code>line-through</code> or <code>blink</code>
     */
    public String getTextDecoration()
    {
        return textDecoration.toString();
    }
    
    /**
     * The text color used for the box.
     * @return color specification
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * @return the em value of the context
     */
    public double getEm()
    {
        return em;
    }

    /**
     * @return the ex value of the context
     */
    public double getEx()
    {
        return ex;
    }

    /**
     * @return the dpi value used in the context
     */
    public double getDpi()
    {
        return dpi;
    }

    //=========================================================================
    
    /** 
     * Updates the context according to the given element style. The properties that are not defined 
     * in the style are left unchanged.
     * @param style the style data 
     */
    public void update(NodeData style)
    {
        //setup the font
        String family;
        TermList fmlspec = style.getValue(TermList.class, "font-family");
        if (fmlspec == null)
            family = font.getFamily();
        else
            family = getFontName(fmlspec);
        
        double size;
        double psize = (parent == null) ? CSSUnits.medium_font : parent.getEm();
        CSSProperty.FontSize fsize = style.getProperty("font-size");
        if (fsize == null)
            size = em;
        else if (fsize == CSSProperty.FontSize.length || fsize == CSSProperty.FontSize.percentage)
        {
            TermLengthOrPercent lenspec = style.getValue(TermLengthOrPercent.class, "font-size");
            if (lenspec != null)
            {
                em = psize;
                size = ptLength(lenspec, psize);
            }
            else
                size = em;
        }
        else
            size = CSSUnits.convertFontSize(psize, fsize);
        
        CSSProperty.FontWeight weight = style.getProperty("font-weight");
        if (weight != null) fontWeight = weight;
        CSSProperty.FontStyle fstyle =  style.getProperty("font-style");
        if (fstyle != null) fontStyle = fstyle;
        int fs = Font.PLAIN;
        if (representsBold(fontWeight))
            fs = Font.BOLD;
        if (fontStyle == CSSProperty.FontStyle.ITALIC || fontStyle == CSSProperty.FontStyle.OBLIQUE)
            fs = fs | Font.ITALIC;
        
        font = new Font(family, fs, (int) Math.round(size));
        em = size;
        
        CSSProperty.FontVariant variant = style.getProperty("font-variant");
        if (variant != null) fontVariant = variant;
        CSSProperty.TextDecoration decor = style.getProperty("text-decoration");
        if (decor != null) textDecoration = decor;
        
        //color
        TermColor clr = style.getValue(TermColor.class, "color");
        if (clr != null) color = clr.getValue();
    }
    
    /** 
     * Updates a Graphics according to this context
     * @param Graphics to be updated
     */
    public void updateGraphics(Graphics2D g)
    {
        g.setFont(font);
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    }
     
    /** 
     * Updates this context according to the given style. Moreover given Graphics is updated
     * to this style and used for taking the font metrics.
     * @param style the style data to be used
     * @param g Graphics to be updated and used 
     */
    public void updateForGraphics(NodeData style, Graphics2D g)
    {
        if (style != null) update(style);
        updateGraphics(g);
        fm = g.getFontMetrics();
        ex = (int) (fm.getHeight() * 0.6); //em has been updated in update()
    }
    
    
    //-----------------------------------------------------------------------
    
    /**
     * Computes current text line height.
     * @return the height of the normal text line in pixels
     */
    public int getFontHeight()
    {
        return fm.getHeight();
    }
    
    /** 
     * Converts a length from a CSS length or percentage to 'pt'.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'pt' 
     */
    public double ptLength(TermLengthOrPercent spec, double whole)
    {
        float nval = spec.getValue();
        if (spec.isPercentage())
            return (whole * nval) / 100;
        else
        {
            TermLength.Unit unit = spec.getUnit();
            
            double ret = 0;
            if (unit == TermLength.Unit.pt)
            {
                ret = nval;
            }
            else if (unit == TermLength.Unit.in)
            {
                ret = nval * 72;
            }
            else if (unit == TermLength.Unit.cm)
            {
                ret = (nval * 72) / 2.54;
            }
            else if (unit == TermLength.Unit.mm)
            {
                ret = (nval * 72) / 25.4;
            }
            else if (unit == TermLength.Unit.pc)
            {
                ret = nval * 12;
            }
            else if (unit == TermLength.Unit.px)
            {
                ret = (nval * 72) / dpi;
            }
            else if (unit == TermLength.Unit.em)
            {
                ret = em * nval;
            }
            else if (unit == TermLength.Unit.ex)
            {
                ret = ex * nval;
            }
            return ret;
        }
    }

    /** 
     * Converts a length from a CSS length to 'pt'. Percentages are always evaluated to 0.
     * @param spec the CSS length specification
     * @return font size in 'pt' 
     */
    public double ptLength(TermLengthOrPercent spec)
    {
        return ptLength(spec, 0);
    }
    
    /** 
     * Converts a length from a CSS length or percentage to 'px'.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'px' 
     */
    public double pxLength(TermLengthOrPercent spec, double whole)
    {
        float nval = spec.getValue();
        if (spec.isPercentage())
            return (whole * nval) / 100;
        else
        {
            TermLength.Unit unit = spec.getUnit();
            
            double ret = 0;
            if (unit == TermLength.Unit.pt)
            {
                ret = (nval * dpi) / 72;
            }
            else if (unit == TermLength.Unit.in)
            {
                ret = nval * dpi;
            }
            else if (unit == TermLength.Unit.cm)
            {
                ret = (nval * dpi) / 2.54;
            }
            else if (unit == TermLength.Unit.mm)
            {
                ret = (nval * dpi) / 25.4;
            }
            else if (unit == TermLength.Unit.pc)
            {
                ret = (nval * 12 * dpi) / 72;
            }
            else if (unit == TermLength.Unit.px)
            {
                ret = nval;
            }
            else if (unit == TermLength.Unit.em)
            {
                ret = (em * nval * dpi) / 72; //em is in pt
            }
            else if (unit == TermLength.Unit.ex)
            {
                ret = (ex * nval * dpi) / 72;
            }
            return ret;
        }
    }
    
    /** 
     * Converts a length from a CSS length to 'px'. Percentages are always evaluated to 0.
     * @param spec the CSS length specification
     * @return font size in 'px' 
     */
    public double pxLength(TermLengthOrPercent spec)
    {
        return pxLength(spec, 0);
    }
    
    /**
     * Converts the weight value to bold / not bold
     * @param weight a CSS weight
     * @return true if the given weight corresponds to bold
     */
    public boolean representsBold(CSSProperty.FontWeight weight)
    {
        if (weight == CSSProperty.FontWeight.BOLD ||
            weight == CSSProperty.FontWeight.numeric_600 ||    
            weight == CSSProperty.FontWeight.numeric_700 ||    
            weight == CSSProperty.FontWeight.numeric_800 ||    
            weight == CSSProperty.FontWeight.numeric_900)
        {
            return true;
        }
        else
            return false;
    }
    
    /** 
     * Scans a list of font definitions and chooses the first one that is available
     * @param list of terms obtained from the font-family property
     * @return a font name string according to java.awt.Font
     */
    public String getFontName(TermList list)
    {
        String avail[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (Term<?> term : list)
        {
            Object value = term.getValue();
            if (value instanceof CSSProperty.FontFamily)
                return ((CSSProperty.FontFamily) value).getAWTValue();
            else
            {
                String name = fontAvailable(value.toString(), avail);
                if (name != null) return name;
            }
        }
        //nothing found, use Serif
        return java.awt.Font.SERIF;
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
