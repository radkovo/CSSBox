/*
 * CSSNorm.java
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
 * Created on 21. leden 2005, 22:28
 */

package org.fit.cssbox.css;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.*;
import com.steadystate.css.dom.*;

/**
 * This class provides static methods for CSS property analysis.
 *
 * @author  radek
 */
public class CSSNorm 
{
	public static boolean isNumber(String s)
	{
		try {
			Double.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
    public static boolean isColor(String s)
    {
        if (s.startsWith("#") && (s.length() == 7 || s.length() == 4))
            return true;
        if (s.equals("transparent"))
            return true;
        if (s.startsWith("rgb("))
            return true;
        for (int i = 0; i < s.length(); i++)
            if (!Character.isLetter(s.charAt(i)))
                return false;
        return true;
    }

    public static boolean isUrl(String s)
    {
        return (s.startsWith("url(") || s.startsWith("'"));
    }
    
    public static boolean isRepeat(String s)
    {
        return (s.equals("repeat") || s.equals("repeat-x") || s.equals("repeat-y") || s.equals("no-repeat"));
    }
    
    public static boolean isAttachment(String s)
    {
        return (s.equals("scroll") || s.equals("fixed"));
    }
    
    public static boolean isDirection(String s)
    {
       return (s.equals("top") || s.equals("bottom") || 
               s.equals("right") || s.equals("left") ||
               s.equals("center"));
    }
    
    public static boolean isPercent(String s)
    {
        return s.endsWith("%");
    }
    
    public static boolean isLength(String s)
    {
        return (s.endsWith("em") || s.endsWith("ex") || s.endsWith("px") ||
                s.endsWith("in") || s.endsWith("cm") || s.endsWith("mm") || s.endsWith("pt") || s.endsWith("pc") ||
                s.equals("0"));
    }
    
    public static boolean isBorderWidth(String s)
    {
        return (s.equals("thin") || s.equals("medium") || s.equals("thick") || isLength(s));
    }
    
    public static boolean isBorderStyle(String s)
    {
        return (s.equals("none") || s.equals("hidden") || s.equals("dotted") ||
                s.equals("dashed") || s.equals("solid") || s.equals("double") ||
                s.equals("groove") || s.equals("ridge") || s.equals("inset") ||
                s.equals("outset"));
    }

    public static boolean isFontStyle(String s)
    {
        return (s.equals("normal") || s.equals("italic") || s.equals("oblique"));
    }

    public static boolean isFontVariant(String s)
    {
        return (s.equals("normal") || s.equals("small-caps") || s.equals("oblique"));
    }
    
    public static boolean isFontWeight(String s)
    {
        return (s.equals("normal") || s.equals("bold") || 
                s.equals("bolder") || s.equals("lighter") ||
                s.equals("100") || s.equals("200") || s.equals("300") ||
                s.equals("400") || s.equals("500") || s.equals("600") ||
                s.equals("700") || s.equals("800") || s.equals("900"));
    }
    
    public static boolean isFontSize(String s)
    {
        return (s.equals("xx-small") || s.equals("x-small") || s.equals("small") || 
                s.equals("medium") || 
                s.equals("large") || s.equals("x-large") || s.equals("xx-large") ||
                s.equals("larger") || s.equals("smaller") ||
                isLength(s) || isPercent(s));
    }
    
    public static boolean isLineHeight(String s)
    {
        return (s.equals("normal") || isNumber(s) || isLength(s) || isPercent(s));
    }
    
    public static boolean isListType(String s)
    {
        return (s.equals("disc") || s.equals("circle") || s.equals("square") ||
                s.equals("decimal") || s.equals("decimal-leading-zero") ||
                s.equals("lower-roman") || s.equals("upper-roman") ||
                s.equals("lower-greek") ||
                s.equals("lower-latin") || s.equals("upper-latin") ||
                s.equals("lower-alpha") || s.equals("upper-alpha") ||
                s.equals("hebrew") || s.equals("armenian") || 
                s.equals("georgian") || s.equals("cjk-ideographic") || 
                s.equals("hiragana") || s.equals("katakana") ||
                s.equals("hiragana-iroha") || s.equals("katakana-iroha"));
    }
    
    public static boolean isListPosition(String s)
    {
        return (s.equals("inside") || s.equals("outside"));
    }
    
    //=========================================================================

    public static void setProperty(CSSStyleDeclaration dest, String name, String value, String prio)
    {
        //background: color image repeat attachment position
        if (name.equals("background"))
        {
            String color = "transparent";
            String image = "none";
            String repeat = "repeat";
            String attachment = "scroll";
            String position1 = "0%", position2 = "0%";

            String[] field = value.replaceAll("\\s*,\\s*", ",").split("\\s+");
            for (int i = 0; i < field.length; i++)
            {
                if (isAttachment(field[i])) attachment = field[i];
                else if (isRepeat(field[i])) repeat = field[i];
                else if (isUrl(field[i])) image = field[i];
                else if (isDirection(field[i]) || isPercent(field[i]) || isLength(field[i]))
                {
                    position1 = field[i];
                    if (i+1 < field.length && 
                        (isDirection(field[i+1]) || isPercent(field[i+1]) || isLength(field[i+1])))
                    {
                        position2 = field[i+1];
                        i++;
                    }
                    else
                        position2 = null;
                }
                else if (isColor(field[i]))
                    color = field[i];
            }
            setProperty(dest, "background-color", color, prio);
            setProperty(dest, "background-image", image, prio);
            setProperty(dest, "background-repeat", repeat, prio);
            setProperty(dest, "background-attachment", attachment, prio);
            if (position2 != null)
                setProperty(dest, "background-position", position1+" "+position2, prio);
            else
                setProperty(dest, "background-position", position1, prio);
        }

        //Border: width style color
        else if (name.equals("border"))
        {
            setProperty(dest, "border-top", value, prio);
            setProperty(dest, "border-right", value, prio);
            setProperty(dest, "border-bottom", value, prio);
            setProperty(dest, "border-left", value, prio);            
        }
        else if (name.equals("border-color"))
        {
            setProperty(dest, "border-top-color", value, prio);
            setProperty(dest, "border-right-color", value, prio);
            setProperty(dest, "border-bottom-color", value, prio);
            setProperty(dest, "border-left-color", value, prio);            
        }
        else if (name.equals("border-style"))
        {
            setProperty(dest, "border-top-style", value, prio);
            setProperty(dest, "border-right-style", value, prio);
            setProperty(dest, "border-bottom-style", value, prio);
            setProperty(dest, "border-left-style", value, prio);            
        }
        else if (name.equals("border-width"))
        {
            setProperty(dest, "border-top-width", value, prio);
            setProperty(dest, "border-right-width", value, prio);
            setProperty(dest, "border-bottom-width", value, prio);
            setProperty(dest, "border-left-width", value, prio);            
        }
        else if (name.equals("border-top") || name.equals("border-right") || 
                 name.equals("border-bottom") || name.equals("border-left"))
        {
            String dir = name.substring(7);
            String width = "medium";
            String style = "none";
            String color = "auto"; //toto by mela byt spravne aktualni hodnota 'color'

            String[] field = value.replaceAll("\\s*,\\s*", ",").split("\\s+");
            for (int i = 0; i < field.length; i++)
            {
                if (isBorderWidth(field[i])) width = field[i];
                else if (isBorderStyle(field[i])) style = field[i];
                else if (isColor(field[i])) color = field[i];
            }
            setProperty(dest, "border-"+dir+"-width", width, prio);
            setProperty(dest, "border-"+dir+"-style", style, prio);
            setProperty(dest, "border-"+dir+"-color", color, prio);
        }
        
        //Font
        else if (name.equals("font"))
        {
            if (value.equals("caption") || value.equals("icon") ||
                value.equals("menu") || value.equals("message-box") ||
                value.equals("small-caption") || value.equals("status-bar"))
            {
                setProperty(dest, name, value, prio); //these are atomic
            }
            else
            {
                String style = "normal";
                String variant = "normal";
                String weight = "normal";
                String size = "medium";
                String lheight = "normal";
                String family = "";
                
                String val = value.replaceAll("\\s*,\\s*", ",").replaceAll("\\s*/\\s*", "/");
                String[] field = val.split("\\s+");
                for (int i = 0; i < field.length; i++)
                {
                    if (field[i].equals("normal")) { /* this says nothing */ }
                    else if (isFontStyle(field[i])) style = field[i];
                    else if (isFontVariant(field[i])) variant = field[i];
                    else if (isFontWeight(field[i])) weight = field[i];
                    else if (field[i].indexOf('/') != -1)
                    {
                        String[] spec = field[i].split("/");
                        if (spec.length == 2)
                        {
                            size = spec[0];
                            lheight = spec[1];
                        }
                    }
                    //HACK: css parser omits "/" between size and line height
                    else if (i+1 < field.length && isFontSize(field[i]) && isLineHeight(field[i+1]))
                    {
                    	size = field[i];
                    	lheight = field[i+1];
                    	i++;
                    }
                    else if (isFontSize(field[i])) size = field[i];
                    else /* the remaining must be font family */
                    {
                        family = family + field[i] + " ";
                    }
                }
                if (family.equals("")) family = "sans-serif";
                setProperty(dest, "font-style", style, prio);
                setProperty(dest, "font-variant", variant, prio);
                setProperty(dest, "font-weight", weight, prio);
                setProperty(dest, "font-size", size, prio);
                setProperty(dest, "line-height", lheight, prio);
                setProperty(dest, "font-family", family, prio);
            }
        }
        
        //list-style
        else if (name.equals("list-style"))
        {
            String type = "disc";
            String position = "outside";
            String image = "none";

            String[] field = value.split("\\s+");
            for (int i = 0; i < field.length; i++)
            {
                if (field[i].equals("none")) { type = "none"; image = "none"; }
                else if (isListType(field[i])) type = field[i];
                else if (isListPosition(field[i])) position = field[i];
                else if (isUrl(field[i])) image = field[i];
            }
            setProperty(dest, "list-style-type", type, prio);
            setProperty(dest, "list-style-position", position, prio);
            setProperty(dest, "list-style-image", image, prio);
        }
        
        //margin and padding
        else if (name.equals("margin") || name.equals("padding"))
        {
            String[] vals = new String[4];
            String[] fields = value.trim().split("\\s+");
            if (fields.length == 1)
                for (int i = 0; i < 4; i++) vals[i] = fields[0];
            else if (fields.length == 2)
            {
                vals[0] = vals[2] = fields[0];
                vals[1] = vals[3] = fields[1];
            }
            else if (fields.length == 3)
            {
                vals[0] = fields[0];
                vals[2] = fields[2];
                vals[1] = vals[3] = fields[1];
            }
            else if (fields.length == 4)
                for (int i = 0; i < 4; i++) vals[i] = fields[i];
            else
            {
                System.err.println("CSSNorm::setProperty: Invalid "+name+" specification: "+value);
                for (int i = 0; i < 4; i++) vals[i] = fields[i];
            }
            setProperty(dest, name + "-top", vals[0], prio);
            setProperty(dest, name + "-right", vals[1], prio);
            setProperty(dest, name + "-bottom", vals[2], prio);
            setProperty(dest, name + "-left", vals[3], prio);
        }
        
        //outline
        else if (name.equals("outline"))
        {
            String color = "invert";
            String style = "none";
            String width = "medium";
            String[] field = value.replaceAll("\\s*,\\s*", ",").split("\\s+");
            for (int i = 0; i < field.length; i++)
            {
                if (isColor(field[i]) || field[i].equals("invert")) color = field[i];
                else if (isBorderStyle(field[i])) style = field[i];
                else if (isBorderWidth(field[i])) width = field[i];
            }
            setProperty(dest, "outline-color", color, prio);
            setProperty(dest, "outline-style", style, prio);
            setProperty(dest, "outline-width", width, prio);
        }
        
        //Basic property
        else
        {
            String oldprio = dest.getPropertyPriority(name);
            if (oldprio.equals("") || prio.equals("important")) //do not override important declarations with non-important ones
                dest.setProperty(name, value, prio);
        }
    }
    
    //=========================================================================
    
    public static CSSStyleDeclaration normalizeDeclaration(CSSStyleDeclaration src)
    {
        CSSStyleDeclaration dest = new CSSStyleDeclarationImpl(src.getParentRule());
        while (src.getLength() > 0)
        {
            String name = src.item(0);
            String value = src.getPropertyValue(name);
            String prio = src.getPropertyPriority(name);
            src.removeProperty(name);
            
            setProperty(dest, name, value, prio);
        }
        return dest;
    }

    public static CSSStyleDeclaration joinDeclaration(CSSStyleDeclaration src, CSSStyleDeclaration dest)
    {
        if (dest == null)
            dest = new CSSStyleDeclarationImpl(src.getParentRule());
        for (int i = 0; i < src.getLength(); i++)
        {
            String name = src.item(i);
            String value = src.getPropertyValue(name);
            String prio = src.getPropertyPriority(name);
            String oldprio = dest.getPropertyPriority(name);
            if (oldprio.equals("") || prio.equals("important")) //do not override important declarations with non-important ones
                dest.setProperty(name, value, prio);
        }
        return dest;
    }
    
    public static CSSStyleSheet normalizeStyleSheet(CSSStyleSheet src)
    {
        CSSRuleList rules = src.getCssRules();
        for (int i = 0; i < rules.getLength(); i++)
            if (rules.item(i).getType() == CSSRule.STYLE_RULE)
            {
                CSSStyleRuleImpl rule = (CSSStyleRuleImpl) rules.item(i);
                rule.setStyle((CSSStyleDeclarationImpl) normalizeDeclaration(rule.getStyle()));
            }
        return src;
    }
    
    public static CSSStyleDeclaration computeInheritedStyle(CSSStyleDeclaration parent,
                                                            CSSStyleDeclaration current)
    {
    	//when the parent has no style, return the child style only
    	if (parent == null)
    		return current;
    	
        CSSStyleDeclaration ret = new CSSStyleDeclarationImpl(current.getParentRule());
        //copy all styles that are inherited by default
        for (int i = 0; i < parent.getLength(); i++)
        {
            String name = parent.item(i);
            if (isInherited(name))
            {
                String value = parent.getPropertyValue(name);
                String prio = parent.getPropertyPriority(name);
                ret.setProperty(name, value, prio);
            }
        }
        //copy the remaining from current style, inherit the properties
        //declared as 'inherit'
        for (int i = 0; i < current.getLength(); i++)
        {
            String name = current.item(i);
            String value = current.getPropertyValue(name);
            String prio = current.getPropertyPriority(name);
            if (value.equals("inherit"))
            {
                value = parent.getPropertyValue(name);
                if (name.length() > 0 && value.length() > 0)
                    try {
                        ret.setProperty(name, value, prio);
                    } catch (DOMException e) {
                        System.err.println("Error: setProperty('"+name+"','"+value+"','"+prio+"'): "+e.getMessage());
                    }
            }
            else
                ret.setProperty(name, value, prio);
        }

        CSSUnits.normalizeUnits(parent, ret);
        return ret;
    }

    public static boolean isInherited(String property)
    {
        return (property.equals("azimuth") ||
                property.equals("border-collapse") ||
                property.equals("border-spacing") ||
                property.equals("caption-side") ||
                property.equals("color") ||
                property.equals("cursor") ||
                property.equals("direction") ||
                property.equals("elevation") ||
                property.equals("emtpty-cells") ||
                property.equals("font-family") ||
                property.equals("font-size") ||
                property.equals("font-style") ||
                property.equals("font-variant") ||
                property.equals("font-weight") ||
                property.equals("letter-spacing") ||
                property.equals("line-height") ||
                property.equals("list-style-image") ||
                property.equals("list-style-position") ||
                property.equals("list-style-type") ||
                property.equals("orphans") ||
                property.equals("page-break-inside") ||
                property.equals("pitch-range") ||
                property.equals("pitch") ||
                property.equals("richness") ||
                property.equals("speak-header") ||
                property.equals("speak-numeral") ||
                property.equals("speak-punctuation") ||
                property.equals("speak") ||
                property.equals("speech-rate") ||
                property.equals("stress") ||
                property.equals("text-align") ||
                property.equals("text-indent") ||
                property.equals("text-transform") ||
                property.equals("visibility") ||
                property.equals("voice-family") ||
                property.equals("volume") ||
                property.equals("white-space") ||
                property.equals("windows") ||
                property.equals("word-spacing"));
    }
    
    //=========================================================================

    /**
     * Defines a standard HTML style sheet defining the basic style of the individual elements.
     * It corresponds to the CSS2.1 recommendation (Appendix D).
     * @return the style string
     */
    public static String stdStyleSheet()
    {
        return
        "html, address,"+
        "blockquote,"+
        "body, dd, div,"+
        "dl, dt, fieldset, form,"+
        "frame, frameset,"+
        "h1, h2, h3, h4,"+
        "h5, h6, noframes,"+
        "ol, p, ul, center,"+
        "dir, hr, menu, pre   { display: block }"+
        "li              { display: list-item }"+
        "head            { display: none }"+
        "table           { display: table }"+
        "tr              { display: table-row }"+
        "thead           { display: table-header-group }"+
        "tbody           { display: table-row-group }"+
        "tfoot           { display: table-footer-group }"+
        "col             { display: table-column }"+
        "colgroup        { display: table-column-group }"+
        "td, th          { display: table-cell; }"+
        "caption         { display: table-caption }"+
        "th              { font-weight: bolder; text-align: center }"+
        "caption         { text-align: center }"+
        "body            { margin: 8px; line-height: 1.12 }"+
        "h1              { font-size: 2em; margin: .67em 0 }"+
        "h2              { font-size: 1.5em; margin: .75em 0 }"+
        "h3              { font-size: 1.17em; margin: .83em 0 }"+
        "h4, p,"+
        "blockquote, ul,"+
        "fieldset, form,"+
        "ol, dl, dir,"+
        "menu            { margin: 1.12em 0 }"+
        "h5              { font-size: .83em; margin: 1.5em 0 }"+
        "h6              { font-size: .75em; margin: 1.67em 0 }"+
        "h1, h2, h3, h4,"+
        "h5, h6, b,"+
        "strong          { font-weight: bolder }"+
        "blockquote      { margin-left: 40px; margin-right: 40px }"+
        "i, cite, em,"+
        "var, address    { font-style: italic }"+
        "pre, tt, code,"+
        "kbd, samp       { font-family: monospace }"+
        "pre             { white-space: pre }"+
        "button, textarea,"+
        "input, object,"+
        "select          { display:inline-block; }"+
        "big             { font-size: 1.17em }"+
        "small, sub, sup { font-size: .83em }"+
        "sub             { vertical-align: sub }"+
        "sup             { vertical-align: super }"+
        "table           { border-spacing: 2px; }"+
        "thead, tbody,"+
        "tfoot           { vertical-align: middle }"+
        "td, th          { vertical-align: inherit }"+
        "s, strike, del  { text-decoration: line-through }"+
        "hr              { border: 1px inset }"+
        "ol, ul, dir,"+
        "menu, dd        { margin-left: 40px }"+
        "ol              { list-style-type: decimal }"+
        "ol ul, ul ol,"+
        "ul ul, ol ol    { margin-top: 0; margin-bottom: 0 }"+
        "u, ins          { text-decoration: underline }"+
        "br:before       { content: \"\\A\" }"+
        ":before, :after { white-space: pre-line }"+
        "center          { text-align: center }"+
        "abbr, acronym   { font-variant: small-caps; letter-spacing: 0.1em }"+
        ":link, :visited { text-decoration: underline }"+
        ":focus          { outline: thin dotted invert }"+

        "BDO[DIR=\"ltr\"]  { direction: ltr; unicode-bidi: bidi-override }"+
        "BDO[DIR=\"rtl\"]  { direction: rtl; unicode-bidi: bidi-override }"+

        "*[DIR=\"ltr\"]    { direction: ltr; unicode-bidi: embed }"+
        "*[DIR=\"rtl\"]    { direction: rtl; unicode-bidi: embed }";        
    }

    /**
     * A style sheet defining the additional basic style not covered by the CSS recommendation.
     * @return The style string
     */
    public static String userStyleSheet()
    {
        return
        "body   { color: black; background-color: #fafafa;}"+
        "a      { color: blue; text-decoration: underline; }"+
        "script { display: none; }"+
        "option { display: none; }"+
        "br     { display: block; }"+
        "hr     { display: block; margin-top: 1px solid; }"+
        
        //standard <ul> margin according to Mozilla
        "ul     { margin-left: 0; padding-left: 40px; }";
    }
    
}
