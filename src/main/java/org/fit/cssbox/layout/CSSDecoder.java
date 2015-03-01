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
 * Created on 7. zari 2005, 15:39
 */

package org.fit.cssbox.layout;

import java.awt.Rectangle;

import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.vutbr.web.css.*;

/**
 * This class implements converting the CSS specifications to Java data types.
 *
 * @author  burgetr
 */
public class CSSDecoder 
{
    protected static final Logger log = LoggerFactory.getLogger(CSSDecoder.class);

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
            return (int) context.pxLength(val, whole);
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
        if (auto)
            return autoval;
        else if (value == null)
            return defval;
        else
            return (int) context.pxLength(value, whole);
    }
    
    /**
     * Computes the width and height of a replaced object based on the following properties:
     * <ul>
     * <li>Intrinsic width and height</li>
     * <li>The <code>width</code> and <code>height</code> attributes</li>
     * <li>Effective style</li>
     * </ul>
     * @param obj The replaced content object
     * @param box The element box whose size should be computed
     * @return A rectangle with the width and height set accordingly
     */
    public static Rectangle computeReplacedObjectSize(ReplacedContent obj, ElementBox box)
    {
        int boxw; //resulting size
        int boxh;
        
        int intw; //intrinsic sizes
        int inth;
        float intr;
        if (obj != null)
        {
            intw = obj.getIntrinsicWidth();
            inth = obj.getIntrinsicHeight();
            if (intw == 0 || inth == 0)
            {
                log.warn("Obtained a zero intrinsic width or height for " + obj.toString());
                intw = inth = 1; //a fallback for avoiding zeros in ratios
            }
            intr = (float) intw / inth;
            boxw = intw;
            boxh = inth;
        }
        else
        {
            boxw = intw = 20; //some reasonable default values
            boxh = inth = 20;
            intr = 1.0f;
        }
        
        //total widths used for percentages
        int twidth = box.getContainingBlock().getContentWidth();
        int theight = box.getViewport().getContentHeight();
        
        //try to use the attributes
        Element el = box.getElement();
        int atrw = -1;
        int atrh = -1;
        try {
            if (!HTMLNorm.getAttribute(el, "width").equals(""))
                atrw = HTMLNorm.computeAttributeLength(HTMLNorm.getAttribute(el, "width"), twidth);
        } catch (NumberFormatException e) {
            log.info("Invalid width value: " + HTMLNorm.getAttribute(el, "width"));
        }
        try {
            if (!HTMLNorm.getAttribute(el, "height").equals(""))
                atrh = HTMLNorm.computeAttributeLength(HTMLNorm.getAttribute(el, "height"), theight);
        } catch (NumberFormatException e) {
            log.info("Invalid height value: " + HTMLNorm.getAttribute(el, "width"));
        }
        //apply intrinsic ration when necessary
        if (atrw == -1 && atrh == -1)
        {
            boxw = intw;
            boxh = inth;
        }
        else if (atrw == -1)
        {
            boxw = Math.round(intr * atrh);
            boxh = atrh;
        }
        else if (atrh == -1)
        {
            boxw = atrw;
            boxh = Math.round(atrw / intr);
        }
        else
        {
            boxw = atrw;
            boxh = atrh;
            intr = (float) boxw / boxh; //new intrsinsic ratio is set explicitly
        }

        //compute dimensions from styles (styles should override the attributes)
        CSSDecoder dec = new CSSDecoder(box.getVisualContext());
        CSSProperty.Width width = box.getStyle().getProperty("width");
        if (width == CSSProperty.Width.AUTO) width = null; //auto and null are equal for width
        CSSProperty.Height height = box.getStyle().getProperty("height");
        if (height == CSSProperty.Height.AUTO) height = null; //auto and null are equal for height
        if (width == null && height != null)
        {
            //compute boxh, boxw is intrinsic
            int autoh = Math.round(boxw / intr);
            boxh = dec.getLength(box.getLengthValue("height"), height == CSSProperty.Height.AUTO, boxh, autoh, theight);
            boxw = Math.round(intr * boxh);
        }
        else if (width != null && height == null)
        {
            //compute boxw, boxh is intrinsic
            int autow = Math.round(intr * boxh);
            boxw = dec.getLength(box.getLengthValue("width"), width == CSSProperty.Width.AUTO, boxw, autow, twidth);
            boxh = Math.round(boxw / intr);
        }
        else
        {
            boxw = dec.getLength(box.getLengthValue("width"), width == CSSProperty.Width.AUTO, boxw, intw, twidth);
            boxh = dec.getLength(box.getLengthValue("height"), height == CSSProperty.Height.AUTO, boxh, inth, theight);
        }
        
        return new Rectangle(boxw, boxh);
    }

    
}
