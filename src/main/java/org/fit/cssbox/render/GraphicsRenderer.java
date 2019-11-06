/*
 * GraphicsRenderer.java
 * Copyright (c) 2005-2013 Radek Burget
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
 * Created on 8.3.2013, 11:41:50 by burgetr
 */
package org.fit.cssbox.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.layout.BackgroundImage;
import org.fit.cssbox.layout.BlockBox;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.LengthSet;
import org.fit.cssbox.layout.ListItemBox;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.ReplacedContent;
import org.fit.cssbox.layout.ReplacedImage;
import org.fit.cssbox.layout.ReplacedText;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.layout.VisualContext;
import org.fit.cssbox.layout.Box.DrawStage;
import org.fit.cssbox.misc.CSSStroke;
import org.fit.cssbox.misc.Coords;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.CSSProperty.TextDecoration;

/**
 * This renderers displays the boxes graphically using the given Graphics2D context.
 * 
 * @author burgetr
 */
public class GraphicsRenderer implements BoxRenderer
{
    /** the used graphic context */
    protected Graphics2D g;

    /** applied transformations */
    protected Map<ElementBox, AffineTransform> savedTransforms;
    
    /**
     * Constructs a renderer using the given graphics contexts.
     * @param g The graphics context used for painting the boxes.
     */
    public GraphicsRenderer(Graphics2D g)
    {
        this.g = g;
        savedTransforms = new HashMap<ElementBox, AffineTransform>();
    }
    
    //====================================================================================================
    
    public void startElementContents(ElementBox elem)
    {
        //setup transformations for the contents
        AffineTransform at = Transform.createTransform(elem);
        if (at != null)
        {
            savedTransforms.put(elem, g.getTransform());
            g.transform(at);
        }
    }

    public void finishElementContents(ElementBox elem)
    {
        //restore the stransformations
        AffineTransform origAt = savedTransforms.get(elem);
        if (origAt != null)
            g.setTransform(origAt);
    }
    
    public void renderElementBackground(ElementBox elem)
    {
        AffineTransform origAt = null;
        AffineTransform at = Transform.createTransform(elem);
        if (at != null)
        {
            origAt = g.getTransform();
            g.transform(at);
        }
        
        if (elem instanceof Viewport)
            clearViewport((Viewport) elem);
        drawBackground(elem, g);
        
        if (origAt != null)
            g.setTransform(origAt);
    }

    @Override
    public void renderMarker(ListItemBox elem)
    {
        drawMarker(elem, g);
    }

    public void renderTextContent(TextBox text)
    {
        drawContent(text, g);
    }

    public void renderReplacedContent(ReplacedBox box)
    {
        AffineTransform origAt = null;
        if (box instanceof ElementBox)
        {
            AffineTransform at = Transform.createTransform((ElementBox) box);
            if (at != null)
            {
                origAt = g.getTransform();
                g.transform(at);
            }
        }
        
        drawReplacedContent(box, g);
        
        if (origAt != null)
            g.setTransform(origAt);
    }

    public void close()
    {
    }    
    
    public void clearCanvas(Viewport vp)
    {
        clearViewport(vp);
    }
    
    //=============================================================================================
    // ElementBox
    
    protected void clearViewport(Viewport vp)
    {
        if (vp.getBgcolor() != null)
        {
            Color color = g.getColor();
            g.setColor(vp.getBgcolor());
            g.fillRect(0, 0, Math.round(vp.getCanvasWidth()), Math.round(vp.getCanvasHeight()));
            g.setColor(color);
        }
    }
    
    /** 
     * Draw the background and border of this box (no subboxes).
     * This method is normally called automatically from {@link Box#draw(DrawStage)}.
     * @param g the graphics context used for drawing 
     */
    protected void drawBackground(ElementBox elem, Graphics2D g)
    {
        Color color = g.getColor(); //original color
        Shape oldclip = setupBoxClip(g, elem); //original clip region
        elem.getVisualContext().updateGraphics(g);
        
        //border bounds
        Rectangle brd = elem.getAbsoluteBorderBounds();
        
        //draw the background - it should be visible below the border too
        if (elem.getBgcolor() != null)
        {
            g.setColor(elem.getBgcolor());
            final Rectangle2D abrd = awtRect2D(brd);
            g.fill(abrd);
        }
        
        //draw the background images
        if (elem.getBackgroundImages() != null)
        {
            Rectangle bg = elem.getAbsoluteBackgroundBounds();
            for (BackgroundImage img : elem.getBackgroundImages())
            {
                BufferedImage bimg = img.getBufferedImage();
                if (bimg != null)
                    g.drawImage(bimg, Math.round(bg.x), Math.round(bg.y), null);
            }
        }
        
        //draw the border
        drawBorders(elem, g, brd.x, brd.y, brd.x + brd.width - 1, brd.y + brd.height - 1);
        
        g.setClip(oldclip); //restore the clipping
        g.setColor(color); //restore original color
    }
    
    protected void drawBorders(ElementBox elem, Graphics2D g, float bx1, float by1, float bx2, float by2)
    {
        LengthSet border = elem.getBorder();
        if (border.top > 0 && bx2 > bx1)
            drawBorder(elem, g, bx1, by1, bx2, by1, border.top, 0, 0, "top", false);
        if (border.right > 0 && by2 > by1)
            drawBorder(elem, g, bx2, by1, bx2, by2, border.right, -border.right + 1, 0, "right", true); 
        if (border.bottom > 0 && bx2 > bx1)
            drawBorder(elem, g, bx1, by2, bx2, by2, border.bottom, 0, -border.bottom + 1, "bottom", true); 
        if (border.left > 0 && by2 > by1)
            drawBorder(elem, g, bx1, by1, bx1, by2, border.left, 0, 0, "left", false); 
    }
    
    private void drawBorder(ElementBox elem, Graphics2D g, float x1, float y1, float x2, float y2, float width, 
                            float right, float down, String side, boolean reverse)
    {
        TermColor tclr = elem.getStyle().getSpecifiedValue(TermColor.class, "border-"+side+"-color");
        CSSProperty.BorderStyle bst = elem.getStyle().getProperty("border-"+side+"-style");
        if (bst != CSSProperty.BorderStyle.HIDDEN && (tclr == null || !tclr.isTransparent()))
        {
            //System.out.println("Elem: " + this + "side: " + side + "color: " + tclr);
            Color clr = null;
            if (tclr != null)
                clr = CSSUnits.convertColor(tclr.getValue());
            if (clr == null)
            {
                clr = elem.getVisualContext().getColor();
                if (clr == null)
                    clr = Color.BLACK;
            }
            g.setColor(clr);
            g.setStroke(new CSSStroke(width, bst, reverse));
            g.draw(new Line2D.Float(x1 + right, y1 + down, x2 + right, y2 + down));
        }
    }

    //====================================================================================================
    // ListItemBox
    
    /**
     * Draw the list item symbol, number or image depending on list-style-type
     */
    protected void drawMarker(ListItemBox elem, Graphics2D g)
    {
        Shape oldclip = setupBoxClip(g, elem); //original clip region
        
        if (elem.getMarkerImage() != null)
        {
            if (!drawImage(elem, g))
                drawBullet(elem, g);
        }
        else
            drawBullet(elem, g);
        
        g.setClip(oldclip);
    }
    
    /**
     * Draws a bullet or text marker
     */
    protected void drawBullet(ListItemBox elem, Graphics2D g)
    {
        final VisualContext ctx = elem.getVisualContext();
        ctx.updateGraphics(g);
        float x = elem.getAbsoluteContentX() - 1.2f * ctx.getEm();
        float y = elem.getAbsoluteContentY() + 0.5f * ctx.getEm();
        float r = 0.4f * ctx.getEm();
        if (elem.getStyleType() == CSSProperty.ListStyleType.CIRCLE) 
            g.draw(new Ellipse2D.Float(x, y, r, r));
        else if (elem.getStyleType() == CSSProperty.ListStyleType.SQUARE) 
            g.fill(new Rectangle2D.Float(x, y, r, r));
        else if (elem.getStyleType() == CSSProperty.ListStyleType.DISC)
            g.fill(new Ellipse2D.Float(x, y, r, r));
        else if (elem.getStyleType() != CSSProperty.ListStyleType.NONE)
            drawText(elem, g, elem.getMarkerText());
    }
    
    /**
     * Draws an image marker 
     */
    protected boolean drawImage(ListItemBox elem, Graphics2D g)
    {
        float ofs = elem.getFirstInlineBoxBaseline();
        if (ofs < 0)
            ofs = elem.getVisualContext().getBaselineOffset(); //use the font baseline
        float x = elem.getAbsoluteContentX() - 0.5f * elem.getVisualContext().getEm();
        float y = elem.getAbsoluteContentY() + ofs;
        Image img = elem.getMarkerImage().getImage();
        if (img != null)
        {
            int w = img.getWidth(elem.getMarkerImage());
            int h = img.getHeight(elem.getMarkerImage());
            g.drawImage(img, Math.round(x - w), Math.round(y - h), elem.getMarkerImage());
            return true;
        }
        else
            return false; //could not decode the image
    }

    /**
     * Draws a text marker
     */
    protected void drawText(ListItemBox elem, Graphics2D g, String text)
    {
        // top left corner
        float x = elem.getAbsoluteContentX();
        float y = elem.getAbsoluteContentY();

        //Align Y with baseline
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(text, g);
        float ofs = elem.getFirstInlineBoxBaseline();
        if (ofs < 0)
            ofs = elem.getVisualContext().getBaselineOffset(); //use the font baseline
        
        // Draw the string
        g.drawString(text,
                     x + (float) rect.getX() - (float) rect.getWidth(),
                     y + ofs);
    }
    
    
    //====================================================================================================
    // TextBox
    
    /** 
     * Draw the text content of a text box (no subboxes)
     * @param tb the text box to draw
     * @param g the graphics context to draw on
     */
    protected void drawContent(TextBox tb, Graphics2D g)
    {
        //top left corner
        final float x = tb.getAbsoluteBounds().x;
        final float y = tb.getAbsoluteBounds().y;

        //Draw the string
        String t = tb.getText();
        if (!t.isEmpty())
        {
            Shape oldclip = setupBoxClip(g, tb);
            tb.getVisualContext().updateGraphics(g);
            
            if (tb.getWordSpacing() == null && Coords.eq(tb.getExtraWidth(), 0))
                drawAttributedString(tb, g, x, y, t);
            else
                drawByWords(tb, g, x, y, t);
            
            g.setClip(oldclip);
        }
    }

    private void drawByWords(TextBox tb, Graphics2D g, float x, float y, String text)
    {
        String[] words = text.split(" ");
        if (words.length > 0)
        {
            final FontMetrics fm = g.getFontMetrics();
            final float[][] offsets = tb.getWordOffsets(fm, words);
            for (int i = 0; i < words.length; i++)
                drawAttributedString(tb, g, x + offsets[i][0], y, words[i]);
        }
        else
            drawAttributedString(tb, g, x, y, text);
    }
    
    /**
     * Draws a single string with eventual attributes based on the current visual context.
     */
    private void drawAttributedString(TextBox tb, Graphics2D g, float x, float y, String text)
    {
        final Set<TextDecoration> decoration = tb.getEfficientTextDecoration();
        if (!decoration.isEmpty()) 
        {
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FONT, tb.getVisualContext().getFont());
            if (decoration.contains(CSSProperty.TextDecoration.UNDERLINE))
                as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            if (decoration.contains(CSSProperty.TextDecoration.LINE_THROUGH))
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            g.drawString(as.getIterator(), x, y + tb.getBaselineOffset());
        } 
        else
            g.drawString(text, x, y + tb.getBaselineOffset());
    }
    
    //====================================================================================================
    // Replaced boxes
    
    public void drawReplacedContent(ReplacedBox box, Graphics2D g)
    {
        final ReplacedContent obj = box.getContentObj();
        if (obj != null)
        {
            Shape oldclip = g.getClip();
            Rectangle2D extclip = null;
            if (box instanceof BlockBox)
                extclip = awtRect2D(((BlockBox) box).getClippingRectangle());
            g.setClip(applyClip(oldclip, awtRect2D(((ElementBox) box).getClippedContentBounds()), extclip));
            
            if (obj instanceof ReplacedImage)
            {
                drawReplacedImage((ReplacedImage) obj, g, 
                        Math.round(box.getContentObjWidth()), Math.round(box.getContentObjHeight()));
            }
            else if (obj instanceof ReplacedText)
            {
                ((ReplacedText) obj).getContentViewport().draw(this); //draw the contents using this renderer
            }
            
            g.setClip(oldclip);
        }
    }

    
    public void drawReplacedImage(ReplacedImage img, Graphics2D g, int width, int height)
    {
        Rectangle bounds = img.getOwner().getAbsoluteContentBounds();

        if (img.getImage() != null)
        {
            // update our configuration
            img.getVisualContext().updateGraphics(g);

            // no container that would repaint -- wait for the complete image
            if (img.getContainer() == null)
                img.waitForLoad();
            // draw image
            g.drawImage(img.getImage(),
                    Math.round(bounds.x), Math.round(bounds.y),
                    Math.round(width), Math.round(height),
                    img);
        }
        else
        {
            img.getVisualContext().updateGraphics(g);
            g.setStroke(new BasicStroke(1));
            g.drawRect(Math.round(bounds.x), Math.round(bounds.y),
                    Math.round(bounds.width - 1), Math.round(bounds.height - 1));
        }

    }
    
    //====================================================================================================
    
    /**
     * Computes a new clipping region from the current one and the eventual clipping box
     * @param current current clipping region
     * @param newclip new clipping box to be used
     * @return the new clipping rectangle
     */
    protected Rectangle2D applyClip(java.awt.Shape current, Rectangle2D newclip)
    {
        if (current == null)
            return newclip;
        else
        {
            if (current instanceof java.awt.Rectangle)
                return ((java.awt.Rectangle) current).createIntersection(newclip);
            else
                return current.getBounds().createIntersection(newclip);
        }
    }

    /**
     * Computes a new clipping region from the current one and the eventual clipping box
     * @param current current clipping region
     * @param newclip new clipping box to be used
     * @param extclip an external clipping rectangle specified e.g. using the {@code clip} CSS property
     * @return the new clipping rectangle
     */
    protected Rectangle2D applyClip(java.awt.Shape current, Rectangle2D newclip, Rectangle2D extclip)
    {
        if (extclip != null)
            return applyClip(current, newclip.createIntersection(extclip));
        else
            return applyClip(current, newclip);
    }

    /**
     * Configures the clipping of he given graphics object according to the given box setting.
     * If the box is not clipped, nothing is changed.
     * @param g the graphics to be set up
     * @param box the box drawn
     * @return the original clipping shape for restoring later
     */
    protected Shape setupBoxClip(Graphics2D g, Box box)
    {
        Shape oldclip = g.getClip();
        if (box.getClipBlock() != null)
        {
            Rectangle2D extclip = null;
            if (box instanceof BlockBox)
                extclip = awtRect2D(((BlockBox) box).getClippingRectangle());
            g.setClip(applyClip(oldclip, awtRect2D(box.getClipBlock().getClippedContentBounds()), extclip));
        }
        return oldclip;
    }
    
    protected Rectangle2D awtRect2D(Rectangle rect)
    {
        if (rect == null)
            return null;
        return new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
    }
    
    
    
}
