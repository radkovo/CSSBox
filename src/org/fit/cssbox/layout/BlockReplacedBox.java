/*
 * BlockReplacedBox.java
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
 * Created on 27.9.2006, 22:08:12 by radek
 */
package org.fit.cssbox.layout;

import java.awt.*;

import org.w3c.dom.*;
import cz.vutbr.web.css.*;

/**
 * @author radek
 *
 */
public class BlockReplacedBox extends BlockBox implements ReplacedBox
{
    protected int boxw; //image width attribute
    protected int boxh; //image height attribute
    protected ReplacedContent obj; //the contained object
    
    /** 
     * Creates a new instance of BlockReplacedBox 
     */
    public BlockReplacedBox(Element el, Graphics2D g, VisualContext ctx)
    {
        super(el, g, ctx);
    }

    /** 
     * Creates a new instance of from an inline variant 
     */
    public BlockReplacedBox(InlineReplacedBox src)
    {
        super(src);
        this.boxw = src.boxw;
        this.boxh = src.boxh;
        setContentObj(src.obj);
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
        return boxw + margin.left + padding.left + border.left +
                marginRightDecl + padding.right + border.right;
    }

    @Override
    public int getMinimalWidth()
    {
        return boxw + margin.left + padding.left + border.left +
                marginRightDecl + padding.right + border.right;
    }

    @Override
    public Rectangle getMinimalAbsoluteBounds()
    {
        return new Rectangle(getAbsoluteContentX(), getAbsoluteContentY(), boxw, boxh);
    }
    
    @Override
    public boolean isWhitespace()
    {
        return false;
    }

    @Override
    public boolean isReplaced()
    {
        return true;
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
    protected void loadSizes(boolean update)
    {
        super.loadSizes(update);
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

        TermPercent whole = CSSFactory.getTermFactory().createPercent(100.0f);
        try {
            if (!el.getAttribute("width").equals(""))
                boxw = Integer.parseInt(el.getAttribute("width"));
            else //try to get from style
            {
                CSSProperty.Width width = style.getProperty("width");
                CSSDecoder dec = new CSSDecoder(ctx);
                boxw = dec.getLength(getLengthValue("width"), width == CSSProperty.Width.AUTO, whole, whole, boxw);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid width value: " + el.getAttribute("width"));
        }
        try {
            if (!el.getAttribute("height").equals(""))
                boxh = Integer.parseInt(el.getAttribute("height"));
            else //try to get from style
            {
                CSSProperty.Height height = style.getProperty("height");
                CSSDecoder dec = new CSSDecoder(ctx);
                boxh = dec.getLength(getLengthValue("height"), height == CSSProperty.Height.AUTO, whole, whole, boxh);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid height value: " + el.getAttribute("height"));
        }
        content.width = boxw;
        content.height = boxh;
        bounds.setSize(totalWidth(), totalHeight());
        preferredWidth = getWidth();
        wset = true;
        hset = true;
    }

	@Override
	public boolean hasFixedHeight()
	{
		return true;
	}

	@Override
	public boolean hasFixedWidth()
	{
		return true;
	}

	@Override
	public void draw(Graphics2D g, int turn, int mode)
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
