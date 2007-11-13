/**
 * StyleSheet.java
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
 * Created on 2.8.2006, 12:08:08 by burgetr
 */
package org.fit.cssbox.css;

import java.util.*;

import org.w3c.dom.css.*;

/**
 * A style sheet with the rules oredered by priority.
 * 
 * @author burgetr
 */
public class StyleSheet
{
    /** Vector of the rules represented as RuleSpec objects */
    private Vector<RuleSpec> rules;
    
    /**
     * Creates a new StyleSheet from the CSSStyleSheet. Parses the selectors and creates
     * the approproate rules
     * @param sheet The source style sheet
     */
	public StyleSheet(CSSStyleSheet sheet)
    {
        rules = new Vector<RuleSpec>();
        CSSRuleList cssrules = sheet.getCssRules();
        for (int ri = 0; ri < cssrules.getLength(); ri++)
        {
            if (cssrules.item(ri).getType() == CSSRule.STYLE_RULE)
            {
                CSSStyleRule rule = (CSSStyleRule) cssrules.item(ri);
                Vector<SelectorSpec> specs = getSelectorSpecs(rule.getSelectorText());
                //create a rule for each selector
                for (int i = 0; i < specs.size(); i++)
                {
                    SelectorSpec spec = specs.elementAt(i);
                    rules.add(new RuleSpec(spec, rule.getStyle(), rules.size()));
                }
            }
        }
        Collections.sort(rules);
    }

    /**
     * @return the number of rules in the style sheet
     */
    public int size()
    {
        return rules.size();
    }
    
    /**
     * @param index the rule index
     * @return the particular style rule
     */
    public RuleSpec getRule(int index)
    {
        return (RuleSpec) rules.elementAt(index);
    }
    
    
    //=====================================================================================
    
    /**
     * Parse the selector and create the selector specifiactions. When there are multiple
     * selectors sepearated by comma, more than one selector specification is returned.
     * @return A vector of SelectorSpec objects 
     */
    private Vector<SelectorSpec> getSelectorSpecs(String field)
    {
        Vector<SelectorSpec> ret = new Vector<SelectorSpec>();
        int si = 0;
        while (si < field.length())
        {
            //skip whitespaces before
            while (si < field.length() && Character.isWhitespace(field.charAt(si))) si++;
            //parse a node spec.
            SelectorSpec newspec = new SelectorSpec();
            si = newspec.parseSpec(field, si);
            ret.add(newspec);
            //skip whitespaces after
            while (si < field.length() && Character.isWhitespace(field.charAt(si))) si++;
            
            //check for operators
            if (si < field.length())
            {
                char ch = field.charAt(si);
                if (ch == ',')
                    si++;
                else
                    break;
            }            
        }
        return ret;
    }

}
