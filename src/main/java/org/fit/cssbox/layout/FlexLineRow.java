package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;

import java.util.ArrayList;

/**
 * Flex lint in horizontal flex container
 *
 * @author Ondra, Ondry
 */
public class FlexLineRow extends FlexLine {

    /**
     * Saved start remaining height used for align-content: stretch
     */
    protected float savedHeight;

    /**
     * Y coordinate of top side of flex line
     */
    protected float y;

    /**
     * Creates new instance of flex line in horizontal flex container
     *
     * @param owner flex container
     */
    public FlexLineRow(FlexBox owner) {
        this.owner = owner;
        y = 0;
        height = 0;
        itemsInLine = new ArrayList<>();
        isFirstItem = true;
        width = owner.getContentWidth();
        remainingMainSpace = width;
        refItem = null;
        savedHeight = -1;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    protected void applyAlignContent(ArrayList<FlexLine> lines, int countOfPreviousLines) {
        if (owner.crossSize == 0 || owner.flexWrap == FlexBox.FLEX_WRAP_NOWRAP)
            return;

        float remainingHeightSpace = owner.crossSize;
        for (FlexLine flexLine : lines) {
            FlexLineRow line = (FlexLineRow) flexLine;
            if (line.savedHeight == -1)
                remainingHeightSpace -= line.getHeight();
            else
                remainingHeightSpace -= line.savedHeight;
        }
        if (owner.alignContent == FlexBox.ALIGN_CONTENT_FLEX_START) {
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                setY(getY() + remainingHeightSpace);
            else
                return;
        } else if (owner.alignContent == FlexBox.ALIGN_CONTENT_FLEX_END) {
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                return;
            else
                setY(getY() + remainingHeightSpace);

        } else if (owner.alignContent == FlexBox.ALIGN_CONTENT_CENTER) {
            setY(getY() + remainingHeightSpace / 2);

        } else if (owner.alignContent == FlexBox.ALIGN_CONTENT_STRETCH) {
            savedHeight = getHeight();
            setHeight(getHeight() + remainingHeightSpace / lines.size());

        } else if (owner.alignContent == FlexBox.ALIGN_CONTENT_SPACE_BETWEEN) {
            if (lines.size() != 1)
                setY(getY() + (countOfPreviousLines) * remainingHeightSpace / (lines.size() - 1));
            else if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                setY(getY() + remainingHeightSpace);
            else
                return;
        } else if (owner.alignContent == FlexBox.ALIGN_CONTENT_SPACE_AROUND) {
            if (countOfPreviousLines == 0)
                setY(getY() + remainingHeightSpace / (2 * lines.size()));
            else
                setY(getY() + remainingHeightSpace / (2 * lines.size()) + (countOfPreviousLines) * remainingHeightSpace / lines.size() + 1);
        }
    }

    @Override
    protected void setCrossCoordToItem(FlexItem item) {
        if (applyAlignSelf(item))
            return;

        if (owner.alignItems == FlexBox.ALIGN_ITEMS_FLEX_START)
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(0, y + getHeight() - item.getHeight());
            else
                item.setPosition(0, y);
        else if (owner.alignItems == FlexBox.ALIGN_ITEMS_FLEX_END)
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(0, y);
            else
                item.setPosition(0, y + getHeight() - item.getHeight());
        else if (owner.alignItems == FlexBox.ALIGN_ITEMS_CENTER)
            item.setPosition(0, y + (getHeight() - item.getHeight()) / 2);
        else if (owner.alignItems == FlexBox.ALIGN_ITEMS_BASELINE)
            alignBaseline(item);
        else
            alignStretch(item);
    }

    @Override
    protected void setPositionAndAdjustCrossSize(FlexItem item) {
        if (getHeight() < item.getHeight()) {
            if (!(owner.flexWrap == FlexBox.FLEX_WRAP_NOWRAP && owner.hasFixedHeight()))
                setHeight(item.getHeight());

            if (owner.alignItems == FlexBox.ALIGN_ITEMS_STRETCH) {
                for (FlexItem itemInLine : itemsInLine) {
                    if ((itemInLine.alignSelf == FlexItem.ALIGN_SELF_AUTO && owner.alignItems == FlexBox.ALIGN_ITEMS_STRETCH)
                            || itemInLine.alignSelf == FlexItem.ALIGN_SELF_STRETCH) {

                        if (!itemInLine.hasFixedHeight() && !itemInLine.isNotAlignSelfAuto() && itemInLine.alignSelf != FlexItem.ALIGN_SELF_STRETCH) {

                            itemInLine.bounds.height = getHeight();
                            processHeight(itemInLine);
                            itemInLine.content.height = itemInLine.bounds.height - itemInLine.padding.top - itemInLine.padding.bottom -
                                    itemInLine.border.top - itemInLine.border.bottom - itemInLine.margin.top - itemInLine.margin.bottom;
                        }
                    }
                }
            } else if (owner.alignItems == FlexBox.ALIGN_ITEMS_FLEX_END) {
                for (FlexItem flexItem : itemsInLine) {
                    flexItem.setPosition(flexItem.bounds.x, y + getHeight() - flexItem.getHeight());
                }
            } else if (owner.alignItems == FlexBox.ALIGN_ITEMS_CENTER) {
                for (FlexItem flexItem : itemsInLine) {
                    flexItem.setPosition(flexItem.bounds.x, y + (getHeight() - flexItem.getHeight()) / 2);
                }
            }
        }

        setCrossCoordToItem(item);

        if (getHeight() < item.bounds.y - y + item.getHeight()) {
            if (!(owner.flexWrap == FlexBox.FLEX_WRAP_NOWRAP && owner.hasFixedHeight()))
                setHeight(item.bounds.y - y + item.getHeight());
        }

        for (FlexItem flexItem : itemsInLine) {
            if (flexItem.alignSelf != CSSProperty.AlignSelf.AUTO) {
                applyAlignSelf(flexItem);
            }
        }
    }

    @Override
    protected void setLineCrossSize() {
        if (owner.flexWrap == FlexBox.FLEX_WRAP_NOWRAP) {
            setHeight(owner.crossSize);
            return;
        }
        float height = 0;
        for (FlexItem item : itemsInLine) {
            if (height < item.bounds.height)
                height = item.bounds.height;
        }
        setHeight(height);
    }

    @Override
    protected void applyAlignItemsAndSelf() {
        for (int j = 0; j < itemsInLine.size(); j++) {
            if (j == 0) {
                setCrossCoordToItem(itemsInLine.get(j));
                continue;
            }
            setPositionAndAdjustCrossSize(itemsInLine.get(j));
        }
    }

    @Override
    protected boolean applyAlignSelf(FlexItem item) {
        if (!item.isNotAlignSelfAuto())
            return false;

        if (getHeight() < item.getHeight())
            if (!(owner.flexWrap == FlexBox.FLEX_WRAP_NOWRAP && owner.hasFixedHeight()))
                setHeight(item.getHeight());

        if (item.alignSelf == CSSProperty.AlignSelf.INHERIT) {
            CSSProperty.AlignSelf parentAlignSelf = owner.style.getProperty("align-self");
            if (parentAlignSelf != null && parentAlignSelf != CSSProperty.AlignSelf.AUTO && parentAlignSelf != CSSProperty.AlignSelf.INHERIT && parentAlignSelf != CSSProperty.AlignSelf.INITIAL) {
                item.alignSelf = parentAlignSelf;
            }
        } else if (item.alignSelf == FlexItem.ALIGN_SELF_FLEX_START)
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(0, y + getHeight() - item.getHeight());
            else
                item.setPosition(0, y);
        else if (item.alignSelf == FlexItem.ALIGN_SELF_FLEX_END)
            if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
                item.setPosition(0, y);
            else
                item.setPosition(0, y + getHeight() - item.getHeight());
        else if (item.alignSelf == FlexItem.ALIGN_SELF_CENTER)
            item.setPosition(0, y + (getHeight() - item.getHeight()) / 2);
        else if (item.alignSelf == FlexItem.ALIGN_SELF_BASELINE)
            alignBaseline(item);
        else
            alignStretch(item);
        return true;
    }

    @Override
    protected void applyJustifyContent(FlexItem item, float widthOfPreviousItems, int j, float totalWidthOfItems) {
        if (owner.justifyContent == FlexBox.JUSTIFY_CONTENT_FLEX_END) {
            if (owner.isDirectionReversed())
                item.setPosition(widthOfPreviousItems, item.bounds.y);
            else
                item.setPosition(owner.mainSize - widthOfPreviousItems - item.bounds.width, item.bounds.y);
        } else if (owner.justifyContent == FlexBox.JUSTIFY_CONTENT_CENTER) {
            float halfOfRemainSpace = (owner.mainSize) / 2;
            item.setPosition(halfOfRemainSpace - totalWidthOfItems / 2 + widthOfPreviousItems, item.bounds.y);

        } else if (owner.justifyContent == FlexBox.JUSTIFY_CONTENT_SPACE_BETWEEN) {
            if (itemsInLine.size() - 1 == 0 || totalWidthOfItems > owner.mainSize) {
                if (owner.isDirectionReversed())
                    item.setPosition(owner.mainSize - widthOfPreviousItems - item.bounds.width, item.bounds.y);
                else
                    item.setPosition(widthOfPreviousItems, item.bounds.y);
            } else {
                float spaceBetween;

                if (owner.isDirectionReversed()) {
                    if (j == 0) {
                        item.setPosition(owner.mainSize - item.bounds.width, item.bounds.y);
                    } else if (j == itemsInLine.size() - 1) {
                        item.setPosition(0, item.bounds.y);
                        spaceBetween = getRemainingMainSpace() / (itemsInLine.size() - 1);
                        item.setPosition(spaceBetween * (itemsInLine.size() - 1 - j) + totalWidthOfItems - widthOfPreviousItems - item.bounds.width, item.bounds.y);
                    }
                } else {
                    if (j == 0) {
                        item.setPosition(0, item.bounds.y);
                        spaceBetween = getRemainingMainSpace() / (itemsInLine.size() - 1);
                        item.setPosition(spaceBetween * j + widthOfPreviousItems, item.bounds.y);
                    }
                }
            }

        } else if (owner.justifyContent == FlexBox.JUSTIFY_CONTENT_SPACE_AROUND) {
            if (itemsInLine.size() - 1 == 0 || totalWidthOfItems > owner.mainSize) {
                float halfOfRemainSpace = (owner.bounds.x + owner.mainSize) / 2;
                item.setPosition(halfOfRemainSpace - totalWidthOfItems / 2 + widthOfPreviousItems, item.bounds.y);
            } else {
                float spaceAround = getRemainingMainSpace() / (itemsInLine.size());
                spaceAround /= 2;
                if (owner.isDirectionReversed()) {
                    item.setPosition(spaceAround + (itemsInLine.size() - 1 - j) * 2 * spaceAround + widthOfPreviousItems, item.bounds.y);
                } else {
                    item.setPosition(j * 2 * spaceAround + widthOfPreviousItems + spaceAround, item.bounds.y);
                }
            }
        } else {
            if (owner.isDirectionReversed())
                item.setPosition(owner.mainSize - widthOfPreviousItems - item.bounds.width, item.bounds.y);
            else
                item.setPosition(widthOfPreviousItems, item.bounds.y);
        }
    }


    @Override
    protected void alignByJustifyContent() {
        int widthOfPreviousItems = 0;
        int totalWidthOfItems = 0;
        for (FlexItem flexItem : itemsInLine) totalWidthOfItems += flexItem.getWidth();

        if ((owner.isDirectionReversed() || owner.justifyContent == FlexBox.JUSTIFY_CONTENT_FLEX_END) &&
                owner.justifyContent != FlexBox.JUSTIFY_CONTENT_SPACE_BETWEEN &&
                owner.justifyContent != FlexBox.JUSTIFY_CONTENT_FLEX_START) {
            for (int j = itemsInLine.size() - 1; j >= 0; j--) {
                FlexItem item = itemsInLine.get(j);
                applyJustifyContent(item, widthOfPreviousItems, j, totalWidthOfItems);
                widthOfPreviousItems += item.getWidth();
            }
        } else {
            for (int j = 0; j < itemsInLine.size(); j++) {
                FlexItem item = itemsInLine.get(j);
                applyJustifyContent(item, widthOfPreviousItems, j, totalWidthOfItems);
                widthOfPreviousItems += item.getWidth();
            }
        }
    }

    /**
     * Aligns flex item with baseline value of align-items/self property
     *
     * @param item flex item
     */
    private void alignBaseline(FlexItem item) {
        if (item.getPadding().top + item.getMargin().top + item.ctx.getBaselineOffset() > refItem.getPadding().top + refItem.getMargin().top + refItem.ctx.getBaselineOffset()) {
            refItem = item;
        }
        for (FlexItem itemInLine : itemsInLine) {
            if ((!itemInLine.isNotAlignSelfAuto() && owner.alignItems == CSSProperty.AlignItems.BASELINE) || itemInLine.alignSelf == CSSProperty.AlignSelf.BASELINE) {
                if (itemInLine != refItem) {
                    itemInLine.setPosition(itemInLine.bounds.x, y + refItem.getPadding().top + refItem.getMargin().top + refItem.ctx.getBaselineOffset()
                            - itemInLine.getPadding().top - itemInLine.getMargin().top - itemInLine.ctx.getBaselineOffset());
                    if (getHeight() < (refItem.getPadding().top + refItem.getMargin().top + refItem.ctx.getBaselineOffset()
                            - itemInLine.getPadding().top - itemInLine.getMargin().top - itemInLine.ctx.getBaselineOffset() + itemInLine.bounds.height)) {
                        setHeight((refItem.getPadding().top + refItem.getMargin().top + refItem.ctx.getBaselineOffset() - itemInLine.getPadding().top - itemInLine.getMargin().top - itemInLine.ctx.getBaselineOffset() + itemInLine.bounds.height));
                    }
                    owner.fixContainerHeight(itemInLine);
                }
                item.setPosition(0, y + refItem.getPadding().top + refItem.getMargin().top + refItem.ctx.getBaselineOffset() - item.getPadding().top - item.getMargin().top - item.ctx.getBaselineOffset());
            } else {
                item.setPosition(0, y);
            }
        }
    }

    /**
     * Aligns flex item with stretch value of align-items/self property
     *
     * @param item flex item
     */
    private void alignStretch(FlexItem item) {
        if (!item.hasFixedHeight()) {
            item.bounds.height = getHeight();
            processHeight(item);
        }

        if (owner.flexWrap == CSSProperty.FlexWrap.NOWRAP) {
            if (owner.hasFixedHeight() && getHeight() > owner.getContentHeight()) {
                if (!item.hasFixedHeight()) {
                    if (owner.style.getProperty("height") != CSSProperty.Height.length)
                        owner.setContentHeight(getHeight());

                    item.bounds.height = owner.getContentHeight();
                    processHeight(item);
                }
            }
        }
        item.content.height = item.bounds.height - item.padding.top - item.padding.bottom - item.border.top - item.border.bottom - item.margin.top - item.margin.bottom;

        if (owner.flexWrap == FlexBox.FLEX_WRAP_WRAP_REVERSE)
            item.setPosition(0, y + getHeight() - item.getHeight());
        else
            item.setPosition(0, y);
    }

    /**
     * Auxiliary method for processing height
     *
     * @param item flex item
     */
    private void processHeight(FlexItem item) {
        if (item.bounds.height > item.max_size.height && item.max_size.height != -1) {
            item.bounds.height = item.max_size.height + item.padding.top + item.padding.bottom + item.border.top + item.border.bottom + item.margin.top + item.margin.bottom;
        }
    }
}