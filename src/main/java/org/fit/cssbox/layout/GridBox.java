package org.fit.cssbox.layout;

import cz.vutbr.web.css.*;
import cz.vutbr.web.csskit.fn.RepeatImpl;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * A box corresponding to a Grid container
 *
 * @author Ondra
 */
public class GridBox extends BlockBox {

    /**
     * Row gaps size
     */
    protected float gapRow;

    /**
     * Column gaps size
     */
    protected float gapColumn;

    /**
     * Row tracks in explicit grid
     */
    protected TermList gridTemplateRowValues;

    /**
     * Column tracks in explicit grid
     */
    protected TermList gridTemplateColumnValues;

    /**
     * Are specified rows in explicit grid?
     */
    protected boolean isGridTemplateRows;

    /**
     * Are specified columns in explicit grid?
     */
    protected boolean isGridTemplateColumns;

    /**
     * Is specified one auto value in explicit grid?
     */
    protected boolean isGridTemplateRowsAuto;

    /**
     * Is specified one auto value in explicit grid?
     */
    protected boolean isGridTemplateColumnsAuto;

    /**
     * Are specified columns in explicit grid?
     */
    protected boolean isGridTemplateColumnsNone;

    /**
     * Auto flow for automatic grid items
     */
    protected boolean isGridAutoFlowRow;

    /**
     * Rows of implicit grid
     */
    protected boolean isGridAutoRow;

    /**
     * Columns of implicit grid?
     */
    protected boolean isGridAutoColumn;

    /**
     * Row tracks values in implicit grid
     */
    protected float gridAutoRows;

    /**
     * Column tracks values in implicit grid
     */
    protected float gridAutoColumns;

    /**
     * Min-content of rows in implicit grid
     */
    protected boolean isMinContentAutoRow;

    /**
     * Max-content of rows in implicit grid
     */
    protected boolean isMaxContentAutoRow;

    /**
     * Min-content of columns in implicit grid
     */
    protected boolean isMinContentAutoColumn;

    /**
     * Max-content of columns in implicit grid
     */
    protected boolean isMaxContentAutoColumn;

    /**
     * Max row line of container
     */
    protected int maxRowLine;

    /**
     * Max column line of container
     */
    protected int maxColumnLine;

    /**
     * Rows values (in pixels) after computing auto, min-content or max-content values
     */
    protected ArrayList<Float> arrayOfRows = new ArrayList<>();

    /**
     * Columns values (in pixels) after computing auto, min-content or max-content values
     */
    protected ArrayList<Float> arrayOfColumns = new ArrayList<>();

    /**
     * Fr units sum
     */
    protected float flexFactorSum;

    /**
     * Fixed tracks count
     */
    protected float sumOfPixels;

    /**
     * 1fr size row
     */
    protected float oneFrUnitRow;

    /**
     * 1fr size column
     */
    protected float oneFrUnitColumn;

    /**
     * Repeated tracks sum of repeat function
     */
    protected TermFunction.Repeat.Unit countOfRepeated;

    /**
     * Repeat function in grid
     */
    protected boolean isRepeatRows;

    /**
     * Creates a new instance of Grid container
     */
    public GridBox(Element n, VisualContext ctx) {
        super(n, ctx);

        typeoflayout = new GridLayoutManager(this);
        isblock = true;
        isGridAutoRow = false;
        isGridAutoColumn = false;
        isMinContentAutoRow = false;
        isMaxContentAutoRow = false;
        isMinContentAutoColumn = false;
        isMaxContentAutoColumn = false;
        maxRowLine = 1;
        maxColumnLine = 1;
        flexFactorSum = 0;
        sumOfPixels = 0;
    }

    /**
     * Convert an inline box to a Grid container
     */
    public GridBox(InlineBox src) {
        super(src.el, src.ctx);

        typeoflayout = new GridLayoutManager(this);

        setStyle(src.getStyle());
        isblock = true;
        isGridAutoRow = false;
        isGridAutoColumn = false;
        isMinContentAutoRow = false;
        isMaxContentAutoRow = false;
        isMinContentAutoColumn = false;
        isMaxContentAutoColumn = false;
        maxRowLine = 1;
        maxColumnLine = 1;
        flexFactorSum = 0;
        sumOfPixels = 0;

    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadGridBoxStyles();
    }

    /**
     *
     */
    private void loadGridBoxStyles() {
        loadGridBoxGaps();
        loadGridTemplateRowColumn();
        loadGridAutoFlow();
    }

    /**
     * Loads styles according to Grid container
     */
    protected void loadGridBoxGaps() {

        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.GridGap tmpGapRow = style.getProperty("grid-row-gap");
        CSSProperty.GridGap tmpGapColumn = style.getProperty("grid-column-gap");

        if (tmpGapRow == null) {
            tmpGapRow = CSSProperty.GridGap.NORMAL;
        }

        if (tmpGapRow == CSSProperty.GridGap.length) {
            gapRow = dec.getLength(getLengthValue("grid-row-gap"), false, null, null, 0);
        } else if (tmpGapRow == CSSProperty.GridGap.NORMAL) {
            gapRow = 0.0f;
        }

        if (tmpGapColumn == null) {
            tmpGapColumn = CSSProperty.GridGap.NORMAL;
        }

        if (tmpGapColumn == CSSProperty.GridGap.length) {
            gapColumn = dec.getLength(getLengthValue("grid-column-gap"), false, null, null, 0);
        } else if (tmpGapColumn == CSSProperty.GridGap.NORMAL) {
            gapColumn = 0.0f;
        }
    }

    /**
     * Loads grid-template row/columns property
     */
    protected void loadGridTemplateRowColumn() {
        CSSProperty.GridTemplateRowsColumns gridTemplateRows = style.getProperty("grid-template-rows");
        if (gridTemplateRows == null) {
            gridTemplateRows = CSSProperty.GridTemplateRowsColumns.NONE;
        }

        if (gridTemplateRows == CSSProperty.GridTemplateRowsColumns.list_values) {
            gridTemplateRowValues = style.getValue(TermList.class, "grid-template-rows");
            isGridTemplateRows = true;
        } else if (gridTemplateRows == CSSProperty.GridTemplateRowsColumns.AUTO) {
            isGridTemplateRowsAuto = true;
        } else {
            isGridTemplateRows = false;
        }


        CSSProperty.GridTemplateRowsColumns gridTemplateColumns = style.getProperty("grid-template-columns");
        if (gridTemplateColumns == null) {
            gridTemplateColumns = CSSProperty.GridTemplateRowsColumns.NONE;
        }

        if (gridTemplateColumns == CSSProperty.GridTemplateRowsColumns.list_values) {
            gridTemplateColumnValues = style.getValue(TermList.class, "grid-template-columns");
            isGridTemplateColumns = true;
        } else if (gridTemplateColumns == CSSProperty.GridTemplateRowsColumns.AUTO) {
            isGridTemplateColumnsAuto = true;
        } else {
            isGridTemplateColumnsNone = true;
        }
    }

    /**
     * Loads grid-auto-flow property
     */
    protected void loadGridAutoFlow() {
        CSSProperty.GridAutoFlow gridAutoFlow = style.getProperty("grid-auto-flow");
        if (gridAutoFlow == null) {
            gridAutoFlow = CSSProperty.GridAutoFlow.ROW;
        }

        if (gridAutoFlow == CSSProperty.GridAutoFlow.ROW) {
            isGridAutoFlowRow = true;
        } else if (gridAutoFlow == CSSProperty.GridAutoFlow.COLUMN) {
            isGridAutoFlowRow = false;
        }
    }

    /**
     * Loads grid-auto-rows property
     *
     * @param conth available height
     * @return false if auto, else true if not
     */
    protected boolean isGridAutoRows(float conth) {
        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.GridAutoRowsColumns gridAutoRowsColumns = style.getProperty("grid-auto-rows");
        if (gridAutoRowsColumns == null) {
            gridAutoRowsColumns = CSSProperty.GridAutoRowsColumns.AUTO;
        }

        if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.length) {
            gridAutoRows = dec.getLength(getLengthValue("grid-auto-rows"), false, 0, 0, conth);
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.AUTO) {
            return false;
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.MIN_CONTENT) {
            isMinContentAutoRow = true;
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.MAX_CONTENT) {
            isMaxContentAutoRow = true;
        }
        return true;
    }

    /**
     * Loads grid-auto-columns property
     *
     * @param contw available width
     * @return false if auto, else true if not
     */
    protected boolean isGridAutoColumns(float contw) {
        CSSDecoder dec = new CSSDecoder(ctx);
        CSSProperty.GridAutoRowsColumns gridAutoRowsColumns = style.getProperty("grid-auto-columns");
        if (gridAutoRowsColumns == null) {
            gridAutoRowsColumns = CSSProperty.GridAutoRowsColumns.AUTO;
        }

        if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.length) {
            gridAutoColumns = dec.getLength(getLengthValue("grid-auto-columns"), false, 0, 0, contw);
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.AUTO) {
            return false;
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.MIN_CONTENT) {
            isMinContentAutoColumn = true;
        } else if (gridAutoRowsColumns == CSSProperty.GridAutoRowsColumns.MAX_CONTENT) {
            isMaxContentAutoColumn = true;
        }
        return true;
    }

    /**
     * Computes grid item size from explicit grid with pixels
     *
     * @param dec       decoder
     * @param tl        terList
     * @param oneFrUnit 1fr size
     * @param cont      available space
     * @return -1 if is auto, min-content or max-content, else grid item size
     */
    protected int findSizeOfGridItem(CSSDecoder dec, TermList tl, int i, float oneFrUnit, float cont) {
        int length;

        try {
            if (tl.get(i).getValue().toString().equals("auto") ||
                    tl.get(i).getValue().toString().equals("min-content") ||
                    tl.get(i).getValue().toString().equals("max-content")) {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }

        TermLength.Unit unit;
        TermLengthOrPercent a = (TermLengthOrPercent) tl.get(i);
        unit = a.getUnit();

        if (unit != TermNumeric.Unit.fr) {
            length = (int) dec.getLength((TermLengthOrPercent) tl.get(i), false, 0, 0, cont);
        } else {
            int tmpForFrUnit = (int) dec.getLength((TermLengthOrPercent) tl.get(i), false, 0, 0, cont);
            length = (int) (tmpForFrUnit * oneFrUnit);
        }
        return length;
    }

    /**
     * Computes auto value sizes in pixels
     *
     * @return true if is auto, else false if not
     */
    protected boolean processColumnAuto() {
        float content = getContentWidth();
        float size = 0;
        float countgaps;
        int countofauto = 0;
        float finalautovalue;

        for (Float arrayOfColumn : arrayOfColumns) {
            if (arrayOfColumn != 0) {
                size += arrayOfColumn;
            } else {
                countofauto++;
            }
        }
        countgaps = (arrayOfColumns.size() - 1) * gapColumn;

        if (countofauto != 0) {
            finalautovalue = (content - size - countgaps) / countofauto;

            for (int i = 0; i < arrayOfColumns.size(); i++) {
                if (arrayOfColumns.get(i) == 0) {
                    arrayOfColumns.remove(i);
                    arrayOfColumns.add(i, finalautovalue);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Computes explicit grid size
     *
     * @param dec decoder
     * @param tl termList
     * @param oneFrUnit 1fr unit in pixels
     * @param cont available space
     * @return -1 if is auto, min-content or max-content, else number
     */
    protected float sumOfLengthForGridTemplateColumnRow(CSSDecoder dec, TermList tl, float oneFrUnit, float cont) {
        float c = 0;
        for (Term<?> term : tl) {
            if (term.getValue().toString().equals("auto") ||
                    term.getValue().toString().equals("min-content") ||
                    term.getValue().toString().equals("max-content")) {
                return -1;
            }
        }

        TermLength.Unit unit;
        for (Term<?> term : tl) {
            TermLengthOrPercent a = (TermLengthOrPercent) term;
            unit = a.getUnit();
            if (unit != TermNumeric.Unit.fr) {
                c += dec.getLength((TermLengthOrPercent) term, false, 0, 0, cont);
            } else {
                float tmpForFrUnit = dec.getLength((TermLengthOrPercent) term, false, 0, 0, cont);
                c += (tmpForFrUnit * oneFrUnit);
            }
        }
        return c;
    }

    /**
     * Auxiliary method, checks track
     * @param tmp termList
     * @return false if is auto, min-content or max-content, else true if not
     */
    public boolean containsOnlyUnit(TermList tmp) {
        if (tmp != null) {
            for (Term<?> term : tmp) {
                if (term.getValue().toString().equals("auto") ||
                        term.getValue().toString().equals("min-content") ||
                        term.getValue().toString().equals("max-content")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Computes array sizes
     *
     * @return array size with gaps
     */
    public float getSizeOfArrayOfRows() {
        float count = 0;
        for (Float arrayOfRow : arrayOfRows) {
            count += arrayOfRow;
        }
        count += (arrayOfRows.size() - 1) * gapRow;
        return count;
    }

    /**
     * Computes repeat rows in grid
     */
    public void containsRepeatInRows() {
        RepeatImpl repeat;
        Term term;
        if (gridTemplateRowValues != null) {
            term = gridTemplateRowValues.get(0);
            try {
                repeat = (RepeatImpl) term;
            } catch (Exception e) {
                return;
            }
            TermFunction.Repeat.Unit a = repeat.getNumberOfRepetitions();
            gridTemplateRowValues = null;
            CSSDecoder dec = new CSSDecoder(ctx);
            for (int i = 0; i < a.getNumberOfRepetitions(); i++) {
                arrayOfRows.add(i, dec.getLength((TermLengthOrPercent) repeat.getRepeatedTerms().get(0), false, 0, 0, getContentHeight()));
            }
            isRepeatRows = true;
            maxRowLine = a.getNumberOfRepetitions() + 1;
        }
    }

    /**
     * Computes repeat columns in grid
     */
    public void containsRepeatInColumns() {
        RepeatImpl repeat;
        Term term;
        if (gridTemplateColumnValues != null) {
            term = gridTemplateColumnValues.get(0);
            try {
                repeat = (RepeatImpl) term;
            } catch (Exception e) {
                return;
            }
            countOfRepeated = repeat.getNumberOfRepetitions();
            gridTemplateColumnValues = null;
            CSSDecoder dec = new CSSDecoder(ctx);
            for (int i = 0; i < countOfRepeated.getNumberOfRepetitions(); i++) {
                arrayOfColumns.add(i, dec.getLength((TermLengthOrPercent) repeat.getRepeatedTerms().get(0), false, 0, 0, getContentWidth()));
            }
        }
    }
}
