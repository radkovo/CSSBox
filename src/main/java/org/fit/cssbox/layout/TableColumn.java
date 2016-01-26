/*
 * TableColumn.java
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
 * Created on 3.10.2006, 10:10:58 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.*;

import cz.vutbr.web.css.*;

import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a table column. It is not visible and its only purpose is to
 * describe the table column properties. These properties are used while the table is
 * laid out. 
 * 
 * @author burgetr
 */
public class TableColumn extends BlockBox
{
    private static Logger log = LoggerFactory.getLogger(TableColumn.class);

    /** Column span taken from the 'span' attribute (default 1) */
    protected int span;
    
    /** The width of the column or individual columns */
    protected String colwidth;
    
    /** The minimal column width */
    protected int mincwidth;
    
    /** The maximal column width */
    protected int maxcwidth;
    
    /** true if the width is relative [%] */
    protected boolean wrelative;
    
    /** relative width [%] when used */
    protected int percent;
    
    /** the absolute width when specified */
    protected int abswidth;
    
    //====================================================================================
    
    /**
     * Creates a new table column
     */
    public TableColumn(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
        loadAttributes();
    }
    
    /**
     * Creates a new table cell from an inline box
     */
    public TableColumn(InlineBox src)
    {
        super(src);
        isblock = true;
        loadAttributes();
    }

    public void copyValues(TableColumn src)
    {
        super.copyValues(src);
        span = src.span;
        colwidth = (src.colwidth == null) ? null : new String(src.colwidth);
        mincwidth = src.mincwidth;
        maxcwidth = src.maxcwidth;
        wrelative = src.wrelative;
        percent = src.percent;
        abswidth = src.abswidth;
    }
    
    /**
     * Creates a copy of the column
     */
    public TableColumn copyBox()
    {
        TableColumn ret = new TableColumn(el, g, ctx);
        ret.copyValues(this);
        return ret;
    }
    
    /**
     * @return the column span
     */
    public int getSpan()
    {
        return span;
    }

    /**
     * If the box width has been specified explicitly, this function obtains the specifiaction string.
     * @return the width specification of the column(s)
     */
    public String getSpecifiedWidth()
    {
        return colwidth;
    }
    
    /**
     * Set the width of the column form an attribute or CSS
     * @param width the new width
     */
    public void setSpecifiedWidth(String width)
    {
        colwidth = width;
        try {
            content = new Dimension(0, 0);
            content.width = Integer.parseInt(width);
            bounds.width = content.width;
            abswidth = content.width;
            wset = true;
        } catch (NumberFormatException e) {
            if (!width.equals(""))
                log.warn("Invalid width value: " + width);
        }
    }

    /**
     * Set the width of the column
     * @param width the new width
     */
    public void setColumnWidth(int width)
    {
        content = new Dimension(0, 0);
        content.width = width;
        bounds.width = content.width;
    }
    
    /**
     * @return the maximal width of the cell contents
     */
    public int getMaximalWidth()
    {
        return maxcwidth;
    }

    /**
     * Set the maximal width of the cell contents.
     * @param maxwidth the maxwidth to set
     */
    public void setMaximalWidth(int maxwidth)
    {
        this.maxcwidth = maxwidth;
    }

    /**
     * @return the minimal width of the cell contents
     */
    public int getMinimalWidth()
    {
        return mincwidth;
    }

    /**
     * @param minwidth the minimal width to set
     */
    public void setMinimalWidth(int minwidth)
    {
        this.mincwidth = minwidth;
    }

    /**
     * @return the percentage if the box has a relative width, 0 otherwise
     */
    public int getPercent()
    {
        return percent;
    }

    /**
     * @param percent the percentage to set
     */
    public void setPercent(int percent)
    {
        this.percent = percent;
    }

    /**
     * @return true if the width is specified relatively
     */
    public boolean isWrelative()
    {
        return wrelative;
    }

    /**
     * @param wrelative true if the width is specified relatively
     */
    public void setRelative(boolean wrelative)
    {
        this.wrelative = wrelative;
    }
    
    //====================================================================================

    @Override
    public boolean affectsDisplay()
    {
        return false;
    }

    @Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        return true;
    }

    @Override
    protected void loadSizes(boolean update)
    {
        bounds = new Rectangle(0, 0, 0, 0);
        min_size = new Dimension();
        max_size = new Dimension();
        margin = new LengthSet();
        emargin = margin;
        padding = new LengthSet();
        border = new LengthSet();
        isempty = true;
        displayed = false;
        visible = false;
        coords = new LengthSet();
        
        if (colwidth.equals("")) //no width set - try to get from style
        {
            int contw = getContainingBlock().width;
            CSSDecoder dec = new CSSDecoder(ctx);
            CSSProperty.Width wprop = style.getProperty("width");
            if (wprop != null && wprop != CSSProperty.Width.AUTO)
            {
                TermLengthOrPercent width = getLengthValue("width");
                abswidth = dec.getLength(width, false, 0, 0, contw);
                content.width = abswidth;
                wset = true;
	            if (width.isPercentage())
	            {
	            	wrelative = true;
                    percent = Math.round(width.getValue());
                    if (percent == 0)
                        wrelative = false; //consider 0% as absolute 0
	            }
            }
        }
        
        bounds.width = content.width;
    }
    
    //====================================================================================
    
    protected void loadAttributes()
    {
        try {
            if (!HTMLNorm.getAttribute(el, "span").equals(""))
                span = Integer.parseInt(HTMLNorm.getAttribute(el, "span"));
            else
                span = 1;
        } catch (NumberFormatException e) {
            log.warn("Invalid span value: " + HTMLNorm.getAttribute(el, "span"));
        }
        setSpecifiedWidth(HTMLNorm.getAttribute(el, "width"));
    }

    /**
     * Creates a new <col> element that represents an anonymous column
     * @param doc the document
     * @return the new element
     */
    public static Element createAnonymousColumn(Document doc)
    {
        Element div = doc.createElement("col");
        div.setAttribute("class", "Xanonymous");
        div.setAttribute("style", "display:table-column;");
        return div;
    }
    
}
