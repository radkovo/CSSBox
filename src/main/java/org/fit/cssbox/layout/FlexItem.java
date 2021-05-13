package org.fit.cssbox.layout;

import cz.vutbr.web.css.*;
import cz.vutbr.web.csskit.TermIntegerImpl;
import org.w3c.dom.Element;

public class FlexItem extends BlockBox implements Comparable<FlexItem> {

    public static final CSSProperty.AlignSelf ALIGN_SELF_AUTO = CSSProperty.AlignSelf.AUTO;
    public static final CSSProperty.AlignSelf ALIGN_SELF_FLEX_START = CSSProperty.AlignSelf.FLEX_START;
    public static final CSSProperty.AlignSelf ALIGN_SELF_FLEX_END = CSSProperty.AlignSelf.FLEX_END;
    public static final CSSProperty.AlignSelf ALIGN_SELF_CENTER = CSSProperty.AlignSelf.CENTER;
    public static final CSSProperty.AlignSelf ALIGN_SELF_BASELINE = CSSProperty.AlignSelf.BASELINE;
    public static final CSSProperty.AlignSelf ALIGN_SELF_STRETCH = CSSProperty.AlignSelf.STRETCH;

    public static final CSSProperty.FlexBasis FLEX_BASIS_CONTENT = CSSProperty.FlexBasis.CONTENT;
    public static final CSSProperty.FlexBasis FLEX_BASIS_LENGTH = CSSProperty.FlexBasis.length;
    public static final CSSProperty.FlexBasis FLEX_BASIS_PERCENTAGE = CSSProperty.FlexBasis.percentage;
    public static final CSSProperty.FlexBasis FLEX_BASIS_AUTO = CSSProperty.FlexBasis.AUTO;

    /**
     * align self specified by the style
     */
    protected CSSProperty.AlignSelf alignSelf;
    /**
     * flex basis specified by the style
     */
    protected CSSProperty.FlexBasis flexBasis;
    /**
     * flex grow specified by the style
     */
    protected CSSProperty.FlexGrow flexGrow;
    /**
     * flex shrink specified by the style
     */
    protected CSSProperty.FlexShrink flexShrink;
    /**
     * order specified by the style
     */
    protected CSSProperty.Order flexOrder;

    /**
     * float value of flex grow specified by style
     */
    protected float flexGrowValue;
    /**
     * float value of flex shrink specified by style
     */
    protected float flexShrinkValue;

    /**
     * int value of order specified by style
     */
    protected int flexOrderValue;

    /**
     * defines if flex basis is set by content
     */
    protected boolean flexBasisSetByCont;

    /**
     * hypotetical main size of flex item which is width in horizontal flex container and height in vertical flex container.
     */
    protected float hypotheticalMainSize;

    /**
     * defines if cross size of flex item is set by content
     */
    protected boolean crossSizeSetByCont;

    /**
     * defines if flex basis is set by percentage value
     */
    protected boolean crossSizeSetByPercentage;

    /**
     *
     * @param n
     * @param ctx
     */
    public FlexItem(Element n, VisualContext ctx) {
        super(n, ctx);
        isblock = true;
        hypotheticalMainSize = 0;
        flexBasisSetByCont = false;
        crossSizeSetByCont = false;
        crossSizeSetByPercentage = false;
    }

    /**
     *
     * @param src
     */
    public FlexItem(InlineBox src) {
        super(src.el, src.ctx);

        setStyle(src.getStyle());
        isblock = true;
        hypotheticalMainSize = 0;
        flexBasisSetByCont = false;
        crossSizeSetByCont = false;
        crossSizeSetByPercentage = false;
    }

    /**
     *
     * @return
     */
    public int getFlexOrderValue() {
        return flexOrderValue;
    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadFlexItemsStyles();
    }

    /**
     *
     */
    public void loadFlexItemsStyles() {

        alignSelf = style.getProperty("align-self");
        if (alignSelf == null) alignSelf = CSSProperty.AlignSelf.AUTO;

        flexBasis = style.getProperty("flex-basis");
        if (flexBasis == null) flexBasis = CSSProperty.FlexBasis.AUTO;

        flexGrow = style.getProperty("flex-grow");
        if (flexGrow == null) flexGrow = CSSProperty.FlexGrow.INITIAL;

        flexShrink = style.getProperty("flex-shrink");
        if (flexShrink == null) flexShrink = CSSProperty.FlexShrink.INITIAL;

        flexOrder = style.getProperty("order");
        if (flexOrder == null) flexOrder = CSSProperty.Order.INITIAL;

        setFactorValues();
        setFlexOrder();
    }

    /**
     *
     */
    private void setFactorValues() {
        Term<?> len = style.getValue("flex-grow", false);
        flexGrowValue = setFactor(len, true);
        len = style.getValue("flex-shrink", false);
        flexShrinkValue = setFactor(len, false);
    }

    /**
     *
     * @param len
     * @param isGrow
     * @return
     */
    private float setFactor(Term<?> len, boolean isGrow) {
        float ret;
        if (len != null) {
            if (len instanceof TermInteger) ret = ((TermInteger) len).getValue();
            else ret = ((TermNumber) len).getValue();
        } else {
            if (isGrow) ret = 0;
            else ret = 1;
        }
        return ret;
    }

    /**
     *
     */
    private void setFlexOrder() {
        TermIntegerImpl len = (TermIntegerImpl) style.getValue("order", false);

        flexOrderValue = 0;

        if (len != null)
            flexOrderValue = len.getIntValue();
    }

    @Override
    public int compareTo(FlexItem i) {
        return this.flexOrderValue - i.getFlexOrderValue();
    }

    /**
     *
     */
    protected void disableFloats() {
        floatY = 0;
        floatXl = 0;
        floatXr = 0;
        floating = CSSProperty.Float.NONE;
    }

    /**
     *
     * @param container
     * @return
     */
    protected float setHypotheticalMainSize(FlexBox container) {
        CSSDecoder dec = new CSSDecoder(container.ctx);
        float contw = container.getContentWidth();

        if (flexBasis == FLEX_BASIS_LENGTH || flexBasis == FLEX_BASIS_PERCENTAGE) {
            //flex-basis is set by style
            if (flexBasis == FLEX_BASIS_PERCENTAGE && !container.isRowContainer()) {
                //COLUMN
                if (container.content.height != 0) {
                    hypotheticalMainSize = dec.getLength(getLengthValue("flex-basis"), false, 0, 0, container.mainSize);
                } else {
                    if (style.getProperty("height") == CSSProperty.Height.AUTO || style.getProperty("height") == null || style.getProperty("height") == CSSProperty.Height.valueOf("percentage")) {
                        flexBasisSetByCont = true;
                    } else {
                        hypotheticalMainSize = dec.getLength(getLengthValue("height"), false, 0, 0, 0); //use height
                        flexBasisSetByCont = false;
                    }
                }
            } else {
                hypotheticalMainSize = dec.getLength(getLengthValue("flex-basis"), false, 0, 0, contw);
            }
            //when setted flex basis in row container is smaller than min content -> flex basis = min content
            if (container.isRowContainer()) {
                if (hypotheticalMainSize < getMinimalContentWidth())
                    hypotheticalMainSize = getMinimalContentWidth();
            }
        } else if (flexBasis == FlexItem.FLEX_BASIS_CONTENT) {
            flexBasisSetByCont = true;
        } else if (flexBasis == FlexItem.FLEX_BASIS_AUTO) {
            if (container.isRowContainer()) {
                //ROW
                if (style.getProperty("width") == CSSProperty.Width.AUTO || style.getProperty("width") == null) {
                    flexBasisSetByCont = true;
                } else {
                    hypotheticalMainSize = dec.getLength(getLengthValue("width"), false, 0, 0, contw); //use width
                }
            } else {
                //COLUMN
                if (style.getProperty("height") == CSSProperty.Height.AUTO || style.getProperty("height") == null) {
                    flexBasisSetByCont = true;
                } else if (style.getProperty("height") == CSSProperty.Height.valueOf("percentage")) {
                    if (container.content.height != 0)
                        hypotheticalMainSize = dec.getLength(getLengthValue("height"), false, 0, 0, container.mainSize);
                    else
                        flexBasisSetByCont = true;
                } else {
                    hypotheticalMainSize = dec.getLength(getLengthValue("height"), false, 0, 0, 0); //use height
                    flexBasisSetByCont = false;
                }
            }
        }

        //set flex basis content in row container, in column container it is solved later
        if (flexBasisSetByCont && container.isRowContainer())
            setFlexBasisBasedByContent();
        return hypotheticalMainSize;
    }

    /**
     *
     */
    private void setFlexBasisBasedByContent() {
        hypotheticalMainSize = getMaximalContentWidth();
    }

    /**
     *
     * @param value
     * @return
     */
    protected float boundFlexBasisByMinAndMax(float value) {
        if (((FlexBox) parent).isRowContainer()) {
            if (min_size.width != -1)
                if (value < min_size.width)
                    value = min_size.width;

            if (max_size.width != -1)
                if (value > max_size.width)
                    value = max_size.width;

        } else {
            if (min_size.height != -1)
                if (value < min_size.height)
                    value = min_size.height;

            if (max_size.height != -1)
                if (value > max_size.height)
                    value = max_size.height;
        }
        return value;
    }

    /**
     *
     */
    protected void fixRightMargin() {
        FlexBox parent = (FlexBox) this.getContainingBlockBox();
        CSSDecoder decoder = new CSSDecoder(parent.ctx);

        if (style.getProperty("margin-right") != CSSProperty.Margin.AUTO || style.getProperty("margin") != CSSProperty.Margin.AUTO) {
            margin.right = decoder.getLength(getLengthValue("margin-right"), false, 0, 0, 0);
            emargin.right = margin.right;
        }
    }

    /**
     *
     * @return
     */
    protected boolean isNotAlignSelfAuto() {
        if (alignSelf == CSSProperty.AlignSelf.AUTO)
            return false;
        else
            return true;
    }

    /**
     *
     * @param container
     */
    protected void setCrossSize(FlexBox container) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (container.isRowContainer()) {
            if (style.getProperty("height") == null && style.getProperty("min-height") == null) {
                //vyska itemu u row direction nenastavena
                crossSizeSetByCont = true;
            } else if ((style.getProperty("height") == CSSProperty.Height.percentage) && !getContainingBlockBox().hasFixedHeight()) {
                //vyska itemu u row direction nastavena v %
                crossSizeSetByPercentage = true;
            } else if (style.getProperty("height") == CSSProperty.Height.length) {
                setContentHeight(dec.getLength(getLengthValue("height"), false, 0, 0, 0));
            }
        } else {
            float contw = container.crossSize;
            float width = dec.getLength(getLengthValue("width"), false, -1, -1, contw);

            //is width set
            if (width != -1) {
                //yes, set it to content and bound it by min and max
                content.width = width;
                if (content.width < min_size.width && min_size.width != -1)
                    content.width = min_size.width;

                if (content.width > max_size.width && max_size.width != -1)
                    content.width = max_size.width;
            } else {
                //no, set to max content width
                content.width = getMaximalContentWidth();

                if (content.width + margin.left + margin.right + padding.left + padding.right + border.right + border.left > container.crossSize) {
                    content.width = container.crossSize - margin.left - margin.right - padding.left - padding.right - border.right - border.left;
                    if (getMinimalContentWidth() + margin.left + margin.right + padding.left + padding.right + border.right + border.left > container.crossSize)
                        content.width = getMinimalContentWidth();
                }
                crossSizeSetByCont = true;
            }
        }
    }
}
