/*
 * Engine.java
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
 * Created on 13. 11. 2019, 15:02:05 by burgetr
 */
package org.fit.cssbox.layout;

import java.net.URL;

import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.render.BoxRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstraction of the whole rendering engine. The subclasses of this class represent
 * the combinations of the generic rendering engine, a device-dependent combination of
 * a render and visual context implementation. 
 * 
 * @author burgetr
 */
public abstract class Engine
{
    private static Logger log = LoggerFactory.getLogger(Engine.class);

    private org.w3c.dom.Element root;
    private DOMAnalyzer decoder;
    private URL baseurl;
    private Viewport viewport;

    private BrowserConfig config;
    private boolean autoSizeUpdate;
    private boolean autoMediaUpdate;
    
    
    /** 
     * Creates a new instance of the browser engine for a document. After creating the engine,
     * the layout itself may be computed by calling {@link #createLayout(Dimension)}.
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param baseurl the document base URL   
     */
    public Engine(org.w3c.dom.Element root, DOMAnalyzer decoder, URL baseurl)
    {
        this.root = root;
        this.decoder = decoder;
        this.baseurl = baseurl;
        this.config = new BrowserConfig();
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
    public Engine(org.w3c.dom.Element root,
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
     * Gets the DOM root used for rendering.
     * @return The root element of the DOM tree.
     */
    public org.w3c.dom.Element getRootElement()
    {
        return root;
    }

    /**
     * Gets the DOMAnalyzer used for decoding the CSS styles.
     * @return the used DOMAnalyzer
     */
    public DOMAnalyzer getDecoder()
    {
        return decoder;
    }

    /**
     * Gets the base URL used for relative URLs.
     * @return The base URL
     */
    public URL getBaseUrl()
    {
        return baseurl;
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
        initOutputMedia(dim.width, dim.height);
        
        if (autoMediaUpdate)
        {
            decoder.getMediaSpec().setDimensions(visibleRect.width, visibleRect.height);
            decoder.recomputeStyles();
        }
        
        log.trace("Creating boxes");
        BoxFactory factory = new BoxFactory(decoder, baseurl);
        factory.setConfig(config);
        factory.reset();
        VisualContext ctx = createVisualContext(factory);
        viewport = factory.createViewportTree(root, ctx, dim.width, dim.height);
        log.trace("We have " + factory.next_order + " boxes");
        viewport.setVisibleRect(new Rectangle(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height));
        viewport.initSubtree();
        
        log.trace("Layout for "+dim.width+"px");
        viewport.doLayout(dim.width, true, true);
        log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");

        if (autoSizeUpdate)
        {
            log.trace("Updating viewport size");
            viewport.updateBounds(new Dimension(dim.width, dim.height));
            log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
        }
        
        if (viewport.getWidth() > dim.width || viewport.getHeight() > dim.height)
        {
            initOutputMedia(Math.max(viewport.getWidth(), dim.width), Math.max(viewport.getHeight(), dim.height));
        }
        
        log.trace("Positioning for "+viewport.getWidth()+"x"+viewport.getHeight()+"px");
        viewport.absolutePositions();
        
        log.trace("Drawing");
        renderViewport(viewport);
    }

    /**
     * Recomputes the layout according to a new visible viewport and redraws the layout.
     * @param visibleRect the new viewport position and size
     */
    public void updateVisibleArea(Rectangle visibleRect)
    {
        viewport.setVisibleRect(visibleRect);
        viewport.absolutePositions();
        renderViewport(viewport);
    }
    
    /**
     * Redraws all the rendered boxes.
     */
    public void redrawBoxes()
    {
        renderViewport(viewport);
    }
    
    /**
     * Renders the viewport using the internal renderer.
     * @param viewport the viewport to be rendered
     */
    protected void renderViewport(Viewport viewport)
    {
        final BoxRenderer r = getRenderer();
        viewport.draw(r);
        r.close();
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

    //==================================================================================================================
    
    /**
     * Initializes the output media for the given width and height. This method may be called
     * either at the beginning process or anytime when the rendered page size changes and it
     * gets re-rendered.
     * @param width the output page width
     * @param height the output page height
     */
    protected void initOutputMedia(float width, float height)
    {
        // to be re-implemented in subclasses
    }
    
    /**
     * Creates a root visual context which is later used in created boxes.
     * @param factory The box factory used for creating the boxes
     * @return The new visual context instance.
     */
    protected abstract VisualContext createVisualContext(BoxFactory factory);
    
    /**
     * Gets a renderer that is used for rendering the output in this rendering engine.
     * @return The renderer instance
     */
    public abstract BoxRenderer getRenderer();
    
}
