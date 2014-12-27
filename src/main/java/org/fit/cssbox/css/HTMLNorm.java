/*
 * HTMLNorm.java
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
 * Created on 6. �nor 2005, 18:52
 */

package org.fit.cssbox.css;

import java.util.Vector;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermPercent;

import org.w3c.dom.*;

/**
 * This class provides a mechanism of converting some HTML presentation
 * atributes to the CSS styles and other methods related to HTML specifics.
 *
 * @author  radek
 */
public class HTMLNorm 
{

    /**
     * Obtains the value of the given element attribute. This function fixes the difference
     * in return values between the different DOM implementations.
     * @param el the element
     * @param name the attribute name
     * @return the attribute value or an empty string when the attribute is not present
     */
    public static String getAttribute(Element el, String name)
    {
        return el.hasAttribute(name) ? el.getAttribute(name) : "";
    }
    
    /**
     * Recursively converts some HTML presentation attributes to the inline style of the element.
     * The original attributes are left in the DOM tree, the <code>XDefaultStyle</code> attribute is
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
            final Element el = (Element) n;
            final String tagname = el.getTagName().toLowerCase();
            //Analyze HTML attributes
            String attrs = "";
            //background
            if (tagname.equals("table") ||
                tagname.equals("th") ||
                tagname.equals("td") ||
                tagname.equals("body"))
            { 
                if (el.getAttributes().getNamedItem("background") != null)
                    attrs = attrs + "background-image: url(" + el.getAttribute("background") + ");";
            }
            if (tagname.equals("table") ||
                    tagname.equals("tr") ||
                    tagname.equals("th") ||
                    tagname.equals("td") ||
                    tagname.equals("body"))
            {
                    if (el.getAttributes().getNamedItem("bgcolor") != null)
                        attrs = attrs + "background-color: " + el.getAttribute("bgcolor") + ";";
            }
            //setting table and cell borders
            if (tagname.equals("table"))
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
                    rules = el.getAttribute("rules").toLowerCase();
                
                if (!border.equals("0"))
                {
                    String fstyle = "border-@-style:solid;border-@-width:"+border+"px;";
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
                    fstyle = "border-@-style:solid;border-@-width:1px;";
                    if (rules.equals("rows"))
                    {
                        itab = itab + applyBorders(fstyle, "top");
                        itab = itab + applyBorders(fstyle, "bottom");
                        attrs = attrs + "border-collapse:collapse;"; //seems to cause table border collapsing
                    }
                    else if (rules.equals("cols"))
                    {
                        itab = itab + applyBorders(fstyle, "left");
                        itab = itab + applyBorders(fstyle, "right");
                        attrs = attrs + "border-collapse:collapse;";
                    }
                    else if (rules.equals("all"))
                    {
                        itab = itab + applyBorders(fstyle, "top");
                        itab = itab + applyBorders(fstyle, "bottom");
                        itab = itab + applyBorders(fstyle, "left");
                        itab = itab + applyBorders(fstyle, "right");
                    }
                }
            }
            //inherited cell properties
            if (tagname.equals("th") ||
                tagname.equals("td"))
            {
                if (itab.length() > 0)
                    attrs = itab + attrs;
            }
            //other borders
            if (tagname.equals("img") ||
                tagname.equals("object"))
            {
                if (el.getAttributes().getNamedItem("border") != null)
                {
                    String border = el.getAttribute("border");
                    String fstyle;
                    if (border.equals("0"))
                        fstyle = "border-@-style:none;";
                    else
                        fstyle = "border-@-style:solid;border-@-width:"+border+"px;";
                    attrs = attrs + applyBorders(fstyle, "top");
                    attrs = attrs + applyBorders(fstyle, "right");
                    attrs = attrs + applyBorders(fstyle, "bottom");
                    attrs = attrs + applyBorders(fstyle, "left");
                }
            }
            //object alignment
            if (tagname.equals("img") ||
            	tagname.equals("object") ||
            	tagname.equals("applet") ||
            	tagname.equals("iframe") ||
            	tagname.equals("input"))
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
            //table alignment
            if (tagname.equals("col") ||
                tagname.equals("colgroup") ||
                tagname.equals("tbody") ||
                tagname.equals("td") ||
                tagname.equals("tfoot") ||
                tagname.equals("th") ||
                tagname.equals("thead") ||
                tagname.equals("tr"))
                {
                    if (el.getAttributes().getNamedItem("align") != null)
                    {
                        String align = el.getAttribute("align");
                        if (align.equals("left"))
                            attrs = attrs + "text-align:left;";
                        else if (align.equals("right"))
                            attrs = attrs + "text-align:right;";
                        else if (align.equals("center"))
                            attrs = attrs + "text-align:center;";
                        else if (align.equals("justify"))
                            attrs = attrs + "text-align:justify;";
                    }
                    if (el.getAttributes().getNamedItem("valign") != null)
                    {
                        String align = el.getAttribute("valign");
                        if (align.equals("top"))
                            attrs = attrs + "vertical-align:top;";
                        else if (align.equals("middle"))
                            attrs = attrs + "vertical-align:middle;";
                        else if (align.equals("bottom"))
                            attrs = attrs + "vertical-align:bottom;";
                        else if (align.equals("baseline"))
                            attrs = attrs + "vertical-align:baseline;";
                    }
                }
            //Text properties
            if (tagname.equals("font"))
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
                el.setAttribute("XDefaultStyle", HTMLNorm.getAttribute(el, "XDefaultStyle") + ";" + attrs);
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            attributesToStyles(child.item(i), itab);
    }
    
    /**
     * Computes a length defined using an HTML attribute (e.g. width for tables).
     * @param value The attribute value
     * @param whole the value used as 100% when value is a percentage
     * @return the computed length
     */
    public static int computeAttributeLength(String value, int whole) throws NumberFormatException
    {
        String sval = value.trim().toLowerCase();
        if (sval.endsWith("%"))
        {
            double val = Double.parseDouble(sval.substring(0, sval.length() - 1));
            return (int) Math.round(val * whole / 100.0);
        }
        else if (sval.endsWith("px"))
        {
            return (int) Math.rint(Double.parseDouble(sval.substring(0, sval.length() - 2)));
        }
        else
        {
            return (int) Math.rint(Double.parseDouble(sval));
        }
    }
    
    /**
     * Creates a CSS length or percentage from a string.
     * @param spec The string length or percentage according to CSS
     * @return the length or percentage
     */
    public static TermLengthOrPercent createLengthOrPercent(String spec)
    {
        spec = spec.trim();
        if (spec.endsWith("%"))
        {
            try {
                float val = Float.parseFloat(spec.substring(0, spec.length() - 1));
                TermPercent perc = CSSFactory.getTermFactory().createPercent(val);
                return perc;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else
        {
            try {
                float val = Float.parseFloat(spec);
                TermLength len = CSSFactory.getTermFactory().createLength(val, TermLength.Unit.px);
                return len;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
    
    private static String applyBorders(String template, String dir)
    {
        return template.replaceAll("@", dir);
    }
    
    //=======================================================================
    
    /**
     * Provides a cleanup of a HTML DOM tree according to the HTML syntax restrictions.
     * Currently, following actions are implemented:
     * <ul>
     * <li>Table cleanup
     *      <ul>
     *      <li>elements that are not acceptable witin a table are moved before the table</li>
     *      </ul>
     * </li>
     * </ul>
     * @param doc the processed DOM Document.
     */
    public static void normalizeHTMLTree(Document doc)
    {
        //normalize tables
        NodeList tables = doc.getElementsByTagName("table");
        for (int i = 0; i < tables.getLength(); i++)
        {
            Vector<Node> nodes = new Vector<Node>();
            recursiveFindBadNodesInTable(tables.item(i), null, nodes);
            for (Node n : nodes)
            {
                moveSubtreeBefore(n, tables.item(i));
            }
        }
    }

    /**
     * Finds all the nodes in a table that cannot be contained in the table according to the HTML syntax.
     * @param n table root
     * @param cellroot last cell root
     * @param nodes resulting list of nodes
     */
    private static void recursiveFindBadNodesInTable(Node n, Node cellroot, Vector<Node> nodes)
    {
        Node cell = cellroot;
        
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String tag = n.getNodeName();
            if (tag.equalsIgnoreCase("table"))
            {
                if (cell != null) //do not enter nested tables
                    return;
            }
            else if (tag.equalsIgnoreCase("tbody") || 
                     tag.equalsIgnoreCase("thead") || 
                     tag.equalsIgnoreCase("tfoot") ||
                     tag.equalsIgnoreCase("tr") ||
                     tag.equalsIgnoreCase("col") ||
                     tag.equalsIgnoreCase("colgroup"))
            {
            }
            else if (tag.equalsIgnoreCase("td") || tag.equalsIgnoreCase("th") || tag.equalsIgnoreCase("caption"))
            {
                cell = n;
            }
            else //other elements
            {
                if (cell == null)
                {
                    nodes.add(n);
                    return;
                }
            }
        } //other nodes
        else if (n.getNodeType() == Node.TEXT_NODE)
        {
            if (cell == null && n.getNodeValue().trim().length() > 0)
            {
                nodes.add(n);
                return;
            }
        }
        
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveFindBadNodesInTable(child.item(i), cell, nodes);
    }
    
    private static void moveSubtreeBefore(Node root, Node ref)
    {
        ref.getParentNode().insertBefore(root, ref);
    }
    
}
