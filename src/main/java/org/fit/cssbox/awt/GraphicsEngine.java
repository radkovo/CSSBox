/*
 * GraphicsEngine.java
 * Copyright (c) 2005-2019 Radek Burget
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
 * Created on 13. 11. 2019, 15:05:30 by burgetr
 */
package org.fit.cssbox.awt;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.net.URL;

import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.FontTable;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.Engine;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.layout.VisualContext;
import org.fit.cssbox.render.BoxRenderer;
import org.fit.cssbox.render.GraphicsRenderer;
import org.w3c.dom.Element;

/**
 * An implementation of the rendering engine using a Graphics2D renderer and
 * visual context. 
 *
 * @author burgetr
 */
public class GraphicsEngine extends Engine
{
    private boolean createImage;
    private BufferedImage img;
    private Graphics2D ig;
    
    boolean useFractionalMetrics = false;
    boolean useKerning = true;

    
    /** 
     * Creates a new instance of the browser engine for a document. After creating the engine,
     * the layout itself may be computed by calling {@link #createLayout(Dimension)}.
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param baseurl the document base URL   
     */
    public GraphicsEngine(Element root, DOMAnalyzer decoder, URL baseurl)
    {
        super(root, decoder, baseurl);
        this.createImage = true;
    }

    /** 
     * Creates a new instance of the browser engine for a document and creates the layout.
     * 
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param dim the preferred canvas dimensions
     * @param baseurl the document base URL   
     */
    public GraphicsEngine(Element root, DOMAnalyzer decoder, Dimension dim, URL baseurl)
    {
        super(root, decoder, dim, baseurl);
        this.createImage = true;
    }

    /**
     * Gets the graphic context for drawing in the page image.
     * @return the graphics context
     */
    public Graphics2D getImageGraphics()
    {
        return ig;
    }
    
    /**
     * Gets the image used for rendering the page.
     * @return image containing the rendered page
     */
    public BufferedImage getImage()
    {
        return img;
    }
    
    /**
     * Sets a custom image that is used for rendering. Setting the custom image prevents BrowserCanvas
     * from creating the image automatically. This can be used for rendering to an image of a specific
     * size or format.
     * @param image The new image to be used for rendering.
     */
    public void setImage(BufferedImage image)
    {
        img = image;
        ig = img.createGraphics();
        setupGraphics(ig);
        createImage = false;
    }

    /**
     * Tests if fractional metrics are used by this engine.
     * @return {@code true} when yes
     */
    public boolean isUseFractionalMetrics()
    {
        return useFractionalMetrics;
    }

    /**
     * Switches using the fractional metrics on or off. This is a good idea for high-resolution or vector
     * output (such as SVG). On low-resolution display (such as a standard bitmap output), better results
     * are usually achieved with the fractional metrics switched off. Default is off.
     * @param useFractionalMetrics
     */
    public void setUseFractionalMetrics(boolean useFractionalMetrics)
    {
        this.useFractionalMetrics = useFractionalMetrics;
    }

    /**
     * Tests if font kerning is used by this engine.
     * @return {@code true} when yes
     */
    public boolean isUseKerning()
    {
        return useKerning;
    }

    /**
     * Switches the font kerning default is on.
     * @param useKerning
     */
    public void setUseKerning(boolean useKerning)
    {
        this.useKerning = useKerning;
    }

    //==========================================================================================================
    
    /**
     * Sets the default Graphics2D parametres.
     * @param g The graphics to be configured.
     */
    protected void setupGraphics(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (useFractionalMetrics)
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }
    
    @Override
    protected void initOutputMedia(float width, float height)
    {
        if (createImage)
        {
            img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
            ig = img.createGraphics();
            setupGraphics(ig);
        }
    }

    @Override
    protected VisualContext createVisualContext(BrowserConfig config, FontTable fontTable)
    {
        GraphicsVisualContext ctx = new GraphicsVisualContext(ig, null, config, fontTable);
        if (useKerning)
            ctx.getDefaultFontAttributes().put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        return ctx;
    }
    
    @Override
    public BoxRenderer getRenderer()
    {
        return new GraphicsRenderer(ig);
    }

    @Override
    protected void renderViewport(Viewport viewport)
    {
        // adds clearCanvas before rendering
        GraphicsRenderer r = (GraphicsRenderer) getRenderer();
        r.clearCanvas(viewport);
        viewport.draw(r);
        r.close();
    }
    
}
