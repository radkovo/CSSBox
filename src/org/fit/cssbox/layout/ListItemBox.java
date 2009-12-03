/**
 * ListItemBox.java
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
 * Created on 26.9.2006, 21:25:38 by radek
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

import org.w3c.dom.Element;
import cz.vutbr.web.css.*;

/**
 * This class represents a list-item box. This box behaves the same way
 * as a block box with some modifications.
 * @author radek
 */
public class ListItemBox extends BlockBox
{

	/**
	 * Create a new list item
	 */
	public ListItemBox(Element n, Graphics2D g, VisualContext ctx)
	{
		super(n, g, ctx);
		isblock = true;
	}

	/**
	 * Create a new list item from an inline box
	 */
	public ListItemBox(InlineBox src)
	{
		super(src);
		isblock = true;
	}

	
    @Override
	public void draw(Graphics2D g, int turn, int mode)
    {
    	super.draw(g, turn, mode);
    	if (displayed && isVisible())
    	{
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_FG) drawBullet(g);
            }
    	}
    }
    
    /**
     * Draw a bullet
     */
    private void drawBullet(Graphics2D g)
    {
    	int x = (int) Math.round(getContentX() - 1.2 * ctx.getEm());
    	int y = (int) Math.round(getContentY() + 0.4 * ctx.getEm());
    	int r = (int) Math.round(0.6 * ctx.getEm());
    	CSSProperty.ListStyleType type = style.getProperty("list-style-type");
    	if (type == CSSProperty.ListStyleType.CIRCLE) 
    		g.drawOval(x, y, r, r);
    	else if (type == CSSProperty.ListStyleType.SQUARE) 
    		g.fillRect(x, y, r, r);
    	//else if (type == CSSProperty.ListStyleType.BOX) //not documented, recognized by Konqueror 
    	//	g.drawRect(x, y, r, r);
    	else if (type != CSSProperty.ListStyleType.NONE) //use 'disc'
    		g.fillOval(x, y, r, r);
    }
    
}
