/*
 * Output.java
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
 * Created on 30. leden 2005, 18:59
 */

package org.fit.cssbox.css;

import org.w3c.dom.Node;

/**
 * Abstract class that represents any document output implementation.
 *
 * @author  radek
 */
public abstract class Output 
{
    protected Node root;
    
    public Output(Node root)
    {
        this.root = root;
    }
    
    public abstract void dumpTo(java.io.OutputStream out);
    
    public abstract void dumpTo(java.io.PrintWriter writer);
    
}
