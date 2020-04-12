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
package org.fit.cssbox.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fit.cssbox.css.BackgroundDecoder;
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
import org.fit.cssbox.layout.ContentImage;
import org.fit.cssbox.misc.Coords;
import org.fit.cssbox.render.BackgroundImageGradient;
import org.fit.cssbox.render.BackgroundImageImage;
import org.fit.cssbox.render.StructuredRenderer;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.CSSProperty.TextDecoration;

/**
 * This renderers displays the boxes graphically using the given Graphics2D context.
 * 
 * @author burgetr
 */
public class GraphicsRenderer extends StructuredRenderer
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
    
    /**
     * Sets the default Graphics2D parametres and configures according to a given visual context.
     * @param g The graphics to be configured.
     * @param ctx The visual context.
     */
    protected void setupGraphics(Graphics2D g, GraphicsVisualContext ctx)
    {
        ctx.updateGraphics(g);
    }
    
    /**
     * Clears the drawing area represented by a Viewport and fills it with a background color.
     * @param vp the Viewport for obtaining the canvas area and the background color 
     */
    public void clearCanvas()
    {
        clearViewport(getViewport());
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
        drawListMarker(elem, g);
    }

    public void renderTextContent(TextBox text)
    {
        drawTextContent(text, g);
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
    
    //=============================================================================================
    // ElementBox
    
    protected void clearViewport(Viewport vp)
    {
        Color bgcolor = convertColor(vp.getConfig().getViewportBackgroundColor());
        if (getViewportBackground() != null && getViewportBackground().getBgcolor() != null)
            bgcolor = convertColor(getViewportBackground().getBgcolor());
        
        Color color = g.getColor();
        g.setColor(bgcolor);
        g.fillRect(0, 0, Math.round(vp.getCanvasWidth()), Math.round(vp.getCanvasHeight()));
        g.setColor(color);
    }
    
    /** 
     * Draws the background and border of an element box.
     * @param g the graphics context used for drawing 
     */
    protected void drawBackground(ElementBox elem, Graphics2D g)
    {
        Color color = g.getColor(); //original color
        Shape oldclip = setupBoxClip(g, elem); //original clip region
        setupGraphics(g, (GraphicsVisualContext) elem.getVisualContext());
        
        //border bounds
        Rectangle brd = elem.getAbsoluteBorderBounds();
        
        //draw the background
        BackgroundDecoder bg = findBackgroundSource(elem);
        if (bg != null)
        {
            //draw the background color
            if (bg.getBgcolor() != null)
            {
                g.setColor(convertColor(bg.getBgcolor()));
                final Rectangle2D abrd = awtRect2D(brd);
                g.fill(abrd);
            }
            
            //draw the background images
            if (bg.getBackgroundImages() != null)
            {
                final BackgroundBitmap bitmap = new BackgroundBitmap(elem);
                for (BackgroundImage img : bg.getBackgroundImages())
                {
                    if (img instanceof BackgroundImageImage)
                    {
                        bitmap.addBackgroundImage((BackgroundImageImage) img);
                    }
                    else if (img instanceof BackgroundImageGradient)
                    {
                        bitmap.addBackgroundImage((BackgroundImageGradient) img);
                    }
                }
                if (bitmap.getBufferedImage() != null)
                {
                    g.drawImage(bitmap.getBufferedImage(), Math.round(brd.x), Math.round(brd.y), null);
                }
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
    
    protected void drawBorder(ElementBox elem, Graphics2D g, float x1, float y1, float x2, float y2, float width, 
                            float right, float down, String side, boolean reverse)
    {
        TermColor tclr = elem.getStyle().getSpecifiedValue(TermColor.class, "border-"+side+"-color");
        CSSProperty.BorderStyle bst = elem.getStyle().getProperty("border-"+side+"-style");
        if (bst != CSSProperty.BorderStyle.HIDDEN && (tclr == null || !tclr.isTransparent()))
        {
            //System.out.println("Elem: " + this + "side: " + side + "color: " + tclr);
            Color clr = null;
            if (tclr != null)
                clr = convertColor(tclr.getValue());
            if (clr == null)
            {
                clr = convertColor(elem.getVisualContext().getColor());
                if (clr == null)
                    clr = Color.BLACK;
            }
            g.setColor(clr);
            Stroke oldstroke = g.getStroke();
            g.setStroke(new CSSStroke(width, bst, reverse));
            g.draw(new Line2D.Float(x1 + right, y1 + down, x2 + right, y2 + down));
            g.setStroke(oldstroke);
        }
    }

    //====================================================================================================
    // ListItemBox
    
    /**
     * Draw the list item symbol, number or image depending on list-style-type
     */
    protected void drawListMarker(ListItemBox elem, Graphics2D g)
    {
        setupGraphics(g, (GraphicsVisualContext) elem.getVisualContext());
        Shape oldclip = setupBoxClip(g, elem); //original clip region
        
        if (elem.getMarkerImage() != null)
        {
            if (!drawListImage(elem, g))
                drawListBullet(elem, g);
        }
        else
            drawListBullet(elem, g);
        
        g.setClip(oldclip);
    }
    
    /**
     * Draws a bullet or text marker
     */
    protected void drawListBullet(ListItemBox elem, Graphics2D g)
    {
        final VisualContext ctx = elem.getVisualContext();
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
            drawListTextMarker(elem, g, elem.getMarkerText());
    }
    
    /**
     * Draws an image marker 
     */
    protected boolean drawListImage(ListItemBox elem, Graphics2D g)
    {
        float ofs = elem.getFirstInlineBoxBaseline();
        if (ofs < 0)
            ofs = elem.getVisualContext().getBaselineOffset(); //use the font baseline
        float x = elem.getAbsoluteContentX() - 0.5f * elem.getVisualContext().getEm();
        float y = elem.getAbsoluteContentY() + ofs;
        ContentImage img = elem.getMarkerImage().getImage();
        if (img != null)
        {
            if (img instanceof BitmapImage)
            {
                float w = img.getWidth();
                float h = img.getHeight();
                g.drawImage(((BitmapImage) img).getBufferedImage(), Math.round(x - w), Math.round(y - h), null);
                return true;
            }
            else
                return false;
        }
        else
            return false; //could not decode the image
    }

    /**
     * Draws a text marker in a list.
     */
    protected void drawListTextMarker(ListItemBox elem, Graphics2D g, String text)
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
    protected void drawTextContent(TextBox tb, Graphics2D g)
    {
        //top left corner
        final float x = tb.getAbsoluteBounds().x;
        final float y = tb.getAbsoluteBounds().y;

        //Draw the string
        String t = tb.getText();
        if (!t.isEmpty())
        {
            Shape oldclip = setupBoxClip(g, tb);
            setupGraphics(g, (GraphicsVisualContext) tb.getVisualContext());
            
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
            final float[][] offsets = tb.getWordOffsets(words);
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
            as.addAttribute(TextAttribute.FONT, ((GraphicsVisualContext) tb.getVisualContext()).getFont());
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
    
    protected void drawReplacedContent(ReplacedBox box, Graphics2D g)
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

    
    protected void drawReplacedImage(ReplacedImage img, Graphics2D g, int width, int height)
    {
        Rectangle bounds = img.getOwner().getAbsoluteContentBounds();

        if (img.getImage() != null && img.getImage() instanceof BitmapImage)
        {
            // update our configuration
            setupGraphics(g, (GraphicsVisualContext) img.getVisualContext());

            // draw image of the given size and position
            final AffineTransform tr = new AffineTransform();
            tr.translate(bounds.x, bounds.y);
            tr.scale(bounds.width / img.getIntrinsicWidth(), bounds.height / img.getIntrinsicHeight());
            g.drawImage(((BitmapImage) img.getImage()).getBufferedImage(), tr, null);
        }
        else
        {
            setupGraphics(g, (GraphicsVisualContext) img.getVisualContext());
            Stroke oldstroke = g.getStroke();
            g.setStroke(new BasicStroke(1));
            g.drawRect(Math.round(bounds.x), Math.round(bounds.y),
                    Math.round(bounds.width - 1), Math.round(bounds.height - 1));
            g.setStroke(oldstroke);
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
    
    /**
     * Converts a CSSBox LengthRect to an AWT Rectangle2D for drawing.
     * @param rect the rectangle to be converted
     * @return the resulting Rectangle2D
     */
    protected Rectangle2D awtRect2D(Rectangle rect)
    {
        if (rect == null)
            return null;
        return new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
    }
    
    /**
     * Convetrs the CSS parser color representation to the AWT color used by CSSBox.
     * @param src Source CSS parser color representation
     * @return the resulting AWT color
     */
    public static java.awt.Color convertColor(cz.vutbr.web.csskit.Color src)
    {
        if (src == null)
            return null;
        return new java.awt.Color(src.getRGB(), true);
    }

}
