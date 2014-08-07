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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import org.w3c.dom.Element;

/**
 * Replaced block box. 
 * @author radek
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
		isempty = (obj == null);
		if (!isempty)
		    obj.setOwner(this);
	}
    
    @Override
    public int getMaximalWidth()
    {
        return boxw + declMargin.left + padding.left + border.left +
                declMargin.right + padding.right + border.right;
    }

    @Override
    public int getMinimalWidth()
    {
        return boxw + declMargin.left + padding.left + border.left +
                declMargin.right + padding.right + border.right;
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
        Rectangle objsize = CSSDecoder.computeReplacedObjectSize(obj, this);
        content.width = boxw = objsize.width;
        content.height = boxh = objsize.height;
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
    protected boolean separatedFromTop(ElementBox box)
    {
        return true;
    }

    @Override
    protected boolean separatedFromBottom(ElementBox box)
    {
        return true;
    }

    public void drawContent(Graphics2D g)
    {
        if (obj != null)
        {
            Shape oldclip = g.getClip();
            g.setClip(applyClip(oldclip, getClippedContentBounds()));
            obj.draw(g, boxw, boxh);
            g.setClip(oldclip);
        }
    }
    
    @Override
	public void draw(DrawStage turn)
    {
        if (isDisplayed() && isVisible())
        {
            if (!this.formsStackingContext())
            {
                switch (turn)
                {
                    case DRAW_NONINLINE:
                        if (floating == FLOAT_NONE)
                        {
                            getViewport().getRenderer().renderElementBackground(this);
                        }
                        break;
                    case DRAW_FLOAT:
                        if (floating != FLOAT_NONE)
                        {
                            getViewport().getRenderer().renderElementBackground(this);
                            getViewport().getRenderer().startElementContents(this);
                            getViewport().getRenderer().renderReplacedContent(this);
                            getViewport().getRenderer().finishElementContents(this);
                        }
                        break;
                    case DRAW_INLINE:
                        if (floating == FLOAT_NONE)
                        {
                            getViewport().getRenderer().startElementContents(this);
                            getViewport().getRenderer().renderReplacedContent(this);
                            getViewport().getRenderer().finishElementContents(this);
                        }
                }
            }
        }
    }

    @Override
    public void drawStackingContext(boolean include)
    {
        if (isDisplayed() && isDeclaredVisible())
        {
            //1.the background and borders of the element forming the stacking context.
            if (this.isBlock())
                getViewport().getRenderer().renderElementBackground(this);

            getViewport().getRenderer().startElementContents(this);
            //2.the child stacking contexts with negative stack levels (most negative first).
            //3.the in-flow, non-inline-level, non-positioned descendants.
            //4.the non-positioned floats. 
            //5.the in-flow, inline-level, non-positioned descendants, including inline tables and inline blocks. 
            getViewport().getRenderer().renderReplacedContent(this);
            //6.the child stacking contexts with stack level 0 and the positioned descendants with stack level 0.
            //7.the child stacking contexts with positive stack levels (least positive first).
            getViewport().getRenderer().finishElementContents(this);
        }
    }
}
