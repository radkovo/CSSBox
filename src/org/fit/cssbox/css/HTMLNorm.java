/*
 * HTMLNorm.java
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
 * Created on 6. únor 2005, 18:52
 */

package org.fit.cssbox.css;

import org.w3c.dom.*;

/**
 * This class provides a mechanism of converting some HTML presentation
 * atributes to the CSS styles.
 *
 * @author  radek
 */
public class HTMLNorm 
{
    /**
     * Recursively converts some HTML presentation attributes to the inline style of the element.
     * The original attributes are left in the DOM tree, the <code>style</code> attribute is
     * modified appropriately. Some of the values (e.g. the font sizes) are converted approximately
     * since their exact interpretation is not defined.
     * @param n the root node of the DOM subtree where the conversion is done
     * @param tab_inh the inline style inherited from a parent table, empty if we're not in a table
     */
    public static void attributesToStyles(Node n, String tab_inh)
    {
        String itab = tab_inh;
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            Element el = (Element) n;
            //Analyze HTML attributes
            String attrs = "";
            //background
            if (el.getTagName().equals("table") ||
                el.getTagName().equals("tr") ||
                el.getTagName().equals("th") ||
                el.getTagName().equals("td") ||
                el.getTagName().equals("body"))
            {
                if (el.getAttributes().getNamedItem("bgcolor") != null)
                    attrs = attrs + "background-color: " + el.getAttribute("bgcolor") + ";";
            }
            //setting table and cell borders
            if (el.getTagName().equals("table"))
            {
                String border = "0";
                String frame = "void";
                String rules = "none";
                int cpadding = 0;
                int cspacing = 0;
                itab = ""; //new table has its own settings
                
                //cell padding
                if (el.getAttributes().getNamedItem("cellpadding") != null)
                {
                	try {
                		cpadding = Integer.parseInt(el.getAttribute("cellpadding"));
                		itab = itab + "padding: " + cpadding + "px; ";
                	} catch (NumberFormatException e) {
                	}
                }
                //cell spacing
                if (el.getAttributes().getNamedItem("cellspacing") != null)
                {
                	try {
                		cspacing = Integer.parseInt(el.getAttribute("cellspacing"));
                		attrs = attrs + "border-spacing: " + cspacing + "px; ";
                	} catch (NumberFormatException e) {
                	}
                }
                //borders
                if (el.getAttributes().getNamedItem("border") != null)
                {
                    border = el.getAttribute("border");
                    if (!border.equals("0"))
                    {
                        frame = "border";
                        rules = "all";
                    }
                }
                if (el.getAttributes().getNamedItem("frame") != null)
                    frame = el.getAttribute("frame").toLowerCase();
                if (el.getAttributes().getNamedItem("rules") != null)
                    frame = el.getAttribute("rules").toLowerCase();
                
                if (!border.equals("0"))
                {
                    String fstyle = "border-@-color:auto;border-@-style:solid;border-@-width:"+border+"px;";
                    if (frame.equals("above"))
                        attrs = attrs + applyBorders(fstyle, "top");
                    if (frame.equals("below"))
                        attrs = attrs + applyBorders(fstyle, "bottom");
                    if (frame.equals("hsides"))
                    {
                        attrs = attrs + applyBorders(fstyle, "left");
                        attrs = attrs + applyBorders(fstyle, "right");
                    }
                    if (frame.equals("lhs"))
                        attrs = attrs + applyBorders(fstyle, "left");
                    if (frame.equals("rhs"))
                        attrs = attrs + applyBorders(fstyle, "right");
                    if (frame.equals("vsides"))
                    {
                        attrs = attrs + applyBorders(fstyle, "top");
                        attrs = attrs + applyBorders(fstyle, "bottom");
                    }
                    if (frame.equals("box"))
                    {
                        attrs = attrs + applyBorders(fstyle, "left");
                        attrs = attrs + applyBorders(fstyle, "right");
                        attrs = attrs + applyBorders(fstyle, "top");
                        attrs = attrs + applyBorders(fstyle, "bottom");
                    }
                    if (frame.equals("border"))
                    {
                        attrs = attrs + applyBorders(fstyle, "left");
                        attrs = attrs + applyBorders(fstyle, "right");
                        attrs = attrs + applyBorders(fstyle, "top");
                        attrs = attrs + applyBorders(fstyle, "bottom");
                    }
                    
                    //when 'rules' are set, 1px border is inherited by the cells
                    fstyle = "border-@-color:auto;border-@-style:solid;border-@-width:1px;";
                    if (rules.equals("rows"))
                    {
                        itab = itab + applyBorders(fstyle, "top");
                        itab = itab + applyBorders(fstyle, "bottom");
                    }
                    if (rules.equals("rows"))
                    {
                        itab = itab + applyBorders(fstyle, "left");
                        itab = itab + applyBorders(fstyle, "right");
                    }
                    if (rules.equals("all"))
                    {
                        itab = itab + applyBorders(fstyle, "top");
                        itab = itab + applyBorders(fstyle, "bottom");
                        itab = itab + applyBorders(fstyle, "left");
                        itab = itab + applyBorders(fstyle, "right");
                    }
                }
            }
            //inherited cell properties
            if (el.getTagName().equals("th") ||
                el.getTagName().equals("td"))
            {
                if (itab.length() > 0)
                    attrs = itab + attrs;
            }
            //other borders
            if (el.getTagName().equals("img") ||
                el.getTagName().equals("object"))
            {
                if (el.getAttributes().getNamedItem("border") != null)
                {
                    String border = el.getAttribute("border");
                    String fstyle;
                    if (border.equals("0"))
                        fstyle = "border-@-style:none;";
                    else
                        fstyle = "border-@-color:auto;border-@-style:solid;border-@-width:"+border+"px;";
                    attrs = attrs + applyBorders(fstyle, "top");
                    attrs = attrs + applyBorders(fstyle, "right");
                    attrs = attrs + applyBorders(fstyle, "bottom");
                    attrs = attrs + applyBorders(fstyle, "left");
                }
            }
            //object alignment
            if (el.getTagName().equals("img") ||
            	el.getTagName().equals("object") ||
            	el.getTagName().equals("applet"))
            {
            	if (el.getAttributes().getNamedItem("align") != null)
            	{
            		String align = el.getAttribute("align");
            		if (align.equals("left"))
            			attrs = attrs + "float:left;";
            		else if (align.equals("right"))
            			attrs = attrs + "float:right;";
            	}
            }
            //Text properties
            if (el.getTagName().equals("font"))
            {
                if (el.getAttributes().getNamedItem("color") != null)
                    attrs = attrs + "color: " + el.getAttribute("color") + ";";
                if (el.getAttributes().getNamedItem("face") != null)
                    attrs = attrs + "font-family: " + el.getAttribute("face") + ";";
                if (el.getAttributes().getNamedItem("size") != null)
                {
                    String sz = el.getAttribute("size");
                    String ret = "normal";
                    if (sz.equals("1")) ret = "xx-small";
                    else if (sz.equals("2")) ret = "x-small";
                    else if (sz.equals("3")) ret = "small";
                    else if (sz.equals("4")) ret = "normal";
                    else if (sz.equals("5")) ret = "large";
                    else if (sz.equals("6")) ret = "x-large";
                    else if (sz.equals("7")) ret = "xx-large";
                    else if (sz.startsWith("+"))
                    {
                        String sn = sz.substring(1);
                        if (sn.equals("1")) ret = "120%";
                        else if (sn.equals("2")) ret = "140%";
                        else if (sn.equals("3")) ret = "160%";
                        else if (sn.equals("4")) ret = "180%";
                        else if (sn.equals("5")) ret = "200%";
                        else if (sn.equals("6")) ret = "210%";
                        else if (sn.equals("7")) ret = "220%";
                    }
                    else if (sz.startsWith("-"))
                    {
                        String sn = sz.substring(1);
                        if (sn.equals("1")) ret = "90%";
                        else if (sn.equals("2")) ret = "80%";
                        else if (sn.equals("3")) ret = "70%";
                        else if (sn.equals("4")) ret = "60%";
                        else if (sn.equals("5")) ret = "50%";
                        else if (sn.equals("6")) ret = "40%";
                        else if (sn.equals("7")) ret = "30%";
                    }
                    attrs = attrs + "font-size: " + ret;
                }
            }

            if (attrs.length() > 0)
                el.setAttribute("style", el.getAttribute("style") + ";" + attrs);            
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            attributesToStyles(child.item(i), itab);
    }
    
    //=======================================================================
    
    private static String applyBorders(String template, String dir)
    {
        return template.replaceAll("@", dir);
    }
    
}
