package org.fit.cssbox.layout;

public class InlineBoxLayoutManager implements LayoutManager {

    private InlineBox inlineBox;

    public InlineBoxLayoutManager(InlineBox inlineBox) {
        this.inlineBox = inlineBox;
    }

    /**
     * Compute the width and height of this element. Layout the sub-elements.
     *
     * @param availw    Maximal width available to the child elements
     * @param force     Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return True if the box has been succesfully placed
     */
    @Override
    public boolean doLayout(float availw, boolean force, boolean linestart) {
        System.out.println("jsem InlineBox doLAyout");
        //if (getElement() != null && getElement().getAttribute("id").equals("mojo"))
        //  System.out.println("jo!");
        //Skip if not displayed
        if (!inlineBox.displayed) {
            inlineBox.content.setSize(0, 0);
            inlineBox.bounds.setSize(0, 0);
            return true;
        }

        inlineBox.setAvailableWidth(availw);

        inlineBox.setCurline(new LineBox(inlineBox, inlineBox.getStartChild(), 0));
        float wlimit = inlineBox.getAvailableContentWidth();
        float x = 0; //current x
        boolean ret = true;
        inlineBox.rest = null;

        int lastbreak = inlineBox.startChild; //last possible position of a line break
        inlineBox.collapsedCompletely = true;

        for (int i = inlineBox.startChild; i < inlineBox.endChild; i++) {
            Box subbox = inlineBox.getSubBox(i);
            if (subbox.canSplitBefore())
                lastbreak = i;
            //when forcing, force the first child only and the children before
            //the first possible break
            boolean f = force && (i == inlineBox.startChild || lastbreak == inlineBox.startChild);
            boolean fit = subbox.doLayout(wlimit - x, f, linestart && (i == inlineBox.startChild));
            if (fit) //something has been placed
            {
                if (subbox instanceof Inline) {
                    subbox.setPosition(x, 0); //the y position will be updated later
                    x += subbox.getWidth();
                    inlineBox.getCurline().considerBox((Inline) subbox);
                    if (((Inline) subbox).finishedByLineBreak())
                        inlineBox.lineBreakStop = true;
                    if (!((Inline) subbox).collapsedCompletely())
                        inlineBox.collapsedCompletely = false;
                } else
                    InlineBox.getLog().debug("Warning: doLayout(): subbox is not inline: " + subbox);
                if (subbox.getRest() != null) //is there anything remaining?
                {
                    InlineBox rbox = inlineBox.copyBox();
                    rbox.splitted = true;
                    rbox.splitid = inlineBox.splitid + 1;
                    rbox.setStartChild(i); //next starts with me...
                    rbox.nested.setElementAt(subbox.getRest(), i); //..but only with the rest
                    rbox.adoptChildren();
                    inlineBox.setEndChild(i + 1); //...and this box stops with this element
                    inlineBox.rest = rbox;
                    break;
                } else if (inlineBox.lineBreakStop) //nothing remained but there was a line break
                {
                    if (i + 1 < inlineBox.endChild) //some children remaining
                    {
                        InlineBox rbox = inlineBox.copyBox();
                        rbox.splitted = true;
                        rbox.splitid = inlineBox.splitid + 1;
                        rbox.setStartChild(i + 1); //next starts with the next one
                        rbox.adoptChildren();
                        inlineBox.setEndChild(i + 1); //...and this box stops with this element
                        inlineBox.rest = rbox;
                    }
                    break;
                }
            } else //nothing from the child has been placed
            {
                if (lastbreak == inlineBox.startChild) //no children have been placed, give up
                {
                    ret = false;
                    break;
                } else //some children have been placed, contintue the next time
                {
                    InlineBox rbox = inlineBox.copyBox();
                    rbox.splitted = true;
                    rbox.splitid = inlineBox.splitid + 1;
                    rbox.setStartChild(lastbreak); //next time start from the last break
                    rbox.adoptChildren();
                    inlineBox.setEndChild(lastbreak); //this box stops here
                    inlineBox.rest = rbox;
                    break;
                }
            }
            if (subbox.canSplitAfter())
                lastbreak = i + 1;
        }
        //compute the vertical positions of the boxes
        //updateLineMetrics();
        inlineBox.content.width = x;
        inlineBox.content.height = inlineBox.ctx.getFontHeight();
        inlineBox.setHalflead((inlineBox.content.height - inlineBox.ctx.getFontHeight()) / 2);
        alignBoxes(inlineBox);
        inlineBox.setSize(inlineBox.totalWidth(), inlineBox.totalHeight());

        return ret;
    }

    /**
     * Vertically aligns the contained boxes according to their vertical-align properties.
     */
    protected void alignBoxes(InlineBox inlineBox) {
        for (int i = inlineBox.startChild; i < inlineBox.endChild; i++) {
            Box sub = inlineBox.getSubBox(i);
            if (!sub.isBlock()) {
                //position relative to the line box
                float dif = inlineBox.curline.alignBox((Inline) sub);
                //recompute to the content box
                dif = dif - inlineBox.getLineboxOffset();
                //recompute to the bounding box
                if (sub instanceof InlineBox)
                    dif = dif - ((ElementBox) sub).getContentOffsetY();
                //update the Y coordinate
                if (dif != 0)
                    sub.moveDown(dif);
                //update minDescendantY and maxDescendantY
                float y1 = sub.getContentY();
                if (sub instanceof InlineBox) {
                    final float dy = ((InlineBox) sub).getMinDescendantY();
                    if (dy < 0)
                        y1 += dy;
                }
                inlineBox.minDescendantY = Math.min(inlineBox.minDescendantY, y1);
                float y2 = sub.getContentY() + sub.getContentHeight() - 1;
                if (sub instanceof InlineBox) {
                    final float dy = ((InlineBox) sub).getMaxDescendantY();
                    if (dy > sub.getContentHeight())
                        y2 += dy;
                }
                inlineBox.maxDescendantY = Math.max(inlineBox.maxDescendantY, y2);
            }
        }
    }
}
