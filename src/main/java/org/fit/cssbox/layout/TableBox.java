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

import java.util.Iterator;
import java.util.Vector;

import org.fit.cssbox.css.HTMLNorm;
import org.w3c.dom.Element;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;

/**
 * A box that represents a table.
 * http://www.w3.org/TR/CSS21/tables.html
 * 
 * @author burgetr
 */
public class TableBox extends BlockBox
{
    //private static Logger log = LoggerFactory.getLogger(TableBox.class);
    
	private final float DEFAULT_SPACING = 0;
	
    protected TableBodyBox header;
    protected TableBodyBox footer;
    protected Vector<TableBodyBox> bodies;
    protected Vector<TableColumn> columns;
    
    /** total number of columns in the table */
    protected int columnCount;

    /** cell spacing */
    protected float spacing = 0;
    
    /** an anonymous table body (for lines that are not in any other body) */
    private TableBodyBox anonbody;
    
    /** true if the column width have been already calculated */
    private boolean columnsCalculated = false;

    //====================================================================================
    
    /**
     * Create a new table
     */
    public TableBox(Element n, VisualContext ctx)
    {
        super(n, ctx);
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
        super(src.el, src.ctx);
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

    /** @return the header body, or {@code null} if none */
    public TableBodyBox getHeader() { return header; }

    /** @return the footer body, or {@code null} if none */
    public TableBodyBox getFooter() { return footer; }

    /** @return the list of body sections */
    public Vector<TableBodyBox> getBodies() { return bodies; }

    /** @return the list of columns */
    public Vector<TableColumn> getColumns() { return columns; }

    /** @return the cell spacing value in pixels */
    public float getSpacing() { return spacing; }

    /**
     * Marks the column widths as already calculated.
     * Called by {@link TableLayoutManager} after {@code calculateColumns()} completes.
     */
    void markColumnsCalculated() { columnsCalculated = true; }

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
        propagateCellSpacing(spacing);
    }

    @Override
    public void initLayoutManager()
    {
        layoutManager = new TableLayoutManager(this);
    }

    @Override
    public boolean doLayout(float widthlimit, boolean force, boolean linestart)
    {
        setAvailableWidth(widthlimit);
        ((TableLayoutManager) layoutManager).performTableLayout(this);
        return true;
    }
    
    @Override
    protected void loadSizes(boolean update)
    {
        //load the content width from the attribute (transform to declaration)
        if (!update)
        {
            //create an important 'width' and 'height' styles for this element
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
            String height = HTMLNorm.getAttribute(getElement(), "height");
            if (!height.equals(""))
            {
                TermLengthOrPercent hspec = HTMLNorm.createLengthOrPercent(height);
                if (hspec != null)
                {
                    Declaration dec = CSSFactory.getRuleFactory().createDeclaration();
                    dec.setProperty("height");
                    dec.unlock();
                    dec.add(hspec);
                    dec.setImportant(true);
                    style.push(dec);
                }
            }
            //TODO the table height is not applied yet
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
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, float contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);

        if (width == null) auto = true;
        if (exact) wset = !auto;
        if (wset && exact && width.isPercentage()) wrelative = true;
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
            //According to CSS spec. 17.4, percentage widths should use the size of the original containing box, not the anonymous box
            float fullw = getContainingBlockBox().getContainingBlock().width;
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, fullw);
        }
    }
    
    @Override
    protected void computeHeightsInFlow(TermLengthOrPercent height, boolean auto, boolean exact, float contw, float conth, boolean update)
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
    protected float getMaximalContentWidth()
    {
        float ret = 0;
        if (header != null)
        {
            float m = header.getMaximalWidth();
            if (m > ret) ret = m;
        }
        if (footer != null)
        {
            float m = footer.getMaximalWidth();
            if (m > ret) ret = m;
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            float m = it.next().getMaximalWidth();
            if (m > ret) ret = m;
        }
        return ret;
    }

    @Override
    protected float getMinimalContentWidth()
    {
        float ret = 0;
        if (header != null)
        {
            float m = header.getMinimalWidth();
            if (m > ret) ret = m;
        }
        if (footer != null)
        {
            float m = footer.getMinimalWidth();
            if (m > ret) ret = m;
        }
        for (Iterator<TableBodyBox> it = bodies.iterator(); it.hasNext(); )
        {
            float m = it.next().getMinimalWidth();
            if (m > ret) ret = m;
        }
        return ret;
    }
    
    @Override
    protected float getMinimalDecorationWidth()
    {
        if (wset)
            return super.getMinimalDecorationWidth();
        else
            return getMinimalWidth(); //all the content is considered for tables
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
                        anonbody = new TableBodyBox(anonelem, ctx);
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

    private void propagateCellSpacing(float spacing)
    {
        if (header != null)
            header.setSpacing(spacing);
        for (TableBodyBox body : bodies)
            body.setSpacing(spacing);
        if (footer != null)
            footer.setSpacing(spacing);
    }
    
}
