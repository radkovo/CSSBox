/*
 * NodeSpec.java
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
 * Created on 23. leden 2005, 9:15
 */

package org.fit.cssbox.css;

import java.util.*;
import org.w3c.dom.*;

/**
 * A node specification used in a selector. The simple selectors contain a single node
 * specification, structured selectors contain multiple node specifications in some relation.  
 * 
 * @author  radek
 */
public class NodeSpec 
{
    //parsing states
    private static final int SNAME = 0;     //store characters to element name
    private static final int SANAME = 1;    //store characters to attribute name
    private static final int SAVALUE = 2;   //store characters to attribute value
    private static final int SVOID = 4;     //waiting

    private String name;
    private Vector<AttributeSpec> attrSpecs;
    
    //priority counters
    private int prio_b = 0; //number of ID attributes
    private int prio_c = 0; //number of other attributes
    
    public NodeSpec()
    {
        name = "";
        attrSpecs = new Vector<AttributeSpec>();
    }
    
    public NodeSpec(String name, Vector<AttributeSpec> attrSpecs) 
    {
        this.name = new String(name);
        this.attrSpecs = new Vector<AttributeSpec>(attrSpecs);
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void addAttrSpec(AttributeSpec spec)
    {
        attrSpecs.add(spec);
    }
    
    /**
     * Returns true if the specified element matches this specification.
     */
    public boolean matches(Element q)
    {
        boolean match = true;
        if (!name.equals("*") && !name.equals(q.getNodeName()))
        {
            match = false; 
        }
        else
        {
            for (int i = 0; i < attrSpecs.size(); i++)
            {
                AttributeSpec attrSpec = (AttributeSpec) attrSpecs.elementAt(i);
                if (!attributeMatch(attrSpec, q))
                {
                    match = false;
                    break;
                }
            }
        }
        return match;
    }
    
    /**
     * Checks if the required attribute of the selector matches to a corresponding one
     * specified for the DOM element
     * @param attrSpec the attribute specification from the selector
     * @param  elem the DOM element
     * @return <code>true</code> if the attribute exists and it has the required value
     */
    private boolean attributeMatch(AttributeSpec attrSpec, Element elem)
    {
        //special system attributes (pseudoclasses)
        if (attrSpec.getName().equals("__pseudoclass"))
        {
            if (elem.getNodeName().equals("a"))
                return attrSpec.getValue().equals("link"); //when pseudoclass is specified, only the "link" state is used
            else
                return false;
        }
        //normal attributes
        if (elem.getAttributes().getNamedItem(attrSpec.getName()) == null || //the required attribute doesn't exist
            !attrSpec.matches(elem.getAttribute(attrSpec.getName()))) //or the value doesn't match
            return false;
        else
            return true;
    }
    
    /**
     * Parses a selector field. 
     */
    public int parseSpec(String field, int pos)
    {
        String name = "";
        String aname = "";
        String faname = "";  //future attr name
        String avalue = "";
        int dest = SNAME;
        boolean save = false; //save previous attribute
        boolean empty = true; //attribute name is already empty
        int comp = 0;         //comparation style
        int fcomp = 0;
        boolean inquot = false; //in quotes
        char quote = '"';

        int si = 0;
        for (si = pos; si < field.length(); si++)
        {
            char ch = field.charAt(si);
            if (ch == '#')
            {
                faname = "id"; dest = SAVALUE; save = true;
                fcomp = AttributeSpec.COMP_EXACT;
                prio_b++;
            }
            else if (ch == '.')
            {
                faname = "class"; dest = SAVALUE; save = true;
                fcomp = AttributeSpec.COMP_SPACE;
                prio_c++;
            }
            else if (ch == ':')
            {
                faname = "__pseudoclass"; dest = SAVALUE; save = true;
                fcomp = AttributeSpec.COMP_EXACT;
                prio_c++;
            }
            else if (ch == '[')
            {
                faname = ""; dest = SANAME; save = true;
                fcomp = AttributeSpec.COMP_ANY;
                prio_c++;
            }
            else if (ch == '=')
            {
                dest = SVOID;
                comp = AttributeSpec.COMP_EXACT;
            }
            else if (ch == '~' && si+1 < field.length() && field.charAt(si+1) == '=')
            {
                dest = SVOID;
                comp = AttributeSpec.COMP_SPACE;
                si++;
            }
            else if (ch == '|' && si+1 < field.length() && field.charAt(si+1) == '=')
            {
                dest = SVOID;
                comp = AttributeSpec.COMP_HYPHEN;
                si++;
            }
            else if ((ch == '"' || ch == '\'') && dest == SVOID)
            {
                dest = SAVALUE;
                quote = ch;
                inquot = true;
            }
            else if (ch == quote && dest == SAVALUE)
            {
                dest = SVOID;
                inquot = false;
            }
            else if (ch == ']')
            {
            }
            else if (!inquot && 
                     (ch == '>' || ch == '+' || ch == ',' || Character.isWhitespace(ch)))
            {
                break; //end of this part
            }
            else
            {
                if (save)
                {
                    if (!empty)
                        addAttrSpec(new AttributeSpec(aname, avalue, comp));
                    aname = faname; faname = ""; avalue = "";
                    comp = fcomp; fcomp = -1;
                    save = false; empty = (aname.length() == 0);
                }
                switch (dest)
                {
                    case 0: name = name + ch; break;
                    case 1: aname = aname + ch; empty = false; break;
                    case 2: avalue = avalue + ch; empty = false; break;
                }
            }
        }
        setName(name.trim());
        if (!empty)
            addAttrSpec(new AttributeSpec(aname, avalue, comp));
        return si;
    }
    
    public int getSpecificity()
    {
        int prio_d = name.equals("*")?0:1;
        return prio_b*100 + prio_c*10 + prio_d;
    }
    
    public String toString()
    {
        String ret = name;
        for (int i = 0; i < attrSpecs.size(); i++)
            ret = ret + "[" + ((AttributeSpec) attrSpecs.elementAt(i)).toString() + "]";
        return ret;
    }
}
