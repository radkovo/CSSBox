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
     * Returns the declared line height.
     * @return The declared line height in pixels.
     */
    public float getLineHeight();
    
    
    /**
     * Returns the maximal declared line height of this box and its children.
     * @return Maximal declared line height in pixels.
     */
    public float getMaxLineHeight();

    /**
     * Obtains the distance from the line box top to the baseline of this box.
     * This corresponds to the total required space above the baseline of this box in the parent line box.
     * @return the <em>y</em> offset of the baseline in the parent line box
     */
    public float getBaselineOffset();
    
	/**
	 * Obtains the required height of the parent line box below the baseline.
	 * @return the total required space below the baseline in pixels
	 */
	public float getBelowBaseline();
	
	/**
	 * Obtains the total line box height obtained for this box and its
	 * subboxes during the layout. It consists of two parts: baselineOffset + belowBaseline.
	 * @return the total required height of the line in pixels
	 */
	public float getTotalLineHeight();
	
	public float getHalfLead();
	
    /**
     * Obtains the expected maximal length of the first line of this inline box.
     * This is useful when the line breaks are preserved. Otherwise, the length of the whole text is returned.
     * @return the length of the first line of the contents in pixels
     */
    public float getFirstLineLength();

    /**
     * Obtains the expected maximal length of the last line of this inline box.
     * This is useful when the line breaks are preserved. Otherwise, the length of the whole text is returned.
     * @return the length of the last line of the contents in pixels
     */
    public float getLastLineLength();
    
    /** 
     * Checks whether the inline box contains a line break. This may be only true when the line breaks are preserved
     * according to the whitespace style.
     * @return <code>true</code> when there is at least one preserved line break inside 
     */
    public boolean containsLineBreak();

    /**
     * Indicates whether the layout of this box content has been finished by a line break. 
     * @return <code>true</code> if the layout of this box has finished by a line break
     */
    public boolean finishedByLineBreak();
    
    /**
     * Indicates whether the box was completely collapsed to empty string during the laytout.
     * For example, it contains only whitespace characters, whitespace collapsing is applied
     * and the previous box already ends with a whitespace.
     * @return <code>true</code> when the box has been completely collapsed
     */
    public boolean collapsedCompletely();
    
    /**
     * Computes the number of expansion points (generally spaces between words) that can be used
     * for extending the box width
     * @param atLineStart is the box at the start of a line?
     * @param atLineEnd is the box at the end of a line line?
     * @return the number of expansion points
     */
    public int getWidthExpansionPoints(boolean atLineStart, boolean atLineEnd);
    
    /**
     * Extends the width of the box by the given offset
     * @param ofs the additional width in pixels (positive or negative)
     * @param atLineStart is the box at the start of a line?
     * @param atLineEnd is the box at the end of a line line?
     */
    public void extendWidth(float ofs, boolean atLineStart, boolean atLineEnd);
    
}
