/*
 * AttributeSpec.java
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
 * Created on 22. leden 2005, 20:01
 */

package org.fit.cssbox.css;

/**
 * A specification of a required value of an attribute according to a selector specification.
 * It specifies the attribute name, its required value and the way the required value is
 * compared with the actual value.
 *
 * @author  radek
 */
public class AttributeSpec 
{
	/** any value matches */
	public static final int COMP_ANY = 0; 
	/** exact match required */
	public static final int COMP_EXACT = 1;
	/** any space-separated word match required */
    public static final int COMP_SPACE = 2; 
    /** any hyphen-separated word match required */
    public static final int COMP_HYPHEN = 3; 
    
    /** attribute name */
    private String name;
    /** required value */
    private String value;
    /** comparison style - one of th COMP_XXX values */
    private int comp;
    
    /** 
     * Creates a new attribute specification
     * @param name the attribute name
     * @param value the required value
     * @param comp the comparison style 
     */
    public AttributeSpec(String name, String value, int comp) 
    {
        this.name = new String(name);
        this.value = new String(value);
        this.comp = comp;
    }
    
    /**
     * @return the attribute name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the required value
     */
    public String getValue()
    {
        return value;
    }
    
    /**
     * Checks wheter the given value matches the specification
     * @param s the value to be tested
     * @return <code>true</code> if the string matches the specification, <code>false</code> otherwise 
     */
    public boolean matches(String s)
    {
        //comapre the remaining ones
        if (comp == COMP_ANY)
            return true;
        else if (comp == COMP_EXACT)
            return s.equals(value);
        else
        {
            char sep = ' ';
            if (comp == COMP_HYPHEN) sep = '-';
            int pos = s.indexOf(value);
            if (pos != -1)
                return ((pos == 0 || s.charAt(pos-1) == sep) &&
                        (pos + value.length() == s.length() || s.charAt(pos+value.length()) == sep));
            else
                return false;
        }
    }
    
    
    public String toString()
    {
        String op = "?";
        if (comp == COMP_ANY) op = "*";
        else if (comp == COMP_EXACT) op = "=";
        else if (comp == COMP_SPACE) op = "~=";
        else if (comp == COMP_HYPHEN) op = "|=";
        return name+" "+op+" "+value;
    }
    
}
