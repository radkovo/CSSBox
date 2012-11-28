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

import java.awt.Graphics2D;
import java.net.URL;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.w3c.dom.Document;

/**
 * The replaced content represented by a text (HTML or XML) document.
 * 
 * @author burgetr
 */
public class ReplacedText extends ReplacedContent
{
    private Document doc;
    private URL base;
    private String encoding;
    private BrowserCanvas canvas;

    public ReplacedText(ElementBox owner, Document doc, URL base, String encoding)
    {
        super(owner);
        this.doc = doc;
        this.base = base;
        this.encoding = encoding;
        createBrowser();
    }

    @Override
    public void draw(Graphics2D g, int width, int height)
    {
        
    }

    @Override
    public int getIntrinsicWidth()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIntrinsicHeight()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getIntrinsicRatio()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void doLayout()
    {
        canvas.createLayout(owner.getContent());
    }

    //==========================================================================
    
    private void createBrowser()
    {
        DOMAnalyzer da = new DOMAnalyzer(doc, base);
        if (encoding == null)
            encoding = da.getCharacterEncoding();
        da.setDefaultEncoding(encoding);
        da.attributesToStyles();
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
        da.getStyleSheets();
        
        canvas = new BrowserCanvas(da.getRoot(), da, base);
        canvas.setConfig(owner.getViewport().getConfig());
        
    }
    
}
