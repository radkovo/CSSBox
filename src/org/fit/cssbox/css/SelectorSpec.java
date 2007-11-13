/*
 * SelectorSpec.java
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
 * Created on 23. leden 2005, 9:57
 */

package org.fit.cssbox.css;

import java.util.*;
import org.w3c.dom.*;

/**
 * A selector specification.
 *
 * @author  radek
 */
public class SelectorSpec 
{
    private static final int OP_DESC = 0;   //no operator
    private static final int OP_CHILD = 1;  //operator >
    private static final int OP_SIBL = 2;   //operator +
    
    private Vector<NodeSpec> nodes;
    private Vector<Integer> operators;
    
    private int specificity = 0;
    
    /** Creates a new instance of SelectorSpec */
    public SelectorSpec() 
    {
        nodes = new Vector<NodeSpec>();
        operators = new Vector<Integer>();
    }
    
    /** Creates a copy of a SelectorSpec */
    public SelectorSpec(SelectorSpec src)
    {
        nodes = new Vector<NodeSpec>(src.nodes);
        operators = new Vector<Integer>(src.operators);
    }
    
    /**
     * Checks whether an Element from a DOM tree matches this selector.
     * @param e the element to be matched 
     * @return <code>true</code> if the element matches the selector
     */
    public boolean matches(Element e)
    {
        int i = nodes.size()-1;
        //check the basic node
        if (i >= 0)
        {
            NodeSpec ns = (NodeSpec) nodes.elementAt(i);
            if (!ns.matches(e))
                return false;
            i--;
        }
        //check related nodes
        Element rel = e;
        while (i >= 0)
        {
            NodeSpec ns = (NodeSpec) nodes.elementAt(i);
            int op = ((Integer) operators.elementAt(i)).intValue();
            switch (op)
            {
                case OP_DESC: rel = findAncestor(rel, ns); break;
                case OP_CHILD: rel = findParent(rel, ns); break;
                case OP_SIBL: rel = findSibling(rel, ns); break;
            }
            if (rel == null)
                return false;
            i--;
        }
        return true;
    }
    
    /**
     * Parses a portion of a selector that corresponds to a single element. The parsing starts 
     * at the specified position.
     *  @param field the whole selector string
     *  @param pos starting position in the string for parsing
     *  @return the end position where the parsing stopped
     */
    public int parseSpec(String field, int pos)
    {
        specificity = 0;
        int si = pos;
        while (si < field.length())
        {
            //skip whitespaces before
            while (si < field.length() && Character.isWhitespace(field.charAt(si))) si++;
            //parse a node spec.
            NodeSpec newnode = new NodeSpec();
            si = newnode.parseSpec(field, si);
            nodes.add(newnode);
            specificity += newnode.getSpecificity();
            //skip whitespaces after
            while (si < field.length() && Character.isWhitespace(field.charAt(si))) si++;
            
            //check for operators
            if (si < field.length())
            {
                char ch = field.charAt(si);
                if (ch == '>') { operators.add(new Integer(OP_CHILD)); si++; }
                else if (ch == '+') { operators.add(new Integer(OP_SIBL)); si++; }
                else if (ch == ',') break;
                else operators.add(new Integer(OP_DESC));
            }            
        }
        return si;
    }

    /**
     * @return the specificity of the selector
     */
    public int getSpecificity()
    {
        return specificity;
    }
    
    public String toString()
    {
        String ret = "";
        for (int i = 0; i < nodes.size(); i++)
        {
            ret = ret + ((NodeSpec) nodes.elementAt(i)).toString() + " ";
            if (i < operators.size())
            {
                int op = ((Integer) operators.elementAt(i)).intValue();
                if (op == OP_CHILD) ret = ret + "> ";
                else if (op == OP_SIBL) ret = ret + "+ ";            
            }
        }
        return ret;
    }
    
    //=======================================================================
    
    /**
     * Locates an ancestor of this element that corresponds to the specified
     * conditions.
     */
    private Element findAncestor(Element elem, NodeSpec spec)
    {
        Node n = elem.getParentNode();
        while (n != null)
        {
            if (n.getNodeType() == Node.ELEMENT_NODE && spec.matches((Element) n))
                    return (Element) n;
            n = n.getParentNode();
        }
        return null;
    }

    /**
     * Locates a parent of this element that corresponds to the specified
     * conditions.
     */
    private Element findParent(Element elem, NodeSpec spec)
    {
        Node n = elem.getParentNode();
        while (n != null)
        {
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                if (spec.matches((Element) n))
                    return (Element) n;
                else
                    return null;
            }
            n = n.getParentNode();
        }
        return null;
    }

    /**
     * Locates a preceding sibling of this element that corresponds to the specified
     * conditions.
     */
    private Element findSibling(Element elem, NodeSpec spec)
    {
        Node n = elem.getPreviousSibling();
        while (n != null)
        {
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                if (spec.matches((Element) n))
                    return (Element) n;
                else
                    return null;
            }
            n = n.getPreviousSibling();
        }
        return null;
    }
    
}
