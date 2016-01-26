/**
 * TableCaptionBox.java
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
 * Created on 29.9.2006, 14:12:05 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermLengthOrPercent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A box that contains the table caption. 
 * @author burgetr
 */
public class TableCaptionBox extends BlockBox
{
    private static Logger log = LoggerFactory.getLogger(TableCaptionBox.class);

    /**
     * Create a new table caption
     */
    public TableCaptionBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
    }

    /**
     * Create a new table caption from an inline box
     */
    public TableCaptionBox(InlineBox src)
    {
        super(src);
        isblock = true;
    }    
    
    
    //In contrast to a normal block box, a different content block width is used and availwidth is used for determining the free space
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (width == null) auto = true; //no value behaves as 'auto'
        
        //According to CSS spec. 17.4, we should take the size of the original containing box, not the anonymous box
        contw = getContainingBlockBox().getContainingBlock().width;
        
        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");
        preferredWidth = -1;
        
        if (!widthComputed) update = false;
        
        if (auto)
        {
            if (exact) wset = false;
            margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
            margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
            declMargin.left = margin.left;
            declMargin.right = margin.right;
            /* For the first time, we always try to use the maximal width even for the
             * boxes out of the flow. When updating, only the in-flow boxes are adjusted. */
            if (!update || isInFlow())
            {
                content.width = contw - margin.left - border.left - padding.left
                                  - padding.right - border.right - margin.right;
                if (content.width < 0) content.width = 0;
            }
            preferredWidth = -1; //we don't prefer anything (auto width)
        }
        else
        {
            if (exact) 
            {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
            margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
            margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
            declMargin.left = margin.left;
            declMargin.right = margin.right;
            
            //We will prefer some width if the value is not percentage
            boolean prefer = !width.isPercentage();
            //We will include the margins in the preferred width if they're not percentages
            int prefml = (mleft == null) || mleft.isPercentage() || mleftauto ? 0 : margin.left;
            int prefmr = (mright == null) || mright.isPercentage() || mrightauto ? 0 : margin.right;
            //Compute the preferred width
            if (prefer)
                preferredWidth = prefml + border.left + padding.left + content.width +
                                 padding.right + border.right + prefmr;
            
            //Compute the margins if we're in flow and we know the width
            if (isInFlow() && prefer) 
            {
                if (mleftauto && mrightauto)
                {
                    int rest = contw - content.width - border.left - padding.left
                                     - padding.right - border.right;
                    if (rest < 0) rest = 0;
                    margin.left = (rest + 1) / 2;
                    margin.right = rest / 2;
                }
                else if (mleftauto)
                {
                    margin.left = contw - content.width - border.left - padding.left
                                        - padding.right - border.right - margin.right;
                    //if (margin.left < 0) margin.left = 0; //"treated as zero"
                }
                else if (mrightauto)
                {
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    //if (margin.right < 0) margin.right = 0; //"treated as zero"
                }
                else //everything specified, ignore right margin
                {
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    //if (margin.right < 0) margin.right = 0; //"treated as zero"
                }
            }
        }
    }
}
