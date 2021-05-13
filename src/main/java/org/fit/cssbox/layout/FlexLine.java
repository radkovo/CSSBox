package org.fit.cssbox.layout;

import java.util.ArrayList;

abstract public class FlexLine {

    /**
     * Flex container for which this line was created
     */
    protected FlexBox owner;

    /**
     * Total width in pixels (for horizontal alignment
     */
    protected float width;

    /**
     * Total height in pixels (for horizontal alignment)
     */
    protected float height;

    /**
     * Defines if flex line does contain any items or not
     */
    protected boolean isFirstItem;

    /**
     * ArrayList for flex items belonging to this flex line
     */
    protected ArrayList<FlexItem> itemsInLine;

    /**
     * remaining space in main size for items
     */
    protected float remainingMainSpace;

    /**
     * for align-items and align-self :baseline
     * it is used as reference item
     */
    protected FlexItem refItem;

    public FlexBox getOwner() {
        return owner;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRemainingMainSpace() {
        return remainingMainSpace;
    }

    public void setRemainingMainSpace(float remainingMainSpace) {
        this.remainingMainSpace = remainingMainSpace;
        if (this.remainingMainSpace < 0) this.remainingMainSpace = 0;
    }

    /**
     * Applies align content to all items in each line of ArrayList lines.
     *
     * @param lines                ArrayList to apply on
     * @param countOfPreviousLines count of lines previously processed
     */
    protected abstract void applyAlignContent(ArrayList<FlexLine> lines, int countOfPreviousLines);

    /**
     * Sets start coordinate in cross size to item in according this line.
     *
     * @param item item to set cross coordinates
     */
    protected abstract void setCrossCoordToItem(FlexItem item);

    /**
     * Sets start coordinate in cross size to item and adjusts cross size for flex line.
     *
     * @param item
     */
    protected abstract void setPositionAndAdjustCrossSize(FlexItem item);

    /**
     * Sets line cross size according to flex-wrap property and items's heights.
     */
    protected abstract void setLineCrossSize();

    /**
     * Applies align-items and align-self CSS properties.
     */
    protected abstract void applyAlignItemsAndSelf();

    /**
     * Applies align-self CSS property to item. Checks if is auto or not.
     *
     * @param item item to apply
     * @return false if is align-self: auto, true if not
     */
    protected abstract boolean applyAlignSelf(FlexItem item);

    /**
     * Sets main coordinate of flex item in flex line according to justify-content CSS property.
     *
     * @param item                 item to set coordinte
     * @param sizeOfPreviousItems  sum of main sizes of all previously processed items in line
     * @param j                    count of previously processed items
     * @param totalMainSizeOfItems sum of main sizes of all items in row
     */
    protected abstract void applyJustifyContent(FlexItem item, float sizeOfPreviousItems, int j, float totalMainSizeOfItems);

    /**
     * Applies justify content to all items in line.
     */
    protected abstract void alignByJustifyContent();


    /**
     * Tries add item to this flex line. It succeed if there is enough space in this line for item.
     * In case of success it adds this item to ArrayList itemsInLine.
     *
     * @param item item to add to line
     * @return true if success, false if not
     */
    protected boolean registerItem(FlexItem item) {
        float totalMainSizeOfItem;
        if (owner.isRowContainer())
            totalMainSizeOfItem = item.hypotheticalMainSize + item.margin.left + item.margin.right + item.padding.left + item.padding.right + item.border.left + item.border.right;
        else {
            totalMainSizeOfItem = item.content.height + item.margin.top + item.margin.bottom + item.padding.top + item.padding.bottom + item.border.top + item.border.bottom;
            if (owner.mainSizeSetByCont)
                ((FlexLineColumn) this).height += totalMainSizeOfItem;
        }
        if (isFirstItem) {
            setRemainingMainSpace(remainingMainSpace - totalMainSizeOfItem);
            itemsInLine.add(item);
            refItem = item;
            isFirstItem = false;
            return true;
        }

        //not first item in row, does it fit into line?
        if ((totalMainSizeOfItem > remainingMainSpace) &&
                owner.flexWrap != FlexBox.FLEX_WRAP_NOWRAP &&
                !owner.mainSizeSetByCont)
            return false;

        setRemainingMainSpace(remainingMainSpace - totalMainSizeOfItem);

        itemsInLine.add(item);
        return true;
    }
}
