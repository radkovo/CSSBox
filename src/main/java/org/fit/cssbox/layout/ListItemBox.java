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

import org.w3c.dom.Element;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.ListStyleType;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermURI;

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
	public ListItemBox(Element n, VisualContext ctx)
	{
		super(n, ctx);
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
        image = loadMarkerImage(s);
    }
    
    private ReplacedImage loadMarkerImage(NodeData style)
    {
        CSSProperty.ListStyleImage img = style.getProperty("list-style-image");
        if (img == CSSProperty.ListStyleImage.uri)
        {
            TermURI urlstring = style.getValue(TermURI.class, "list-style-image");
            ReplacedImage bgimg = new ReplacedImage(this, ctx, urlstring.getBase(), urlstring.getValue());
            if (ctx.getConfig().getLoadBackgroundImages())
                bgimg.setImage(ctx.getImageLoader().loadImage(bgimg.getUrl()));
            return bgimg;
        }
        else
            return null;
    }

    @Override
	public void draw(DrawStage turn)
    {
        if (displayed && isDeclaredVisible())
        {
            if (turn == DrawStage.DRAW_INLINE && floating == FLOAT_NONE && position == POS_STATIC)
                getViewport().getRenderer().renderMarker(this);
            
            super.draw(turn);
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
    
    public CSSProperty.ListStyleType getStyleType()
    {
        return styleType;
    }

    /**
     * Returns the list-style-type property value.
     * @return the list-style-type property value as a string
     */
    public String getListStyleType()
    {
        return styleType.toString();
    }
    
    /**
     * Returns the image used as a list item marker if any.
     * @return The image or {@code null} when no image is specified or available.
     */
    public ReplacedImage getMarkerImage()
    {
        return image;
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
        final String text = formatItemNumber(itemNumber, styleType);
        return text + ". ";
    }

    /**
     * Formats the item number depending on the style type
     * @param itemNumber
     * @param styleType
     * @return
     */
    public static String formatItemNumber(int itemNumber, ListStyleType styleType)
    {
        String text;
        if (itemNumber > 0)
        {
            switch (styleType)
            {
                case UPPER_ALPHA:
                    text = "";
                    while (itemNumber > 0)
                    {
                        text = String.valueOf(((char) (65 + ((itemNumber - 1) % 26)))) + text;
                        itemNumber = (itemNumber - 1) / 26;
                    }
                    break;
                case LOWER_ALPHA:
                    text = "";
                    while (itemNumber > 0)
                    {
                        text = String.valueOf(((char) (97 + ((itemNumber - 1) % 26)))) + text;
                        itemNumber = (itemNumber - 1) / 26;
                    }
                    break;
                case UPPER_ROMAN:
                    text = binaryToRoman(itemNumber);
                    break;
                case LOWER_ROMAN:
                    text = binaryToRoman(itemNumber).toLowerCase();
                    break;
                default:
                    text = String.valueOf(itemNumber); // default decimal
                    break;
            }
        }
        else
            text = String.valueOf(itemNumber); // non-positive numbers only in decimal
        return text;
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
     * Conversion int to Roman numbers
     * from http://www.roseindia.net/java/java-tips/45examples/misc/roman/roman.shtml
     *
     * @param binary the int to be converted
     * @return the roman number string
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
