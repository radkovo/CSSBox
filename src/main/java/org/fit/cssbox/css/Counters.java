/*
 * Counters.java
 * Copyright (c) 2005-2019 Radek Burget
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
 * Created on 30. 10. 2019, 19:43:49 by burgetr
 */
package org.fit.cssbox.css;

import java.util.HashMap;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;

/**
 * An array of CSS counters.
 * 
 * @author burgetr
 */
public class Counters extends HashMap<String, Integer>
{
    private static final long serialVersionUID = 1L;
    
    private Counters parent;
    
    
    /**
     * Creates the root counter scope.
     */
    public Counters()
    {
        super();
        parent = null;
    }
    
    /**
     * Creates a new counter scope based on a parent scope.
     * @param parent
     */
    public Counters(Counters parent)
    {
        super();
        this.parent = parent;
    }
    
    /**
     * Finds the nearest ancestor-or-self scope that defines certain counter.
     * @param key the counter name
     * @return the counter scope or {@code null} when the counter is not found in any scope
     */
    public Counters getScope(String key)
    {
        if (containsKey(key))
        {
            return this;
        }
        else
        {
            if (parent != null)
                return parent.getScope(key);
            else
                return null;
        }
        
    }
    
    /**
     * Finds the value of the given counter in this scope and the ancestor scopes.
     * @param key the counter name
     * @return the counter value or 0 when the counter was not set in any scope.
     */
    public int getCounter(String key)
    {
        final Counters scope = getScope(key);
        return (scope == null) ? 0 : scope.get(key);
    }
    
    /**
     * Creates and resets the counter in this scope.
     * @param key the counter name
     * @param value the initial value of the counter
     */
    public void resetCounter(String key, int value)
    {
        put(key, value);
    }
    
    /**
     * Finds the counter given counter scope and increments the counter by the given value.
     * When the counter is not found in any scope, a new one with 0 value is created in this scope.
     * @param key the counter name
     * @param value the increment value
     */
    public void incrementCounter(String key, int value)
    {
        // If 'counter-increment' or 'content' on an element or pseudo-element refers to a counter that is not
        // in the scope of any 'counter-reset', implementations should behave as though a 'counter-reset' had
        // reset the counter to 0 on that element or pseudo-element. 
        Counters scope = getScope(key);
        if (scope == null)
        {
            resetCounter(key, 0);
            scope = this;
        }
        scope.put(key, scope.get(key) + value);
    }
    
    /**
     * Updates the counters according to the style properties. Applies the {@code counter-reset}
     * and {@code counter-increment} properties according to the CSS specification.
     * @param style an element style
     */
    public void applyStyle(NodeData style)
    {
        CSSProperty.CounterReset cr = style.getProperty("counter-reset");
        if (cr == CSSProperty.CounterReset.list_values)
        {
            TermList terms = style.getValue(TermList.class, "counter-reset");
            for (Term<?> term : terms)
            {
                if (term instanceof TermPair<?, ?>)
                {
                    TermPair<?, ?> pair = (TermPair<?, ?>) term;
                    resetCounter((String) pair.getKey(), (Integer) pair.getValue());
                }
            }
        }
        
        CSSProperty.CounterIncrement ci = style.getProperty("counter-increment");
        if (ci == CSSProperty.CounterIncrement.list_values)
        {
            TermList terms = style.getValue(TermList.class, "counter-increment");
            for (Term<?> term : terms)
            {
                if (term instanceof TermPair<?, ?>)
                {
                    TermPair<?, ?> pair = (TermPair<?, ?>) term;
                    incrementCounter((String) pair.getKey(), (Integer) pair.getValue());
                }
            }
        }
    }
    
}
