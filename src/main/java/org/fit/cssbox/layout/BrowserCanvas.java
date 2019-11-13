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

import java.awt.Graphics;
import java.net.URL;

import javax.swing.JPanel;

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

    private GraphicsEngine engine;
    
    /** 
     * Creates a new instance of the browser engine for a document. After creating the engine,
     * the layout itself may be computed by calling {@link #createLayout(Dimension)}.
     * @param root the &lt;body&gt; element of the document to be rendered
     * @param decoder the CSS decoder used to compute the style
     * @param baseurl the document base URL   
     */
    public BrowserCanvas(org.w3c.dom.Element root, DOMAnalyzer decoder, URL baseurl)
    {
        engine = new GraphicsEngine(root, decoder, baseurl);
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
        engine = new GraphicsEngine(root, decoder, dim, baseurl);
    }

    /**
     * Gets the rendering engine used in this canvas.
     * @return the rendering engine
     */
    public GraphicsEngine getEngine()
    {
        return engine;
    }

    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(engine.getImage(), 0, 0, null);
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
        engine.createLayout(dim, visibleRect);
        setPreferredSize(new java.awt.Dimension(engine.getImage().getWidth(), engine.getImage().getHeight()));
        revalidate();
    }
    
    /**
     * Redraws all the rendered boxes.
     */
    public void redrawBoxes()
    {
        engine.redrawBoxes();
        revalidate();
    }

}
