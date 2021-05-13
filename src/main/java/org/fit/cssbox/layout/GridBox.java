package org.fit.cssbox.layout;

import cz.vutbr.web.css.*;
import cz.vutbr.web.csskit.fn.MinMaxImpl;
import cz.vutbr.web.csskit.fn.RepeatImpl;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 *
 */
public class GridBox extends BlockBox {

    /** Size of row gaps in grid container */
    protected float gapRow;

    /** Size of column gaps in grid container */
    protected float gapColumn;

    /** Represents row tracks in explicit grid */
    protected TermList gridTemplateRowValues;

    /** Represents column tracks in explicit grid */
    protected TermList gridTemplateColumnValues;

    /** Are specified rows in explicit grid? */
    protected boolean isGridTemplateRows;

    /** Are specified columns in explicit grid? */
    protected boolean isGridTemplateColumns;

    /** Is specified only one auto value in explicit grid? */
    protected boolean isGridTemplateRowsAuto;

    /** Is specified only one auto value in explicit grid? */
    protected boolean isGridTemplateColumnsAuto;

    /** Are specified columns in explicit grid? */
    protected boolean isGridTemplateColumnsNone;

    /** Is specified auto flow for automatic grid items? */
    protected boolean isGridAutoFlowRow;

    /** Are specified rows of implicit grid? */
    protected boolean isGridAutoRow;

    /** Are specified columns of implicit grid? */
    protected boolean isGridAutoColumn;

    /** Size of row tracks in implicit grid */
    protected float gridAutoRows;

    /** Size of column tracks in implicit grid */
    protected float gridAutoColumns;

    /** Is specified size min-content of rows in implicit grid? */
    protected boolean isMinContentAutoRow;

    /** Is specified size max-content of rows in implicit grid? */
    protected boolean isMaxContentAutoRow;

    /** Is specified size min-content of columns in implicit grid? */
    protected boolean isMinContentAutoColumn;

    /** Is specified size max-content of columns in implicit grid? */
    protected boolean isMaxContentAutoColumn;

    /** Max row line in container */
    protected int maxRowLine;

    /** Max column line in container */
    protected int maxColumnLine;

    /** There are new rows values (in pixels) after computing auto, min-content or max-content values*/
    protected ArrayList<Float> arrayofrows = new ArrayList<>();

    /** There are new columns values (in pixels) after computing auto, min-content or max-content values*/
    protected ArrayList<Float> arrayofcolumns = new ArrayList<>();

    /** Count of fr units in explicit grid */
    protected float flexFactorSum;

    /** Count of fixed tracks in explicit grid */
    protected float sumOfPixels;

    /** Size of 1fr (in pixels) in explicit grid */
    protected float oneFrUnitColumn;

    /** Size of 1fr (in pixels) in explicit grid */
    protected float oneFrUnitRow;

    /**
     *
     */
    protected TermFunction.Repeat.Unit countOfRepeated;

    /**
     *
     */
    protected boolean isRepeatRows;

    /**
     *
     * @param n
     * @param ctx
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
     *
     * @param src
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
     *
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
     *
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
     *
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
     *
     * @param conth
     * @return
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
     *
     * @param contw
     * @return
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

    protected boolean processColumnAuto() {
        float content = getContentWidth();
        float size = 0;
        float countgaps;
        int countofauto = 0;
        float finalautovalue;

        for (int i = 0; i < arrayofcolumns.size(); i++) {
            if (arrayofcolumns.get(i) != 0) {
                size += arrayofcolumns.get(i);
            } else {
                countofauto++;
            }
        }
        countgaps = (arrayofcolumns.size() - 1) * gapColumn;

        if (countofauto != 0) {
            finalautovalue = (content - size - countgaps) / countofauto;

            for (int i = 0; i < arrayofcolumns.size(); i++) {
                if (arrayofcolumns.get(i) == 0) {
                    arrayofcolumns.remove(i);
                    arrayofcolumns.add(i, finalautovalue);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected float sumOfLengthForGridTemplateColumnRow(CSSDecoder dec, TermList tl, float oneFrUnit, float cont) {
        float c = 0;
        for (int k = 0; k < tl.size(); k++) {
            if (tl.get(k).getValue().toString().equals("auto") ||
                    tl.get(k).getValue().toString().equals("min-content") ||
                    tl.get(k).getValue().toString().equals("max-content")) {
                return -1;
            }
        }

        TermLength.Unit unit;
        for (int k = 0; k < tl.size(); k++) {
            TermLengthOrPercent a = (TermLengthOrPercent) tl.get(k);
            unit = a.getUnit();
            if (unit != TermNumeric.Unit.fr) {
                c += dec.getLength((TermLengthOrPercent) tl.get(k), false, 0, 0, cont);
            } else {
                float tmpForFrUnit = dec.getLength((TermLengthOrPercent) tl.get(k), false, 0, 0, cont);
                c += (tmpForFrUnit * oneFrUnit);
            }
        }
        return c;
    }

    public boolean containsOnlyUnit(TermList tmp) {
        if (tmp != null) {
            for (int i = 0; i < tmp.size(); i++) {
                if (tmp.get(i).getValue().toString().equals("auto") ||
                        tmp.get(i).getValue().toString().equals("min-content") ||
                        tmp.get(i).getValue().toString().equals("max-content")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public float getSizeOfArrayOfRows() {
        float count = 0;
        for (int i = 0; i < arrayofrows.size(); i++) {
            count += arrayofrows.get(i);
        }
        count += (arrayofrows.size() -1) * gapRow;
        return count;
    }

    public void containsRepeatInColumns() {
        //very basic solution of repeat notation in columns
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
                arrayofcolumns.add(i, dec.getLength((TermLengthOrPercent) repeat.getRepeatedTerms().get(0), false, 0, 0, getContentWidth()));
            }
        }
    }

    public void containsRepeatInRows() {
        //very basic solution of repeat notation in columns
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
                arrayofrows.add(i, dec.getLength((TermLengthOrPercent) repeat.getRepeatedTerms().get(0), false, 0, 0, getContentHeight()));
            }
            isRepeatRows = true;
            maxRowLine = a.getNumberOfRepetitions()+1;
            System.out.println("max row line: " + maxRowLine);
        }
    }

    public void processMinMaxFunction() {
        MinMaxImpl max;
        Term term;
    }
}

