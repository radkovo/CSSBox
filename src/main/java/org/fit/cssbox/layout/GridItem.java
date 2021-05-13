package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.csskit.TermIntegerImpl;
import org.w3c.dom.Element;

/**
 *
 */
public class GridItem extends BlockBox implements Comparable<GridItem> {

    /**
     * Declare coordinates of grid item
     */
    protected GridItemCoords gridItemCoords;

    /**
     * Width of item
     */
    protected float widthOfGridItem;

    /**
     * Height of item
     */
    protected float heightOfGridItem;

    /**
     * Distance of grid item from left edge
     */
    protected float columnDistanceFromZero;

    /**
     * Distance of grid item from top edge
     */
    protected float rowDistanceFromZero;

    /**
     * Indicates count of column gaps in explicit grid
     */
    protected int w;

    /**
     * Indicates count of row gaps in explicit grid
     */
    protected int h;

    /**
     *
     */
    protected int gridItemOrderValue;

    /**
     *
     * @param n
     * @param ctx
     */
    public GridItem(Element n, VisualContext ctx) {
        super(n, ctx);

        isblock = true;
        widthOfGridItem = 0;
        heightOfGridItem = 0;
        columnDistanceFromZero = 0;
        rowDistanceFromZero = 0;
        w = 0;
        h = 0;
    }

    /**
     *
     * @param src
     */
    public GridItem(InlineBox src) {
        super(src.el, src.ctx);

        isblock = true;

        setStyle(src.getStyle());

        widthOfGridItem = 0;
        heightOfGridItem = 0;
        columnDistanceFromZero = 0;
        rowDistanceFromZero = 0;
        w = 0;
        h = 0;
    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadGridItemStyles();
    }

    /**
     *
     * @return
     */
    public int getGridItemOrderValue() {
        return gridItemOrderValue;
    }

    @Override
    public int compareTo(GridItem i) {
        return this.gridItemOrderValue - i.getGridItemOrderValue();
    }

    /**
     *
     */
    private void loadGridItemStyles() {
        gridItemCoords = new GridItemCoords();

        loadGridItemRows();
        loadGridItemColumns();

        TermIntegerImpl len = (TermIntegerImpl) style.getValue("order", false);
        gridItemOrderValue = 0;
        if (len != null)
            gridItemOrderValue = len.getIntValue();
        System.out.println("order grid itemu je: " + gridItemOrderValue);
    }

    /**
     *
     */
    protected void loadGridItemRows() {
        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.GridStartEnd gridStart = style.getProperty("grid-row-start");
        CSSProperty.GridStartEnd gridEnd = style.getProperty("grid-row-end");

        if (gridStart == null) {
            gridStart = CSSProperty.GridStartEnd.AUTO;
        }

        if (gridStart == CSSProperty.GridStartEnd.number) {
            gridItemCoords.rowStart = (int) dec.getLength(getLengthValue("grid-row-start"), false, 0, 0, 0);
        } else if (gridStart == CSSProperty.GridStartEnd.AUTO) {
            gridItemCoords.rowStart = 0;
        } else if (gridStart == CSSProperty.GridStartEnd.component_values) {
            gridItemCoords.rowStartSpan = style.getValue(TermList.class, "grid-row-start");
        }

        if (gridEnd == null) {
            gridEnd = CSSProperty.GridStartEnd.AUTO;
        }

        if (gridEnd == CSSProperty.GridStartEnd.AUTO && gridItemCoords.rowStart != 0) {
            gridItemCoords.rowEnd = gridItemCoords.rowStart + 1;
        } else if (gridEnd == CSSProperty.GridStartEnd.number) {
            gridItemCoords.rowEnd = (int) dec.getLength(getLengthValue("grid-row-end"), false, 0, 0, 0);
            if (gridItemCoords.rowStart == gridItemCoords.rowEnd) {
                gridItemCoords.rowEnd = gridItemCoords.rowStart + 1;
            } else if (gridItemCoords.rowStart > gridItemCoords.rowEnd) {
                int tmp = gridItemCoords.rowStart;
                gridItemCoords.rowStart = gridItemCoords.rowEnd;
                gridItemCoords.rowEnd = tmp;
            }
        } else if (gridEnd == CSSProperty.GridStartEnd.component_values) {
            gridItemCoords.rowEndSpan = style.getValue(TermList.class, "grid-row-end");
            gridItemCoords.rowEnd = (int) dec.getLength((TermLengthOrPercent) gridItemCoords.rowEndSpan.get(1), false, 0, 0, 0) + gridItemCoords.rowStart;
        } else if (gridEnd == CSSProperty.GridStartEnd.AUTO) {
            if (gridItemCoords.rowStartSpan != null) {
                gridItemCoords.rowStart = 1;
                gridItemCoords.rowEnd = (int) dec.getLength((TermLengthOrPercent) gridItemCoords.rowStartSpan.get(1), false, 0, 0, 0) + 1;
                gridItemCoords.rowStartSpan = null;
            } else {
                gridItemCoords.rowEnd = 0;
            }
        }
        if (gridItemCoords.rowStartSpan != null) {
            gridItemCoords.rowStart = gridItemCoords.rowEnd - (int) dec.getLength((TermLengthOrPercent) gridItemCoords.rowStartSpan.get(1), false, 0, 0, 0);
        }
        if (gridItemCoords.rowStart == 0 && gridItemCoords.rowEnd != 0) {
            gridItemCoords.rowStart = gridItemCoords.rowEnd - 1;
        }
    }

    /**
     *
     */
    protected void loadGridItemColumns() {
        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.GridStartEnd gridStart = style.getProperty("grid-column-start");
        CSSProperty.GridStartEnd gridEnd = style.getProperty("grid-column-end");

        if (gridStart == null) {
            gridStart = CSSProperty.GridStartEnd.AUTO;
        }

        if (gridStart == CSSProperty.GridStartEnd.number) {
            gridItemCoords.columnStart = (int) dec.getLength(getLengthValue("grid-column-start"), false, 0, 0, 0);
        } else if (gridStart == CSSProperty.GridStartEnd.AUTO) {
            gridItemCoords.columnStart = 0;
        } else if (gridStart == CSSProperty.GridStartEnd.component_values) {
            gridItemCoords.columnStartSpan = style.getValue(TermList.class, "grid-column-start");
        }


        if (gridEnd == null) {
            gridEnd = CSSProperty.GridStartEnd.AUTO;
        }

        if (gridEnd == CSSProperty.GridStartEnd.AUTO && gridItemCoords.columnStart != 0) {
            gridItemCoords.columnEnd = gridItemCoords.columnStart + 1;
        } else if (gridEnd == CSSProperty.GridStartEnd.number) {
            gridItemCoords.columnEnd = (int) dec.getLength(getLengthValue("grid-column-end"), false, 0, 0, 0);
            if (gridItemCoords.columnStart == gridItemCoords.columnEnd) {
                gridItemCoords.columnEnd = gridItemCoords.columnStart + 1;
            } else if (gridItemCoords.columnStart > gridItemCoords.columnEnd) {
                int tmp = gridItemCoords.columnStart;
                gridItemCoords.columnStart = gridItemCoords.columnEnd;
                gridItemCoords.columnEnd = tmp;
            }
        } else if (gridEnd == CSSProperty.GridStartEnd.component_values) {
            gridItemCoords.columnEndSpan = style.getValue(TermList.class, "grid-column-end");
            gridItemCoords.columnEnd = (int) dec.getLength((TermLengthOrPercent) gridItemCoords.columnEndSpan.get(1), false, 0, 0, 0) + gridItemCoords.columnStart;
        } else if (gridEnd == CSSProperty.GridStartEnd.AUTO) {
            if (gridItemCoords.columnStartSpan != null) {
                gridItemCoords.columnStart = 1;
                gridItemCoords.columnEnd = (int) dec.getLength((TermLengthOrPercent) gridItemCoords.columnStartSpan.get(1), false, 0, 0, 0) + 1;
                gridItemCoords.columnStartSpan = null;
            } else {
                gridItemCoords.columnEnd = 0;
            }
        }

        if (gridItemCoords.columnStartSpan != null) {
            gridItemCoords.columnStart = gridItemCoords.columnEnd - (int) dec.getLength((TermLengthOrPercent) gridItemCoords.columnStartSpan.get(1), false, 0, 0, 0);
        }

        if (gridItemCoords.columnStart == 0 && gridItemCoords.columnEnd != 0) {
            gridItemCoords.columnStart = gridItemCoords.columnEnd - 1;
        }
    }

    /**
     *
     * @param gridBox
     */
    public void setWidthOfItem(GridBox gridBox) {
        CSSDecoder dec = new CSSDecoder(ctx);
        float countGridColumnGapsInItem;

        for (int j = gridItemCoords.columnStart - 1; j < gridItemCoords.columnEnd - 1; j++) {
            //very basic solution of repeat notation
            if (gridBox.gridTemplateColumnValues == null && gridBox.arrayofcolumns.size() != 0) {
                widthOfGridItem += gridBox.arrayofcolumns.get(j);
                break;
            }
            if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues == null) {
                widthOfGridItem += gridBox.gridAutoColumns;
            } else if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues != null) {
                if ((j + 1) >= gridBox.gridTemplateColumnValues.size() + 1) {
                    widthOfGridItem += gridBox.gridAutoColumns;
                } else if ((j + 1) < gridBox.gridTemplateColumnValues.size()) {
                    widthOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                } else {
                    widthOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                }
            } else {
                if (gridBox.isGridTemplateColumnsNone) widthOfGridItem = gridBox.getContentWidth();
                else {
                    if (gridBox.isGridTemplateColumnsAuto) {
                        widthOfGridItem = gridBox.getContentWidth();
                    } else {
                        if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth()) == -1) {
                            if (gridItemCoords.columnEnd - gridItemCoords.columnStart > 1) {
                                if (gridBox.gridTemplateColumnValues.get(j).getValue().toString().equals("min-content") ||
                                        gridBox.gridTemplateColumnValues.get(j).getValue().toString().equals("max-content"))
                                    widthOfGridItem = 0;
                            } else {
                                switch (gridBox.gridTemplateColumnValues.get(gridItemCoords.columnStart - 1).getValue().toString()) {
                                    case "min-content":
                                        widthOfGridItem = getMinimalWidth();
                                        break;
                                    case "max-content":
                                        widthOfGridItem = getMaximalWidth();
                                        break;
                                    case "auto":
                                        widthOfGridItem = 0;
                                        break;
                                    default:
                                        widthOfGridItem = gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                                        break;
                                }
                            }
                        } else {
                            widthOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                        }
                    }
                }
            }
        }
        countGridColumnGapsInItem = gridItemCoords.columnEnd - gridItemCoords.columnStart - 1;
        widthOfGridItem += countGridColumnGapsInItem * gridBox.gapColumn;
    }

    /**
     *
     * @param gridBox
     */
    public void setHeightOfItem(GridBox gridBox) {
        CSSDecoder dec = new CSSDecoder(ctx);
        float countGridRowGapsInItem;
        for (int j = gridItemCoords.rowStart - 1; j < gridItemCoords.rowEnd - 1; j++) {

            //very basic solution of repeat notation
            if (gridBox.gridTemplateRowValues == null && gridBox.arrayofrows.size() != 0) {
                heightOfGridItem += gridBox.arrayofrows.get(j);
                break;
            }

            if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues == null) {
                if (gridBox.gridTemplateRowValues == null) {
                    if (gridItemCoords.rowStart > 1) {
                        if (gridBox.gridAutoRows == 0) {
                            heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                        } else
                            heightOfGridItem += gridBox.gridAutoRows;
                    } else {
                        if (gridItemCoords.rowStart == 1) {
                            if (gridBox.gridAutoRows == 0) {
                                heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                            } else
                                heightOfGridItem += gridBox.gridAutoRows;
                        } else {
                            heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                        }
                    }
                } else heightOfGridItem += gridBox.gridAutoRows;
            } else if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues != null) {
                if ((j + 1) >= gridBox.gridTemplateRowValues.size() + 1) {
                    if (gridBox.isMinContentAutoRow || gridBox.isMaxContentAutoRow) {
                        heightOfGridItem += getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                    } else heightOfGridItem += gridBox.gridAutoRows;
                } else if ((j + 1) < gridBox.gridTemplateRowValues.size()) {
                    if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                        heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                    } else {
                        heightOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
                    }
                } else {
                    if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                        heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                    } else {
                        heightOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
                    }
                }
            } else {
                if (!gridBox.isGridTemplateRows) {
                    if (gridBox.isGridTemplateRowsAuto) {
                        heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                    } else {
                        if (containsBlocks() || containsFlow()) {
                            heightOfGridItem = getSubBox(getSubBoxNumber() - 1).getContentY() + getSubBox(getSubBoxNumber() - 1).getContentHeight();
                            heightOfGridItem += margin.top + margin.bottom + border.top + border.bottom + padding.top + padding.bottom;
                        }
                    }
                } else {
                    if (gridItemCoords.rowStart <= gridBox.gridTemplateRowValues.size()) {
                        if (gridBox.gridTemplateRowValues.get(gridItemCoords.rowStart - 1).getValue().toString().equals("auto") ||
                                gridBox.gridTemplateRowValues.get(gridItemCoords.rowStart - 1).getValue().toString().equals("min-content") ||
                                gridBox.gridTemplateRowValues.get(gridItemCoords.rowStart - 1).getValue().toString().equals("max-content")) {
                            heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                        } else {
                            if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                                heightOfGridItem += gridBox.gapRow;
                            } else
                                heightOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
                        }
                    } else {
                        if (!gridBox.isGridAutoRow) {
                            heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                        } else {
                            if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                                heightOfGridItem = getContentHeight() + padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
                            } else {
                                heightOfGridItem += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
                            }
                        }
                    }
                }
            }
        }
        countGridRowGapsInItem = gridItemCoords.rowEnd - gridItemCoords.rowStart - 1;
        heightOfGridItem += countGridRowGapsInItem * gridBox.gapRow;
    }

//    /**
//     *
//     * @param gridBox
//     */
//    public void setDistanceInHorizontalDirection(GridBox gridBox) {
//        CSSDecoder dec = new CSSDecoder(ctx);
//        for (int j = 0; j < gridItemCoords.columnStart - 1; j++) {
//            if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues == null) {
//                columnDistanceFromZero += gridBox.gridAutoColumns;
//            } else if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues != null) {
//                if (gridItemCoords.columnStart >= gridBox.gridTemplateColumnValues.size() + 1) {
//                    columnDistanceFromZero = gridBox.sumOfLengthForGridTemplateColumnRow(dec, gridBox.gridTemplateColumnValues, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
//                    w++;
//                } else {
//                    columnDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
//                }
//            } else {
//                if (!gridBox.containsOnlyUnit(gridBox.gridTemplateColumnValues)) {
//                    if (gridItemCoords.columnStart == 1) {
//                        columnDistanceFromZero = 0;
//                        System.out.println("dsfgfhgjhghkjgkhg");
//                    } else {
//                        columnDistanceFromZero += gridBox.arrayofcolumns.get(j);
//                        System.out.println("okkpkpkpkp");
//                    }
//                } else {
//                    columnDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
//                }
//            }
//        }
//        columnDistanceFromZero += (gridItemCoords.columnStart - 1) * gridBox.gapColumn;
//        if (w != 0) {
//            columnDistanceFromZero += (w - gridBox.gridTemplateColumnValues.size()) * gridBox.gridAutoColumns;
//        }
//    }

//    /**
//     *
//     * @param gridBox
//     */
//    public void setDistanceInVerticalDirection(GridBox gridBox) {
//        CSSDecoder dec = new CSSDecoder(ctx);
//        for (int j = 0; j < gridItemCoords.rowStart - 1; j++) {
//            if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues == null) {
//                rowDistanceFromZero += gridBox.arrayofrows.get(j);
//            } else if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues != null) {
//                if (gridItemCoords.rowStart >= gridBox.gridTemplateRowValues.size() + 1) {
//                    if (gridBox.sumOfLengthForGridTemplateColumnRow(dec, gridBox.gridTemplateRowValues, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
//                        rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                    } else {
//                        rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                    }
//                } else {
//                    if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
//                        rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                    } else {
//                        rowDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitColumn, gridBox.getContentHeight());
//                    }
//                }
//            } else {
//                if (gridBox.isGridTemplateRowsAuto) {
//                    rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                } else {
//                    if (!gridBox.containsOnlyUnit(gridBox.gridTemplateRowValues)) {
//                        if (gridItemCoords.rowStart == 1) {
//                            rowDistanceFromZero = 0;
//                        } else {
//                            rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                        }
//                    } else {
//                        if (gridItemCoords.rowStart > gridBox.gridTemplateRowValues.size()) {
//                            rowDistanceFromZero += gridBox.arrayofrows.get(j);
//                        } else
//                            rowDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
//                    }
//                }
//            }
//        }
//        rowDistanceFromZero += (gridItemCoords.rowStart - 1) * gridBox.gapRow;
//    }
}
