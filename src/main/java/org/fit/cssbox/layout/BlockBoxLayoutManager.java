package org.fit.cssbox.layout;

/**
 * Layout manager of Block box layout
 *
 * @author Ondra, Ondry
 */
public class BlockBoxLayoutManager implements LayoutManager {

    /**
     * Block box container
     */
    protected BlockBox blockBox;

    /**
     * Creates a new instance of Block box layout manager
     *
     * @param blockBox block box container
     */
    public BlockBoxLayoutManager(BlockBox blockBox) {
        this.blockBox = blockBox;
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

        //Skip if not displayed
        if (!blockBox.displayed) {
            blockBox.content.setSize(0, 0);
            blockBox.bounds.setSize(0, 0);
            return true;
        }

        //remove previously splitted children from possible previous layout
        blockBox.clearSplitted();

        //shrink-to-fit when the width is not given by containing box or specified explicitly
        if (!blockBox.hasFixedWidth()) {
            //float min = getMinimalContentWidthLimit();
            float min = Math.max(blockBox.getMinimalContentWidthLimit(), blockBox.getMinimalContentWidth());
            float max = blockBox.getMaximalContentWidth();
            float availcont = availw - blockBox.emargin.left - blockBox.border.left - blockBox.padding.left - blockBox.emargin.right - blockBox.border.right - blockBox.padding.right;
            //float pref = Math.min(max, availcont);
            //if (pref < min) pref = min;
            float pref = Math.min(Math.max(min, availcont), max);
            blockBox.setContentWidth(pref);
            blockBox.updateChildSizes();
        }

        //the width should be fixed from this point
        blockBox.widthComputed = true;

        /* Always try to use the full width. If the box is not in flow, its width
         * is updated after the layout */
        blockBox.setAvailableWidth(availw);


        if (!blockBox.contblock)  //block elements containing inline elements only
            blockBox.layoutInline();
        else //block elements containing block elements
            blockBox.layoutBlocks();

        //allways fits as well possible
        return true;
    }
}
