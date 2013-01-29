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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;

import cz.vutbr.web.css.*;
import cz.vutbr.web.css.CSSProperty.ListStyleType;

/**
 * This class represents a list-item box. This box behaves the same way
 * as a block box with some modifications.
 * @author radek
 * @author mantlikf
 */
public class ListItemBox extends BlockBox
{
    private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private static final int[] BVAL = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    
    /** List style type */
    private CSSProperty.ListStyleType styleType;

    /** Item number in the sequence */
    private int itemNumber;
    
    /** Item image */
    private ReplacedImage image;
    
	/**
	 * Create a new list item
	 */
	public ListItemBox(Element n, Graphics2D g, VisualContext ctx)
	{
		super(n, g, ctx);
		isblock = true;
	}

    @Override
    public void initBox()
    {
        super.initBox();
        itemNumber = findItemNumber();
        initFirstLine(this); //consider the list marker for the first line
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
    public void setStyle(NodeData s)
    {
        super.setStyle(s);
        styleType = style.getProperty("list-style-type");
        if (styleType == null)
            styleType = ListStyleType.DISC;
    }

    @Override
	public void draw(Graphics2D g, int turn, int mode)
    {
    	super.draw(g, turn, mode);
    	if (displayed && isVisible())
    	{
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_FG) drawMarker(g);
            }
    	}
    }
    
    /**
     * Checks whether the list item has a visible bullet.
     * @return <code>true</code> when the bullet type is set to other value than <code>none</code>.
     */
    public boolean hasVisibleBullet()
    {
        return styleType != CSSProperty.ListStyleType.NONE;
    }
    
    /**
     * Return item number in ordered list.
     *
     * @return item number
     */
    public int getItemNumber() 
    {
        return itemNumber;
    }
    
    /**
     * Get ordered list item marker text depending on list-style-type property.
     *
     * @return item text or empty string for unordered list
     */
    public String getMarkerText()
    {
        String text;
        if (styleType == CSSProperty.ListStyleType.UPPER_ALPHA)
            text = "" + ((char) (64 + (itemNumber % 24)));
        else if (styleType == CSSProperty.ListStyleType.LOWER_ALPHA)
            text = "" + ((char) (96 + (itemNumber % 24)));
        else if (styleType == CSSProperty.ListStyleType.UPPER_ROMAN)
            text = "" + binaryToRoman(itemNumber);
        else if (styleType == CSSProperty.ListStyleType.LOWER_ROMAN)
            text = "" + binaryToRoman(itemNumber).toLowerCase();
        else
            text = String.valueOf(itemNumber); // default decimal 
        return text + ". ";
    }

    /**
     * Finds the item number. Currently this correspond to the number of list-item boxes before this box
     * within the parent box.
     */
    private int findItemNumber() 
    {
        ElementBox parent = getParent();
        int cnt = 0;
        for (int i = parent.getStartChild(); i < parent.getEndChild(); i++)
        {
            Box child = parent.getSubBox(i);
            if (child instanceof ListItemBox)
                cnt++;
            if (child == this)
                return cnt;
        }
        return 1;
    }
    
    /**
     * Draw the list item symbol, number or image depending on list-style-type
     */
    public void drawMarker(Graphics2D g)
    {
        Shape oldclip = g.getClip();
        g.setClip(clipblock.getClippedContentBounds());
        if (image != null)
            drawImage(g);
        else
            drawBullet(g);
        g.setClip(oldclip);
    }
    
    /**
     * Draws a bullet or text marker
     */
    protected void drawBullet(Graphics2D g)
    {
        ctx.updateGraphics(g);
    	int x = (int) Math.round(getAbsoluteContentX() - 1.2 * ctx.getEm());
    	int y = (int) Math.round(getAbsoluteContentY() + 0.4 * ctx.getEm());
    	int r = (int) Math.round(0.6 * ctx.getEm());
    	if (styleType == CSSProperty.ListStyleType.CIRCLE) 
    		g.drawOval(x, y, r, r);
    	else if (styleType == CSSProperty.ListStyleType.SQUARE) 
    		g.fillRect(x, y, r, r);
    	//else if (type == CSSProperty.ListStyleType.BOX) //not documented, recognized by Konqueror 
    	//	g.drawRect(x, y, r, r);
    	else if (styleType == CSSProperty.ListStyleType.DISC)
    		g.fillOval(x, y, r, r);
    	else if (styleType != CSSProperty.ListStyleType.NONE)
    	    drawText(g, getMarkerText());
    }
    
    /**
     * Draws an image marker 
     */
    protected void drawImage(Graphics2D g)
    {
        int x = (int) Math.round(getAbsoluteContentX() - 1.2 * ctx.getEm());
        int y = (int) Math.round(getAbsoluteContentY() + 0.4 * ctx.getEm());
        Image img = image.getImage();
        if (img != null)
        {
            int w = img.getWidth(image);
            int h = img.getHeight(image);
            x = x - w / 2;
            y = y + h / 2;
            g.drawImage(img, x, y, image);
        }
    }

    /**
     * Draws a text marker
     */
    protected void drawText(Graphics2D g, String text)
    {
        // top left corner
        int x = getAbsoluteContentX();
        int y = getAbsoluteContentY();

        //Align Y with baseline
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(text, g);
        int ofs = getFirstInlineBoxBaseline();
        if (ofs == -1)
            ofs = ctx.getBaselineOffset(); //use the font baseline
        
        // Draw the string
        g.drawString(text,
                     x + ((int) rect.getX()) - ((int) Math.round(rect.getWidth())),
                     y + ofs);
    }
    
    /**
     * Conversion int to Roman numbers
     * from http://www.roseindia.net/java/java-tips/45examples/misc/roman/roman.shtml
     *
     * @param binary
     * @return
     */
    private static String binaryToRoman(int binary)
    {
        if (binary <= 0 || binary >= 4000) 
            throw new NumberFormatException("Value outside roman numeral range.");
        String roman = ""; // Roman notation will be accumualated here.

        // Loop from biggest value to smallest, successively subtracting,
        // from the binary value while adding to the roman representation.
        for (int i = 0; i < RCODE.length; i++)
        {
            while (binary >= BVAL[i])
            {
                binary -= BVAL[i];
                roman += RCODE[i];
            }
        }
        return roman;
    }
}
