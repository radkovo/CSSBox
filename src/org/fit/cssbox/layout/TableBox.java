/**
 * TableBox.java
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
 * Created on 29.9.2006, 13:52:23 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A box that represents a table.
 * http://www.w3.org/TR/CSS21/tables.html
 * 
 * @author burgetr
 */
public class TableBox extends BlockBox
{
    protected TableCaptionBox caption;
    protected TableBodyBox header;
    protected TableBodyBox footer;
    protected Vector<TableBodyBox> bodies;
    protected Vector<TableColumn> columns;
    
    /** total number of columns in the table */
    protected int columnCount;

    /** cell spacing */
    protected int spacing = 2;
    
    /** an anonymous table body (for lines that are not in any other body) */
    private TableBodyBox anonbody;
    
    /** true if the column width have been already calculated */
    private boolean columnsCalculated = false;

    //====================================================================================
    
    /**
     * Create a new table
     */
    public TableBox(Element n, Graphics g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
        loadTableStyle();
    }
    
    /**
     * Create a new table from an inline box
     */
    public TableBox(InlineBox src)
    {
        super(src);
        isblock = true;
        loadTableStyle();
    }

    /**
     * Determine the number of columns for the whole table
     * @return the column number
     */
    public int getColumnCount()
    {
        return columnCount;
    }
    
	@Override
	public boolean hasFixedWidth()
	{
		return wset; //the table has fixed width only if set explicitly
	}
	
    //====================================================================================

    @Override
    public void initBox()
    {
        organizeContent(); //organize the child elements according to their display property
    }
	
    @Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
    	if (this.getElement().getAttribute("id").equals("jojo"))
    		System.out.println("jo!");
        int y = 0;
        int maxw = 0;
        
        /* Always try to use the full width. If the box is not in flow, its width
         * is updated after the layout */
        setAvailableWidth(totalWidth());

        //calculate the column widths
        calculateColumns();
        
        //layout the bodies
        if (header != null)
        {
        	header.setSpacing(spacing);
            header.doLayout(widthlimit, columns);
            header.setPosition(0, y);
            if (header.getWidth() > maxw)
                maxw = header.getWidth();
            y += header.getHeight();
        }
        if (footer != null)
        {
        	footer.setSpacing(spacing);
            footer.doLayout(widthlimit, columns);
            footer.setPosition(0, y);
            if (footer.getWidth() > maxw)
                maxw = footer.getWidth();
            y += footer.getHeight();
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            TableBodyBox body = it.next();
            body.setSpacing(spacing);
            body.doLayout(widthlimit, columns);
            body.setPosition(0, y);
            if (body.getWidth() > maxw)
                maxw = body.getWidth();
            y += body.getHeight();
        }
        content.width = maxw;
        content.height = y;
        setSize(totalWidth(), totalHeight());
        return true;
    }
    
    @Override
    protected void loadSizes(boolean update)
    {
    	if (this.getElement().getAttribute("id").equals("xoxo"))
    		System.out.println("jo!");
        //load the content width from the attribute
        if (!update)
        {
            String width = getElement().getAttribute("width");
            if (!width.equals(""))
            {
                if (!width.endsWith("%"))
                    width = width + "px";
                style.setProperty("width", width, "important");
            }
        }
        super.loadSizes(update);
    }

    
    /** 
     * Calculates the widths and margins for the table.
     * @param width the specified width
     * @param exact true if this is the exact width, false when it's a max/min width
     * @param contw containing block width
     * @param wknown <code>true</code>, if the containing block width is known
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    protected void computeWidths(String width, boolean exact, int contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (width.equals("")) width = "auto";
        if (exact) wset = !width.equals("auto");
        if (wset && exact && dec.isPercent(width)) wrelative = true;
        String mleft = getStyleProperty("margin-left");
        String mright = getStyleProperty("margin-right");
        preferredWidth = -1;
        
        //if column widths haven't been calculated yet,
        //we can reload everything from scratch
        if (!columnsCalculated) update = false;
        
        if (!wset && !update) //width unknown and the content size unknown
        {
            /* For the first time, we always try to use the maximal width even for the table.
             * That means: 'auto' margins are taken as 0, width comes from the parent element. */
            margin.left = dec.getLength(mleft, 0, 0, contw);
            margin.right = dec.getLength(mright, 0, 0, contw);
            content.width = contw - margin.left - border.left - padding.left
                              - padding.right - border.right - margin.right;
        }
        else
        {
        	if (!update)
        		content.width = dec.getLength(width, 0, 0, contw);
            margin.left = dec.getLength(mleft, 0, 0, contw);
            margin.right = dec.getLength(mright, 0, 0, contw);

            if (isInFlow()) 
            {
                if (mleft.equals("auto") && mright.equals("auto"))
                {
                    preferredWidth = border.left + padding.left + content.width +
                                     padding.right + border.right;
                    int rest = contw - content.width - border.left - padding.left
                                     - padding.right - border.right;
                    margin.left = (rest + 1) / 2;
                    margin.right = rest / 2;
                }
                else if (mleft.equals("auto"))
                {
                    preferredWidth = border.left + padding.left + content.width +
                                     padding.right + border.right + margin.right;
                    margin.left = contw - content.width - border.left - padding.left
                                        - padding.right - border.right - margin.right;
                    if (margin.left < 0) margin.left = 0; //"treated as zero"
                }
                else if (mright.equals("auto"))
                {
                    preferredWidth = margin.left + border.left + padding.left + content.width +
                                     padding.right + border.right;
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    if (margin.right < 0) margin.right = 0; //"treated as zero"
                }
                else //everything specified, ignore right margin
                {
                    preferredWidth = margin.left + border.left + padding.left + content.width +
                                        padding.right + border.right + margin.right;
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    if (margin.right < 0) margin.right = 0; //"treated as zero"
                }
            }
        }
    }
    
    @Override
    protected int getMaximalContentWidth()
    {
        int ret = 0;
        if (header != null)
        {
            int m = header.getMaximalWidth();
            if (m > ret) ret = m;
        }
        if (footer != null)
        {
            int m = footer.getMaximalWidth();
            if (m > ret) ret = m;
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            int m = it.next().getMaximalWidth();
            if (m > ret) ret = m;
        }
        return ret;
    }

    @Override
    protected int getMinimalContentWidth()
    {
        int ret = 0;
        if (header != null)
        {
            int m = header.getMinimalWidth();
            if (m > ret) ret = m;
        }
        if (footer != null)
        {
            int m = footer.getMinimalWidth();
            if (m > ret) ret = m;
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            int m = it.next().getMinimalWidth();
            if (m > ret) ret = m;
        }
        return ret;
    }
    
    //====================================================================================
    
    /**
     * Determine the number of columns for the whole table
     */
    public void determineColumnCount()
    {
        int ret = 0;
        if (header != null)
        {
            int c = header.getColumnCount();
            if (c > ret) ret = c;
        }
        if (footer != null)
        {
            int c = footer.getColumnCount();
            if (c > ret) ret = c;
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            int c = it.next().getColumnCount();
            if (c > ret) ret = c;
        }
        columnCount = ret;
    }

    /**
     * Analyzes the cells in the body and updates the stored column parametres 
     */
    private void updateColumns(TableBodyBox body)
    {
        for (int i = 0; i < columns.size(); i++)
            if (i < body.getColumnCount())
                body.updateColumn(i, columns.elementAt(i));
    }
    
    /**
     * Calculates the column widths.
     */
    private void calculateColumns()
    {
        int wlimit = getAvailableContentWidth();
        System.out.println("wset="+wset);
        System.out.println("wlimit="+wlimit);
        
        //create the columns that haven't been specified explicitely
        determineColumnCount();
        while (columns.size() < columnCount)
            columns.add(new TableColumn(createAnonymousColumn(getParent().getElement().getOwnerDocument()), g, ctx));
        
        //load the parametres and ensure the minimal column widths
        if (header != null)
            updateColumns(header);
        if (footer != null)
            updateColumns(footer);
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
            updateColumns(it.next());

        //for (int i = 0; i < columns.size(); i++)
        //    System.out.println("Col " + i + " : " + columns.elementAt(i).getWidth());
        
        
        //now, the columns are at minimal widths
        //gather column statistics
        int sumabs = 0; //total length of absolute columns
        int sumperc = 0; //total percentage
        int sumnonemin = 0; //total minimal length of the columns with no width specified
        int sumnonemax = 0; //total maximal length of the columns with no width specified
        int totalwperc = 0; //total table width computed from percentage columns
        for (int i = 0; i < columns.size(); i++) //compute the sums
        {
            TableColumn col = columns.elementAt(i);
            if (col.wrelative)
            {
            	sumperc += col.percent;
            	int maxw = col.getMaximalWidth();
            	int newtotal = maxw * 100 / col.percent;
            	if (newtotal > totalwperc) totalwperc = newtotal;
            }
            else
            {
                if (col.wset)
                    sumabs += col.abswidth;
                else
                {
                    sumnonemin += col.getWidth();
                    sumnonemax += col.getMaximalWidth();
                }
            }
        }
        
        //guess the total table width (not including spacing now)
        if (totalwperc > wlimit) totalwperc = wlimit;
        int totalwabs = 0; //from absolute fields
        if (sumabs + sumnonemax > 0)
        {
            int abspart = 100 - sumperc; //the absolute part is how many percent
            totalwabs = (abspart == 0) ? wlimit : (sumabs + sumnonemax) * 100 / abspart; //what is 100%
        }
        
        int mintotalw = getMinimalContentWidth();
        int totalw = Math.max(totalwperc, totalwabs); //desired width
        if (wset && totalw < wlimit)    //if the width is set explicitly, ensure the width
            totalw = wlimit;
        if (totalw > wlimit) totalw = wlimit; //we would like not to exceed the limit
        if (totalw < mintotalw) totalw = mintotalw; //but we cannot be below the minimal width
        
        System.out.println("Percent: " + totalwperc);
        System.out.println("Abs+%: " + totalwabs);
        System.out.println("Minimum: " + getMinimalWidth());
        System.out.println("wlimit: " + wlimit);
        System.out.println("mintotalw: " + mintotalw);
        System.out.println("result:" + totalw);
        
        //set the percentage columns to their values, if possible
        int remain = totalw;
        if (sumperc > 0)
        {
            for (int i = 0; i < columns.size(); i++) //set the column sizes
            {
                TableColumn col = columns.elementAt(i);
                if (col.wrelative)
                {
                    int neww = col.percent * totalw / 100;
                    if (neww > remain) neww = remain;
                    if (neww < col.getMinimalWidth()) neww = col.getMinimalWidth();
                    //int dif = neww - col.getWidth();
                    col.setColumnWidth(neww);
                    remain -= neww;
                }
            }
        }
        System.out.println("remain2:" + remain);
        
        //set the absolute columns
        if (sumabs > 0)
        {
            double factor = 1;
            if (sumnonemin == 0)
                factor = remain / (double) sumabs;
            System.out.println("Factor: " + factor);
            for (int i = 0; i < columns.size(); i++) //set the column sizes
            {
                TableColumn col = columns.elementAt(i);
                if (col.wset && !col.wrelative)
                {
                    System.out.println("Col " + i + " : " + col.abswidth);
                    int neww = (int) (col.abswidth * factor);
                    if (neww > remain) neww = remain;
                    if (neww < col.getMinimalWidth()) neww = col.getMinimalWidth();
                    //int dif = neww - col.getWidth();
                    col.setColumnWidth(neww);
                    remain -= neww;
                }
            }
        }
        System.out.println("remain3:" + remain);
        
        //set the remaining columns
        if (sumnonemin > 0)
        {
            double factor = 1;
            factor = remain / (double) sumnonemax;
            System.out.println("Factor: " + factor);
            for (int i = 0; i < columns.size(); i++) //set the column sizes
            {
                TableColumn col = columns.elementAt(i);
                if (!col.wset)
                {
                    int neww = (int) (col.getMaximalWidth() * factor);
                    if (neww > remain) neww = remain;
                    if (neww < col.getMinimalWidth()) neww = col.getMinimalWidth();
                    //int dif = neww - col.getWidth();
                    col.setColumnWidth(neww);
                    remain -= neww;
                }
            }
        }
        
        columnsCalculated = true;
    }
    
    //====================================================================================

    /**
     * Loads the table-specific features from the style
     */
    private void loadTableStyle()
    {
  		CSSDecoder dec = new CSSDecoder(ctx);
    	//border spacing
   		spacing = dec.getLength(getStyleProperty("border-spacing"), "2px", "0", 0);
    }
    
    /**
     * Goes through the list of child boxes and organizes them into captions, header,
     * footer, etc.
     */
    private void organizeContent()
    {
        bodies = new Vector<TableBodyBox>();
        columns = new Vector<TableColumn>();
        anonbody = null;
        for (Iterator<Box> it = nested.iterator(); it.hasNext(); )
        {
        	Box box = it.next();
            if (box instanceof ElementBox)
            {
                ElementBox subbox = (ElementBox) box;
                switch (subbox.getDisplay())
                {
                    case ElementBox.DISPLAY_TABLE_CAPTION:
                        caption = (TableCaptionBox) subbox;
                        break;
                    case ElementBox.DISPLAY_TABLE_HEADER_GROUP:
                        header = (TableBodyBox) subbox;
                        break;
                    case ElementBox.DISPLAY_TABLE_FOOTER_GROUP:
                        footer = (TableBodyBox) subbox;
                        break;
                    case ElementBox.DISPLAY_TABLE_ROW_GROUP:
                        bodies.add((TableBodyBox) subbox);
                        break;
                    case ElementBox.DISPLAY_TABLE_ROW:
                        if (anonbody == null)
                        {
                            //the table itself may not have an owner document if it is an anonymous box itself
                            //therefore, we're using the parent's owner document
                        	Element anonelem = createAnonymousBody(getParent().getElement().getOwnerDocument()); 
                            anonbody = new TableBodyBox(anonelem, g, ctx);
                            anonbody.setContainingBlock(this);
                            anonbody.setParent(this);
                            anonbody.loadSizes();
                            bodies.add(anonbody);
                        }
                        anonbody.addSubBox(subbox);
                        anonbody.isempty = false;
                        subbox.setContainingBlock(anonbody);
                        subbox.setParent(anonbody);
                        it.remove();
                        endChild--;
                        break;
                    case ElementBox.DISPLAY_TABLE_COLUMN:
                        for (int i = 0; i < ((TableColumn) subbox).getSpan(); i++)
                            columns.add((TableColumn) subbox);
                        break;
                    case ElementBox.DISPLAY_TABLE_COLUMN_GROUP:
                        for (int i = 0; i < ((TableColumnGroup) subbox).getSpan(); i++)
                            columns.add(((TableColumnGroup) subbox).getColumn(i));
                        break;
                    default:
                        System.err.println("TableBox: Warning: Element ignored: " + subbox);
                        break;
                }
            }
        }
        if (anonbody != null)
        {
        	anonbody.endChild = anonbody.nested.size();
        	addSubBox(anonbody);
        }
    }

    /**
     * Creates a new <tbody> element that represents an anonymous table body
     * @param doc the document
     * @return the new element
     */
    private static Element createAnonymousBody(Document doc)
    {
        Element div = doc.createElement("tbody");
        div.setAttribute("class", "Xanonymous");
        div.setAttribute("style", "display:table-row-group;");
        return div;
    }
    
    /**
     * Creates a new <col> element that represents an anonymous column
     * @param doc the document
     * @return the new element
     */
    private static Element createAnonymousColumn(Document doc)
    {
        Element div = doc.createElement("col");
        div.setAttribute("class", "Xanonymous");
        div.setAttribute("style", "display:table-column;");
        return div;
    }
    
}
