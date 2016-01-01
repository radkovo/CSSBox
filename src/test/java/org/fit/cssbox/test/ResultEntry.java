/*
 * ResultEntry.java
 * Copyright (c) 2005-2016 Radek Burget
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
 * Created on 1. 1. 2016, 14:29:45 by burgetr
 */
package org.fit.cssbox.test;

/**
 *
 * @author burgetr
 */
public class ResultEntry
{
    public String name;
    public float result;
    
    public ResultEntry()
    {
    }
    
    public ResultEntry(String name, float result)
    {
        this.name = name;
        this.result = result;
    }
    
}
