/*
 * TextBox.java
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
 * Created on 5. únor 2006, 13:42
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.awt.geom.*;

import cz.vutbr.web.css.CSSProperty;

import org.w3c.dom.*;

/**
 * A box that corresponds to a text node.
 *
 * @author  radek
 */
public class TextBox extends Box
{
    /** Assigned text node */
    protected Text textNode;
    
    /** Text string after whitespace processing */
    protected String text;
    
    /** The start index of the text substring to be displayed */
    protected int textStart;

    /** The end index of the text substring to be displayed (excl) */
    protected int textEnd;

    /** Maximal total width */
    protected int maxwidth;
    
    /** Minimal total width */
    protected int minwidth;
    
    //===================================================================
    
    /**
     * Creates a new TextBox formed by a DOM text node.
     * @param n the corresponding DOM text node
     * @param g the graphics context used for rendering
     * @param ctx current visual context
     */
    public TextBox(Text n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        textNode = n;
        setWhiteSpace(ElementBox.WHITESPACE_NORMAL); //resets the text content and indices
        
        ctx.updateForGraphics(null, g);

        minwidth = computeMinimalWidth();
        maxwidth = computeMaximalWidth();
    }

    /**
     * Copy all the values from another text box.
     * @param src the source text box
     */
    public void copyValues(TextBox src)
    {
        super.copyValues(src);
        text = new String(src.text);
    }
    
    /** 
     * Create a new box from the same DOM node in the same context
     * @return the new TextBox 
     */
    public TextBox copyTextBox()
    {
        TextBox ret = new TextBox(textNode, g, ctx);
        ret.copyValues(this);
        return ret;
    }
    
    public String toString()
    {
        return "Text: " + text;
    }
    
    @Override
    public void initBox()
    {
        if (getParent() != null)
        {
            //reset the whitespace processing according to the parent settings
            CSSProperty.WhiteSpace ws = getParent().getWhiteSpace();
            if (ws != ElementBox.WHITESPACE_NORMAL)
                setWhiteSpace(ws);
        }
    }

    /**
     * @return the text contained in this box
     */
    @Override
    public String getText()
    {
        if (text != null)
            return text.substring(textStart, textEnd);
        else
            return "";
    }

    @Override
    public boolean isDeclaredVisible()
    {
        return true;
    }

    @Override
    public boolean isDisplayed()
    {
        if (getParent() == null)
            return true;
        else
            return parent.isDisplayed();
    }

    @Override
    public boolean isVisible()
    {
        if (getParent() == null)
            return true;
        else
            return parent.isVisible();
    }
    
    //=======================================================================

    /**
     * Sets the whitespace processing for this box. The text content is then treated accordingly. The text start and text end
     * indices are reset to their initial values.
     */
    public void setWhiteSpace(CSSProperty.WhiteSpace value)
    {
        if (value == ElementBox.WHITESPACE_NORMAL || value == ElementBox.WHITESPACE_NOWRAP || value == ElementBox.WHITESPACE_PRE_LINE)
            text = collapseWhitespaces(node.getNodeValue());
        else
            text = node.getNodeValue();
        
        textStart = 0;
        textEnd = text.length();
        isempty = (textEnd == 0); //not trimming, space cannot be omited
    }
    
    /**
     * Applies the whitespace removal rules used in HTML
     * @param src source string
     * @return a new string with additional whitespaces removed
     */
    private String collapseWhitespaces(String src)
    {
        StringBuffer ret = new StringBuffer();
        boolean inws = false;
        for (int i = 0; i < src.length(); i++)
        {
            char ch = src.charAt(i);
            if (Character.isWhitespace(ch))
            {
                if (!inws)
                {
                    ret.append(' ');
                    inws = true;
                }
            }
            else
            {
                inws = false;
                ret.append(ch);
            }
        }
        return new String(ret);
    }
    
    /**
     * @return the start offset in the text string
     */ 
    protected int getTextStart()
    {
        return textStart;
    }
    
    /**
     * @param index the start offset in the text string
     */ 
    protected void setTextStart(int index)
    {
        textStart = index;
    }
    
    /**
     * @return the end offset in the text string (not included)
     */ 
    protected int getTextEnd()
    {
        return textEnd;
    }
    
    /**
     * @param index the end offset in the text string (not included)
     */ 
    protected void setTextEnd(int index)
    {
        textEnd = index;
    }
    
	@Override
    public boolean affectsDisplay()
    {
        return !isEmpty();
    }
    
	@Override
    public boolean isWhitespace()
    {
        return getText().trim().length() == 0;
    }
    
	@Override
    public int getContentX() 
    {
        return bounds.x;
    }
    
	@Override
    public int getAbsoluteContentX() 
    {
        return absbounds.x;
    }
    
	@Override
    public int getContentY() 
    {
        return bounds.y;
    }

	@Override
    public int getAbsoluteContentY() 
    {
        return absbounds.y;
    }

	@Override
    public int getContentWidth() 
    {
        return bounds.width;
    }
    
	@Override
    public int getContentHeight() 
    {
        return bounds.height;
    }

	@Override
    public int getAvailableContentWidth() 
    {
        return availwidth;
    }
    
    @Override
    public int getLineHeight()
    {
        return ctx.getFontHeight();
    }

    @Override
    public int getBaselineOffset()
    {
        return ctx.getBaselineOffset();
    }
    
    @Override
    public int getMaxBaselineOffset()
    {
        return ctx.getBaselineOffset();
    }
    
    @Override
    public int totalHeight() 
    {
        return bounds.width;
    }
    
	@Override
    public int totalWidth() 
    {
        return bounds.height;
    }
    
	@Override
    public Rectangle getMinimalAbsoluteBounds()
    {
    	return absbounds;
    }
    
	@Override
    public boolean isInFlow()
	{
		return true;
	}
	
	@Override
	public boolean containsFlow()
	{
		return false;
	}
	
    @Override
    public boolean canSplitInside()
    {
        return (getText().indexOf(' ') != -1);
    }
    
    @Override
    public boolean canSplitBefore()
    {
        String s = node.getNodeValue();
        if (textEnd > textStart)
        	return (s.charAt(textStart) == ' ' ||
        			(textStart > 0 && s.charAt(textStart-1) == ' '));
        else
        	return false;
    }
    
    @Override
    public boolean canSplitAfter()
    {
        String s = node.getNodeValue();
        if (textEnd > textStart)
	        return (s.charAt(textEnd-1) == ' ' ||
	                (textEnd < s.length() && s.charAt(textEnd) == ' '));
        else
        	return false;
    }
    
	@Override
	public boolean hasFixedWidth()
	{
		return false; //the width depends on the content
	}

	@Override
	public boolean hasFixedHeight()
	{
		return false;
	}

    //=======================================================================
    
	/** 
	 * Compute the width and height of this element. Layout the sub-elements.
     * @param widthlimit Maximal width available for the element
     * @param force Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return <code>true</code> if the box has been succesfully placed
     */
	@Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        //Skip if not displayed
        if (!displayed)
        {
            bounds.setSize(0, 0);
            return true;
        }
        
        setAvailableWidth(widthlimit);
        
        boolean split = false;
        int wlimit = getAvailableContentWidth();
        boolean empty = (text.trim().length() == 0);
        int end = text.length();
        FontMetrics fm = g.getFontMetrics();
        int w = 0, h = 0;
        if (!empty || !linestart) //ignore empty text elements at the begining of a line
        {
            //ignore spaces at the begining of a line
            if (linestart)
                while (textStart < end && text.charAt(textStart) == ' ')
                    textStart++;
            //try to place the text
            do
            {
                w = fm.stringWidth(text.substring(textStart, end));
                h = fm.getHeight();
                if (w > wlimit) //exceeded - try to split
                {
                    if (empty) //empty or just spaces - don't place at all
                    {
                        w = 0; h = 0; split = false; break;
                    }
                    int wordend = text.substring(0, end).lastIndexOf(' '); //find previous word
                    while (wordend > 0 && text.charAt(wordend-1) == ' ') wordend--; //skip trailing spaces
                    if (wordend <= textStart) //no previous word, cannot split
                    {
                        if (!force) //let it split as good as possible
                        {
                            end = textStart;
                            split = false;
                        }
                        else
                            split = true;
                        break;
                    }
                    else
                    {
                        end = wordend;
                        split = true;
                    }
                }
            } while (end > textStart && w > wlimit);
        }
        textEnd = end;
        bounds.setSize(w, h);
        
        //if not the whole element was placed, create the rest
        if (split)
        {
            //find the start of the next word
            int start = textEnd;
            while (start < text.length() && text.charAt(start) == ' ') start++;
            if (start < text.length())
            {
                TextBox rtext = copyTextBox();
                rtext.splitted = true;
                rtext.setTextStart(start);
                rest = rtext;
            }
            else
                rest = null;
        }
        else
            rest = null;
        
        return ((textEnd > textStart) || empty);
    }
    
	@Override
    public void absolutePositions()
    {
        if (displayed)
        {
            //my top left corner
            absbounds.x = getParent().getAbsoluteContentX() + bounds.x;
            absbounds.y = getParent().getAbsoluteContentY() + bounds.y;
	        absbounds.width = bounds.width;
	        absbounds.height = bounds.height;
        }
    }
    
	@Override
    public int getMinimalWidth()
    {
		return minwidth;
    }
	
    private int computeMinimalWidth()
    {
        //returns the length of the longest word
        int ret = 0;
        String t = getText();
        if (t.length() > 0)
        {
            FontMetrics fm = g.getFontMetrics();
            int s1 = 0;
            int s2 = t.indexOf(' ');
            do
            {
                if (s2 == -1) s2 = t.length();
                int w = fm.stringWidth(t.substring(s1, s2));
                if (w > ret) ret = w;
                s1 = s2 + 1;
                s2 = t.indexOf(' ', s1);
            } while (s1 < t.length() && s2 < t.length());
        }
        return ret;
    }
    
	@Override
    public int getMaximalWidth()
    {
		return maxwidth;
    }
	
    private int computeMaximalWidth()
    {
        //returns the lenth of the whole string
        FontMetrics fm = g.getFontMetrics();
        return fm.stringWidth(getText());
    }

    /** 
     * Draw the text content of this box (no subboxes)
     * @param g the graphics context to draw on
     */
    protected void drawContent(Graphics2D g)
    {
        //top left corner
        int x = absbounds.x;
        int y = absbounds.y;

        //Draw the string
        if (textEnd > textStart)
        {
            String t = text.substring(textStart, textEnd);
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(t, g);
            Shape oldclip = g.getClip();
            g.setClip(clipblock.getAbsoluteContentBounds());
            g.drawString(t, x + (int) rect.getX(), y - (int) rect.getY());
            g.setClip(oldclip);
        }
    }
    
	@Override
    public void draw(Graphics2D g, int turn, int mode)
    {
        if (displayed)
        {
            if (turn == DRAW_ALL || turn == DRAW_NONFLOAT)
            {
                if (mode == DRAW_BOTH || mode == DRAW_FG) drawContent(g);
            }
        }
    }
    
	@Override
    public void drawExtent(Graphics2D g)
    {
        //draw the full box
        g.setColor(Color.ORANGE);
        g.drawRect(absbounds.x, absbounds.y, bounds.width, bounds.height);
        for (int i = 0; i < getText().length(); i++)
        {
            if (i != 0) System.out.print(" : ");
            char ch = getText().charAt(i);
            System.out.print(ch);
            System.out.print((int) ch);
            if (Character.isWhitespace(ch))
                System.out.print("!");
            if (Character.isSpaceChar(ch))
                System.out.print("*");
        }
        System.out.println();
    }

}
