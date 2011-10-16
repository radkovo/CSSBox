/*
 * InlineBlockBox.java
 * Copyright (c) 2005-2011 Radek Burget
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
 * Created on 16.10.2011, 22:51:58 by radek
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

import org.w3c.dom.Element;

/**
 * A box corresponding to an inline-block element.
 * 
 * @author radek
 */
public class InlineBlockBox extends BlockBox implements Inline
{

	public InlineBlockBox(Element n, Graphics2D g, VisualContext ctx)
	{
		super(n, g, ctx);
		isblock = false;
	}

	public InlineBlockBox(InlineBox src)
	{
		super(src);
		isblock = false;
	}

	//========================================================================
	
	public int getMaxLineHeight()
	{
		return 0;
	}

	public int getBaselineOffset()
	{
		return 0;
	}

	public int getBelowBaseline()
	{
		return 0;
	}

	public int getTotalLineHeight()
	{
		return 0;
	}

	public int getHalfLead()
	{
		return 0;
	}

}
