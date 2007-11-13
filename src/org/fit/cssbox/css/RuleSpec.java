/*
 * RuleSpec.java
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
 * Created on 24. leden 2005, 18:58
 */

package org.fit.cssbox.css;

import org.w3c.dom.css.*;

/**
 * A CSS rule specification with a parsed selector.
 *
 * @author  radek
 */
public class RuleSpec implements Comparable<RuleSpec>
{
    private SelectorSpec selector;
    private CSSStyleDeclaration decl;
    private int order;
    
    /** 
     * Creates a new instance of RuleSpec with a separate selector and declaration
     * @param selector Rule selector specification
     * @param decl Style declaration
     * @param order the index of the rule in the style sheet 
     */
    public RuleSpec(SelectorSpec selector, CSSStyleDeclaration decl, int order)
    {
        this.selector = selector;
        this.decl = decl;
        this.order = order;
    }
    
    /**
     * Creates a copy of a RuleSpec
     */
    public RuleSpec(RuleSpec src)
    {
        selector = new SelectorSpec(src.selector);
        decl = src.decl;
        order = src.order;
    }
    
    /** 
     * Creates a new rule that corresponds to the inline style declaration
     * (theselector is empty)
     * @param decl the style declaration 
     */
    public RuleSpec(CSSStyleDeclaration decl)
    {
        this.selector = new InlineSelectorSpec();
        this.decl = decl;
        this.order = Integer.MAX_VALUE;
    }
    
    /**
     * @return the rule selector
     */
    public SelectorSpec getSelector()
    {
        return selector;
    }
    
    /**
     * @return the rule property declaration
     */
    public CSSStyleDeclaration getStyle()
    {
        return decl;
    }
    
    /**
     * @return the rule property declaration string
     */
    public String getDeclarationString()
    {
        return decl.getCssText();
    }
    
    /**
     * Compares this object with the specified object for order. 
     * @return a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object.
     */
    public int compareTo(RuleSpec other) 
    {
        if (selector.getSpecificity() < other.getSelector().getSpecificity())
            return -1;
        else if (selector.getSpecificity() > other.getSelector().getSpecificity())
            return 1;
        else if (order < other.order)
            return -1;
        else if (order > other.order)
            return 1;
        else
            return 0;
    }    
    
    public String toString()
    {
        return selector.toString() + " { " + decl.getCssText() + " }" + " [" + order + "]";
    }
}
