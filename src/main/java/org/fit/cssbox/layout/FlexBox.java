package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A box corresponding to a Flexbox container
 *
 * @author Ondra, Ondry
 */
public class FlexBox extends BlockBox {

    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW = CSSProperty.FlexDirection.ROW;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW_REVERSE = CSSProperty.FlexDirection.ROW_REVERSE;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_COLUMN_REVERSE = CSSProperty.FlexDirection.COLUMN_REVERSE;

    public static final CSSProperty.FlexWrap FLEX_WRAP_NOWRAP = CSSProperty.FlexWrap.NOWRAP;
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
     * Flex-direction property
     */
    protected CSSProperty.FlexDirection flexDirection;
    /**
     * Flex-wrap property
     */
    protected CSSProperty.FlexWrap flexWrap;
    /**
     * Justify-content property
     */
    protected CSSProperty.JustifyContent justifyContent;
    /**
     * Align-content property
     */
    protected CSSProperty.AlignContent alignContent;
    /**
     * Align-items property
     */
    protected CSSProperty.AlignItems alignItems;

    /**
     * Main size of container
     */
    protected float mainSize;
    /**
     * Cross size of container
     */
    protected float crossSize;

    /**
     * Defines main size set by content
     */
    protected boolean mainSizeSetByCont;

    /**
     * First flex line of container
     */
    protected FlexLine firstFlexLine;

    /**
     * Creates a new instance of Flexbox container
     */
    public FlexBox(Element n, VisualContext ctx) {
        super(n, ctx);

        setLayoutManager(new FlexBoxLayoutManager(this));
        isblock = true;
        firstFlexLine = null;
        mainSizeSetByCont = false;
    }

    /**
     * Convert an inline box to a Flexbox container
     */
    public FlexBox(InlineBox src) {
        super(src.el, src.ctx);

        setLayoutManager(new FlexBoxLayoutManager(this));

        setStyle(src.getStyle());
        isblock = true;
        firstFlexLine = null;
        mainSizeSetByCont = false;
    }

    /**
     * Sets main size before layout content
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
     * Sets cross size before layout content
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
     * Loads styles according to Flex container
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
     * Checks direction of container
     *
     * @return true if container is row, else if container is column
     */
    public boolean isRowContainer() {
        return flexDirection == FLEX_DIRECTION_ROW || flexDirection == FLEX_DIRECTION_ROW_REVERSE;
    }

    /**
     * Checks reversed direction of container
     *
     * @return true if container is reversed, else if not reversed
     */
    public boolean isDirectionReversed() {
        return flexDirection == FLEX_DIRECTION_ROW_REVERSE || flexDirection == FLEX_DIRECTION_COLUMN_REVERSE;
    }

    /**
     * Creates first line based on direction of container
     *
     * @param lines list of Flex lines
     * @return new flex line
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
     * Creates flex lines based on direction of container and fills them with flex items
     *
     * @param lines flex lines
     * @param items flex items
     */
    protected void createAndFillLines(ArrayList<FlexLine> lines, ArrayList<FlexItem> items) {
        FlexLine line = createFirstLineToList(lines);

        for (FlexItem item : items) {
            boolean result = line.registerItem(item);
            if (!result) {
                FlexLine newLine;
                if (isRowContainer()) {
                    newLine = new FlexLineRow(this);
                } else {
                    newLine = new FlexLineColumn(this);
                }
                lines.add(newLine);
                newLine.registerItem(item);
                line = newLine;
            }
        }
        if (flexWrap == FLEX_WRAP_WRAP_REVERSE)
            Collections.reverse(lines);
    }

    /**
     * Fixes height of container after layout items
     *
     * @param item flex item
     */
    protected void fixContainerHeight(FlexItem item) {
        if (!hasFixedHeight()) {
            if (isRowContainer()) {
                if (max_size.height == -1) {
                    if (item.bounds.y + item.totalHeight() > crossSize) {
                        crossSize = item.bounds.y + item.totalHeight();
                        setContentHeight(crossSize);
                    }
                } else {
                    if (item.bounds.y + item.totalHeight() < max_size.height) {
                        if (item.bounds.y + item.totalHeight() > crossSize) {
                            crossSize = item.bounds.y + item.totalHeight();
                            setContentHeight(crossSize);
                        }
                    } else {
                        setContentHeight(max_size.height);
                    }
                }
            } else {
                mainSize += item.bounds.height;
                setContentHeight(mainSize);
            }
        }
    }
}
