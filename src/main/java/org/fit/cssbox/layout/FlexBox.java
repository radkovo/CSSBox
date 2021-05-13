package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;

public class FlexBox extends BlockBox {

    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW = CSSProperty.FlexDirection.ROW;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW_REVERSE = CSSProperty.FlexDirection.ROW_REVERSE;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_COLUMN = CSSProperty.FlexDirection.COLUMN;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_COLUMN_REVERSE = CSSProperty.FlexDirection.COLUMN_REVERSE;

    public static final CSSProperty.FlexWrap FLEX_WRAP_NOWRAP = CSSProperty.FlexWrap.NOWRAP;
    public static final CSSProperty.FlexWrap FLEX_WRAP_WRAP = CSSProperty.FlexWrap.WRAP;
    public static final CSSProperty.FlexWrap FLEX_WRAP_WRAP_REVERSE = CSSProperty.FlexWrap.WRAP_REVERSE;

    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_FLEX_START = CSSProperty.JustifyContent.FLEX_START;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_FLEX_END = CSSProperty.JustifyContent.FLEX_END;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_CENTER = CSSProperty.JustifyContent.CENTER;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_SPACE_BETWEEN = CSSProperty.JustifyContent.SPACE_BETWEEN;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_SPACE_AROUND = CSSProperty.JustifyContent.SPACE_AROUND;

    public static final CSSProperty.AlignContent ALIGN_CONTENT_FLEX_START = CSSProperty.AlignContent.FLEX_START;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_FLEX_END = CSSProperty.AlignContent.FLEX_END;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_CENTER = CSSProperty.AlignContent.CENTER;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_SPACE_BETWEEN = CSSProperty.AlignContent.SPACE_BETWEEN;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_SPACE_AROUND = CSSProperty.AlignContent.SPACE_AROUND;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_STRETCH = CSSProperty.AlignContent.STRETCH;

    public static final CSSProperty.AlignItems ALIGN_ITEMS_FLEX_START = CSSProperty.AlignItems.FLEX_START;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_FLEX_END = CSSProperty.AlignItems.FLEX_END;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_CENTER = CSSProperty.AlignItems.CENTER;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_BASELINE = CSSProperty.AlignItems.BASELINE;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_STRETCH = CSSProperty.AlignItems.STRETCH;

    /**
     * flex direction specified by the style
     */
    protected CSSProperty.FlexDirection flexDirection;
    /**
     * flex wrap specified by the style
     */
    protected CSSProperty.FlexWrap flexWrap;
    /**
     * justify content specified by the style
     */
    protected CSSProperty.JustifyContent justifyContent;
    /**
     * align content specified by the style
     */
    protected CSSProperty.AlignContent alignContent;
    /**
     * align items specified by the style
     */
    protected CSSProperty.AlignItems alignItems;

    /**
     * main size of this box (in horizontal (row) container it is width, in vertical it is height)
     */
    protected float mainSize;
    /**
     * cross size of this box (in horizontal (row) container it is height, in vertical it is width)
     */
    protected float crossSize;

    /**
     * defines if is main size set by content
     */
    protected boolean mainSizeSetByCont;

    /**
     * first flex line (row or column based on container direction) of this container
     */
    protected FlexLine firstFlexLine;

    /**
     *
     * @param n
     * @param ctx
     */
    public FlexBox(Element n, VisualContext ctx) {
        super(n, ctx);

        typeoflayout = new FlexBoxLayoutManager(this);
        isblock = true;
        firstFlexLine = null;
        mainSizeSetByCont = false;
    }

    /**
     *
     * @param src
     */
    public FlexBox(InlineBox src) {
        super(src.el, src.ctx);

        typeoflayout = new FlexBoxLayoutManager(this);

        setStyle(src.getStyle());
        isblock = true;
        firstFlexLine = null;
        mainSizeSetByCont = false;
    }

    /**
     *
     */
    public void setInceptiveMainSize() {
        if (isRowContainer()) {
            mainSize = getContentWidth();
        } else {
            if (style.getProperty("height") == null || (style.getProperty("height") == CSSProperty.Height.percentage && parent.style.getProperty("height") == null)) {
                mainSize = 0;
                mainSizeSetByCont = true;
            } else
                mainSize = getContentHeight();
        }
    }

    /**
     *
     */
    public void setInceptiveCrossSize() {
        if (isRowContainer()) {
            crossSize = getContentHeight();
        } else {
            crossSize = getContentWidth();
        }
    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadFlexBoxStyles();
    }

    /**
     *
     */
    private void loadFlexBoxStyles() {
        flexDirection = style.getProperty("flex-direction");
        if (flexDirection == null) flexDirection = FLEX_DIRECTION_ROW;

        flexWrap = style.getProperty("flex-wrap");
        if (flexWrap == null) flexWrap = FLEX_WRAP_NOWRAP;

        justifyContent = style.getProperty("justify-content");
        if (justifyContent == null) justifyContent = JUSTIFY_CONTENT_FLEX_START;

        alignContent = style.getProperty("align-content");
        if (alignContent == null) alignContent = ALIGN_CONTENT_STRETCH;

        alignItems = style.getProperty("align-items");
        if (alignItems == null) alignItems = ALIGN_ITEMS_STRETCH;
    }

    /**
     *
     * @return
     */
    public boolean isRowContainer() {
        if (flexDirection == FLEX_DIRECTION_ROW || flexDirection == FLEX_DIRECTION_ROW_REVERSE)
            return true;
        else
            return false;
    }

    /**
     *
     * @return
     */
    public boolean isDirectionReversed() {
        if (flexDirection == FLEX_DIRECTION_ROW_REVERSE || flexDirection == FLEX_DIRECTION_COLUMN_REVERSE)
            return true;
        else
            return false;
    }

    /**
     *
     * @param lines
     * @return
     */
    private FlexLine createFirstLineToList(ArrayList<FlexLine> lines) {
        FlexLine line = firstFlexLine;
        if (line == null) {
            if (isRowContainer())
                line = new FlexLineRow(this);
            else
                line = new FlexLineColumn(this);
        }
        lines.add(line);
        return line;
    }

    /**
     *
     * @param lines
     * @param items
     */
    protected void createAndFillLines(ArrayList<FlexLine> lines, ArrayList<FlexItem> items) {
        FlexLine line = createFirstLineToList(lines);

        for (int i = 0; i < items.size(); i++) {
            FlexItem Item = items.get(i);
            //try set item to line
            boolean result = line.registerItem(Item);
            if (!result) {
                //this item does not fit anymore
                //make new line and register item to it

                FlexLine newLine;
                if (isRowContainer()) {
                    newLine = new FlexLineRow(this);
                } else {
                    newLine = new FlexLineColumn(this);
                }
                lines.add(newLine);
                newLine.registerItem(Item);
                line = newLine;
            }
        }
        if (flexWrap == FLEX_WRAP_WRAP_REVERSE)
            Collections.reverse(lines);
    }

    /**
     *
     * @param Item
     */
    protected void fixContainerHeight(FlexItem Item) {
        if (!hasFixedHeight()) {
            //height is not fixed
            if (isRowContainer()) {
                //row
                if (max_size.height == -1) {
                    //container has no height limit
                    if (Item.bounds.y + Item.totalHeight() > crossSize) {
                        //Item is higher than container so far
                        crossSize = Item.bounds.y + Item.totalHeight();
                        setContentHeight(crossSize);
                    }
                } else {
                    //container has max height limit
                    if (Item.bounds.y + Item.totalHeight() < max_size.height) {
                        //it fits to max
                        if (Item.bounds.y + Item.totalHeight() > crossSize) {
                            crossSize = Item.bounds.y + Item.totalHeight();
                            setContentHeight(crossSize);
                        }
                    } else {
                        //it doesn't fit to max, set max
                        setContentHeight(max_size.height);
                    }
                }
            } else {
                mainSize += Item.bounds.height;
                setContentHeight(mainSize);
            }
        }
    }
}
