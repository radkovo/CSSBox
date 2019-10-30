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
    
    
    public Counters()
    {
        super();
        parent = null;
    }
    
    public Counters(Counters parent)
    {
        super();
        this.parent = parent;
    }
    
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
     * Finds the value of the given counter in this scope and the parent scopes.
     * @param key the counter name
     * @return the counter value or 0 when the counter was not set in any scope.
     */
    public int getCounter(String key)
    {
        final Counters scope = getScope(key);
        return (scope == null) ? 0 : scope.get(key);
    }
    
    public void resetCounter(String key, int value)
    {
        put(key, value);
    }
    
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
