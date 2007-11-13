/*
 * InlineSelectorSpec.java
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
 * Created on 28. leden 2005, 9:24
 */

package org.fit.cssbox.css;

/**
 * This is an 'inline selector'. It is empty but it has the maximal
 * specificity.
 * @author  burgetr
 */
public class InlineSelectorSpec extends SelectorSpec
{
    
    public InlineSelectorSpec() 
    {
        super();
    }
    
    public int getSpecificity() 
    {
        return Integer.MAX_VALUE;
    }    
    
    
}
