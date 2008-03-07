/**
 * InlineReplacedBox.java
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
 * Created on 27.9.2006, 21:08:14 by radek
 */
package org.fit.cssbox.layout;

import java.awt.*;

import org.w3c.dom.*;


/**
 * @author radek
 *
 */
public class InlineReplacedBox extends InlineBox
{
    protected int boxw; //image width attribute
    protected int boxh; //image height attribute
    protected ReplacedContent obj; //the contained object
    
    /** 
     * Creates a new instance of ImgBox 
     */
    public InlineReplacedBox(Element el, Graphics g, VisualContext ctx)
    {
        super(el, g, ctx);
        lineHeight = boxh;
    }
    
    /**
	 * @return the content object
	 */
	public ReplacedContent getContentObj()
	{
		return obj;
	}

	/**
	 * @param obj the obj to set
	 */
	public void setContentObj(ReplacedContent obj)
	{
		this.obj = obj;
		obj.setOwner(this);
		isempty = (obj == null);
	}

    @Override
    public int getMaximalWidth()
    {
        return boxw;
    }

    @Override
    public int getMinimalWidth()
    {
        return boxw;
    }
    
    @Override
    public Rectangle getMinimalBounds()
    {
        return new Rectangle(getContentX(), getContentY(), boxw, boxh);
    }

    @Override
    public int getMaxLineHeight()
    {
        return boxh;
    }

@Override
    public boolean isWhitespace()
    {
        return false;
    }

    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart) 
    {
        //Skip if not displayed
        if (!displayed)
        {
            content.setSize(0, 0);
            bounds.setSize(0, 0);
            return true;
        }

        setAvailableWidth(availw);
        int wlimit = getAvailableContentWidth();
        if (getWidth() <= wlimit)
            return true;
        else
            return force;
    }

	@Override
    protected void loadSizes()
    {
        super.loadSizes();
        //TODO: Incorporate the ratio according to CSS specs. 10.3.2
        if (obj != null)
        {
            boxw = obj.getIntrinsicWidth();
            boxh = obj.getIntrinsicHeight();
        }
        else
        {
            boxw = 20; //some reasonable default values
            boxh = 20;
        }
        try {
            if (!el.getAttribute("width").equals(""))
                boxw = Integer.parseInt(el.getAttribute("width"));
            else //try to get from style
            {
                CSSDecoder dec = new CSSDecoder(ctx);
                boxw = dec.getLength(getStyleProperty("width"), "100%", "100%", boxw);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid width value: " + el.getAttribute("width"));
        }
        try {
            if (!el.getAttribute("height").equals(""))
                boxh = Integer.parseInt(el.getAttribute("height"));
            else //try to get from style
            {
                CSSDecoder dec = new CSSDecoder(ctx);
                boxh = dec.getLength(getStyleProperty("height"), "100%", "100%", boxh);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid height value: " + el.getAttribute("height"));
        }
        content.width = boxw;
        content.height = boxh;
        bounds.setSize(totalWidth(), totalHeight());
    }
    
    public void draw(Graphics g, int turn, int mode)
    {
        ctx.updateGraphics(g);
        if (displayed && isVisible())
        {
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
            }
            
            if (obj != null) obj.draw(g, boxw, boxh);
        }
    }

}
