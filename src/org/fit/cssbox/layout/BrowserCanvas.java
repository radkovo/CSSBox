/*
 * BrowserCanvas.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
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
    protected BlockBox box;

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
    public BlockBox getRootBox()
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
        Graphics ig = img.getGraphics();
        
        VisualContext ctx = new VisualContext();
        ctx.em = 12; //some default values of em and ex
        ctx.ex = 10;
        
        viewport = new Viewport(ig, ctx, dim.width, dim.height);
        
        System.err.println("Creating boxes");
        Box.next_order = 0;
        box = (BlockBox) Box.createBoxTree(root, ig, ctx, decoder, baseurl, viewport, viewport, viewport, null);
        System.err.println("We have " + Box.next_order + " boxes");
        viewport.addSubBox(box);
        viewport.initBoxes();
        
        System.err.println("Collapsing margins");
        viewport.loadSizes();
        viewport.collapseMargins();
        
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
            ig = img.getGraphics();
        }
        
        System.err.println("Positioning for "+img.getWidth()+"x"+img.getHeight()+"px");
        viewport.absolutePositions(null);
        
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
        Graphics ig = img.getGraphics();
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
        Graphics ig = img.getGraphics();
        clearCanvas();
        viewport.draw(ig);
        revalidate();
    }
    
    /**
     * @return the graphics context for drawing in the page image
     */
    public Graphics getImageGraphics()
    {
        return img.getGraphics();
    }
    
}
