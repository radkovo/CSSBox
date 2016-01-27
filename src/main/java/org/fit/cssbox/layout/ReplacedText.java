/*
 * ReplacedText.java
 * Copyright (c) 2005-2012 Radek Burget
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
 * Created on 28.11.2012, 13:00:49 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.net.URL;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The replaced content represented by a text (HTML or XML) document.
 * 
 * @author burgetr
 */
public class ReplacedText extends ReplacedContent
{
    private static Logger log = LoggerFactory.getLogger(ReplacedText.class);

    private Document doc;
    private URL base;
    private String encoding;
    private DOMAnalyzer decoder;
    private Viewport viewport;
    
    /** The last dimension used for layout or null when no layout has been created */
    private Dimension currentDimension;

    /** The final dimension used for layout or null when no layout has been created */
    private Dimension layoutDimension;
    
    public ReplacedText(ElementBox owner, Document doc, URL base, String encoding)
    {
        super(owner);
        this.doc = doc;
        this.base = base;
        this.encoding = encoding;
        currentDimension = null;
        layoutDimension = null;
        createDecoder();
    }

    /**
     * Obtains the viewport of the contents.
     * @return the viewport
     */
    public Viewport getContentViewport()
    {
        return viewport;
    }
    
    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        viewport.draw(owner.getViewport().getRenderer());
    }

    @Override
    public int getIntrinsicWidth()
    {
        checkLayout();
        return viewport.getWidth();
    }

    @Override
    public int getIntrinsicHeight()
    {
        checkLayout();
        return viewport.getHeight();
    }

    @Override
    public float getIntrinsicRatio()
    {
        return (float) getIntrinsicWidth() / (float) getIntrinsicHeight();
    }
    
    @Override
    public void doLayout()
    {
        layoutDimension = new Dimension(owner.getContent()); //use owner content size for dimension
        checkLayout();
    }

    @Override
    public void absolutePositions()
    {
        viewport.absolutePositions();
    }

    //==========================================================================
    
    private void createDecoder()
    {
        decoder = new DOMAnalyzer(doc, base);
        if (encoding == null)
            encoding = decoder.getCharacterEncoding();
        decoder.setDefaultEncoding(encoding);
        decoder.attributesToStyles();
        decoder.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
        decoder.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
        decoder.getStyleSheets();
    }
    
    /**
     * Obtains the dimension that should be used for the layout.
     * @return the dimension
     */
    private Dimension getLayoutDimension()
    {
        Dimension dim;
        if (layoutDimension != null)
        {
            dim = new Dimension(layoutDimension);
            if (dim.width <= 0) dim.width = 10; //use some minimum size when the size is not known
            if (dim.height <= 0) dim.height = 10;
        }
        else
            dim = new Dimension(10, 10);
        return dim;
    }
    
    /**
     * Checks whether the layout is computed and recomputes it when necessary.
     */
    private void checkLayout()
    {
        Dimension dim = getLayoutDimension();
        if (currentDimension == null || !currentDimension.equals(dim)) //the dimension has changed
        {
            createLayout(dim);
            //containing box for the new viewport
            viewport.setContainingBlockBox(owner);
            if (owner instanceof BlockBox)
                viewport.clipByBlock((BlockBox) owner);
            
            owner.removeAllSubBoxes();
            owner.addSubBox(viewport);
            currentDimension = new Dimension(dim);
        }
    }
    
    private void createLayout(Dimension dim)
    {
        VisualContext ctx = new VisualContext(null, getOwner().getViewport().getFactory());
        
        log.trace("Creating boxes");
        BoxFactory factory = new BoxFactory(decoder, base);
        factory.setConfig(owner.getViewport().getConfig());
        factory.reset();
        viewport = factory.createViewportTree(decoder.getRoot(), owner.getGraphics(), ctx, dim.width, dim.height);
        log.trace("We have " + factory.next_order + " boxes");
        viewport.initSubtree();
        
        log.trace("Layout for "+dim.width+"px");
        viewport.doLayout(dim.width, true, true);
        log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");

        log.trace("Positioning for "+viewport.getWidth()+"x"+viewport.getHeight()+"px");
        viewport.absolutePositions();
    }
    
}
