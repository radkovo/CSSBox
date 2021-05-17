package org.fit.cssbox.layout;

import cz.vutbr.web.css.TermList;

/**
 * Represents coordinates of grid items
 *
 * @author Ondra
 */
public class GridItemCoords {

    /**
     * grid-row-start property
     */
    public int rowStart = 0;

    /**
     * grid-row-end property
     */
    public int rowEnd = 0;

    /**
     * grid-column-start property
     */
    public int columnStart = 0;

    /**
     * grd-column-end property
     */
    public int columnEnd = 0;

    /**
     * grid-row-start property with span
     */
    public TermList rowStartSpan = null;

    /**
     * grid-row-end property with span
     */
    public TermList rowEndSpan = null;

    /**
     * grid-column-start property with span
     */
    public TermList columnStartSpan = null;

    /**
     * grid-column-end property with span
     */
    public TermList columnEndSpan = null;

    /**
     * Creates new coordinates
     */
    public GridItemCoords() {

    }

    /**
     * Creates new coordinates with values
     *
     * @param rowStart    grid-row-start
     * @param rowEnd      grid-row-end
     * @param columnStart grid-column-start
     * @param columnEnd   grid-column-end
     */
    public GridItemCoords(int rowStart, int rowEnd, int columnStart, int columnEnd) {
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    /**
     * Creates coordinates from existing
     * @param src source
     */
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
