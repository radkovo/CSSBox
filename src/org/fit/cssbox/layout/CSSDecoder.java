/*
 * CSSDecoder.java
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
 * Created on 7. zברם 2005, 15:39
 */

package org.fit.cssbox.layout;

/**
 * Trida implementujici metody pro prevod CSS specifikaci
 * na Javovske datove typy.
 *
 * @author  burgetr
 */
public class CSSDecoder 
{
    private VisualContext context = new VisualContext();
    
    public CSSDecoder(VisualContext c)
    {
        context = c;
    }
    
    public VisualContext getContext()
    {
        return context;
    }

    public void setContext(VisualContext c)
    {
        context = c;
    }
    
    //===================================================================
    
    public String getValue(String value, String defval)
    {
        if (value == null || value.trim().length() == 0)
            return defval;
        else
            return value;
    }
    
    //=============== Delky na pixely ===================================
    
    /** Returns the length in pixels from a CSS definition
     * @param value The value to be converted
     * @param defval The value to be used when the first one is null or empty
     * @param auto The value to be used when "auto" is specified
     * @param whole the length to be returned as 100% (in case of percentage values)
     */
    public int getLength(String value, String defval, String auto, int whole)
    {
        try {
            String val = getValue(value, defval);
            if (val.equals("auto")) val = auto;
            if (val.endsWith("%"))
            {
                String sval = val.substring(0, val.length()-1);
                double pval = Double.parseDouble(sval);
                return (int) ((whole * pval) / 100);
            }
            else
                return context.getLength(val);
        } catch (NumberFormatException e) {
            return whole;
        }
    }
    
    /** Returns the length in pixels from a CSS definition
     * @param value The value to be converted
     * @param defval The value to be used when the first one is null or empty
     * @param auto The value to be used when "auto" is specified
     * @param whole the length to be returned as 100% (in case of percentage values)
     */
    public int getLength(String value, int defval, int auto, int whole)
    {
        try {
            if (value == null || value.trim().length() == 0)
                return defval;
            String val = value;
            if (val.equals("auto"))
                    return auto;
            if (val.endsWith("%"))
            {
                String sval = val.substring(0, val.length()-1);
                double pval = Double.parseDouble(sval);
                return (int) ((whole * pval) / 100);
            }
            else
                return context.getLength(val);
        } catch (NumberFormatException e) {
            return whole;
        }
    }
    
}
