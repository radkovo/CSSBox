/*
 * StructuredRenderer.java
 * Copyright (c) 2005-2020 Radek Burget
 *
 * CSSBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CSSBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 11. 4. 2020, 18:02:20 by burgetr
 * Extended by Tomáš Chocholatý
 */
package org.fit.cssbox.render;

import cz.vutbr.web.csskit.Color;
import org.fit.cssbox.awt.BackgroundBitmap;
import org.fit.cssbox.awt.BitmapImage;
import org.fit.cssbox.css.BackgroundDecoder;
import org.fit.cssbox.layout.*;


/**
 * A base implementation of a renderer that automates the most common actions.
 *
 * @author burgetr
 * @author Tomas Chocholaty
 */
public abstract class StructuredRenderer implements BoxRenderer {
    private Viewport vp;
    private ElementBox bgSource; //viewport background source element
    private BackgroundDecoder vpBackground;

    private float rootWidth;
    private float rootHeight;
    private String typeOfRenderer;

    public StructuredRenderer(float rootWidth, float rootHeight, String typeOfRenderer) {
        this.rootWidth = rootWidth;
        this.rootHeight = rootHeight;
        this.typeOfRenderer = typeOfRenderer;
    }

    public float getRootWidth() {
        return rootWidth;
    }

    public float getRootHeight() {
        return rootHeight;
    }

    protected StructuredRenderer() {
    }

    @Override
    public void init(Viewport vp) {
        this.vp = vp;
        findViewportBackgroundSource();
    }

    /**
     * Returns the viewport for which the renderer was initialized.
     *
     * @return The viewport box.
     */
    public Viewport getViewport() {
        return vp;
    }

    /**
     * Returns the element box used for obtaining the viewport background. It may be the
     * HTML or BODY element for HTML mode, the root element for non-HTML mode.
     *
     * @return The background source element or {@code null} when the box tree is empty.
     */
    public ElementBox getViewportBackgroundSource() {
        return bgSource;
    }

    /**
     * Returns a representation of the viewport background.
     *
     * @return the corresponding background decoder
     */
    public BackgroundDecoder getViewportBackground() {
        return vpBackground;
    }

    //================================================================================================

    /**
     * Finds the element box that provides the background for the given element box. Normally,
     * it is the element box itself but there are certain special cases such as the viewport
     * or table elements.
     *
     * @param elem The element to find the background source for.
     * @return the background decoder with the appropriate source or {@code null} when no
     * background should be drawn for the given element.
     */
    protected BackgroundDecoder findBackgroundSource(ElementBox elem) {
        if (elem == vp) {
            return vpBackground;
        } else if (elem == bgSource) {
            // do not draw the background for the original viewport background source
            return null;
        } else if (elem instanceof BlockTableBox) {
            // anonymous table box has never a background
            return null;
        } else if (elem instanceof TableCellBox) {
            //if no background is specified for the cell, we try to use the row, row group, column, column group
            final TableCellBox cell = (TableCellBox) elem;
            BackgroundDecoder bg = new BackgroundDecoder(elem);
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerRow());
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerColumn());
            if (bg.isBackgroundEmpty())
                bg = new BackgroundDecoder(cell.getOwnerRow().getOwnerBody());

            if (bg.isBackgroundEmpty())
                return null; //nothing found
            else
                return bg;
        } else {
            // other elements - use the element itself
            return new BackgroundDecoder(elem);
        }
    }

    /**
     * Finds the background source element according to
     * https://www.w3.org/TR/CSS2/colors.html#background
     */
    protected void findViewportBackgroundSource() {
        ElementBox root = getViewport().getRootBox();
        if (root != null) {
            BackgroundDecoder rootBg = new BackgroundDecoder(root);

            if (rootBg.isBackgroundEmpty() && getViewport().getConfig().getUseHTML()) {
                // For HTML, try to use the body if there is no background set for the root box
                ElementBox body = getViewport().getElementBoxByName("body", false);
                if (body != null) {
                    bgSource = body;
                    vpBackground = new BackgroundDecoder(body);
                } else {
                    bgSource = root;
                    vpBackground = rootBg;
                }
            } else {
                bgSource = root;
                vpBackground = rootBg;
            }
        }
    }

    /**
     * Render Background and border of element
     *
     * @param elem element for rendering backgrounds and borders
     */
    @Override
    public void renderElementBackground(ElementBox elem) {
        Rectangle bb = elem.getAbsoluteBorderBounds();
        BackgroundDecoder bg = findBackgroundSource(elem);
        if (bg != null) {
            if (bg.getBgcolor() != null) {
                renderColorBg(elem, bb, bg);
            }
            if (bg.getBackgroundImages() != null && bg.getBackgroundImages().size() > 0) {
                BackgroundBitmap bitmap = null;
                for (int i = bg.getBackgroundImages().size() - 1; i >= 0; i--) {
                    BackgroundImage img = bg.getBackgroundImages().get(i);
                    if (img instanceof BackgroundImageImage) {
                        if (bitmap == null) {
                            bitmap = new BackgroundBitmap(elem);
                        }
                        bitmap.addBackgroundImage((BackgroundImageImage) img);
                    } else if (img instanceof BackgroundImageGradient) {
                        //flush eventual bitmaps
                        if (bitmap != null && bitmap.getBufferedImage() != null) {
                            renderImageBg(elem, bb, bitmap);
                            bitmap = null;
                        }
                        //insert the gradient with repeating
                        addGradient((BackgroundImageGradient) img, elem, bb);
                    }
                }

                //flush eventual bitmaps
                if (bitmap != null && bitmap.getBufferedImage() != null) {
                    renderImageBg(elem, bb, bitmap);
                }
            }
        }
        renderBorder(elem, bb);
    }

    /**
     * Render Background and border of element
     *
     * @param box element for rendering replaced content
     */
    @Override
    public void renderReplacedContent(ReplacedBox box) {
        ReplacedContent cont = box.getContentObj();
        if (cont != null) {
            if (cont instanceof ReplacedImage) {
                ContentImage img = ((ReplacedImage) cont).getImage();
                if (img != null) {
                    if (img instanceof BitmapImage) {
                        insertReplacedImage(box, img);
                    }
                }
            } else if (cont instanceof ReplacedText) {//HTML objekty
                insertReplacedText(box);
            }
        }
    }

    /**
     * Render lists markers
     *
     * @param elem the list-item box
     */
    @Override
    public void renderMarker(ListItemBox elem) {
        if (elem.getMarkerImage() != null) {
            if (!writeMarkerImage(elem)) writeBullet(elem);
        } else
            writeBullet(elem);
    }

    /**
     * Draw all types of markers
     *
     * @param lb the list-item box
     */
    protected void writeBullet(ListItemBox lb) {
        if (lb.hasVisibleBullet()) {
            VisualContext ctx = lb.getVisualContext();
            float x = lb.getAbsoluteContentX() + lb.getPadding().left - 1.2f * ctx.getEm();
            float y = lb.getAbsoluteContentY() + 0.5f * ctx.getEm();
            float r = 0.4f * ctx.getEm();

            switch (lb.getListStyleType()) {
                case "circle":
                    writeCircleBullet(lb, x, y, r, ctx.getColor());
                    break;
                case "square":
                    writeSquareBullet(lb, x, y, r, ctx.getColor());
                    break;
                case "disc":
                    writeDiscBullet(lb, x, y, r, ctx.getColor());
                    break;
                default:
                    writeOtherBullet(lb, x, y);
                    break;
            }
        }
    }

    /**
     * Draw all types of markers
     *
     * @param lb the list-item box
     * @return true if image bullet was drawn
     */
    protected boolean writeMarkerImage(ListItemBox lb) {
        VisualContext ctx = lb.getVisualContext();
        float ofs = lb.getFirstInlineBoxBaseline();
        if (ofs == -1) ofs = ctx.getBaselineOffset(); //use the font baseline
        float x = lb.getAbsoluteContentX() - 0.5f * ctx.getEm();
        float y = lb.getAbsoluteContentY() + ofs;
        ContentImage img = lb.getMarkerImage().getImage();
        if (img != null) {
            if (img instanceof BitmapImage) {
                float iw = img.getWidth();
                float ih = img.getHeight();
                float ix = x - iw;
                float iy = y - ih;
                createImageBullet(lb, ix, iy, iw, ih, img);
                return true;
            } else
                return false;
        } else
            return false;
    }

    /**
     * Adds a gradient to the background with repetitions.
     *
     * @param img  the gradient image to be added.
     * @param elem instance of ElementBox
     * @param bb   the background bounding box
     */
    protected void addGradient(BackgroundImageGradient img, ElementBox elem, Rectangle bb) {
        BackgroundRepeater rep = new BackgroundRepeater();
        final Gradient grad = img.getGradient();
        final Rectangle pos = img.getComputedPosition();
        Rectangle clipped = elem.getClippedBounds();
        clipped = new Rectangle(clipped.x - bb.x, clipped.y - bb.y, clipped.width, clipped.height); //make the clip relative to the background bounds

        if (grad instanceof LinearGradient) {
            rep.repeatImage(bb, pos, clipped, img.isRepeatX(), img.isRepeatY(),
                    (x, y) -> addLinearGradient(img, x + bb.x, y + bb.y, elem));

        } else if (grad instanceof RadialGradient) {
            rep.repeatImage(bb, pos, clipped, img.isRepeatX(), img.isRepeatY(),
                    (x, y) -> {
                        addRadialGradient(img, x + bb.x, y + bb.y, elem);
                    });
        }
    }

    /**
     * Checks whether the clipping is necessary for the element: it has overflow different from visible.
     * This method is overload in further implementations
     *
     * @param elem - the element
     * @return return true if element need clip path for border radius. Otherwise return false
     */
    protected boolean clippingUsed(ElementBox elem) {
        return (elem instanceof BlockBox && ((BlockBox) elem).getOverflowX() != BlockBox.OVERFLOW_VISIBLE);
    }

    /**
     * Create class which represent and calculate of points for border and call method for render border
     * This method is overload in further implementations
     *
     * @param elem - the element
     * @param bb   - the background bounding box
     */
    protected void renderBorder(ElementBox elem, Rectangle bb) {
    }


    /**
     * Render colored background
     * This method is overload in further implementations
     *
     * @param elem - the element
     * @param bb   - the background bounding box
     * @param bg   - source of background
     */
    protected void renderColorBg(ElementBox elem, Rectangle bb, BackgroundDecoder bg) {
    }

    /**
     * Render background with image
     * This method is overload in further implementations
     *
     * @param elem   - the element
     * @param bb     - the background bounding box
     * @param bitmap - image
     */
    protected void renderImageBg(ElementBox elem, Rectangle bb, BackgroundBitmap bitmap) {
    }

    /**
     * Method for draw image for replacement content
     * This method is overload in further implementations
     *
     * @param box - replaced box
     * @param img - image for replace
     */
    protected void insertReplacedImage(ReplacedBox box, ContentImage img) {
    }

    /**
     * Method for draw text for replacement content
     * This method is overload in further implementations
     *
     * @param box - replaced box
     */
    protected void insertReplacedText(ReplacedBox box) {
    }

    /**
     * Method for draw circle for list
     * This method is overload in further implementations
     *
     * @param lb    - the list-item box
     * @param x     - x coordinate
     * @param y     - y coordinate
     * @param r     - radius of circle
     * @param color - color for bullet
     */
    protected void writeCircleBullet(ListItemBox lb, float x, float y, float r, Color color) {
    }

    /**
     * Method for draw square for list
     * This method is overload in further implementations
     *
     * @param lb    - the list-item box
     * @param x     - x coordinate
     * @param y     - y coordinate
     * @param r     - radius of circle
     * @param color - color for bullet
     */
    protected void writeSquareBullet(ListItemBox lb, float x, float y, float r, Color color) {
    }

    /**
     * Method for draw filled circle for list
     * This method is overload in further implementations
     *
     * @param lb    - the list-item box
     * @param x     - x coordinate
     * @param y     - y coordinate
     * @param r     - radius of circle
     * @param color - color for bullet
     */
    protected void writeDiscBullet(ListItemBox lb, float x, float y, float r, Color color) {
    }

    /**
     * Method for draw other type bullet for list
     * This method is overload in further implementations
     *
     * @param lb - the list-item box
     * @param x  - x coordinate
     * @param y  - y coordinate
     */
    protected void writeOtherBullet(ListItemBox lb, float x, float y) {
    }

    /**
     * Method for draw image for list
     * This method is overload in further implementations
     *
     * @param lb  - the list-item box
     * @param ix  - x coordinate
     * @param iy  - y coordinate
     * @param iw  - image width
     * @param ih  - image height
     * @param img - image
     */
    protected void createImageBullet(ListItemBox lb, float ix, float iy, float iw, float ih, ContentImage img) {
    }

    /**
     * Method for draw linear gradient
     * This method is overload in further implementations
     *
     * @param bgimage - a background image created by a gradient
     * @param absx    - x coordinate
     * @param absy    - y coordinate
     * @param elem    - the element
     */
    protected void addLinearGradient(BackgroundImageGradient bgimage, float absx, float absy, ElementBox elem) {
    }

    /**
     * Method for draw linear gradient
     * This method is overload in further implementations
     *
     * @param bgimage - a background image created by a gradient
     * @param absx    - x coordinate
     * @param absy    - y coordinate
     * @param elem    - the element
     */
    protected void addRadialGradient(BackgroundImageGradient bgimage, float absx, float absy, ElementBox elem) {
    }
}
