package org.fit.cssbox.layout;

import cz.vutbr.web.css.TermList;

public class GridItemCoords {

    public int rowStart = 0;

    public int rowEnd = 0;

    public int columnStart = 0;

    public int columnEnd = 0;

    public TermList rowStartSpan = null;

    public TermList rowEndSpan = null;

    public TermList columnStartSpan = null;

    public TermList columnEndSpan = null;

    public GridItemCoords() {

    }

    public GridItemCoords(int rowStart, int rowEnd, int columnStart, int columnEnd) {
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    public GridItemCoords(GridItemCoords src) {
        rowStart = src.rowStart;
        rowEnd = src.rowEnd;
        columnStart = src.columnStart;
        columnEnd = src.columnEnd;
    }

    @Override
    public String toString() {
        return "grid-row-start: " + rowStart + " grid-row-end: " + rowEnd + " grid-column-start: " + columnStart + " grid-column-end: " + columnEnd;
    }
}
