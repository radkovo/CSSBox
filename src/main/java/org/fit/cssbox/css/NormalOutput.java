/*
 * NormalOutput.java
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
 * Created on 30. leden 2005, 19:02
 */

package org.fit.cssbox.css;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.*;

/**
 * An output generator that outputs the DOM tree in a standard (xml) way
 *
 * @author  radek
 */
public class NormalOutput extends Output
{
    private boolean filterStyles = true;
    
    public NormalOutput(Node root)
    {
        super(root);
    }
    
    public NormalOutput(Node root, boolean filterStyles)
    {
        super(root);
        this.filterStyles = filterStyles;
    }
    
    /**
     * Formats the complete tag tree to an output stream.
     * @param out The output stream to be used for the output.
     */
    public void dumpTo(OutputStream out)
    {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            writer = new PrintWriter(out);
        }
        recursiveDump(root, 0, writer);
        writer.close();
    }
    
    /**
     * Formats the complete tag tree and prints using a writer.
     * @param writer The writer to be used for printing the ouput.
     */
    public void dumpTo(PrintWriter writer)
    {
        recursiveDump(root, 0, writer);
    }
    
    //========================================================================
    
    private void recursiveDump(Node n, int level, PrintWriter p)
    {        
        //Opening tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String tag = "";
            Element el = (Element) n;
            //do not dump original style definitions
            if (filterStyles)
            {
                if (el.getTagName().equals("style")) 
                    return;
                if (el.getTagName().equals("link") &&
                    ("stylesheet".equalsIgnoreCase(el.getAttribute("rel")) || 
                     "text/css".equalsIgnoreCase(el.getAttribute("type"))))
                    return;
            }
            //Replace meta generator
            if (el.getTagName().equals("meta") && "generator".equalsIgnoreCase(el.getAttribute("name")))
                el.setAttribute("content", "CSS Transformer by Radek Burget, burgetr@fit.vutbr.cz");
            //Change encoding to utf-8
            if (el.getTagName().equals("meta") && "content-type".equalsIgnoreCase(el.getAttribute("http-equiv")))
                el.setAttribute("content", "text/html; charset=utf-8");
            //Dump the tag
            tag = tag + "<" + el.getTagName();
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
            //if (n.getNodeName().equals("head"))
            //    p.print("<script type=\"text/javascript\" src=\"visual.js\"></script>");
            p.print("</" + n.getNodeName() + ">");
        }        
    }

    @SuppressWarnings("unused")
	private void recursiveDumpNice(Node n, int level, PrintWriter p)
    {
        
        //Opening tag
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String tag = "";
            Element el = (Element) n;
            if (el.getTagName().equals("style")) 
                return;
            tag = tag + "<" + el.getTagName();
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
    
    private void indent(int level, PrintWriter p)
    {
        String ind = "";
        for (int i = 0; i < level*4; i++) ind = ind + ' ';
        p.print(ind);
    }
    
}
