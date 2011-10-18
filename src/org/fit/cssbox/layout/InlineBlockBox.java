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

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermLengthOrPercent;

import org.w3c.dom.Element;

/**
 * A box corresponding to an inline-block element.
 * 
 * @author radek
 */
public class InlineBlockBox extends BlockBox implements Inline
{
    protected int baseline;

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
	
    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        boolean ret = super.doLayout(availw, force, linestart);
        baseline = getLastInlineBoxBaseline(this) + getContentOffsetY();
        System.out.println("Ofs: " + getContentOffsetY());
        System.out.println("H: " + getHeight());
        return ret;
    }
	
    @Override
    public boolean hasFixedWidth()
    {
        return wset; //the width should not be computed from the parent
    }

    @Override
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update)
    {
        //The same as for absolutely positioned boxes (shrink-to-fit or explicitely set)
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (width == null) auto = true; //no value behaves as "auto"

        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");
        preferredWidth = -1;
        
        if (!widthComputed) update = false;
        
        //compute width when set. If not, it will be computed during the layout
        if (cblock != null && cblock.wset)
        {
            wset = (exact && !auto && width != null);
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, contw);
        }
        else
        {
            wset = (exact && !auto && width != null && !width.isPercentage());
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, 0);
        }

        //auto margins are treated as zero
        margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
        margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
        
    }
        
    @Override
    public int getMinimalContentWidthLimit()
    {
        int ret;
        if (wset)
            ret = content.width;
        else if (min_size.width != -1)
            ret = min_size.width;
        else
            ret = 0;
            
        return ret;
    }
    
    //========================================================================
	
    public int getMaxLineHeight()
	{
		return getHeight();
	}

    public int getBaselineOffset()
	{
		return baseline;
	}

	public int getBelowBaseline()
	{
		return getHeight() - baseline;
	}

	public int getTotalLineHeight()
	{
		return getHeight();
	}

	public int getHalfLead()
	{
		return 0;
	}
	
    //========================================================================
	
	private int getLastInlineBoxBaseline(ElementBox root)
	{
	    //find last in-flow box
	    Box box = null;
        for (int i = root.getSubBoxNumber() - 1; i >= 0; i--)
        {
            box = root.getSubBox(i);
            if (box.isInFlow())
                break;
            else
                box = null;
        }
        
        if (box != null)
        {
	        if (box instanceof Inline)
	        {
                System.out.println(box + ":I: " + (box.getContentY() + ((Inline) box).getBaselineOffset()));
                return box.getContentY() + ((Inline) box).getBaselineOffset();
	        }
            else
            {
                System.out.println(box + ":B: " + (box.getContentY() + getLastInlineBoxBaseline((ElementBox) box)));
	            return box.getContentY() + getLastInlineBoxBaseline((ElementBox) box);
            }
	    }
	    else
	        return 0;
	}
	

}
