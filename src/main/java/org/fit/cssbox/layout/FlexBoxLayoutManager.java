package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Layout manager of Flexible box layout
 *
 * @author Ondra, Ondry
 */
public class FlexBoxLayoutManager implements LayoutManager {

    /**
     * Flex container
     */
    FlexBox flexBox;

    /**
     * Creates a new instance of Flexbox layout manager
     *
     * @param flexBox flex container
     */
    public FlexBoxLayoutManager(FlexBox flexBox) {
        this.flexBox = flexBox;
    }

    /**
     * Layout the sub-elements.
     *
     * @param availw    Maximal width available to the child elements
     * @param force     Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return <code>true</code> if the box has been succesfully placed
     */
    @Override
    public boolean doLayout(float availw, boolean force, boolean linestart) {
        if (!flexBox.displayed) {
            flexBox.content.setSize(0, 0);
            flexBox.bounds.setSize(0, 0);
        }

        flexBox.setInceptiveMainSize();
        flexBox.setInceptiveCrossSize();

        if (flexBox.isRowContainer())
            layoutItemsInRow(getItemList(), force, linestart);
        else
            layoutItemsInColumn(getItemList(), force, linestart);


        if (flexBox.isRowContainer()) {
            flexBox.content.width = flexBox.mainSize;
            flexBox.bounds.width = flexBox.mainSize +
                    flexBox.margin.left + flexBox.margin.right +
                    flexBox.padding.left + flexBox.padding.right +
                    flexBox.border.left + flexBox.border.right;
            flexBox.content.height = flexBox.crossSize;
            flexBox.bounds.height = flexBox.crossSize +
                    flexBox.margin.top + flexBox.margin.bottom +
                    flexBox.padding.top + flexBox.padding.bottom +
                    flexBox.border.top + flexBox.border.bottom;
        } else {
            flexBox.content.width = flexBox.crossSize;
            flexBox.bounds.width = flexBox.crossSize +
                    flexBox.margin.left + flexBox.margin.right +
                    flexBox.padding.left + flexBox.padding.right +
                    flexBox.border.left + flexBox.border.right;
            flexBox.content.height = flexBox.mainSize;
            flexBox.bounds.height = flexBox.mainSize +
                    flexBox.margin.top + flexBox.margin.bottom +
                    flexBox.padding.top + flexBox.padding.bottom +
                    flexBox.border.top + flexBox.border.bottom;
        }
        return true;
    }

    /**
     * Creates list of flex items and disables floats with clearing
     *
     * @return List of flex items
     */
    private ArrayList<FlexItem> getItemList() {
        ArrayList<FlexItem> ItemList = new ArrayList<>();

        for (int i = 0; i < flexBox.getSubBoxNumber(); i++) {
            FlexItem Item = (FlexItem) flexBox.getSubBox(i);
            Item.disableFloats();
            Item.clearing = CSSProperty.Clear.NONE;
            ItemList.add(Item);
        }
        return ItemList;
    }

    /**
     * Layouts content in row flex container
     *
     * @param listItems list of flex items
     * @param force     Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     */
    protected void layoutItemsInRow(ArrayList<FlexItem> listItems, boolean force, boolean linestart) {
        Collections.sort(listItems);

        for (FlexItem Item : listItems) {
            Item.hypotheticalMainSize = Item.setHypotheticalMainSize(flexBox);
            Item.hypotheticalMainSize = Item.boundFlexBasisByMinAndMax(Item.hypotheticalMainSize);
            Item.fixRightMargin();
        }

        ArrayList<FlexLine> lines = new ArrayList<>();
        flexBox.createAndFillLines(lines, listItems);

        handleFlexGrow(lines);
        handleFlexShrink(lines);

        for (FlexItem Item : listItems) {
            Item.setAvailableWidth(Item.hypotheticalMainSize + Item.padding.left + Item.padding.right + Item.border.left + Item.border.right + Item.margin.left + Item.margin.right);
            Item.setCrossSize(flexBox);
        }

        for (FlexItem Item : listItems) {
            Item.doLayout(Item.hypotheticalMainSize, force, linestart);

            Item.fixRightMargin();
            Item.bounds.width = Item.hypotheticalMainSize + Item.padding.left + Item.padding.right + Item.border.left + Item.border.right + Item.margin.left + Item.margin.right;
            Item.content.width = Item.hypotheticalMainSize;

            if (Item.crossSizeSetByCont) {
                if (Item.getSubBoxNumber() != 0) {
                    Item.content.height = Item.getSubBox(Item.getSubBoxNumber() - 1).getBounds().y
                            + Item.getSubBox(Item.getSubBoxNumber() - 1).getBounds().height;
                    Item.bounds.height = Item.content.height + Item.padding.top + Item.padding.bottom + Item.border.top + Item.border.bottom + Item.margin.top + Item.margin.bottom;
                    if (flexBox.crossSize < Item.bounds.height)
                        flexBox.crossSize = Item.bounds.height;
                }
            } else {
                Item.bounds.height = Item.totalHeight();
                Item.content.height = Item.totalHeight() - Item.padding.top - Item.padding.bottom - Item.border.top - Item.border.bottom - Item.margin.top - Item.margin.bottom;
            }
        }

        for (FlexItem Item : listItems) {
            if (Item.crossSizeSetByPercentage) {
                for (int j = 0; j < Item.getSubBoxNumber(); j++) {
                    if (j == Item.getSubBoxNumber() - 1)
                        Item.content.height += Item.getSubBox(j).getContentHeight() + Item.getSubBox(j).getContentY();
                }
                Item.bounds.height = Item.content.height + Item.padding.top + Item.padding.bottom + Item.border.top + Item.border.bottom + Item.margin.top + Item.margin.bottom;
            }
        }
        for (FlexLine value : lines) {
            value.setLineCrossSize();
        }

        for (int i = 0; i < lines.size(); i++) {
            FlexLine flexLine = lines.get(i);

            int sumOfPreviousLinesHeights = 0;
            for (int y = 0; y < i; y++) {
                sumOfPreviousLinesHeights += lines.get(y).getHeight();
            }
            ((FlexLineRow) flexLine).setY(sumOfPreviousLinesHeights);
            flexLine.applyAlignContent(lines, i);

            flexLine.applyAlignItemsAndSelf();
        }
        for (FlexLine flexLine : lines) {
            flexLine.alignByJustifyContent();
        }
        for (FlexLine line : lines)
            for (int j = 0; j < line.itemsInLine.size(); j++)
                flexBox.fixContainerHeight(line.itemsInLine.get(j));
    }

    /**
     * Layouts content in column flex container
     *
     * @param listItems list of flex items
     * @param force     Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     */
    protected void layoutItemsInColumn(ArrayList<FlexItem> listItems, boolean force, boolean linestart) {

        Collections.sort(listItems);

        for (FlexItem Item : listItems) {
            Item.hypotheticalMainSize = Item.setHypotheticalMainSize(flexBox);
            Item.hypotheticalMainSize = Item.boundFlexBasisByMinAndMax(Item.hypotheticalMainSize);
        }

        for (FlexItem Item : listItems) {
            Item.setCrossSize(flexBox);

            processRightMarginAndWidth(Item);
            Item.setAvailableWidth(Item.content.width + Item.padding.left + Item.padding.right + Item.border.left + Item.border.right + Item.margin.left + Item.margin.right);
        }

        for (FlexItem Item : listItems) {
            if (!Item.contblock)
                Item.layoutInline();
            else
                Item.layoutBlocks();

            processRightMarginAndWidth(Item);

            Item.bounds.width = Item.content.width + Item.padding.left + Item.padding.right + Item.border.left + Item.border.right + Item.margin.left + Item.margin.right;

            Item.bounds.height = Item.hypotheticalMainSize + Item.padding.top + Item.padding.bottom + Item.border.top + Item.border.bottom + Item.margin.top + Item.margin.bottom;
            Item.content.height = Item.hypotheticalMainSize;

            adjustHeight(flexBox, Item);
            Item.bounds.height = Item.content.height + Item.padding.top + Item.padding.bottom + Item.border.top + Item.border.bottom + Item.margin.top + Item.margin.bottom;
        }

        ArrayList<FlexLine> lines = new ArrayList<>();
        flexBox.createAndFillLines(lines, listItems);

        for (FlexLine line : lines) line.setLineCrossSize();

        //flex grow and shrink
        handleFlexGrow(lines);
        handleFlexShrink(lines);

        for (FlexLine line : lines) {
            line.setLineCrossSize();
            if (line.height > flexBox.mainSize)
                flexBox.mainSize = line.height;
        }

        for (int i = 0; i < lines.size(); i++) {
            FlexLine flexLine = lines.get(i);

            int sumOfPreviousLinesWidths = 0;
            for (int y = 0; y < i; y++) {
                sumOfPreviousLinesWidths += lines.get(y).getWidth();
            }
            ((FlexLineColumn) flexLine).setX(sumOfPreviousLinesWidths);
            flexLine.applyAlignContent(lines, i);

            flexLine.applyAlignItemsAndSelf();
        }

        for (FlexLine line : lines) {
            line.alignByJustifyContent();
        }
    }

    /**
     * Process correct margin and width
     *
     * @param item flex item
     */
    private void processRightMarginAndWidth(FlexItem item) {
        item.fixRightMargin();

        if (item.crossSizeSetByCont) {
            item.content.width = item.getMaximalContentWidth();
            if (item.content.width > flexBox.crossSize)
                item.content.width = flexBox.crossSize - (item.padding.left + item.padding.right + item.border.left + item.border.right + item.margin.left + item.margin.right);
        }
    }

    /**
     * Edits height in row container
     *
     * @param container flex container
     * @param item      flex item
     */
    protected void adjustHeight(FlexBox container, FlexItem item) {
        if (item.getSubBoxNumber() != 0) {
            if (item.flexBasis != FlexItem.FLEX_BASIS_AUTO && item.flexBasis != FlexItem.FLEX_BASIS_CONTENT) {
                float contheight = item.getSubBox(item.getSubBoxNumber() - 1).bounds.y + item.getSubBox(item.getSubBoxNumber() - 1).bounds.height;
                if (contheight > item.content.height) {
                    item.content.height = contheight;
                    item.content.height = boundByHeight(container, item);
                }
            } else if (item.flexBasisSetByCont) {
                item.content.height = item.getSubBox(item.getSubBoxNumber() - 1).getContentY() + item.getSubBox(item.getSubBoxNumber() - 1).getContentHeight();

            }
        }
    }

    /**
     * Method for adjustHeight() method
     *
     * @param container flex container
     * @param item      flex item
     * @return bounded height
     */
    protected float boundByHeight(FlexBox container, FlexItem item) {
        CSSDecoder dec = new CSSDecoder(item.ctx);
        float height = dec.getLength(item.getLengthValue("height"), false, -1, -1, container.mainSize);
        if (height != -1) {
            if (item.style.getProperty("height") == CSSProperty.Height.percentage && container.mainSize == 0)
                return item.content.height;

            if (height < item.content.height) {
                item.content.height = Math.max(height, item.hypotheticalMainSize);
            }
        } else {
            float minheight = dec.getLength(item.getLengthValue("min-height"), false, -1, -1, container.mainSize);
            if (minheight != -1) {
                if (item.style.getProperty("min-height") == CSSProperty.MinHeight.percentage && container.mainSize == 0)
                    return item.content.height;
                if (minheight < item.content.height)
                    item.content.height = Math.max(minheight, item.hypotheticalMainSize);
            }
        }
        return item.content.height;
    }

    /**
     * Distributes remaining space of container to items, based on flex-grow property
     *
     * @param lines flex lines
     */
    protected void handleFlexGrow(ArrayList<FlexLine> lines) {
        for (FlexLine line : lines) {
            float countOfFlexGrowValue = 0;
            float mainSizeOfItemsInLine = 0;
            for (int j = 0; j < line.itemsInLine.size(); j++) {
                FlexItem itemInRow = line.itemsInLine.get(j);
                countOfFlexGrowValue += itemInRow.flexGrowValue;
                if (flexBox.isRowContainer()) {
                    mainSizeOfItemsInLine += itemInRow.hypotheticalMainSize + itemInRow.margin.left + itemInRow.margin.right +
                            itemInRow.padding.left + itemInRow.padding.right +
                            itemInRow.border.left + itemInRow.border.right;
                } else {
                    mainSizeOfItemsInLine += itemInRow.bounds.height;
                }
            }
            if (countOfFlexGrowValue == 0)
                continue;

            float pieceOfRemainSize = (flexBox.mainSize - mainSizeOfItemsInLine) / countOfFlexGrowValue;
            if (pieceOfRemainSize <= 0)
                continue;

            for (int j = 0; j < line.itemsInLine.size(); j++) {
                FlexItem itemInRow = line.itemsInLine.get(j);

                if (flexBox.isRowContainer()) {
                    float result = (flexBox.availwidth + pieceOfRemainSize * itemInRow.flexGrowValue);
                    if (itemInRow.hypotheticalMainSize + result > itemInRow.max_size.width && itemInRow.max_size.width != -1) {
                        itemInRow.hypotheticalMainSize = itemInRow.max_size.width;
                    } else
                        itemInRow.hypotheticalMainSize += result;
                } else {
                    float result = (itemInRow.content.height + pieceOfRemainSize * itemInRow.flexGrowValue);
                    if (itemInRow.content.height + result > itemInRow.max_size.height && itemInRow.max_size.height != -1) {
                        itemInRow.content.height = itemInRow.max_size.height;
                    } else
                        itemInRow.content.height = result;

                    itemInRow.bounds.height = itemInRow.content.height +
                            itemInRow.margin.top + itemInRow.margin.bottom +
                            itemInRow.padding.top + itemInRow.padding.bottom +
                            itemInRow.border.top + itemInRow.border.bottom;
                }
            }
        }
    }

    /**
     * Distributes remaining space of container to items, based on flex-shrink property
     *
     * @param lines flex lines
     */
    protected void handleFlexShrink(ArrayList<FlexLine> lines) {
        for (FlexLine line : lines) {
            float countOfFlexShrinkValue = 0;
            float mainSizeOfItemsInLine = 0;

            for (int j = 0; j < line.itemsInLine.size(); j++) {
                FlexItem itemInRow = line.itemsInLine.get(j);
                countOfFlexShrinkValue += itemInRow.flexShrinkValue;
                if (flexBox.isRowContainer()) {
                    mainSizeOfItemsInLine += itemInRow.hypotheticalMainSize + itemInRow.margin.left + itemInRow.margin.right +
                            itemInRow.padding.left + itemInRow.padding.right +
                            itemInRow.border.left + itemInRow.border.right;
                } else {
                    mainSizeOfItemsInLine += itemInRow.bounds.height;
                }
            }

            if (countOfFlexShrinkValue == 0 || flexBox.mainSize == 0)
                continue;

            float pieceOfOverflowSize = (mainSizeOfItemsInLine - flexBox.mainSize) / countOfFlexShrinkValue;
            if (pieceOfOverflowSize <= 0)
                continue;

            for (int j = 0; j < line.itemsInLine.size(); j++) {
                FlexItem itemInLine = line.itemsInLine.get(j);

                if (flexBox.isRowContainer()) {
                    float result = (flexBox.availwidth - pieceOfOverflowSize * itemInLine.flexShrinkValue);

                    if (itemInLine.hypotheticalMainSize + result < itemInLine.min_size.width && itemInLine.min_size.width != -1) {
                        itemInLine.hypotheticalMainSize = itemInLine.min_size.width;
                    } else if (itemInLine.hypotheticalMainSize + result < itemInLine.getMinimalContentWidth()) {
                        itemInLine.hypotheticalMainSize = itemInLine.getMinimalContentWidth();
                    } else
                        itemInLine.hypotheticalMainSize += result;
                } else {
                    float result = (itemInLine.content.height - pieceOfOverflowSize * itemInLine.flexShrinkValue);

                    if (result < itemInLine.min_size.height && itemInLine.min_size.height != -1) {
                        itemInLine.content.height = itemInLine.min_size.height;
                    } else if (itemInLine.getSubBoxNumber() != 0 && result < itemInLine.getSubBox(itemInLine.getSubBoxNumber() - 1).bounds.y + itemInLine.getSubBox(itemInLine.getSubBoxNumber() - 1).bounds.height) {
                        itemInLine.content.height = itemInLine.getSubBox(itemInLine.getSubBoxNumber() - 1).bounds.y + itemInLine.getSubBox(itemInLine.getSubBoxNumber() - 1).bounds.height;
                    } else
                        itemInLine.content.height = result;

                    itemInLine.bounds.height = itemInLine.content.height +
                            itemInLine.margin.top + itemInLine.margin.bottom +
                            itemInLine.padding.top + itemInLine.padding.bottom +
                            itemInLine.border.top + itemInLine.border.bottom;
                }
            }
        }
    }
}
