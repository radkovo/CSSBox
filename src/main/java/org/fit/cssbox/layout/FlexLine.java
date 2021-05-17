package org.fit.cssbox.layout;

import java.util.ArrayList;

/**
 * Abstract class of Flex line
 * Stores items into same lines and it's hypothetical space of Flex container
 *
 * @author Ondra, Ondry
 */
abstract public class FlexLine {

    /**
     * Flex container
     */
    protected FlexBox owner;

    /**
     * Total width
     */
    protected float width;

    /**
     * Total height
     */
    protected float height;

    /**
     * Defines if flex line does contain any items
     */
    protected boolean isFirstItem;

    /**
     * List for flex items according to flex line
     */
    protected ArrayList<FlexItem> itemsInLine;

    /**
     * Remaining main space
     */
    protected float remainingMainSpace;

    /**
     * It's used as reference item for property align-items and align-self :baseline
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
     * Applies align content to all items in each flex line.
     *
     * @param lines                flex lines
     * @param countOfPreviousLines count previously flex lines
     */
    protected abstract void applyAlignContent(ArrayList<FlexLine> lines, int countOfPreviousLines);

    /**
     * Sets start coordinate in cross size to item in flex line.
     *
     * @param item flex item
     */
    protected abstract void setCrossCoordToItem(FlexItem item);

    /**
     * Sets start coordinate in cross size to item and adjusts cross size for flex line.
     *
     * @param item flex item
     */
    protected abstract void setPositionAndAdjustCrossSize(FlexItem item);

    /**
     * Sets line cross size according to flex-wrap property and item's heights.
     */
    protected abstract void setLineCrossSize();

    /**
     * Applies align-items and align-self properties.
     */
    protected abstract void applyAlignItemsAndSelf();

    /**
     * Applies align-self property to item. Checks if is auto or not.
     *
     * @param item flex item
     * @return false if is align-self: auto, true if not
     */
    protected abstract boolean applyAlignSelf(FlexItem item);

    /**
     * Sets main coordinate of flex item in flex line according to justify-content property.
     *
     * @param item                 flex item
     * @param sizeOfPreviousItems  sum of main sizes of all previously items in line
     * @param j                    count of previously items
     * @param totalMainSizeOfItems sum of main sizes of all items in row
     */
    protected abstract void applyJustifyContent(FlexItem item, float sizeOfPreviousItems, int j, float totalMainSizeOfItems);

    /**
     * Applies justify content to all items in line.
     */
    protected abstract void alignByJustifyContent();


    /**
     * Tries add a item to this flex line. It succeed if there is enough space in this line for item.
     * In case of success it adds this item to flex line.
     *
     * @param item flex item
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

        if ((totalMainSizeOfItem > remainingMainSpace) &&
                owner.flexWrap != FlexBox.FLEX_WRAP_NOWRAP &&
                !owner.mainSizeSetByCont)
            return false;

        setRemainingMainSpace(remainingMainSpace - totalMainSizeOfItem);

        itemsInLine.add(item);
        return true;
    }
}
