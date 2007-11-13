/*
 * GenericTagOutput.java
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
 * Created on 30. leden 2005, 19:15
 */

package org.fit.cssbox.css;

import org.w3c.dom.*;

/**
 *
 * @author  radek
 */
public class GenericTagOutput extends Output
{
    
    public GenericTagOutput(Document doc)
    {
        super(doc);
    }
    
    /**
     * Formats the complete tag tree to an output stream
     */
    public void dumpTo(java.io.PrintStream out)
    {
        recursiveDump(root, 0, out);
    }
    
    //========================================================================
    
    private void recursiveDump(Node n, int level, java.io.PrintStream p)
    {        
        //Opening tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String tag = "";
            Element el = (Element) n;
            //do not dump original style definitions
            if (el.getTagName().equals("style")) 
                return;
            if (el.getTagName().equals("link") &&
                (el.getAttribute("rel").equalsIgnoreCase("stylesheet") || 
                 el.getAttribute("type").equalsIgnoreCase("text/css")))
                return;
            //Replace meta generator
            if (el.getTagName().equals("meta") && el.getAttribute("name").equals("generator"))
                el.setAttribute("content", "CSS Transformer by Radek Burget, burgetr@fit.vutbr.cz");
            //Dump the tag
            tag = tag + "<" + genericTag(el.getTagName());
            NamedNodeMap attrs = el.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++)
            {
                Node attr = attrs.item(i);
                tag = tag + " " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"";
            }
            tag = tag + ">";
            p.print(tag);
        }
        else if (n.getNodeType() == Node.TEXT_NODE)
        {
            p.print(n.getNodeValue());
        }
                                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveDump(child.item(i), level+1, p);   

        //Closing tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            p.print("</" + n.getNodeName() + ">");
        }        
    }

    @SuppressWarnings("unused")
	private void recursiveDumpNice(Node n, int level, java.io.PrintStream p)
    {
        
        //Opening tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String tag = "";
            Element el = (Element) n;
            if (el.getTagName().equals("style")) 
                return;
            tag = tag + "<" + genericTag(el.getTagName());
            NamedNodeMap attrs = el.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++)
            {
                Node attr = attrs.item(i);
                tag = tag + " " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"";
            }
            tag = tag + ">";
            indent(level, p);
            p.println(tag);
        }
        else if (n.getNodeType() == Node.TEXT_NODE)
        {
            indent(level, p);
            p.println(n.getNodeValue());
        }
                                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveDumpNice(child.item(i), level+1, p);   

        //Closing tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            indent(level, p);
            p.println("</" + n.getNodeName() + ">");
        }        
    }
    
    private void indent(int level, java.io.PrintStream p)
    {
        String ind = "";
        for (int i = 0; i < level*4; i++) ind = ind + ' ';
        p.print(ind);
    }
    
    private String genericTag(String s)
    {
        s = s.toLowerCase();

        if (s.equals("tt") || s.equals("i") || s.equals("b") ||
            s.equals("u") || s.equals("s") || s.equals("strike") ||
            s.equals("big") || s.equals("small") || s.equals("em") ||
            s.equals("strong") || s.equals("dfn") || s.equals("code") ||
            s.equals("samp") || s.equals("kbd") || s.equals("var") ||
            s.equals("cite") || s.equals("abbr") || s.equals("acronym"))
            return "span";

        if (s.equals("address") || s.equals("blockquote") || s.equals("h1") ||
            s.equals("h2") || s.equals("h3") || s.equals("h4") ||
            s.equals("h5") || s.equals("h6") || s.equals("p") ||
            s.equals("center") || s.equals("dir") || s.equals("pre"))
            return "div";

        return s;
    }
    
}
