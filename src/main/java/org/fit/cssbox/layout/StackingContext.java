/*
 * StackingContext.java
 * Copyright (c) 2005-2013 Radek Burget
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
 * Created on 15.2.2013, 14:25:24 by burgetr
 */
package org.fit.cssbox.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 * A stacking context. It gathers the information about child stacking contexts and their z-indices.
 * 
 * @author burgetr
 */
public class StackingContext extends HashMap<Integer, Vector<ElementBox>> 
{
    private static final long serialVersionUID = -2945581861967274084L;

    /** The box that creates this stacking context */
    private ElementBox elementBox;
        
    
    public StackingContext(ElementBox element)
    {
        super();
        this.elementBox = element;
    }
    
    public ElementBox getElementBox()
    {
        return elementBox;
    }
    
    public Integer[] getZIndices()
    {
        Set<Integer> zindices = keySet();
        Integer[] clevels = zindices.toArray(new Integer[0]); 
        Arrays.sort(clevels);
        return clevels;
    }
    
    public Vector<ElementBox> getElementsForZIndex(int zindex)
    {
        return get(zindex);
    }
    
    public void registerChildContext(ElementBox element)
    {
        int zindex = element.hasZIndex() ? element.getZIndex() : 0; //put 'auto' z-indices into level 0
        Vector<ElementBox> list = get(zindex);
        if (list == null)
        {
            list = new Vector<ElementBox>();
            put(zindex, list);
        }
        list.add(element);
    }
    
}
