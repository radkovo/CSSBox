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
import java.util.HashSet;
import java.util.Set;

import cz.vutbr.web.css.NodeData;

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
        //supported.add("object");
        supported.add("img");
    }
    
    public boolean isTagSupported(String name)
    {
        return supported.contains(name.toLowerCase());
    }
    
    public ElementBox createBox(ElementBox parent, Element n, Viewport viewport, NodeData style)
    {
        String name = n.getNodeName().toLowerCase();
        if (name.equals("object"))
            return createSubtreeObject(parent, n, viewport, style);
        else if (name.equals("img"))
            return createSubtreeImg(parent, n, viewport, style);
        else
            return null;
    }
    
    protected ElementBox createSubtreeImg(ElementBox parent, Element n, Viewport viewport, NodeData style)
    {
        InlineReplacedBox rbox = new InlineReplacedBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
        rbox.setViewport(viewport);
        rbox.setStyle(style);
        rbox.setContentObj(new ReplacedImage(rbox, rbox.getVisualContext(), factory.getBaseURL()));
        if (rbox.isBlock())
            return new BlockReplacedBox(rbox);
        else
            return rbox;
    }
    
    protected ElementBox createSubtreeObject(ElementBox parent, Element n, Viewport viewport, NodeData style)
    {
        return null;
    }
    
}
