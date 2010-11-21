/*
 * Inline.java
 * Copyright (c) 2005-2010 Radek Burget
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
 * Created on 21.11.2010, 19:07:41 by radek
 */
package org.fit.cssbox.layout;

/**
 * This interface defines common methods of objects that are treated
 * in inline context - inline elements and text boxes
 * @author radek
 */
public interface Inline
{

    /**
     * Obtains the distance from the whole content top to the baseline of this box.
     * This corresponds to the total required space above the baseline.
     * @return the <em>y</em> offset of the baseline in this box
     */
    public int getBaselineOffset();
    
	/**
	 * Obtains the height of the box below the baseline.
	 * @return the total required space below the baseline
	 */
	public int getBelowBaseline();
	
	/**
	 * Obtains the total line height obtained for this box and its
	 * subboxes during the layout. It consists of two parts:
	 * baselineOffset + belowBaseline.
	 * @return the total required height of the line in pixels
	 */
	public int getTotalLineHeight();
	
}
