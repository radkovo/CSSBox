/*
 * BrowserCanvas.java
 * Copyright (c) 2005-2007 Radek Burget
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
import org.w3c.dom.*;

import org.fit.cssbox.css.DOMAnalyzer;

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

	protected Element root;
    protected DOMAnalyzer decoder;
    protected URL baseurl;
    protected Viewport viewport;

    protected BufferedImage img;

    protected BrowserConfig config;
    protected boolean createImage;
    protected boolean autoSizeUpdate;
    
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
    }
    
    /** 
     * Creates a new instance of the browser engine for a document and creates the layout.
     * 
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param dim the viewport dimensions
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
     * Creates the document layout according to the viewport size. If the size of the resulting
     * page is greater than the specified one (e.g. there is an explicit width or height specified
     * for the resulting page), the viewport size is updated automatically.
     * @param dim the viewport size
     */
    public void createLayout(Dimension dim)
    {
        if (createImage)
            img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = img.createGraphics();
        
        VisualContext ctx = new VisualContext(null);
        
        System.err.println("Creating boxes");
        BoxFactory factory = new BoxFactory(decoder, baseurl);
        factory.setConfig(config);
        factory.reset();
        viewport = factory.createViewportTree(root, ig, ctx, dim.width, dim.height);
        System.err.println("We have " + factory.next_order + " boxes");
        viewport.initSubtree();
        
        System.err.println("Layout for "+dim.width+"px");
        viewport.doLayout(dim.width, true, true);
        System.err.println("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");

        if (autoSizeUpdate)
        {
            System.err.println("Updating viewport size");
            viewport.updateBounds();
            System.err.println("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
        }
        
        if (createImage && (viewport.getWidth() > dim.width || viewport.getHeight() > dim.height))
        {
            img = new BufferedImage(Math.max(viewport.getWidth(), dim.width),
                                    Math.max(viewport.getHeight(), dim.height),
                                    BufferedImage.TYPE_INT_RGB);
            ig = img.createGraphics();
        }
        
        System.err.println("Positioning for "+viewport.getWidth()+"x"+viewport.getHeight()+"px");
        viewport.absolutePositions();
        
        clearCanvas();
        viewport.draw(ig);
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
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
        viewport.draw(ig);
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
    
}
