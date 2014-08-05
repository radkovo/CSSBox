/*
 * BrowserCanvas.java
 * Copyright (c) 2005-2014 Radek Burget
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
 * Created on 13. z��� 2005, 15:44
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.render.GraphicsRenderer;

/**
 * This class provides an abstraction of a browser rendering area and the main layout engine
 * interface. Afrer the layout, a document image is created by drawing all the boxes and it
 * is drawn on the component.
 * 
 * @author  burgetr
 */
public class BrowserCanvas extends JPanel
{
	private static final long serialVersionUID = -8715215920271505397L;
    private static Logger log = LoggerFactory.getLogger(BrowserCanvas.class);

	protected Element root;
    protected DOMAnalyzer decoder;
    protected URL baseurl;
    protected Viewport viewport;

    protected BufferedImage img;

    protected BrowserConfig config;
    protected boolean createImage;
    protected boolean autoSizeUpdate;
    protected boolean autoMediaUpdate;
    
    /** 
     * Creates a new instance of the browser engine for a document. After creating the engine,
     * the layout itself may be computed by calling {@link #createLayout(Dimension)}.
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param baseurl the document base URL   
     */
    public BrowserCanvas(org.w3c.dom.Element root, DOMAnalyzer decoder, URL baseurl)
    {
        this.root = root;
        this.decoder = decoder;
        this.baseurl = baseurl;
        this.config = new BrowserConfig();
        this.createImage = true;
        this.autoSizeUpdate = true;
        this.autoMediaUpdate = true;
    }
    
    /** 
     * Creates a new instance of the browser engine for a document and creates the layout.
     * 
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param dim the preferred canvas dimensions
     * @param baseurl the document base URL   
     */
    public BrowserCanvas(org.w3c.dom.Element root,
                         DOMAnalyzer decoder,
                         Dimension dim, URL baseurl)
    {
        this(root, decoder, baseurl);
        createLayout(dim);
    }
    
    /**
     * Obtains the current browser configuration.
     * @return current configuration.
     */
    public BrowserConfig getConfig()
    {
        return config;
    }

    /**
     * Sets the browser configuration used for rendering.
     * @param config the new configuration.
     */
    public void setConfig(BrowserConfig config)
    {
        this.config = config;
    }

    /**
     * After creating the layout, the root box of the document can be accessed through this method.
     * @return the root box of the rendered document. Normally, it corresponds to the &lt;html&gt; element
     */
    public ElementBox getRootBox()
    {
        if (viewport == null)
            return null;
        else
            return viewport.getRootBox();
    }
    
    /**
     * After creating the layout, the viewport box can be accessed through this method.
     * @return the viewport box. This box provides a container of all the rendered boxes.
     */
    public Viewport getViewport()
    {
    	return viewport;
    }
    
    /**
     * Creates the document layout according to the given viewport size where the visible area size
     * is equal to the whole canvas. If the size of the resulting page is greater than the specified
     * one (e.g. there is an explicit width or height specified for the resulting page), the viewport
     * size is updated automatically.
     * @param dim the viewport size
     */
    public void createLayout(Dimension dim)
    {
        createLayout(dim, new Rectangle(dim));
    }
    
    /**
     * Creates the document layout according to the canvas and viewport size and position. If the size 
     * of the resulting page is greater than the specified one (e.g. there is an explicit width or height
     * specified for the resulting page), the total canvas size is updated automatically.
     * @param dim the total canvas size 
     * @param visibleRect the viewport (the visible area) size and position
     */
    public void createLayout(Dimension dim, Rectangle visibleRect)
    {
        if (createImage)
            img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = img.createGraphics();
        
        if (autoMediaUpdate)
        {
            decoder.getMediaSpec().setDimensions(visibleRect.width, visibleRect.height);
            decoder.recomputeStyles();
        }
        
        log.trace("Creating boxes");
        BoxFactory factory = new BoxFactory(decoder, baseurl);
        factory.setConfig(config);
        factory.reset();
        VisualContext ctx = new VisualContext(null, factory);
        viewport = factory.createViewportTree(root, ig, ctx, dim.width, dim.height);
        log.trace("We have " + factory.next_order + " boxes");
        viewport.setVisibleRect(visibleRect);
        viewport.initSubtree();
        
        log.trace("Layout for "+dim.width+"px");
        viewport.doLayout(dim.width, true, true);
        log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");

        if (autoSizeUpdate)
        {
            log.trace("Updating viewport size");
            viewport.updateBounds(dim);
            log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
        }
        
        if (createImage && (viewport.getWidth() > dim.width || viewport.getHeight() > dim.height))
        {
            img = new BufferedImage(Math.max(viewport.getWidth(), dim.width),
                                    Math.max(viewport.getHeight(), dim.height),
                                    BufferedImage.TYPE_INT_RGB);
            ig = img.createGraphics();
        }
        
        log.trace("Positioning for "+viewport.getWidth()+"x"+viewport.getHeight()+"px");
        viewport.absolutePositions();
        
        log.trace("Drawing");
        clearCanvas();
        GraphicsRenderer r = new GraphicsRenderer(ig); 
        viewport.draw(r);
        r.close();
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        revalidate();
    }
    
    public void updateVisibleArea(Rectangle visibleRect)
    {
        viewport.setVisibleRect(visibleRect);
        viewport.absolutePositions();
        clearCanvas();
        GraphicsRenderer r = new GraphicsRenderer(getImageGraphics()); 
        viewport.draw(r);
        r.close();
        revalidate();
    }
    
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, null);
    }    

    /**
     * Fills the whole canvas with the white background.
     */
    public void clearCanvas()
    {
        Graphics2D ig = img.createGraphics();
        viewport.drawBackground(ig);
    }
    
    /**
     * Redraws all the rendered boxes.
     */
    public void redrawBoxes()
    {
        Graphics2D ig = img.createGraphics();
        clearCanvas();
        viewport.draw(new GraphicsRenderer(ig));
        revalidate();
    }
    
    /**
     * @return the graphics context for drawing in the page image
     */
    public Graphics2D getImageGraphics()
    {
        return img.createGraphics();
    }
    
    /**
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
        createImage = false;
    }
    
    /**
     * Enables or disables the automatic viewport size update according to its contents. This is enabled by default.
     * @param b <code>true</code> for enable, <code>false</code> for disable.
     */
    public void setAutoSizeUpdate(boolean b)
    {
        autoSizeUpdate = b;
    }
    
    /**
     * Checks whether the automatic viewport size update is enabled.
     * @return <code>true</code> when enabled
     */
    public boolean getAutoSizeUpdate()
    {
        return autoSizeUpdate;
    }

    /**
     * Enables or disables automatic updating of the display area size specified in the current media specification.
     * When enabled, the size in the media specification is updated automatically when
     * {@link BrowserCanvas#createLayout(Dimension, Rectangle)} is called. When disabled, the media specification
     * is not modified automatically. By default, the automatic update is enabled.
     * 
     * @param autoMediaUpdate {@code true} when enabled.
     */
    public void setAutoMediaUpdate(boolean autoMediaUpdate)
    {
        this.autoMediaUpdate = autoMediaUpdate;
    }
    
    /**
     * Checks whether the automatic media update is enabled.
     * @return {@code true} if enabled.
     */
    public boolean getAutoMediaUpdate()
    {
        return autoMediaUpdate;
    }

}
