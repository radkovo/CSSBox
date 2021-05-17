package org.fit.cssbox.layout;

import cz.vutbr.web.css.*;

import java.util.ArrayList;

/**
 * Layout manager of Grid layout
 *
 * @author Ondra, Ondry
 */
public class GridLayoutManager implements LayoutManager {

    /**
     * Grid container
     */
    protected GridBox gridBox;

    /**
     * Creates a new instance of Grid layout manager
     *
     * @param gridBox grid container
     */
    public GridLayoutManager(GridBox gridBox) {
        this.gridBox = gridBox;
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

        if (!gridBox.displayed) {
            gridBox.content.setSize(0, 0);
            gridBox.bounds.setSize(0, 0);
        }

        float myconth = 0.0f;
        gridBox.isGridAutoRow = gridBox.isGridAutoRows(gridBox.getContentHeight());
        gridBox.isGridAutoColumn = gridBox.isGridAutoColumns(gridBox.getContentWidth());

        gridBox.containsRepeatInColumns();
        gridBox.containsRepeatInRows();
        setSizeOfGrid();
        processAutomaticItems();


        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem gridItem = (GridItem) gridBox.getSubBox(i);
            System.out.println(gridItem.gridItemCoords.toString());
            if (gridBox.isGridAutoColumn || !gridBox.isGridTemplateColumnsNone) {
                if (findUnitsForFr(gridBox.gridTemplateColumnValues, gridBox.gapColumn, gridBox.getContentWidth())) {
                    gridBox.oneFrUnitColumn = computingFrUnits(gridBox.flexFactorSum, gridBox.sumOfPixels, gridBox.content.width);
                }

                gridBox.flexFactorSum = 0;
                gridBox.sumOfPixels = 0;
            }

            gridBox.containsRepeatInColumns();
            gridBox.containsRepeatInRows();
            gridItem.setWidthOfItem(gridBox);
            gridItem.setAvailableWidth(gridItem.widthOfGridItem);


            if (gridBox.getContentHeight() <= 0) {
                if (!gridBox.isGridAutoRow && gridBox.gridTemplateRowValues != null) {
                    findUnitsForFr(gridBox.gridTemplateRowValues, gridBox.gapRow, gridBox.getContentHeight());
                    myconth += gridBox.sumOfPixels;
                    gridBox.sumOfPixels = 0;
                    gridBox.flexFactorSum = 0;
                    gridBox.setContentHeight(myconth);
                }
            }

            if (!gridBox.isGridAutoRow && gridBox.isGridTemplateRows) {
                if (findUnitsForFr(gridBox.gridTemplateRowValues, gridBox.gapRow, gridBox.getContentHeight())) {
                    gridBox.oneFrUnitRow = computingFrUnits(gridBox.flexFactorSum, gridBox.sumOfPixels, gridBox.getContentHeight());
                }
                gridBox.flexFactorSum = 0;
                gridBox.sumOfPixels = 0;
            }
        }

        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);
            checkColumnLine(griditem.gridItemCoords.columnStart);
        }

        fillColumnsSizesToArray();

        boolean isAuto = gridBox.processColumnAuto();
        if (isAuto) {
            for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                GridItem griditem = (GridItem) gridBox.getSubBox(i);
                if (griditem.widthOfGridItem == 0) {
                    griditem.widthOfGridItem = gridBox.arrayOfColumns.get(griditem.gridItemCoords.columnStart - 1);
                    griditem.setAvailableWidth(griditem.widthOfGridItem);
                    if (griditem.widthOfGridItem < griditem.getMaximalWidth()) {
                        griditem.setAvailableWidth(griditem.getMaximalWidth());
                        griditem.widthOfGridItem = griditem.getMaximalWidth();
                    }
                }

            }
        }

        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);
            checkNewSizeOfColumnsBigItems();

            griditem.setContentWidth(griditem.widthOfGridItem);
            griditem.doLayout(griditem.content.width, force, linestart);

            griditem.setHeightOfItem(gridBox);
        }

        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);
            checkRowLine(griditem.gridItemCoords.rowStart);
        }

        fillRowsSizesToArray();

        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);

            if (!checkBeforeNewSizes()) checkNewSizeOfRowsBigItems();

            setDistanceInHorizontalDirection(gridBox, griditem);
            setDistanceInVerticalDirection(gridBox, griditem);

            griditem.content.width = griditem.widthOfGridItem - griditem.margin.left - griditem.margin.right - griditem.border.left - griditem.border.right - griditem.padding.left - griditem.padding.right;
            griditem.content.height = griditem.heightOfGridItem - griditem.margin.top - griditem.margin.bottom - griditem.border.top - griditem.border.bottom - griditem.padding.top - griditem.padding.bottom;

            griditem.bounds.width = griditem.widthOfGridItem;
            griditem.bounds.height = griditem.heightOfGridItem;

            griditem.bounds.x = griditem.columnDistanceFromZero;
            griditem.bounds.y = griditem.rowDistanceFromZero;

            griditem.setPosition(griditem.bounds.x, griditem.bounds.y);

            if (gridBox.style.getProperty("height") == CSSProperty.Height.AUTO || gridBox.style.getProperty("height") == null) {
                if (gridBox.max_size.height == -1) {
                    if (griditem.bounds.y + griditem.totalHeight() > gridBox.getContentHeight()) {
                        gridBox.setContentHeight(griditem.bounds.y + griditem.totalHeight());
                    }
                } else {
                    if (griditem.bounds.y + griditem.totalHeight() < gridBox.max_size.height) {
                        if (griditem.bounds.y + griditem.totalHeight() > gridBox.getContentHeight()) {
                            gridBox.setContentHeight(griditem.bounds.y + griditem.totalHeight());
                        }
                    } else {
                        gridBox.setContentHeight(gridBox.max_size.height);
                    }
                }
            }

        }

        if (gridBox.getSizeOfArrayOfRows() > gridBox.content.height)
            gridBox.content.height = gridBox.getSizeOfArrayOfRows();

        if (gridBox.isRepeatRows) {
            gridBox.content.height = 0;
            gridBox.bounds.height = 0;
            for (float i : gridBox.arrayOfRows) {
                gridBox.content.height += i;
            }
        }

        gridBox.bounds.height += gridBox.content.height + gridBox.emargin.top + gridBox.emargin.bottom +
                gridBox.padding.top + gridBox.padding.bottom + gridBox.border.top + gridBox.border.bottom;
        return true;
    }

    /**
     * Determines grid size
     */
    protected void setSizeOfGrid() {
        for (int i = 1; i <= gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i - 1);
            if (griditem.gridItemCoords.columnEnd > gridBox.maxColumnLine) {
                gridBox.maxColumnLine = griditem.gridItemCoords.columnEnd;
            } else if (griditem.gridItemCoords.columnEnd == 0 && griditem.gridItemCoords.columnStart == 0 && gridBox.maxColumnLine == 1) {
                if (gridBox.isGridAutoFlowRow) {
                    if (gridBox.isGridTemplateColumns) {
                        gridBox.maxColumnLine = gridBox.gridTemplateColumnValues.size() + 1;
                    } else {
                        gridBox.maxColumnLine = 2;
                    }
                } else {
                    gridBox.maxColumnLine = i + 1;
                }
            }

            if (griditem.gridItemCoords.rowEnd > gridBox.maxRowLine) {
                gridBox.maxRowLine = griditem.gridItemCoords.rowEnd;
            } else if (griditem.gridItemCoords.rowEnd == 0 && griditem.gridItemCoords.rowStart == 0 && gridBox.maxRowLine == 1) {
                if (!gridBox.isGridAutoFlowRow) {
                    if (gridBox.isGridTemplateRows) {
                        gridBox.maxRowLine = gridBox.gridTemplateRowValues.size() + 1;
                    } else {
                        gridBox.maxRowLine = 2;
                    }
                } else {
                    gridBox.maxRowLine = i + 1;
                }
            }
        }

        if (gridBox.gridTemplateColumnValues != null) {
            if (gridBox.maxColumnLine < gridBox.gridTemplateColumnValues.size() + 1) {
                gridBox.maxColumnLine = gridBox.gridTemplateColumnValues.size() + 1;
            }
        }

        if (gridBox.arrayOfColumns != null && !gridBox.isRepeatRows) {
            if (gridBox.maxColumnLine < gridBox.arrayOfColumns.size() + 1) {
                gridBox.maxColumnLine = gridBox.arrayOfColumns.size() + 1;
            }
        }

        if (gridBox.gridTemplateRowValues != null) {
            if (gridBox.maxRowLine < gridBox.gridTemplateRowValues.size() + 1) {
                gridBox.maxRowLine = gridBox.gridTemplateRowValues.size() + 1;
            }
        }
    }

    /**
     * Allocates item coordinates based on available grid space
     */
    protected void processAutomaticItems() {
        if (gridBox.isGridAutoFlowRow) {
            for (int a = 1; a < gridBox.maxRowLine; a++) {
                ArrayList<Integer> columnID = new ArrayList<>();
                for (int b = 1; b < gridBox.maxColumnLine; b++) {
                    columnID.add(b);
                }
                for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(i);
                    if (griditem.gridItemCoords.rowStart == a) {
                        for (int c = griditem.gridItemCoords.rowStart; c < griditem.gridItemCoords.columnEnd; c++) {
                            columnID.remove((Integer) c);
                        }
                    }
                }
                if (a > 1) {
                    for (int y = 1; y < a; y++) {
                        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                            GridItem griditem = (GridItem) gridBox.getSubBox(i);
                            if (griditem.gridItemCoords.rowStart == y) {
                                if (griditem.gridItemCoords.rowEnd >= a + 1) {
                                    for (int c = griditem.gridItemCoords.columnStart; c < griditem.gridItemCoords.columnEnd; c++) {
                                        columnID.remove((Integer) c);
                                    }
                                }
                            }
                        }
                    }
                }
                for (int w = 0; w < gridBox.getSubBoxNumber(); w++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(w);
                    if (griditem.gridItemCoords.rowStart == 0 && griditem.gridItemCoords.columnStart == 0) {
                        if (columnID.isEmpty()) break;
                        griditem.gridItemCoords.rowStart = a;
                        griditem.gridItemCoords.rowEnd = a + 1;
                        griditem.gridItemCoords.columnStart = columnID.get(0);
                        griditem.gridItemCoords.columnEnd = griditem.gridItemCoords.columnStart + 1;
                        columnID.remove(0);
                    }
                }
                for (int w = 0; w < gridBox.getSubBoxNumber(); w++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(w);
                    if (griditem.gridItemCoords.rowStart == 0 && griditem.gridItemCoords.columnStart == 0) {
                        if (a <= gridBox.maxRowLine) {
                            gridBox.maxRowLine += 1;
                            break;
                        }
                    }
                }
            }
        } else {
            for (int a = 1; a < gridBox.maxColumnLine; a++) {
                ArrayList<Integer> rowID = new ArrayList<>();
                for (int b = 1; b < gridBox.maxRowLine; b++) {
                    rowID.add(b);
                }
                for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(i);
                    if (griditem.gridItemCoords.columnStart == a) {
                        for (int c = griditem.gridItemCoords.rowStart; c < griditem.gridItemCoords.rowEnd; c++) {
                            rowID.remove((Integer) c);
                        }
                    }
                }
                if (a > 1) {
                    for (int y = 1; y < a; y++) {
                        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                            GridItem griditem = (GridItem) gridBox.getSubBox(i);
                            if (griditem.gridItemCoords.columnStart == y) {
                                if (griditem.gridItemCoords.columnEnd >= a + 1) {
                                    for (int c = griditem.gridItemCoords.rowStart; c < griditem.gridItemCoords.rowEnd; c++) {
                                        rowID.remove((Integer) c);
                                    }
                                }
                            }
                        }
                    }
                }
                for (int w = 0; w < gridBox.getSubBoxNumber(); w++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(w);
                    if (griditem.gridItemCoords.columnStart == 0 && griditem.gridItemCoords.rowStart == 0) {
                        if (rowID.isEmpty()) break;
                        griditem.gridItemCoords.columnStart = a;
                        griditem.gridItemCoords.columnEnd = a + 1;
                        griditem.gridItemCoords.rowStart = rowID.get(0);
                        griditem.gridItemCoords.rowEnd = griditem.gridItemCoords.rowStart + 1;
                        rowID.remove(0);
                    }
                }
                for (int w = 0; w < gridBox.getSubBoxNumber(); w++) {
                    GridItem griditem = (GridItem) gridBox.getSubBox(w);
                    if (griditem.gridItemCoords.rowStart == 0 && griditem.gridItemCoords.columnStart == 0) {
                        if (a < gridBox.maxColumnLine) {
                            gridBox.maxColumnLine += 1;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Searches units fr in grid tracks
     *
     * @param tmp   termList of sizes
     * @param gap   size of gaps
     * @param contw available width
     * @return true if contains unit fr, else false if not
     */
    protected boolean findUnitsForFr(TermList tmp, float gap, float contw) {
        CSSDecoder dec = new CSSDecoder(gridBox.ctx);
        TermNumeric.Unit unit;
        TermLengthOrPercent a;

        if (tmp == null) return false;

        for (Term<?> term : tmp) {
            if (term.getValue().toString().equals("auto") ||
                    term.getValue().toString().equals("min-content") ||
                    term.getValue().toString().equals("max-content")) {
                return false;
            }
        }

        boolean containFr = false;
        float b;
        int j = 0;

        for (Term<?> term : tmp) {
            a = (TermLengthOrPercent) term;
            unit = a.getUnit();

            if (unit == TermNumeric.Unit.fr) {
                b = dec.getLength(a, false, 0, 0, contw);
                gridBox.flexFactorSum += b;
                if (gridBox.flexFactorSum < 1) {
                    gridBox.flexFactorSum = 1;
                }
                containFr = true;
            } else {
                b = dec.getLength(a, false, 0, 0, contw);
                gridBox.sumOfPixels += b;
            }
            j++;
        }
        gridBox.sumOfPixels += (j - 1) * gap;
        return containFr;
    }

    /**
     * Computes size of fr
     *
     * @param flexFactorSum fr units count
     * @param sumOfPixels   fixed units sum
     * @param availContent  available content
     * @return size of fr unit in pixels
     */
    protected float computingFrUnits(float flexFactorSum, float sumOfPixels, float availContent) {
        if (flexFactorSum == 0) return 0;
        else return (availContent - sumOfPixels) / flexFactorSum;
    }

    /**
     * Searches biggest width in column, then assigns it to all items in same column
     *
     * @param columnStart start coordinate of item
     */
    public void checkColumnLine(int columnStart) {
        float maxwidth = 0;
        GridItem griditem;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.columnStart == columnStart) {
                if ((griditem.gridItemCoords.columnEnd - griditem.gridItemCoords.columnStart) < 2) {
                    if (griditem.widthOfGridItem > maxwidth) {
                        maxwidth = griditem.widthOfGridItem;
                    }
                }
            }
        }
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.columnStart == columnStart) {
                if ((griditem.gridItemCoords.columnEnd - griditem.gridItemCoords.columnStart) < 2) {
                    griditem.widthOfGridItem = maxwidth;
                }
            }
        }
    }

    /**
     * Detects current column grid size, then fills list with these values
     */
    public void fillColumnsSizesToArray() {
        int maxactualcolumnline = 0;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.columnEnd > maxactualcolumnline) {
                maxactualcolumnline = griditem.gridItemCoords.columnEnd;
            }
        }

        for (int a = 1; a < maxactualcolumnline; a++) {
            for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                GridItem griditem = (GridItem) gridBox.getSubBox(i);
                if (griditem.gridItemCoords.columnStart == a) {
                    if (griditem.gridItemCoords.columnEnd - griditem.gridItemCoords.columnStart == 1) {
                        gridBox.arrayOfColumns.add(griditem.widthOfGridItem);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Computing new size of grid items
     */
    public void checkNewSizeOfColumnsBigItems() {
        GridItem griditem;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);
            if ((griditem.gridItemCoords.columnEnd - griditem.gridItemCoords.columnStart) > 1) {
                griditem.widthOfGridItem = 0;
                for (int j = griditem.gridItemCoords.columnStart; j < griditem.gridItemCoords.columnEnd; j++) {
                    try {
                        griditem.widthOfGridItem += gridBox.arrayOfColumns.get(j - 1);
                    } catch (IndexOutOfBoundsException e) {
                        if (gridBox.gridAutoColumns != 0) {
                            griditem.widthOfGridItem += gridBox.gridAutoColumns;
                        } else {
                            griditem.widthOfGridItem += gridBox.gapColumn;
                        }
                    }
                }
                griditem.widthOfGridItem += ((griditem.gridItemCoords.columnEnd - griditem.gridItemCoords.columnStart) - 1) * gridBox.gapColumn;
            }
        }
    }

    /**
     * Searches biggest height in row, then assigns it to all items in same row
     *
     * @param rowStart row coordinate of item
     */
    public void checkRowLine(int rowStart) {
        float maxheight = 0;
        GridItem griditem;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.rowStart == rowStart) {
                if ((griditem.gridItemCoords.rowEnd - griditem.gridItemCoords.rowStart) < 2) {
                    if (griditem.heightOfGridItem > maxheight) {
                        maxheight = griditem.heightOfGridItem;
                    }
                }
            }
        }
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.rowStart == rowStart) {
                if ((griditem.gridItemCoords.rowEnd - griditem.gridItemCoords.rowStart) < 2) {
                    griditem.heightOfGridItem = maxheight;
                }
            }
        }
    }

    /**
     * Detects current row grid size, then fills list with these values
     */
    public void fillRowsSizesToArray() {
        if (gridBox.gridTemplateRowValues != null) {
            getItemAndFill();

            if (gridBox.arrayOfRows.size() < gridBox.gridTemplateRowValues.size()) {
                for (int i = gridBox.arrayOfRows.size(); i < gridBox.gridTemplateRowValues.size(); i++) {
                    if (gridBox.gridTemplateRowValues.get(i).getValue().toString().equals("auto") ||
                            gridBox.gridTemplateRowValues.get(i).getValue().toString().equals("min-content") ||
                            gridBox.gridTemplateRowValues.get(i).getValue().toString().equals("max-content")) {
                        gridBox.arrayOfRows.add(i, 0f);
                    } else {
                        CSSDecoder decoder = new CSSDecoder(gridBox.ctx);
                        gridBox.arrayOfRows.add(i, decoder.getLength((TermLengthOrPercent) gridBox.gridTemplateRowValues.get(i), false, 0, 0, gridBox.getContentWidth()));
                    }
                }
            }
        } else {
            getItemAndFill();
        }
    }

    /**
     * Auxiliary method to fillRowsSizesToArray() method, that gets item and call next method
     */
    private void getItemAndFill() {
        int maxactualrowline = 0;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            GridItem griditem = (GridItem) gridBox.getSubBox(i);
            if (griditem.gridItemCoords.rowEnd > maxactualrowline) {
                maxactualrowline = griditem.gridItemCoords.rowEnd;
            }
        }
        onlyFill(maxactualrowline);
    }

    /**
     * Auxiliary method to fillRowsSizesToArray() method, that only fill list
     *
     * @param maxActualRowLine row size
     */
    public void onlyFill(int maxActualRowLine) {

        for (int a = 1; a < maxActualRowLine; a++) {
            for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
                GridItem griditem = (GridItem) gridBox.getSubBox(i);
                if (griditem.gridItemCoords.rowStart == a) {
                    if (griditem.gridItemCoords.rowEnd - griditem.gridItemCoords.rowStart == 1) {
                        gridBox.arrayOfRows.add(griditem.heightOfGridItem);
                        break;
                    }
                }
            }
        }
        if (gridBox.isRepeatRows) {
            int count = gridBox.countOfRepeated.getNumberOfRepetitions();
            if (gridBox.arrayOfRows.size() > count + 1) {
                gridBox.arrayOfRows.subList(count + 1, gridBox.arrayOfRows.size()).clear();
            }
        }
    }

    /**
     *  Checks auto, min-content or max-content values
     *
     * @return number
     */
    public boolean checkBeforeNewSizes() {
        int count = 0;
        if (gridBox.gridTemplateRowValues != null) {
            for (Term<?> gridTemplateRowValue : gridBox.gridTemplateRowValues) {
                if (((gridTemplateRowValue.getValue().toString().equals("auto") ||
                        gridTemplateRowValue.getValue().toString().equals("min-content") ||
                        gridTemplateRowValue.getValue().toString().equals("max-content")) && gridBox.gridAutoRows != 0) ||
                        ((gridTemplateRowValue.getValue().toString().equals("auto") ||
                                gridTemplateRowValue.getValue().toString().equals("min-content") ||
                                gridTemplateRowValue.getValue().toString().equals("max-content")) && gridBox.gridAutoRows == 0)) {
                    count++;
                }
            }
        } else count = 1;
        return count == 0;
    }

    /**
     * Computing new size of grid items
     */
    public void checkNewSizeOfRowsBigItems() {
        GridItem griditem;
        for (int i = 0; i < gridBox.getSubBoxNumber(); i++) {
            griditem = (GridItem) gridBox.getSubBox(i);

            if ((griditem.gridItemCoords.rowEnd - griditem.gridItemCoords.rowStart) > 1) {
                griditem.heightOfGridItem = 0;
                for (int j = griditem.gridItemCoords.rowStart; j < griditem.gridItemCoords.rowEnd; j++) {
                    try {
                        griditem.heightOfGridItem += gridBox.arrayOfRows.get(j - 1);
                    } catch (IndexOutOfBoundsException e) {
                        if (gridBox.gridAutoRows != 0) {
                            griditem.heightOfGridItem += gridBox.gridAutoRows;
                        } else {
                            griditem.heightOfGridItem += gridBox.gapRow;
                        }
                    }
                }
                griditem.heightOfGridItem += ((griditem.gridItemCoords.rowEnd - griditem.gridItemCoords.rowStart) - 1) * gridBox.gapRow;
            }
        }
    }

    /**
     * Sets distance of item from left corner
     *
     * @param gridBox grid container
     * @param item grid item
     */
    public void setDistanceInHorizontalDirection(GridBox gridBox, GridItem item) {
        CSSDecoder dec = new CSSDecoder(gridBox.ctx);
        for (int j = 0; j < item.gridItemCoords.columnStart - 1; j++) {
            if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues == null) {
                item.columnDistanceFromZero += gridBox.gridAutoColumns;
            } else if (gridBox.isGridAutoColumn && gridBox.gridTemplateColumnValues != null) {
                if (item.gridItemCoords.columnStart >= gridBox.gridTemplateColumnValues.size() + 1) {
                    item.columnDistanceFromZero = gridBox.sumOfLengthForGridTemplateColumnRow(dec, gridBox.gridTemplateColumnValues, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                    item.w++;
                } else {
                    item.columnDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                }
            } else {
                if (!gridBox.containsOnlyUnit(gridBox.gridTemplateColumnValues)) {
                    if (item.gridItemCoords.columnStart == 1) {
                        item.columnDistanceFromZero = 0;
                    } else {
                        item.columnDistanceFromZero += gridBox.arrayOfColumns.get(j);
                    }
                } else {
                    item.columnDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateColumnValues, j, gridBox.oneFrUnitColumn, gridBox.getContentWidth());
                }
            }
        }
        item.columnDistanceFromZero += (item.gridItemCoords.columnStart - 1) * gridBox.gapColumn;
        if (item.w != 0) {
            item.columnDistanceFromZero += (item.w - gridBox.gridTemplateColumnValues.size()) * gridBox.gridAutoColumns;
        }
    }

    /**
     * Sets distance of item from top corner
     *
     * @param gridBox grid container
     * @param item grid item
     */
    public void setDistanceInVerticalDirection(GridBox gridBox, GridItem item) {
        CSSDecoder dec = new CSSDecoder(gridBox.ctx);
        for (int j = 0; j < item.gridItemCoords.rowStart - 1; j++) {
            if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues == null) {
                item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
            } else if (gridBox.isGridAutoRow && gridBox.gridTemplateRowValues != null) {
                if (item.gridItemCoords.rowStart >= gridBox.gridTemplateRowValues.size() + 1) {
                    if (gridBox.sumOfLengthForGridTemplateColumnRow(dec, gridBox.gridTemplateRowValues, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                        item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                    } else {
                        item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                    }
                } else {
                    if (gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight()) == -1) {
                        item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                    } else {
                        item.rowDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitColumn, gridBox.getContentHeight());
                    }
                }
            } else {
                if (gridBox.isGridTemplateRowsAuto) {
                    item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                } else {
                    if (!gridBox.containsOnlyUnit(gridBox.gridTemplateRowValues)) {
                        if (item.gridItemCoords.rowStart == 1) {
                            item.rowDistanceFromZero = 0;
                        } else {
                            item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                        }
                    } else {
                        if (item.gridItemCoords.rowStart > gridBox.gridTemplateRowValues.size()) {
                            item.rowDistanceFromZero += gridBox.arrayOfRows.get(j);
                        } else
                            item.rowDistanceFromZero += gridBox.findSizeOfGridItem(dec, gridBox.gridTemplateRowValues, j, gridBox.oneFrUnitRow, gridBox.getContentHeight());
                    }
                }
            }
        }
        item.rowDistanceFromZero += (item.gridItemCoords.rowStart - 1) * gridBox.gapRow;
    }
}
