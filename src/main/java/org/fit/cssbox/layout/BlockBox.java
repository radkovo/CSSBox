/*
 * BlockBox.java
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
 * Created on 5. �nor 2006, 13:40
 */

package org.fit.cssbox.layout;

import java.util.Iterator;
import java.util.List;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.Clip;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermRect;

import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


/**
 * A box corresponding to a block element
 *
 * @author  radek
 */
public class BlockBox extends ElementBox
{
    private static Logger log = LoggerFactory.getLogger(BlockBox.class);
    
    public static final CSSProperty.Float FLOAT_NONE = CSSProperty.Float.NONE;
    public static final CSSProperty.Float FLOAT_LEFT = CSSProperty.Float.LEFT;
    public static final CSSProperty.Float FLOAT_RIGHT = CSSProperty.Float.RIGHT;
    
    public static final CSSProperty.Clear CLEAR_NONE = CSSProperty.Clear.NONE;
    public static final CSSProperty.Clear CLEAR_LEFT = CSSProperty.Clear.LEFT;
    public static final CSSProperty.Clear CLEAR_RIGHT = CSSProperty.Clear.RIGHT;
    public static final CSSProperty.Clear CLEAR_BOTH = CSSProperty.Clear.BOTH;
    
    public static final CSSProperty.TextAlign ALIGN_LEFT = CSSProperty.TextAlign.LEFT;
    public static final CSSProperty.TextAlign ALIGN_RIGHT = CSSProperty.TextAlign.RIGHT;
    public static final CSSProperty.TextAlign ALIGN_CENTER = CSSProperty.TextAlign.CENTER;
    public static final CSSProperty.TextAlign ALIGN_JUSTIFY = CSSProperty.TextAlign.JUSTIFY;
    
    public static final CSSProperty.Overflow OVERFLOW_VISIBLE = CSSProperty.Overflow.VISIBLE;
    public static final CSSProperty.Overflow OVERFLOW_HIDDEN = CSSProperty.Overflow.HIDDEN;
    public static final CSSProperty.Overflow OVERFLOW_SCROLL = CSSProperty.Overflow.SCROLL;
    public static final CSSProperty.Overflow OVERFLOW_AUTO = CSSProperty.Overflow.AUTO;
    
    public static final CSSProperty.BoxSizing CONTENT_BOX = CSSProperty.BoxSizing.CONTENT_BOX;
    public static final CSSProperty.BoxSizing BORDER_BOX = CSSProperty.BoxSizing.BORDER_BOX;
    
    /** the minimal width of the space between the floating blocks that
     * can be used for placing the in-flow content */
    protected static final float INFLOW_SPACE_THRESHOLD = 15;
    
    /** Does this box contain blocks? */
    protected boolean contblock;
    
    /** Contains any in-flow boxes? */
    protected boolean anyinflow;
    
    /** Floating boxes left */
    protected FloatList fleft;
    
    /** Floating boxes right */
    protected FloatList fright;

    /** If we are a floating box, in which FloatList we are placed. */
    protected FloatList fown;

    /** x coordinate of the content box relative to the content box of the
     * top owner of the FloatList (from left)  */
    protected float floatXl;
    
    /** x coordinate of the content box relative to the content box of the
     * top owner of the FloatList (from right) */
    protected float floatXr;
    
    /** y coordinate of the content box relative to the content box of the 
        top owner of the FloatList */
    protected float floatY;
    
    /** Minimal size of the box. Values of -1 mean 'not set'. */
    protected Dimension min_size;
    
    /** Maximal size of the box. Values of -1 mean 'not set'. */
    protected Dimension max_size;
    
    /** Initialized first line of the box or null when it was not initialized */
    protected LineBox firstLine;
    
    //============================== Width computing ======================
    
    /** true if the box width has been set explicitly */
    protected boolean wset;
    
    /** true if the box height has been set explicitly */
    protected boolean hset;
    
    /** true if the width is relative [%] */
    protected boolean wrelative;
    
    /** true if the left margin is set to auto */
    protected boolean mleftauto;
    
    /** true if the right margin is set to auto */
    protected boolean mrightauto;
    
    /** Width adjustment in pixels. The computed width is adjusted by the given value
     * automatically when updateSizes() is called. This value should be set by setWidthAdjust()
     * and it is reset to zero when loadSizes() is called. */
    protected float widthAdjust;
    
    /** True means that the layout inside has been already computed
     * and the width shouldn't be changed anymore */
    protected boolean widthComputed = false;
    
    /** Originally declared margin. This property saves the original values
     * where the efficient left and right margin may
     * be computed from the containing box */
    protected LengthSet declMargin; 
    
    //============================== Computed style ======================
    
    /** Floating property */
    protected CSSProperty.Float floating;
    
    /** Clearing property */
    protected CSSProperty.Clear clearing;
    
    /** Overflow-X property */
    protected CSSProperty.Overflow overflowX;
        
    /** Overflow-Y property */
    protected CSSProperty.Overflow overflowY;
    
    /** Box-sizing property */
    protected CSSProperty.BoxSizing boxSizing;
        
    /** the left position should be set to static position during the layout */
    protected boolean leftstatic;
    
    /** the top position should be set to static position during the layout */
    protected boolean topstatic;
    
    /** Reference box for absolutely positioned boxes. It is used
     * when some of the absolute coordinates are based on the static position */
    protected Box absReference;
    
    /** Parent box according to DOM. It's used for absolutely positioned boxes
     * where the actual parent box is determined in different way. */
    protected ElementBox domParent;
    
    /** Text-align property */
    protected CSSProperty.TextAlign align;

    /** Text indentation in pixels */
    protected float indent;
    
    /** Clipping region specified using the clip: property (with absolute coordinates) */
    protected TermRect clipRegion;
    
    //=====================================================================
    
    /** Creates a new instance of BlockBox */
    public BlockBox(Element n, VisualContext ctx)
    {
        super(n, ctx);
        
        isblock = true;
        contblock = false;
        anyinflow = false;
        
        fleft = null;
        fright = null;
        fown = null;
        floatXl = 0;
        floatXr = 0;
        floatY = 0;
        firstLine = null;
        
        floating = FLOAT_NONE;
        clearing = CLEAR_NONE;
        overflowX = OVERFLOW_VISIBLE;
        overflowY = OVERFLOW_VISIBLE;
        boxSizing = CONTENT_BOX;
        align = ALIGN_LEFT;
        indent = 0;
        clipRegion = null;
        
        topstatic = false;
        leftstatic = false;
        widthAdjust = 0;
        
      	if (style != null)
      		loadBlockStyle();
    }

    /** Convert an inline box to a block box */
    public BlockBox(InlineBox src)
    {
        super(src.el, src.ctx);
        
        viewport = src.viewport;
        clipblock = src.clipblock;
        isblock = true;
        contblock = false;
        anyinflow = false;
        
        fleft = null;
        fright = null;
        fown = null;
        floatXl = 0;
        floatXr = 0;
        floatY = 0;
        
        floating = FLOAT_NONE;
        clearing = CLEAR_NONE;
        position = src.position;
        overflowX = OVERFLOW_VISIBLE;
        overflowY = OVERFLOW_VISIBLE;
        boxSizing = CONTENT_BOX;
        align = ALIGN_LEFT;
        indent = 0;
        clipRegion = null;

        topset = src.topset;
        leftset = src.leftset;
        bottomset = src.bottomset;
        rightset = src.rightset;
        topstatic = false;
        leftstatic = false;
        widthAdjust = 0;
        
        nested = src.nested;
        startChild = src.startChild;
        endChild = src.endChild;
        
        setStyle(src.getStyle());
    }
    
    public void copyValues(BlockBox src)
    {
        super.copyValues(src);
        contblock = src.contblock;
        anyinflow = src.anyinflow;
        setFloats(src.fleft, src.fright, src.floatXl, src.floatXr, src.floatY);
        fown = src.fown;
        floating = src.floating;
        clearing = src.clearing;
        overflowX = src.overflowX;
        overflowY = src.overflowY;
        boxSizing = src.boxSizing;
        align = src.align;
        indent = src.indent;
        topstatic = src.topstatic;
        leftstatic = src.leftstatic;
        domParent = src.domParent;
        if (src.declMargin != null)
        	declMargin = new LengthSet(src.declMargin);
        clipRegion = src.clipRegion;
    }
    
    @Override
    public BlockBox copyBox()
    {
        BlockBox ret = new BlockBox(el, ctx);
        ret.copyValues(this);
        return ret;
    }

    @Override
    public void initBox()
    {
        setFloats(new FloatList(this), new FloatList(this), 0, 0, 0);
    }

    @Override
    public void addSubBox(Box box)
    {
        super.addSubBox(box);
        if (box.isInFlow())
        {
            anyinflow = true;
            if (box.isBlock())
                contblock = true;
        }
    }

    @Override
    public void setStyle(NodeData s)
    {
    	super.setStyle(s);
    	loadBlockStyle();
    }
    
    @Override
    public String toString()
    {
        return "<" + el.getTagName() + " id=\"" + HTMLNorm.getAttribute(el, "id") + 
               "\" class=\""  + HTMLNorm.getAttribute(el ,"class") + "\">";
    }
    
    //========================================================================
    
    @Override
    public boolean mayContainBlocks()
    {
    	return true;
    }
    
    public boolean containsBlocks()
    {
        return contblock;
    }
    
    public void setFloats(FloatList left, FloatList right, float xl, float xr, float y)
    {
        fleft = left;
        fright = right;
        floatXl = xl;
        floatXr = xr;
        floatY = y;
    }
    
    public void setOwnerFloatList(FloatList list)
    {
    	fown = list;
    }

    public FloatList getOwnerFloatList()
    {
    	return fown;
    }
    
    public CSSProperty.Float getFloating()
    {
        return floating;
    }

    public String getFloatingString()
    {
        return floating.toString();
    }

    public CSSProperty.Clear getClearing()
    {
        return clearing;
    }
    
    public String getClearingString()
    {
        return clearing.toString();
    }
    
    public CSSProperty.Overflow getOverflowX()
    {
        return overflowX;
    }
    
    public CSSProperty.Overflow getOverflowY()
    {
        return overflowY;
    }
    
    public String getOverflowXString()
    {
        return overflowX.toString();
    }
    
    public String getOverflowYString()
    {
        return overflowY.toString();
    }
    
    public float getFloatY()
    {
        return floatY;
    }
    
    /** Returns true if the box is in the normal text flow (not absolutely
     * positioned nor floating) */
    @Override
    public boolean isInFlow()
    {
        return (displayed && floating == FLOAT_NONE && position != POS_ABSOLUTE && position != POS_FIXED);
    }
    
    /** Returns true if the box is absolutely positioned. */
    public boolean isPositioned()
    {
        return (displayed && (position == POS_ABSOLUTE || position == POS_FIXED));
    }
    
    /** Returns true if the box is floating. */
    public boolean isFloating()
    {
        return (displayed && floating != FLOAT_NONE);
    }
    
	@Override
    public boolean containsFlow()
	{
		return anyinflow;
	}
	
    @Override
    public boolean isWhitespace()
    {
        if (anyinflow || encloseFloats())
            return super.isWhitespace();
        else
            return true;
    }

    @Override
	public boolean marginsAdjoin()
	{
    	if (padding.top > 0 || padding.bottom > 0 ||
       		border.top > 0 || border.bottom > 0)
    	{
    		//margins are separated by padding or border
    		return false;
    	}
    	else if (min_size.height > 0)
    	{
    		//when minimal height is not zero, the margins are not adjoining
    		return false;
    	}
    	else if (hset)
    	{
    		//when the height is fixed to zero, margins are adjoining
    		return content.height == 0;
    	}
    	else
    	{
    		//margins can be separated by contents
	        for (int i = startChild; i < endChild; i++)
	        {
	        	Box box = getSubBox(i);
	        	if (box instanceof ElementBox) //all child boxes must have adjoining margins
	        	{
	        		if (!((ElementBox) box).marginsAdjoin())
	        			return false;
	        	}
	        	else
	        	{
	        		if (!box.isWhitespace()) //text boxes must be whitespace
	        			return false;
	        	}
	        }
	        return true;
    	}
	}

    /** Returns true if the element displays at least something */
    @Override
    public boolean affectsDisplay()
    {
        boolean ret = containsFlow();
        //non-zero top or left border
        if (border.top > 0 || border.bottom > 0)
            ret = true;
        //the same with padding
        if (padding.top > 0 || padding.bottom > 0)
            ret = true;
        
        return ret;
    }
	
    @Override
    public boolean canSplitInside()
    {
        return false;
    }

    @Override
    public boolean canSplitBefore()
    {
        return true;
    }

    @Override
    public boolean canSplitAfter()
    {
        return true;
    }

    @Override
    public boolean startsWithWhitespace()
    {
        return false;
    }

    @Override
    public boolean endsWithWhitespace()
    {
        return false;
    }
    
    @Override
    public void setIgnoreInitialWhitespace(boolean b)
    {
    }

    /**
     * Checks if the width of the block may be increase to enclose the children. This is true for special blocks
     * only such as table cells.
     * @return <code>true</code> if the width may be increased
     */
    public boolean canIncreaseWidth()
    {
        return false;
    }
    
    /**
     * Checks whether the block should enclose all the floating children as well.
     * See http://www.w3.org/TR/CSS21/visudet.html#root-height
     * @return <code>true</code> if the floats should enclosed
     */
    protected boolean encloseFloats()
    {
        return overflowX != OVERFLOW_VISIBLE || floating != FLOAT_NONE || position == POS_ABSOLUTE || position == POS_FIXED || display == ElementBox.DISPLAY_INLINE_BLOCK;
    }
    
    /**
     * Checks whether the block bounds may overlap floating element bounds when the block is in flow.
     * See http://www.w3.org/TR/CSS22/visuren.html#bfc-next-to-float
     * @return {@code true} when the block bounds may overlap floating block bounds
     */
    protected boolean mayOverlapFloats()
    {
        return overflowX == OVERFLOW_VISIBLE;
    }
    
    /**
     * Obtains the reference box for absolutely positioned boxes. It is used
     * when some of the absolute coordinates are based on the static position.
     */
    public Box getAbsReference()
    {
        return absReference;
    }
    
    /**
     * Obtains the parent box according to DOM. It's used for absolutely positioned
     * boxes where the actual parent box obtained using {@link Box#getParent()} is 
     * determined in a different way.
     */
    public ElementBox getDomParent()
    {
        return domParent;
    }
    
    /**
     * Sets the width of the content while considering the min- and max- width.
     * @param width the width the set if possible
     */
    public void setContentWidth(float width)
    {
        float w = width;
        if (max_size.width != -1 && w > max_size.width)
            w = max_size.width;
        if (min_size.width != -1 && w < min_size.width)
            w = min_size.width;
        content.width = w;
    }
    
    /**
     * Sets the height of the content while considering the min- and max- height.
     * @param height the height the set if possible
     */
    public void setContentHeight(float height)
    {
        float h = height;
        if (max_size.height != -1 && h > max_size.height)
            h = max_size.height;
        if (min_size.height != -1 && h < min_size.height)
            h = min_size.height;
        content.height = h;
    }
    
    /**
     * Adjusts the content width by the given value. This value is later automatically applied when
     * {@link #updateSizes()} is called and it is reset to zero when {@link #loadSizes()} is called. This function has no
     * effect when the width is explicitly set (i.e. it is not {@code auto}).
     * @param adjust the value to be added to the width
     */
    public void setWidthAdjust(float adjust)
    {
        if (!wset)
            setContentWidth(getContentWidth() - widthAdjust + adjust);
        widthAdjust = adjust;
    }
    
    @Override
    public float totalHeight()
    {
        if (border.top == 0 && border.bottom == 0 &&
            padding.top == 0 && padding.bottom == 0 &&
            content.height == 0) /* no content - margin collapsing applies */
            return Math.max(emargin.top, emargin.bottom);
        else
            return emargin.top + border.top + padding.top + content.height +
                padding.bottom + border.bottom + emargin.bottom;
    }
   
    /**
     * Obtains the first line indentation defined by the text-indent property.
     * @return indentation in pixels
     */
    public float getIndent()
    {
        return indent;
    }

    /**
     * Returns the text alignment setting of this block box.
     * @return the text-align property value
     */
    public CSSProperty.TextAlign getTextAlign()
    {
        return align;
    }
    
    @Override
    public Rectangle getContainingBlock()
    {
        if (position == POS_FIXED)
            return viewport.getVisibleRect();
        else if (position == POS_ABSOLUTE)
            return cbox.getPaddingBounds();
        else
            return super.getContainingBlock();
    }
    
    @Override
    public Rectangle getAbsoluteContainingBlock()
    {
        if (position == POS_FIXED)
            return viewport.getVisibleRect();
        else if (position == POS_ABSOLUTE)
            return cbox.getAbsolutePaddingBounds();
        else
            return super.getAbsoluteContainingBlock();
    }
    
   //========================================================================
    
    /** 
    /**
     * Computes the efficient sizes of in-flow margins for collapsing
     */
    @Override
    public void computeEfficientMargins()
    {
        //minimal margins
        emargin.top = margin.top;
        emargin.bottom = margin.bottom;
        //check if something inside can be collapsed and it is larger
        if (containsBlocks() && containsFlow())
        {
            BlockBox firstseparated = null;
            float mbottom = 0;
            
            for (int i = 0; i < getSubBoxNumber(); i++)
            {
                BlockBox subbox = (BlockBox) getSubBox(i);
                if (subbox.isDisplayed() && subbox.isInFlow())
                {
                    subbox.computeEfficientMargins();
                    boolean boxempty = subbox.marginsAdjoin();
                    
                    //if no separated box yet, update the top margin
                    if (firstseparated == null && !separatedFromTop(this)) 
                    {
                    	if (subbox.emargin.top > emargin.top)
                    		emargin.top = subbox.emargin.top;
                    }
                    
                    //update the bottom margin
                    if (boxempty)
                    {
                        float m = Math.max(subbox.emargin.top, subbox.emargin.bottom); //margins adjoin: we may use both top or bottom
                    	if (m > mbottom)
                    		mbottom = m;
                    }
                    else
                    	mbottom = subbox.emargin.bottom;
                    
                    if (!boxempty && firstseparated == null)
                        firstseparated = subbox;
                }
            }

            //collapse bottom margins
            if (mbottom > emargin.bottom && !separatedFromBottom(this))
            	emargin.bottom = mbottom;
        }
        
        //if the box is empty, collapse it to a single margin
        if (marginsAdjoin())
        {
        	emargin.top = Math.max(emargin.top, emargin.bottom);
        	emargin.bottom = 0;
        }
        
    }
    
    /** Layout the sub-elements.
     * @param availw Maximal width available to the child elements
     * @param force Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return <code>true</code> if the box has been succesfully placed
     */
    /**
     * Selects and assigns the layout manager based on the box contents.
     * Block boxes containing only inline children get an {@link InlineLayoutManager};
     * block boxes containing block-level children get a {@link BlockLayoutManager}.
     */
    @Override
    public void initLayoutManager()
    {
        if (contblock)
            layoutManager = new BlockLayoutManager(this);
        else
            layoutManager = new InlineLayoutManager(this);
    }

    @Override
    public boolean doLayout(float availw, boolean force, boolean linestart)
    {
    	//if (getElement() != null && getElement().getAttribute("id").equals("gbzc"))
    	//	System.out.println("jo!");
        //Skip if not displayed
        if (!displayed)
        {
            content.setSize(0, 0);
            bounds.setSize(0, 0);
            return true;
        }

        //remove previously splitted children from possible previous layout
        clearSplitted();

        //shrink-to-fit when the width is not given by containing box or specified explicitly
        if (!hasFixedWidth())
        {
            //float min = getMinimalContentWidthLimit();
            float min = Math.max(getMinimalContentWidthLimit(), layoutManager.getMinimalContentWidth());
            float max = layoutManager.getMaximalContentWidth();
            float availcont = availw - emargin.left - border.left - padding.left - emargin.right - border.right - padding.right;
            //float pref = Math.min(max, availcont);
            //if (pref < min) pref = min;
            float pref = Math.min(Math.max(min, availcont), max);
            setContentWidth(pref);
            updateChildSizes();
        }

        //the width should be fixed from this point
        widthComputed = true;

        /* Always try to use the full width. If the box is not in flow, its width
         * is updated after the layout */
        setAvailableWidth(totalWidth());

        layoutManager.layout(availw, force, linestart);

        //always fits as well as possible
        return true;
    }

    public void initFirstLine(ElementBox box)
    {
        if (firstLine == null)
            firstLine = new LineBox(this, 0, 0);
        firstLine.considerBoxProperties(box);
        //recursively apply to the first in-flow box, when it is a block box
        for (int i = startChild; i < endChild; i++)
        {
            Box child = getSubBox(i);
            if (child.isInFlow())
            {
                if (child.isBlock())
                    ((BlockBox) child).initFirstLine(box);
                break;
            }
        }
    }
    
    @Override
    protected void computeAbsolutePosition()
    {
        //my top left corner
        final Rectangle cblock = getAbsoluteContainingBlock();
        float x = cblock.x + bounds.x;
        float y = cblock.y + bounds.y;

        if (floating == FLOAT_NONE)
        {
            if (position == POS_RELATIVE)
            {
                x += leftset ? coords.left : (-coords.right);
                y += topset ? coords.top : (-coords.bottom);
            }
            else if (position == POS_ABSOLUTE || position == POS_FIXED)
            {
                if (topstatic || leftstatic)
                {
                    updateStaticPosition();
                }
                x = cblock.x + coords.left;
                y = cblock.y + coords.top;
                //if fixed, update the position by the viewport visible offset
                if (position == POS_FIXED && getContainingBlockBox() instanceof Viewport)
                {
                    x += ((Viewport) getContainingBlockBox()).getVisibleRect().x;
                    y += ((Viewport) getContainingBlockBox()).getVisibleRect().y;
                }
            }
        }
        else if (floating == FLOAT_LEFT)
        {
        	BlockBox listowner = fown.getOwner();
            x = listowner.getAbsoluteContentX() + bounds.x;
            y = listowner.getAbsoluteContentY() + bounds.y;
        }
        else if (floating == FLOAT_RIGHT)
        {
        	BlockBox listowner = fown.getOwner();
            x = listowner.getAbsoluteContentX() + listowner.getContentWidth() - bounds.width - bounds.x;
            y = listowner.getAbsoluteContentY() + bounds.y;
        }

        //set the absolute coordinates
        absbounds.x = x;
        absbounds.y = y;

        //update the width and height according to overflow of the cblock
        absbounds.width = bounds.width;
        absbounds.height = bounds.height;

        if (isVisible())
        {
            if (clipblock == viewport)
                viewport.updateBoundsFor(absbounds);
            else
                viewport.updateBoundsFor(getClippedBounds());
        }
    }
    
    /** Obtains the static position of an absolutely positioned box
     * from the reference box (if any) */
    private void updateStaticPosition()
    {
        if (topstatic || leftstatic)
        {
            ElementBox cblock = getContainingBlockBox();
            if (absReference != null)
            {
                //compute the bounds of the reference box relatively to our containing block
                final Rectangle cb = getContainingBlockBox().getAbsoluteBounds();
                //position relatively to the border edge
                if (topstatic)
                {
                    Rectangle ab = new Rectangle(absReference.getAbsoluteBounds());
                    ab.x = ab.x - cb.x;
                    ab.y = ab.y - cb.y;
                    if (!absReference.isblock || ((BlockBox) absReference).getFloating() == FLOAT_NONE) //not-floating boxes: place below
                        coords.top = ab.y + ab.height - cblock.emargin.top - cblock.border.top;
                    else //floating blocks: place top-aligned
                        coords.top = ab.y - cblock.emargin.top - cblock.border.top;
                }
                if (leftstatic)
                {
                    final ElementBox refcblock = absReference.getContainingBlockBox();
                    if (refcblock != null)
                        coords.left = refcblock.getAbsoluteContentBounds().x - cb.x - cblock.emargin.left - cblock.border.left;
                    else
                        coords.left = 0;
                }
                //the reference box position may be computed later: require recomputing
                viewport.requireRecomputePositions();
            }
            else if (domParent != null) //no reference box, we are probably the first box in our parent
            {
                //find the nearest DOM parent that is part of our box tree
                ElementBox dparent = domParent; 
                while (dparent != null && dparent.getContainingBlockBox() == null)
                    dparent = dparent.getParent();
                //compute the bounds of the reference box relatively to our containing block
                Rectangle ab = new Rectangle(dparent.getAbsoluteContentBounds());
                Rectangle cb = cblock.getAbsoluteBounds();
                ab.x = ab.x - cb.x;
                ab.y = ab.y - cb.y;
                //position relatively to the border edge
                if (topstatic)
                {
                    coords.top = ab.y - cblock.emargin.top - cblock.border.top;
                }
                if (leftstatic)
                {
                    coords.left = ab.x - cblock.emargin.left - cblock.border.left;
                }
                //the reference box position may be computed later: require recomputing
                viewport.requireRecomputePositions();
            }
            else //nothing available, this should not happen
            {
                log.warn("No static position available for " + this.toString());
                //use containing block as a fallback
                if (topstatic)
                    coords.top = cblock.padding.top;
                if (leftstatic)
                    coords.left = cblock.padding.left;
            }
        }
    }

    @Override
    public float getAvailableContentWidth()
    {
        float ret = availwidth - margin.left - border.left - padding.left 
                  - padding.right - border.right - margin.right;
        if (max_size.width != -1 && ret > max_size.width)
            ret = max_size.width;
        return ret;
    }
    
    @Override
    public float getMinimalWidth()
    {
        float ret = 0;
        //if the width is set or known implicitely, return the width
        if (wset && !wrelative)
            ret = content.width;
        //return the maximum of the nested minimal widths
        else
            ret = getMinimalContentWidth();
        //check against the maximal and minimal widths
        if (max_size.width != -1 && ret > max_size.width)
            ret = max_size.width;
        if (min_size.width != -1 && ret < min_size.width)
            ret = min_size.width;
        //increase by margin, padding, border
        ret += declMargin.left + padding.left + border.left +
               declMargin.right + padding.right + border.right;
        return ret;
    }

    /**
     * Computes the minimal content width by delegating to the layout manager.
     * The actual algorithm lives in {@link BlockLayoutManager#getMinimalContentWidth()}
     * and {@link InlineLayoutManager#getMinimalContentWidth()}.
     */
    protected float getMinimalContentWidth()
    {
        if (layoutManager != null)
            return layoutManager.getMinimalContentWidth();
        return 0;
    }

    /**
     * Get the minimal width of the margins, borders and paddings. The box content width
     * is used only when it is explicitly specified.
     * @return
     */
    protected float getMinimalDecorationWidth()
    {
        float ret = 0;
        //if the width is set or known implicitely, return the width
        if (wset)
            ret = content.width;
        //check against the maximal and minimal widths
        if (max_size.width != -1 && ret > max_size.width)
            ret = max_size.width;
        if (min_size.width != -1 && ret < min_size.width)
            ret = min_size.width;
        //increase by margin, padding, border
        ret += declMargin.left + padding.left + border.left +
               declMargin.right + padding.right + border.right;
        return ret;
    }
    
    @Override
    public float getMaximalWidth()
    {
        float ret;
        //if the width is set or known implicitely, return the width
        if (wset && !wrelative)
            ret = content.width;
        else
            ret = getMaximalContentWidth();
        //check against the maximal and minimal widths
        if (max_size.width != -1 && ret > max_size.width)
            ret = max_size.width;
        if (min_size.width != -1 && ret < min_size.width)
            ret = min_size.width;
        //increase by margin, padding, border
        ret += declMargin.left + padding.left + border.left +
               declMargin.right + padding.right + border.right;
        return ret;
    }

    /**
     * Computes the maximal content width by delegating to the layout manager.
     * The actual algorithm lives in {@link BlockLayoutManager#getMaximalContentWidth()}
     * and {@link InlineLayoutManager#getMaximalContentWidth()}.
     */
    protected float getMaximalContentWidth()
    {
        if (layoutManager != null)
            return layoutManager.getMaximalContentWidth();
        return 0;
    }
    
    /**
     * Determines the minimal width as it is limited by the ancestors (for in-flow boxes)
     * or by the explicit setting of <code>width</code> or <code>min-width</code> properties.
     * @return the minimal width
     */ 
    public float getMinimalContentWidthLimit()
    {
    	float ret;
    	float dif = declMargin.left + padding.left + border.left +
               	  declMargin.right + padding.right + border.right;
    	if (wset)
    		ret = content.width;
    	else if (min_size.width != -1)
    		ret = min_size.width;
    	else if (isInFlow())
    		ret = ((BlockBox) getContainingBlockBox()).getMinimalContentWidthLimit() - dif;
    	else
    		ret = 0;
    		
    	return ret;
    }
    
    /**
     * Recursively finds the baseline of the first in-flow inline box.
     * @return The baseline offset in the element content or -1 if there are no in-flow boxes.
     */
    public float getFirstInlineBoxBaseline()
    {
        return recursiveGetFirstInlineBoxBaseline(this);
    }
    
    protected float recursiveGetFirstInlineBoxBaseline(ElementBox root)
    {
        //find the first in-flow box
        Box box = null;
        for (int i = root.startChild; i < root.endChild; i++)
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
                return box.getContentY() + ((Inline) box).getBaselineOffset();
            else
                return box.getContentY() + recursiveGetFirstInlineBoxBaseline((ElementBox) box);
        }
        else
            return -1; //no inline box found
    }
    
    /**
     * @return true if the width is specified relatively
     */
    public boolean isRelative()
    {
        return wrelative;
    }

	@Override
	public boolean hasFixedWidth()
	{
	    return wset //width set explicitly 
	            || isInFlow() //in flow boxes fixed to their containing block
	            || (isPositioned() && !mleftauto && !mrightauto && leftset && rightset); //positioned blocks with auto width but fixed on both sides 
	}

	@Override
	public boolean hasFixedHeight()
	{
		return hset; //only true if the height is set explicitly
	}

    /**
     * Computes the maximal height of the floating boxes inside of this box and
     * all its children.
     * @return the total height of the floating boxes
     */
    public float getFloatHeight()
    {
        if (fleft != null && fright != null)
        {
            float mfy = Math.max(fleft.getMaxYForOwner(this, true), fright.getMaxYForOwner(this, true));
            if (this.containsBlocks())
            {
                for (int i = 0; i < getSubBoxNumber(); i++)
                {
                    Box subbox = getSubBox(i);
                    if (subbox instanceof BlockBox && !((BlockBox) subbox).isPositioned()) //do not consider positioned boxes
                    {
                        float cmfy = ((BlockBox) subbox).getFloatHeight();
                        if (cmfy > mfy) mfy = cmfy;
                    }
                }
            }
            return mfy;
        }
        else
            return 0;
    }
    
    @Override
    public void draw(DrawStage turn)
    {
        if (isDisplayed() && isDeclaredVisible())
        {
            if (!this.formsStackingContext())
            {
                switch (turn)
                {
                    case DRAW_NONINLINE:
                        if (floating == FLOAT_NONE)
                        {
                            if (isVisible())
                                getViewport().getRenderer().renderElementBackground(this);
                            drawChildren(turn);
                        }
                        break;
                    case DRAW_FLOAT:
                        if (floating != FLOAT_NONE)
                        {
                            if (isVisible())
                                getViewport().getRenderer().renderElementBackground(this);
                            drawStackingContext(true);
                        }
                        else
                            drawChildren(turn);
                        break;
                    case DRAW_INLINE:
                        //do nothing but check the children
                        if (floating == FLOAT_NONE)
                        {
                            getViewport().getRenderer().startElementContents(this);
                            drawChildren(turn);
                            getViewport().getRenderer().finishElementContents(this);
                        }
                        break;
                }
            }
        }
    }
    
    @Override
    public Rectangle getClippedBounds()
    {
        //absolutely positioned blocks may have clipping specified
        Rectangle cr = getClippingRectangle();
        if (cr == null)
            return super.getClippedBounds();
        else
            return super.getClippedBounds().intersection(cr);
    }

    @Override
    public Rectangle getClippedContentBounds()
    {
        //absolutely positioned blocks may have clipping specified
        Rectangle cr = getClippingRectangle();
        if (cr == null)
            return super.getClippedContentBounds();
        else
            return super.getClippedContentBounds().intersection(cr);
    }

    /**
     * Computes the absolute coordinates of the clipping rectangle specified using the <code>clip:</code> property.
     * @return The absolute coordinates of the clipping rectangle or <code>null</code> when no clipping is applied.
     */
    public Rectangle getClippingRectangle()
    {
        if (clipRegion != null)
        {
            List<TermLength> args = clipRegion.getValue();
            CSSDecoder dec = new CSSDecoder(ctx);
            Rectangle brd = getAbsoluteBorderBounds();
            float x1 = brd.x;
            float y1 = brd.y;
            float x2 = brd.x + brd.width - 1;
            float y2 = brd.y + brd.height - 1;
            TermLength top = args.get(0);
            TermLength right = args.get(1);
            TermLength bottom = args.get(2);
            TermLength left = args.get(3);
            
            if (left != null)
                x1 = brd.x + dec.getLength((TermLength) left, false, 0, 0, 0);
            if (top != null)
                y1 = brd.y + dec.getLength((TermLength) top, false, 0, 0, 0);
            if (right != null)
                x2 = brd.x + dec.getLength((TermLength) right, false, 0, 0, 0);
            if (bottom != null)
                y2 = brd.y + dec.getLength((TermLength) bottom, false, 0, 0, 0);
            
            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }
        else
            return null;
    }
    
    //=======================================================================
    
    protected void loadBlockStyle()
    {
        floating = style.getProperty("float");
        if (floating == null) floating = FLOAT_NONE;

        clearing = style.getProperty("clear");
        if (clearing == null) clearing = CLEAR_NONE;
        
        overflowX = style.getProperty("overflow-x");
        if (overflowX == null) overflowX = OVERFLOW_VISIBLE;
        overflowY = style.getProperty("overflow-y");
        if (overflowY == null) overflowY = OVERFLOW_VISIBLE;
        //overflow: visible should compute to auto if the other one is not visible
        if (overflowX == OVERFLOW_VISIBLE && overflowY != OVERFLOW_VISIBLE)
            overflowX = OVERFLOW_AUTO;
        else if (overflowY == OVERFLOW_VISIBLE && overflowX != OVERFLOW_VISIBLE)
            overflowY = OVERFLOW_AUTO;
        
        boxSizing = style.getProperty("box-sizing");
        if (boxSizing == null) boxSizing = CONTENT_BOX;
        
        align = style.getProperty("text-align");
        if (align == null) align = ALIGN_LEFT;
        
        //apply combination rules
        //http://www.w3.org/TR/CSS21/visuren.html#dis-pos-flo
        if (display == ElementBox.DISPLAY_NONE)
        {
            position = POS_STATIC;
            floating = FLOAT_NONE;
        }
        else if (position == POS_ABSOLUTE || position == POS_FIXED)
        {
            floating = FLOAT_NONE;
        }
        
        //absolutely positioned elements may have clipping
        if (position == POS_ABSOLUTE)
        {
            CSSProperty.Clip clip = style.getProperty("clip");
            if (clip == Clip.shape)
                clipRegion = style.getValue(TermRect.class, "clip");
        }
    }
    
    /** Compute the total width of a block element according to the min-, max-,
     * width properties */
    protected float blockWidth()
    {
    	return content.width;
    }

    /** Compute the height of a block element according to the min-, max-,
     * height properties */
    protected float blockHeight()
    {
    	return content.height;
    }

    /** 
     * Returns maximal width of floats (left and right together) in 
     * a specified Y interval in this block.
     * @param y1 the starting Y value
     * @param y2 the end Y value
     * @return the maximal sum of the left and the right float width
     */
    protected float maxFloatWidth(float y1, float y2)
    {
        float ret = 0;
        float fy = y1;
        while (fy < y2)
        {
            float w = fleft.getWidth(fy) + fright.getWidth(fy);
            if (w > ret)
                ret = w;
            
            float nexty = FloatList.getNextY(fleft, fright, fy);
            if (nexty != -1)
                fy = nexty;
            else
                break;
        }
        return ret;
    }

    /**
     * Computes the maximal widths of floats on both left and right for the given range of y coordinate.
     * @param y1 starting y coordinate
     * @param y2 ending y coordinate
     * @param fx starting values of [left, right] width (for y1) 
     * @return updated fx[] containging [maxleft, maxright] coordinates.
     */
    protected float[] computeFloatLimits(float y1, float y2, float[] fx)
    {
        float fy = y1;
        while (fy < y2)
        {
            float nexty = FloatList.getNextY(fleft, fright, fy);
            if (nexty != -1)
                fy = nexty;
            else
                break;
            // recompute the limits for the new fy
            if (fy < y2)
            {
                float flx = fleft.getWidth(fy) - floatXl;
                if (flx < 0) flx = 0;
                float frx = fright.getWidth(fy) - floatXr;
                if (frx < 0) frx = 0;
                
                if (fx[0] < flx)
                    fx[0] = flx;
                if (fx[1] < frx)
                    fx[1] = frx;
            }
        }
        return fx;
    }
    
    @Override
    protected void loadSizes()
    {
        loadSizes(false);
    }
    
    @Override
    public void updateSizes()
    {
    	loadSizes(true);
    }
    
    /**
     * Load the sizes from CSS properties.
     * @param update Update mode after the size of the containing box has been updated.
     * The content size is not reset, the margins are recomputed.
     */  
    protected void loadSizes(boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        //containing box sizes
        float contw = getContainingBlock().width;
        float conth = getContainingBlock().height;
        
        //Borders
        if (!update) //borders needn't be updated
            loadBorders(dec, contw);
        
        //Padding
        loadPadding(dec, contw);
        
        //Position
        loadPosition();
        
        //Content and margins
        if (!update)
        {
        	content = new Dimension(0, 0);
        	margin = new LengthSet();
        	declMargin = new LengthSet();
        }
            
        //Margins, widths and heights
        loadWidthsHeights(dec, contw, conth, update);
        
        if (!update)
        {
        	emargin = new LengthSet(margin);
        	widthAdjust = 0; //reset width adjust when not updating
        }
        else
        { //efficient top and bottom margins already computed; update just left and right
            emargin.left = margin.left;
            emargin.right = margin.right;
        }
        
        //Text indentation
        indent = dec.getLength(getLengthValue("text-indent"), false, 0, 0, contw);
    }
    
    /**
     * Loads the padding sizes from the style.
     * 
     * @param dec CSS decoder used for decoding the style
     * @param contw containing block width for decoding percentages
     */
    protected void loadPadding(CSSDecoder dec, float contw)
    {
        padding = new LengthSet();
        padding.top = dec.getLength(getLengthValue("padding-top"), false, null, null, contw);
        padding.right = dec.getLength(getLengthValue("padding-right"), false, null, null, contw);
        padding.bottom = dec.getLength(getLengthValue("padding-bottom"), false, null, null, contw);
        padding.left = dec.getLength(getLengthValue("padding-left"), false, null, null, contw);
    }
    
    /**
     * Loads the widths and margins from the style.
     * 
     * @param dec CSS decoder used for decoding the style
     * @param contw containing block width for decoding percentages
     */
    protected void loadWidthsHeights(CSSDecoder dec, float contw, float conth, boolean update)
    {
        //Minimal width
        TermLengthOrPercent minw = getLengthValue("min-width");
        TermLengthOrPercent minh = getLengthValue("min-height");
        boolean autoMinW = false; 
        boolean autoMinH = (minh != null && minh.isPercentage() && !getContainingBlockBox().hasFixedHeight());
        min_size = new Dimension(dec.getLength(minw, autoMinW, -1, -1, contw), dec.getLength(minh, autoMinH, -1, -1, conth));
        //Maximal width
        TermLengthOrPercent maxw = getLengthValue("max-width");
        TermLengthOrPercent maxh = getLengthValue("max-height");
        boolean autoMaxW = style.getProperty("max-width") == CSSProperty.MaxWidth.NONE;
        boolean autoMaxH = style.getProperty("max-height") == CSSProperty.MaxHeight.NONE
                || (maxh != null && maxh.isPercentage() && !getContainingBlockBox().hasFixedHeight());
        max_size = new Dimension(dec.getLength(maxw, autoMaxW, -1, -1, contw), dec.getLength(maxh, autoMaxH, -1, -1, conth));
        if (max_size.width != -1 && max_size.width < min_size.width)
            max_size.width = min_size.width;
        if (max_size.height != -1 && max_size.height < min_size.height)
            max_size.height = min_size.height; 

        //Calculate widths and margins
        TermLengthOrPercent width = getLengthValue("width");
        boolean wauto = (width == null) || (style.getProperty("width") == CSSProperty.Width.AUTO);
        computeWidths(width, wauto, true, update);
        if (max_size.width != -1 && content.width > max_size.width)
        {
            width = getLengthValue("max-width");
            computeWidths(width, false, false, update);
        }
        if (min_size.width != -1 && content.width < min_size.width)
        {
            width = getLengthValue("min-width");
            computeWidths(width, false, false, update);
        }
        
        //Calculate heights and margins
        // http://www.w3.org/TR/CSS21/visudet.html#Computing_heights_and_margins
        TermLengthOrPercent height = getLengthValue("height");
        boolean hauto = (height == null) || (style.getProperty("height") == CSSProperty.Height.AUTO);
        computeHeights(height, hauto, true, update);
        if (max_size.height != -1 && content.height > max_size.height)
        {
            height = getLengthValue("max-height");
            computeHeights(height, false, false, update);
        }
        if (min_size.height != -1 && content.height < min_size.height)
        {
            height = getLengthValue("min-height");
            computeHeights(height, false, false, update);
        }
        
        //apply box-sizing if different from content-box
        if (boxSizing == BORDER_BOX)
        {
            if (!wauto)
            {
                content.width -= border.left + border.right + padding.left + padding.right;
                if (content.width < 0) content.width = 0;
            }
            if (!hauto)
            {
                content.height -= border.top + border.bottom + padding.top + padding.bottom;
                if (content.height < 0) content.height = 0;
            }
        }
        
        //apply width adjustment when updating
        if (update && widthAdjust != 0)
        {
            if (!wset)
                content.width += widthAdjust;
            else if (mleftauto)
                margin.left += widthAdjust;
            else
                margin.right += widthAdjust;
        }
    }
    
    /** 
     * Calculates widths and margins according to
     *  http://www.w3.org/TR/CSS21/visudet.html#Computing_widths_and_margins .
     * @param width the specified width or null for auto
     * @param exact true if this is the exact width, false when it's a max/min width
     * @param cblock containing block
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    protected void computeWidths(TermLengthOrPercent width, boolean auto, boolean exact, boolean update)
    {
        mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
    	if (position == POS_ABSOLUTE || position == POS_FIXED)
    		computeWidthsAbsolute(width, auto, exact, getContainingBlock().width, update); //containing box created by padding
    	else
    		computeWidthsInFlow(width, auto, exact, getContainingBlock().width, update); //containing block formed by the content only
    }
    
    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, float contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (width == null) auto = true; //no value behaves as 'auto'
        
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        TermLengthOrPercent mright = getLengthValue("margin-right");
        
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
            
            //Compute the margins if we're in flow and we know the width
            if (isInFlow() && prefer) 
            {
                if (mleftauto && mrightauto)
                {
                    float rest = contw - content.width - border.left - padding.left
                                     - padding.right - border.right;
                    if (rest >= 0)
                    {
                        margin.left = margin.right = rest / 2.0f;
                    }
                    else //negative margin - use it just for the right margin
                    {
                        margin.left = 0;
                        margin.right = rest;
                    }
                }
                else if (mleftauto)
                {
                    margin.left = contw - content.width - border.left - padding.left
                                        - padding.right - border.right - margin.right;
                }
                else if (mrightauto)
                {
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    if (margin.right < 0 && getContainingBlockBox() instanceof BlockBox && ((BlockBox) getContainingBlockBox()).canIncreaseWidth())
                        margin.right = 0;
                }
                else //everything specified, ignore right margin
                {
                    margin.right = contw - content.width - border.left - padding.left
                                    - padding.right - border.right - margin.left;
                    if (margin.right < 0 && getContainingBlockBox() instanceof BlockBox && ((BlockBox) getContainingBlockBox()).canIncreaseWidth())
                        margin.right = 0;
                }
            }
        }
    }

    protected void computeWidthsAbsolute(TermLengthOrPercent width, boolean auto, boolean exact, float contw, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
    	if (width == null) auto = true; //no value behaves as "auto"

        TermLengthOrPercent mleft = getLengthValue("margin-left");
        TermLengthOrPercent mright = getLengthValue("margin-right");
        
        if (!widthComputed) update = false;
    	
        if (auto)
        {
            if (exact) wset = false;
            if (!update)
                content.width = dec.getLength(width, auto, 0, 0, contw);
        }
        else
        {
            if (exact) 
            {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
        }
    	
    	//count left, right and width constraints
    	int constr = 0;
    	if (wset) constr++;
    	if (leftset) constr++;
    	if (rightset) constr++;
    	
    	//compute margins
    	if (constr < 3)  //too many auto values - auto margins are treated as zero
    	{
	        if (mleftauto)
	            margin.left = 0;
	        else
	            margin.left = dec.getLength(mleft, false, 0, 0, contw);
	        if (mrightauto)
	            margin.right = 0;
	        else
	            margin.right = dec.getLength(mright, false, 0, 0, contw);
    	}
    	else //everything specified
    	{
    	    if (mleftauto && mrightauto)
    	    {
    	        float rest = contw - coords.left - coords.right - border.left - border.right - padding.left - padding.right - content.width;
                margin.left = (rest + 1) / 2;
                margin.right = rest / 2;
    	    }
    	    else if (mleftauto)
    	    {
    	        margin.right = dec.getLength(mright, false, 0, 0, contw);
    	        margin.left = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.right;
    	    }
    	    else if (mrightauto)
    	    {
    	        margin.left = dec.getLength(mleft, false, 0, 0, contw);
    	        margin.right = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.left;
    	    }
    	    else //over-constrained, both margins apply (right coordinate will be ignored)
    	    {
                margin.left = dec.getLength(mleft, false, 0, 0, contw);
                margin.right = dec.getLength(mright, false, 0, 0, contw);
    	    }
    	}
    	//for absolute positions, the declared margins correspond to computed ones
    	declMargin.left = margin.left;
    	declMargin.right = margin.right;
    	
    	//compute the letf and right positions
	    if (!leftset && !rightset)
	    {
	        leftstatic = true; //left will be set to static position during the layout
    	    coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
	    }
	    else if (!leftset)
	    {
    	    coords.left = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
	    }
	    else if (!rightset)
	    {
    	    coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
	    }
	    else
	    {
	        if (auto) //auto height is computed from the rest
	        	content.width = contw - coords.left - coords.right - border.left - border.right - padding.left - padding.right - margin.left - margin.right;
	        else //over-constrained - compute the right coordinate
	        	coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
	    }
    }
    
    /** 
     * Calculates heights and margins according to
     *  http://www.w3.org/TR/CSS21/visudet.html#Computing_heights_and_margins
     * @param height the specified width
     * @param exact true if this is the exact height, false when it's a max/min height
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    protected void computeHeights(TermLengthOrPercent height, boolean auto, boolean exact, boolean update)
    {
        final Rectangle cb = getContainingBlock(); 
        if (position == POS_ABSOLUTE || position == POS_FIXED)
            computeHeightsAbsolute(height, auto, exact, cb.width, cb.height, update);
        else
            computeHeightsInFlow(height, auto, exact, cb.width, cb.height, update);
        //the computed margins allways correspond to the declared ones
        declMargin.top = margin.top;
        declMargin.bottom = margin.bottom;
    }
    
    protected void computeHeightsInFlow(TermLengthOrPercent height, boolean auto, boolean exact, float contw, float conth, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
        if (height == null) auto = true;

        boolean mtopauto = style.getProperty("margin-top") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mtop = getLengthValue("margin-top");
        boolean mbottomauto = style.getProperty("margin-bottom") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mbottom = getLengthValue("margin-bottom");
        
        if (!auto)
        {
            if (exact)
                hset = true;
            if (!update)
                content.height = dec.getLength(height, false, 0, 0, conth);
        }
        else //height not explicitly set
        {
            if (exact)
                hset = false;
        }
        
        //compute margins - auto margins are treated as zero
        if (mtopauto)
            margin.top = 0;
        else
            margin.top = dec.getLength(mtop, false, 0, 0, contw); //contw is ok here!
        if (mbottomauto)
            margin.bottom = 0;
        else
            margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
    }
    
    protected void computeHeightsAbsolute(TermLengthOrPercent height, boolean auto, boolean exact, float contw, float conth, boolean update)
    {
        CSSDecoder dec = new CSSDecoder(ctx);
        
    	if (height == null) auto = true; //no value behaves as "auto"

    	boolean mtopauto = style.getProperty("margin-top") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mtop = getLengthValue("margin-top");
        boolean mbottomauto = style.getProperty("margin-bottom") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mbottom = getLengthValue("margin-bottom");
    	
        if (!auto && height != null)
        {
            hset = exact; //not a percentage - it's a fixed height
            if (!update)
                content.height = dec.getLength(height, auto, 0, 0, conth);
        }
        else //height not explicitly set
        {
            hset = false;
        }
    	
    	//count top, bottom and height constraints
    	int constr = 0;
    	if (hset) constr++;
    	if (topset) constr++;
    	if (bottomset) constr++;
    	
    	//compute margins
    	if (constr < 3)  //too many auto values - auto margins are treated as zero
    	{
	        if (mtopauto)
	            margin.top = 0;
	        else
	            margin.top = dec.getLength(mtop, false, 0, 0, contw); //contw is ok here!
	        if (mbottomauto)
	            margin.bottom = 0;
	        else
	            margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
    	}
    	else //absolutely positioned, everything specified
    	{
    	    if (mtopauto && mbottomauto)
    	    {
    	        float rest = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height;
                margin.top = (rest + 1) / 2;
                margin.bottom = rest / 2;
    	    }
    	    else if (mtopauto)
    	    {
    	        margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
    	        margin.top = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height - margin.bottom;
    	    }
    	    else if (mbottomauto)
    	    {
                margin.top = dec.getLength(mtop, false, 0, 0, contw);
                margin.bottom = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height - margin.top;
    	    }
    	    else //over-constrained, both margins apply (bottom will be ignored)
    	    {
                margin.top = dec.getLength(mtop, false, 0, 0, contw);
                margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
    	    }
    	}
    	
    	//compute the top and bottom
    	LengthSet m = update ? emargin : margin; //use the efficient margins instead of the declared ones when the efficient have been computed
	    if (!topset && !bottomset)
	    {
	        topstatic = true; //top will be set to static position during the layout
            coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - m.top - m.bottom - content.height;
	    }
	    else if (!topset)
	    {
            coords.top = conth - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - m.top - m.bottom - content.height;
	    }
	    else if (!bottomset)
	    {
            coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - m.top - m.bottom - content.height;
	    }
	    else
	    {
	        if (auto) //auto height is computed from the rest
	            content.height = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - m.top - m.bottom;
	        else //over-constrained - compute the bottom coordinate
	        	coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - m.top - m.bottom - content.height;
	    }
    }
    
    /**
     * Checks if the box content is separated from the top margin by a border or padding.
     */
    protected boolean separatedFromTop(ElementBox box)
    {
        return (box.border.top > 0 || box.padding.top > 0 || box.isRootElement());
    }
    
    /**
     * Checks if the box content is separated from the bottom margin by a border or padding.
     */
    protected boolean separatedFromBottom(ElementBox box)
    {
        return (box.border.bottom > 0 || box.padding.bottom > 0 || box.isRootElement());
    }
    
    /**
     * Computes the collapsed margin height from two adjoining margin heights.
     * @see http://www.w3.org/TR/CSS22/box.html#collapsing-margins
     * @param m1 The first margin height
     * @param m2 The second margin height
     * @return The collapsed margin height
     */
    protected float collapsedMarginHeight(float m1, float m2)
    {
        if (m1 >= 0 && m2 >= 0)
            return Math.max(m1, m2);
        else if (m1 < 0 && m2 < 0)
            return Math.min(m1, m2);
        else
            return m1 + m2;
    }
    
    /**
     * Remove the previously splitted child boxes
     */
    protected void clearSplitted()
    {
        for (Iterator<Box> it = nested.iterator(); it.hasNext(); )
        {
            Box box = it.next();
            if (box.splitted)
            {
                it.remove();
                endChild--;
            }
        }
    }
    
}

