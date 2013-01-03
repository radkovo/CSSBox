/*
 * InlineBlockReplacedBox.java
 * Copyright (c) 2005-2012 Radek Burget
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
 * Created on 26.11.2012, 23:25:09 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import org.fit.cssbox.css.HTMLNorm;
import org.w3c.dom.Element;

import cz.vutbr.web.css.CSSProperty;

/**
 *
 * @author burgetr
 */
public class InlineBlockReplacedBox extends InlineBlockBox implements ReplacedBox
{
    protected int boxw; //image width attribute
    protected int boxh; //image height attribute
    protected ReplacedContent obj; //the contained object
    
    /** 
     * Creates a new instance of BlockReplacedBox 
     */
    public InlineBlockReplacedBox(Element el, Graphics2D g, VisualContext ctx)
    {
        super(el, g, ctx);
    }

    /** 
     * Creates a new instance of from an inline variant 
     */
    public InlineBlockReplacedBox(InlineReplacedBox src)
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
    public boolean canSplitAfter()
    {
        return true;
    }

    @Override
    public boolean canSplitBefore()
    {
        return true;
    }

    @Override
    public boolean canSplitInside()
    {
        return false;
    }

    @Override
    public int getBaselineOffset()
    {
        return boxh;
    }

    @Override
    public int getBelowBaseline()
    {
        return 0;
    }

    @Override
    public int getTotalLineHeight()
    {
        return boxh;
    }
    
    @Override
    public int getMaxLineHeight()
    {
        return boxh;
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

        if (obj != null)
            obj.doLayout();
        
        setAvailableWidth(availw);
        int wlimit = getAvailableContentWidth();
        if (getWidth() <= wlimit)
            return true;
        else
            return force;
    }

    @Override
    public void absolutePositions()
    {
        super.absolutePositions();
        if (obj != null)
            obj.absolutePositions();
    }

    @Override
    protected void loadSizes(boolean update)
    {
        super.loadSizes(update);
        int intw; //intrinsic sizes
        int inth;
        float intr;
        if (obj != null)
        {
            boxw = intw = obj.getIntrinsicWidth();
            boxh = inth = obj.getIntrinsicHeight();
            intr = (float) intw / inth;
        }
        else
        {
            boxw = intw = 20; //some reasonable default values
            boxh = inth = 20;
            intr = 1.0f;
        }
        
        //total widths used for percentages
        int twidth = cblock.getContentWidth();
        int theight = viewport.getContentHeight();
        
        //try to use the attributes
        int atrw = -1;
        int atrh = -1;
        try {
            if (!el.getAttribute("width").equals(""))
                atrw = HTMLNorm.computeAttributeLength(el.getAttribute("width"), twidth);
        } catch (NumberFormatException e) {
            System.err.println("Invalid width value: " + el.getAttribute("width"));
        }
        try {
            if (!el.getAttribute("height").equals(""))
                atrh = HTMLNorm.computeAttributeLength(el.getAttribute("height"), theight);
        } catch (NumberFormatException e) {
            System.err.println("Invalid height value: " + el.getAttribute("width"));
        }
        //apply intrinsic ration when necessary
        if (atrw == -1 && atrh == -1)
        {
            boxw = intw;
            boxh = inth;
        }
        else if (atrw == -1)
        {
            boxw = Math.round(intr * atrh);
            boxh = atrh;
        }
        else if (atrh == -1)
        {
            boxw = atrw;
            boxh = Math.round(atrw / intr);
        }
        else
        {
            boxw = atrw;
            boxh = atrh;
        }

        //compute dimensions from styles (styles should override the attributes)
        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.Width width = style.getProperty("width");
        CSSProperty.Height height = style.getProperty("height");
        if (width == null && height != null)
        {
            //boxw remains untouched, compute boxh
            int autoh = Math.round(boxw / intr);
            boxh = dec.getLength(getLengthValue("height"), height == CSSProperty.Height.AUTO, boxh, autoh, theight);
            if (atrw == -1)
                boxw = Math.round(intr * boxh);
        }
        else if (width != null && height == null)
        {
            //boxh remains untouched, compute boxw
            int autow = Math.round(intr * boxh);
            boxw = dec.getLength(getLengthValue("width"), width == CSSProperty.Width.AUTO, boxw, autow, twidth);
            if (atrh == -1)
                boxh = Math.round(boxw / intr);
        }
        else
        {
            boxw = dec.getLength(getLengthValue("width"), width == CSSProperty.Width.AUTO, boxw, intw, twidth);
            boxh = dec.getLength(getLengthValue("height"), height == CSSProperty.Height.AUTO, boxh, inth, theight);
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
    protected boolean separatedFromTop(ElementBox box)
    {
        return true;
    }

    @Override
    protected boolean separatedFromBottom(ElementBox box)
    {
        return true;
    }

    @Override
    public void draw(Graphics2D g, int turn, int mode)
    {
        ctx.updateGraphics(g);
        if (displayed && isVisible())
        {
            Shape oldclip = g.getClip();
            g.setClip(clipblock.getClippedContentBounds());
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
            }
            
            if (obj != null)
            {
                g.setClip(getClippedContentBounds());
                obj.draw(g, boxw, boxh);
            }
            g.setClip(oldclip);
        }
    }

}
