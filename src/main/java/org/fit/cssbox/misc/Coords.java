/*
 * Coords.java
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
 * Created on 6. 11. 2019, 18:02:22 by burgetr
 */
package org.fit.cssbox.misc;

/**
 * Coordinates-related functions.
 * 
 * @author burgetr
 */
public class Coords
{
    /** A difference threshold to consider two coordinates to be equal */ 
    public static final float COORDS_THRESHOLD = 0.0001f; 

    /**
     * Comapres two coordinate values and returns true when they should be considered equal.
     * @param v1
     * @param v2
     * @return
     */
    public static boolean eq(float v1, float v2)
    {
        return (Math.abs(v1 - v2) < COORDS_THRESHOLD);
    }
    
}
