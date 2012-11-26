/*
 * HTMLBoxFactory.java
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
 * Created on 23.11.2012, 15:52:00 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Graphics2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import cz.vutbr.web.css.NodeData;

import org.fit.cssbox.io.DocumentSource;
import org.w3c.dom.Element;

/**
 *
 * @author burgetr
 */
public class HTMLBoxFactory
{
    private BoxFactory factory;
    private Set<String> supported;
    
    public HTMLBoxFactory(BoxFactory parent)
    {
        this.factory = parent;
        supported = new HashSet<String>(2);
        supported.add("object");
        supported.add("img");
    }
    
    public boolean isTagSupported(String name)
    {
        return supported.contains(name.toLowerCase());
    }
    
    /**
     * Creates the box according to the HTML element.
     * @param parent
     * @param e
     * @param viewport
     * @param style
     * @return The newly created box or <code>null</code> when the element is not supported
     * or cannot be created. 
     */
    public ElementBox createBox(ElementBox parent, Element e, Viewport viewport, NodeData style)
    {
        String name = e.getNodeName().toLowerCase();
        if (name.equals("object"))
            return createSubtreeObject(parent, e, viewport, style);
        else if (name.equals("img"))
            return createSubtreeImg(parent, e, viewport, style);
        else
            return null;
    }
    
    protected ElementBox createSubtreeImg(ElementBox parent, Element e, Viewport viewport, NodeData style)
    {
        InlineReplacedBox rbox = new InlineReplacedBox(e, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
        rbox.setViewport(viewport);
        rbox.setStyle(style);

        String src = e.getAttribute("src");
        rbox.setContentObj(new ReplacedImage(rbox, rbox.getVisualContext(), factory.getBaseURL(), src));
        
        if (rbox.isBlock())
            return new BlockReplacedBox(rbox);
        else
            return rbox;
    }
    
    protected ElementBox createSubtreeObject(ElementBox parent, Element e, Viewport viewport, NodeData style)
    {
        //create the replaced box
        InlineReplacedBox rbox = new InlineReplacedBox(e, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
        rbox.setViewport(viewport);
        rbox.setStyle(style);
        
        //try to create the content object based on the mime type
        try
        {
            String mime = e.getAttribute("type").toLowerCase();
            String cb = e.getAttribute("codebase");
            String dataurl = e.getAttribute("data");
            URL base = new URL(factory.getBaseURL(), cb);
            
            DocumentSource src = factory.createDocumentSource(base, dataurl);
            if (mime.isEmpty())
            {
                mime = src.getContentType();
                if (mime == null || mime.isEmpty())
                    mime = "text/html";
            }
            System.out.println("ctype=" + mime);
            
            ReplacedContent content = null;
            if (mime.startsWith("image/"))
            {
                content = new ReplacedImage(rbox, rbox.getVisualContext(), base, dataurl);
            }
            rbox.setContentObj(content);
        } catch (MalformedURLException e1)
        {
            //something failed, no content object is created => the we should use the object element contents
        }

        if (rbox.getContentObj() != null) //the content object has been sucessfuly created
        {
            //convert the type if necessary
            if (rbox.getDisplay() == ElementBox.DISPLAY_INLINE_BLOCK)
                return new InlineBlockReplacedBox(rbox);
            else if (rbox.getDisplay() == ElementBox.DISPLAY_BLOCK)
                return new BlockReplacedBox(rbox);
            else
                return rbox;
        }
        else //no content object - fallback to a normal box (the default object behavior)
        {
            return null;
        }
    }
    
}
