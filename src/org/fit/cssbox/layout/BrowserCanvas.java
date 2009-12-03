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
 * Created on 13. zברם 2005, 15:44
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
    protected ElementBox box;

    protected BufferedImage img;
    
    /** 
     * Creates a new instance of the browser engine.for a document
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param dim the viewport dimensions
     * @param baseurl the document base URL   
     */
    public BrowserCanvas(org.w3c.dom.Element root,
                         DOMAnalyzer decoder,
                         Dimension dim, URL baseurl)
    {
        this.root = root;
        this.decoder = decoder;
        this.baseurl = baseurl;
        createLayout(dim);
    }
    
    /**
     * After creating the layout, the root box of the document can be accessed through this method.
     * @return the root box of the rendered document. Normally, it corresponds to the &lt;body&gt; element
     */
    public ElementBox getRootBox()
    {
        return box;
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
        img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        System.gc();
        Graphics2D ig = img.createGraphics();
        
        VisualContext ctx = new VisualContext(null);
        
        viewport = new Viewport(root, ig, ctx, dim.width, dim.height);
        
        System.err.println("Creating boxes");
        Box.next_order = 0;
        box = (ElementBox) Box.createBoxTree(root, ig, ctx, decoder, baseurl, viewport, viewport, viewport, viewport, null);
        System.err.println("We have " + Box.next_order + " boxes, root is " + box);
        box.makeRoot();
        viewport.addSubBox(box);
        viewport.initBoxes();
        viewport.loadSizes();
        
        System.err.println("Layout for "+dim.width+"px");
        viewport.doLayout(dim.width, true, true);
        System.err.println("Resulting size: " + box.getWidth() + "x" + box.getHeight() + " (" + box + ")");
        System.err.println("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");

        System.err.println("Updating viewport size");
        viewport.updateBounds();
        System.err.println("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
        
        if (viewport.getWidth() > dim.width || viewport.getHeight() > dim.height)
        {
            img = new BufferedImage(Math.max(viewport.getWidth(), dim.width),
                                    Math.max(viewport.getHeight(), dim.height),
                                    BufferedImage.TYPE_INT_RGB);
            ig = img.createGraphics();
        }
        
        System.err.println("Positioning for "+img.getWidth()+"x"+img.getHeight()+"px");
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
        Color bg = box.getBgcolor();
        if (bg == null) bg = Color.white;
        ig.setColor(bg);
        ig.fillRect(0, 0, img.getWidth(), img.getHeight());
        ig.setColor(Color.black);
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
    
}
