/**
 * TableBox.java
 * Copyright (c) 2005-2014 Radek Burget
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
 * Created on 29.9.2006, 13:52:23 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.util.*;

import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import cz.vutbr.web.css.*;

/**
 * A box that represents a table.
 * http://www.w3.org/TR/CSS21/tables.html
 * 
 * @author burgetr
 */
public class TableBox extends BlockBox
{
    private static Logger log = LoggerFactory.getLogger(TableBox.class);
    
	private final int DEFAULT_SPACING = 0;
	
    protected TableBodyBox header;
    protected TableBodyBox footer;
    protected Vector<TableBodyBox> bodies;
    protected Vector<TableColumn> columns;
    
    /** total number of columns in the table */
    protected int columnCount;

    /** cell spacing */
    protected int spacing = 0;
    
    /** an anonymous table body (for lines that are not in any other body) */
    private TableBodyBox anonbody;
    
    /** true if the column width have been already calculated */
    private boolean columnsCalculated = false;

    //====================================================================================
    
    /**
     * Create a new table
     */
    public TableBox(Element n, Graphics2D g, VisualContext ctx)
    {
        super(n, g, ctx);
        isblock = true;
    }
    
    /**
     * Create a new table from an inline box
     */
    public TableBox(InlineBox src)
    {
        super(src);
        isblock = true;
    }

    /**
     * Create a new table from a block box
     */
    public TableBox(BlockBox src)
    {
        super(src.el, src.g, src.ctx);
        copyValues(src);
        isblock = true;
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
        loadTableStyle();
        organizeContent(); //organize the child elements according to their display property
    }
	
    @Override
    public boolean doLayout(int widthlimit, boolean force, boolean linestart)
    {
        setAvailableWidth(widthlimit);
        int wlimit = getAvailableContentWidth();
        int maxw = 0;
        int y = 0;

        //calculate the column widths
        calculateColumns();
        
        //layout the bodies
        if (header != null)
        {
        	header.setSpacing(spacing);
            header.doLayout(wlimit, columns);
            header.setPosition(0, y);
            if (header.getWidth() > maxw)
                maxw = header.getWidth();
            y += header.getHeight();
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            TableBodyBox body = it.next();
            body.setSpacing(spacing);
            body.doLayout(wlimit, columns);
            body.setPosition(0, y);
            if (body.getWidth() > maxw)
                maxw = body.getWidth();
            y += body.getHeight();
        }
        if (footer != null)
        {
            footer.setSpacing(spacing);
            footer.doLayout(wlimit, columns);
            footer.setPosition(0, y);
            if (footer.getWidth() > maxw)
                maxw = footer.getWidth();
            y += footer.getHeight();
        }
        content.width = maxw;
        content.height = y;
        setSize(totalWidth(), totalHeight());
        return true;
    }
    
    @Override
    protected void loadSizes(boolean update)
    {
        //load the content width from the attribute (transform to declaration)
        if (!update)
        {
            //create an important 'width' style for this element
            String width = HTMLNorm.getAttribute(getElement(), "width");
            if (!width.equals(""))
            {
                TermLengthOrPercent wspec = HTMLNorm.createLengthOrPercent(width);
                if (wspec != null)
                {
                    Declaration dec = CSSFactory.getRuleFactory().createDeclaration();
                    dec.setProperty("width");
                    dec.unlock();
                    dec.add(wspec);
                    dec.setImportant(true);
                    style.push(dec);
                }
            }
        }
        super.loadSizes(update);
    }

    
    /** 
     * Calculates the widths and margins for the table.
     * @param width the specified width
     * @param exact true if this is the exact width, false when it's a max/min width
     * @param contw containing block width
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    @Override
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);

        //According to CSS spec. 17.4, we should take the size of the original containing box, not the anonymous box
        contw = getContainingBlockBox().getContainingBlock().width;
        
        if (width == null) auto = true;
        if (exact) wset = !auto;
        if (wset && exact && width.isPercentage()) wrelative = true;
        preferredWidth = -1;
        margin.left = margin.right = 0; //margins are provided by the anonymous table box
        
        //if column widths haven't been calculated yet,
        //we can reload everything from scratch
        if (!columnsCalculated) update = false;
        
        if (!wset && !update) //width unknown and the content size unknown
        {
            /* For the first time, we always try to use the maximal width even for the table.
             * That means, the width comes from the parent element. */
            content.width = contw - border.left - padding.left - padding.right - border.right;
        }
        else  //explicitly specified content width
        {
            //load the content width
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, contw);
            //preferred width is derived from the
            preferredWidth = border.left + padding.left + content.width + padding.right + border.right;
        }
    }
    
    @Override
    protected void computeHeightsInFlow(TermLengthOrPercent height, boolean auto, boolean exact, int contw, int conth, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        //According to CSS spec. 17.4, we should take the size of the original containing box, not the anonymous box
        contw = getContainingBlockBox().getContainingBlock().width;
        conth = getContainingBlockBox().getContainingBlock().height;
        
        if (height == null) auto = true; //no value behaves as "auto"
        margin.top = margin.bottom = 0; //margins are provided by the anonymous table box
        
        //compute height when set. If not, it will be computed during the layout
        if (getContainingBlockBox().hasFixedWidth())
        {
            hset = (exact && !auto && height != null);
            if (!update)
                content.height = dec.getLength(height, auto, 0, 0, conth);
        }
        else
        {
            hset = (exact && !auto && height != null && !height.isPercentage());
            if (!update)
                content.height = dec.getLength(height, auto, 0, 0, 0);
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
    
    @Override
    protected void drawChildren(DrawStage turn)
    {
        //Draw only the bodies, ignore the remaining children
        if (header != null)
            header.draw(turn);
        for (TableBodyBox body : bodies)
            body.draw(turn);
        if (footer != null)
            footer.draw(turn);
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
        //System.out.println("wset="+wset);
        //System.out.println("wlimit="+wlimit);
        
        //create the columns that haven't been specified explicitely
        determineColumnCount();
        while (columns.size() < columnCount)
            columns.add(new TableColumn(TableColumn.createAnonymousColumn(getParent().getElement().getOwnerDocument()), g, ctx));
        
        //load the parametres and ensure the minimal column widths
        if (header != null)
            updateColumns(header);
        if (footer != null)
            updateColumns(footer);
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
            updateColumns(it.next());

        /*System.out.println("Start:");
        for (int i = 0; i < columns.size(); i++)
            System.out.println("Col " + i + " : " + columns.elementAt(i).getWidth()
            					+ " min=" + columns.elementAt(i).getMinimalWidth()
            					+ " max=" + columns.elementAt(i).getMaximalWidth()
            					+ " abs=" + columns.elementAt(i).abswidth);*/
        
        //now, the columns are at minimal widths
        //gather column statistics
        int sumabs = 0; //total length of absolute columns
        int sumperc = 0; //total percentage
        int mintotalw = 0;  //total minimal length of all the columns
        int sumnonemin = 0; //total minimal length of the columns with no width specified
        int sumnonemax = 0; //total maximal length of the columns with no width specified
        int totalwperc = 0; //total table width computed from percentage columns
        for (TableColumn col : columns) //compute the sums
        {
            mintotalw += col.getMinimalWidth();
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
                    sumabs += Math.max(col.abswidth, col.getMinimalWidth());
                else
                {
                    sumnonemin += col.getWidth();
                    sumnonemax += col.getMaximalWidth();
                }
            }
        }
        
        //guess the total width available for columns (not including spacing now)
        if (totalwperc > wlimit) totalwperc = wlimit;
        int totalwabs = 0; //from absolute fields
        if (sumabs + sumnonemax > 0)
        {
            int abspart = 100 - sumperc; //the absolute part is how many percent
            totalwabs = (abspart == 0) ? wlimit : (sumabs + sumnonemax) * 100 / abspart; //what is 100%
        }
        int totalw = Math.max(totalwperc, totalwabs); //desired width taken from the columns
        
        //apply the table limits
        if (wset)
        {
            totalw = content.width - (columns.size() + 1) * spacing; //total space obtained from definition
        }
        else
        {
            if (totalw > wlimit)
                totalw = wlimit; //we would not like to exceed the limit
        }
        if (totalw < mintotalw) totalw = mintotalw; //we cannot be below the minimal width
        
        //available for further allocation
        int remain = totalw - mintotalw;
        
        /*System.out.println("Percent: " + totalwperc);
        System.out.println("Abs+%: " + totalwabs);
        System.out.println("Minimum: " + getMinimalWidth());
        System.out.println("wlimit: " + wlimit);
        System.out.println("mintotalw: " + mintotalw);
        System.out.println("result:" + totalw);*/
        
        //set the percentage columns to their values, if possible
        if (remain > 0 && sumperc > 0)
        {
            for (TableColumn col : columns) //set the column sizes
            {
                if (col.wrelative)
                {
                    int mincw = col.getMinimalWidth();
                    int neww = col.percent * totalw / 100;
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= (neww - mincw);
                }
            }
            //TODO remove overallocated space from the last columns?
        }
        //System.out.println("remain2:" + remain + " min:" + remainmin);
        
        //set the absolute columns
        if (remain > 0 && sumabs > 0)
        {
            for (TableColumn col : columns) //set the column sizes
            {
                if (col.wset && !col.wrelative)
                {
                    int mincw = col.getMinimalWidth();
                    int neww = col.abswidth;
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= (neww - mincw);
                }
            }
        }
        //System.out.println("remain3:" + remain + " min:" + remainmin);
        
        //set the remaining columns
        if (remain > 0 && sumnonemin > 0 && sumnonemax > 0)
        {
            int remainmax = sumnonemax;
            remain += sumnonemin; 
            for (TableColumn col : columns) //set the column sizes
            {
                if (!col.wset)
                {
                    int mincw = col.getMinimalWidth();
                    int neww = remain * col.getMaximalWidth() / remainmax;   
                    if (neww < mincw) neww = mincw;
                    col.setColumnWidth(neww);
                    remain -= neww;
                    remainmax -= col.getMaximalWidth();
                    if (remainmax <= 0 || remain <= 0) //the remaining columns have zero width
                        break;
                }
            }
        }
        //System.out.println("remain4:" + remain);

        //if something still remains, use it for fixed columns
        if (remain > 0 && sumabs > 0)
        {
            int remainabs = sumabs;
            for (TableColumn col : columns)
            {
                if (col.wset && !col.wrelative)
                {
                    int addw = remain * col.getMaximalWidth() / remainabs;
                    col.setColumnWidth(col.getWidth() + addw);
                    remain -= addw;
                    remainabs -= col.getMaximalWidth();
                }
            }
        }
        
        //if something still remains, use it for percentage columns
        if (remain > 0 && sumperc > 0 && sumperc < 100)
        {
            int remainperc = sumperc;
            for (TableColumn col : columns)
            {
                if (col.wrelative)
                {
                    int addw = remain * col.percent / remainperc;
                    col.setColumnWidth(col.getWidth() + addw);
                    remain -= addw;
                    remainperc -= col.getMaximalWidth();
                    if (remainperc <= 0 || remain <= 0)
                        break;
                }
            }
        }
        
        //if something still remains, use it for all columns
        if (remain > 0)
        {
            int remaincols = columns.size();
            for (int i = columns.size() - 1; i >= 0; i--)
            {
                TableColumn col = columns.elementAt(i); 
                int addw = remain / remaincols;
                col.setColumnWidth(col.getWidth() + addw);
                remain -= addw;
                remaincols--;
            }
        }
        
        //we are wider that we should be, reduce the widths
        if (remain < 0)
        {
            //non-fixed columns
            if (remain < 0 && sumnonemin > 0)
            {
                int totaldif = 0;
                for (TableColumn col : columns)
                    if (!col.wset)
                        totaldif += col.getWidth() - col.getMinimalWidth();
                
                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.elementAt(i);
                    if (!col.wset)
                    {
                        int dif = col.getWidth() - col.getMinimalWidth();
                        int addw = remain * dif / totaldif; 
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
            //fixed columns
            if (remain < 0 && sumabs > 0)
            {
                int totaldif = 0;
                for (TableColumn col : columns)
                    if (col.wset && !col.wrelative)
                        totaldif += col.getWidth() - col.getMinimalWidth();
                
                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.elementAt(i);
                    if (col.wset && !col.wrelative)
                    {
                        int dif = col.getWidth() - col.getMinimalWidth();
                        int addw = remain * dif / totaldif; 
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
            //percentage columns
            if (remain < 0 && sumperc > 0)
            {
                int totaldif = 0;
                for (TableColumn col : columns)
                    if (col.wrelative)
                        totaldif += col.getWidth() - col.getMinimalWidth();
                
                for (int i = columns.size() - 1; i >= 0 && totaldif > 0; i--)
                {
                    TableColumn col = columns.elementAt(i);
                    if (col.wrelative)
                    {
                        int dif = col.getWidth() - col.getMinimalWidth();
                        int addw = remain * dif / totaldif; 
                        col.setColumnWidth(col.getWidth() + addw);
                        remain -= addw;
                        totaldif -= dif;
                        if (remain >= 0)
                            break;
                    }
                }
            }
        }
        
        /*System.out.println("Result:");
        for (int i = 0; i < columns.size(); i++)
            System.out.println("Col " + i + " : " + columns.elementAt(i).getWidth()
            					+ " min=" + columns.elementAt(i).getMinimalWidth()
            					+ " max=" + columns.elementAt(i).getMaximalWidth());*/
        
        columnsCalculated = true;
    }
    
    @Override
	protected void loadBlockStyle()
	{
		super.loadBlockStyle();
		//Ignore the settings of position and float.
		//These properties are implemented by the containing BlockTableBox
		position = POS_STATIC;
		floating = FLOAT_NONE;
	}

    //====================================================================================

	/**
     * Loads the table-specific features from the style
     */
    private void loadTableStyle()
    {
  		CSSDecoder dec = new CSSDecoder(ctx);
    	//border spacing
  		TermList spc = style.getValue(TermList.class, "border-spacing");
  		if (spc != null)
  		{
  			spacing = dec.getLength((TermLength) spc.get(0), false, DEFAULT_SPACING, 0, 0);
  		}
  		else
  			spacing = dec.getLength(getLengthValue("border-spacing"), false, DEFAULT_SPACING, 0, 0);
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
                if (subbox.getDisplay() == ElementBox.DISPLAY_TABLE_HEADER_GROUP)
                {
                    header = (TableBodyBox) subbox;
                    header.setOwnerTable(this);
                }
                else if (subbox.getDisplay() == ElementBox.DISPLAY_TABLE_FOOTER_GROUP)
                {
                    footer = (TableBodyBox) subbox;
                    footer.setOwnerTable(this);
                }
                else if (subbox.getDisplay() == ElementBox.DISPLAY_TABLE_ROW_GROUP)
                {
                    bodies.add((TableBodyBox) subbox);
                    ((TableBodyBox) subbox).setOwnerTable(this);
                }
                else if (subbox.getDisplay() == ElementBox.DISPLAY_TABLE_COLUMN)
                {
                    for (int i = 0; i < ((TableColumn) subbox).getSpan(); i++)
                    {
                        if (i == 0)
                            columns.add((TableColumn) subbox);
                        else
                            columns.add(((TableColumn) subbox).copyBox());
                    }
                }
                else if (subbox.getDisplay() == ElementBox.DISPLAY_TABLE_COLUMN_GROUP)
                {
                    for (int i = 0; i < ((TableColumnGroup) subbox).getSpan(); i++)
                        columns.add(((TableColumnGroup) subbox).getColumn(i));
                }
                else //other element (usually TABLE_ROW), create the anonymous body for it and continue.
                {
                    if (anonbody == null)
                    {
                        //the table itself may not have an owner document if it is an anonymous box itself
                        //therefore, we're using the parent's owner document
                        Element anonelem = viewport.getFactory().createAnonymousElement(getParent().getElement().getOwnerDocument(), "tbody", "table-row-group"); 
                        anonbody = new TableBodyBox(anonelem, g, ctx);
                        anonbody.adoptParent(this);
                        anonbody.setStyle(viewport.getFactory().createAnonymousStyle("table-row-group"));
                        anonbody.setOwnerTable(this);
                        bodies.add(anonbody);
                    }
                    anonbody.addSubBox(subbox);
                    anonbody.isempty = false;
                    subbox.setContainingBlockBox(anonbody);
                    subbox.setParent(anonbody);
                    it.remove();
                    endChild--;
                }
            }
        }
        if (anonbody != null)
        {
        	anonbody.endChild = anonbody.nested.size();
        	addSubBox(anonbody);
        }
    }

    
}
