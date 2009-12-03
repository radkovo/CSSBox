/*
 * CSSDecoder.java
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
 * Created on 7. zברם 2005, 15:39
 */

package org.fit.cssbox.layout;

import cz.vutbr.web.css.*;

/**
 * This class implements converting the CSS specifications to Java data types.
 *
 * @author  burgetr
 */
public class CSSDecoder 
{
    private VisualContext context;
    
    /**
     * Creates a new VisualDecoder in the specified context. The context is
     * used for the unit conversion (e.g. the <code>em</code> units etc.)
     * @param c the visual contect to be assigned  
     */
    public CSSDecoder(VisualContext c)
    {
        context = c;
    }
    
    /**
     * Returns the visual context of this decoder.
     * @return the assigned visual context
     */
    public VisualContext getContext()
    {
        return context;
    }

    /**
     * Changes the visual context assigned to this decoder.
     * @param c The new visual context to be assigned
     */
    public void setContext(VisualContext c)
    {
        context = c;
    }
    
    /**
     * If the value is defined and not empty, it is returned back. Otherwise,
     * the default value is returned.
     * @param value the value to be checked
     * @param defval the default value
     * @return the <code>value</code> if it is defined, <code>defval</code> otherwise
     */
    /*public String getValue(String value, String defval)
    {
        if (value == null || value.trim().length() == 0)
            return defval;
        else
            return value;
    }*/
    
    /** 
     * Returns the length in pixels from a CSS definition. <code>null</code> values
     * of the lengths are interpreted as zero.
     * @param value The length or percentage value to be converted
     * @param auto True, if the property is set to <code>auto</code>
     * @param defval The length value to be used when the first one is null
     * @param autoval The value to be used when "auto" is specified
     * @param whole the length to be returned as 100% (in case of percentage values)
     */
    public int getLength(TermLengthOrPercent value, boolean auto, TermLengthOrPercent defval, TermLengthOrPercent autoval, int whole)
    {
        TermLengthOrPercent val = value;
        if (value == null) val = defval;
        if (auto) val = autoval;
        if (val != null)
        	return (int) Math.round(context.pxLength(val, whole));
        else
        	return 0;
    }
    
    /** 
     * Returns the length in pixels from a CSS definition
     * @param value The length or percentage value to be converted
     * @param auto True, if the property is set to <code>auto</code>
     * @param defval The length value to be used when the first one is null
     * @param autoval The value to be used when "auto" is specified
     * @param whole the length to be returned as 100% (in case of percentage values)
     */
    public int getLength(TermLengthOrPercent value, boolean auto, int defval, int autoval, int whole)
    {
        if (value == null)
            return defval;
        else if (auto)
            return autoval;
        else
            return (int) Math.round(context.pxLength(value, whole));
    }
    
}
