/*
 * GraphicsRenderer.java
 * Copyright (c) 2005-2013 Radek Burget
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
 * Created on 8.3.2013, 11:41:50 by burgetr
 */
package org.fit.cssbox.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.TextBox;

/**
 * This renderers displays the boxes graphically using the given Graphics2D context.
 * 
 * @author burgetr
 */
public class GraphicsRenderer implements BoxRenderer
{
    /** the used graphic context */
    protected Graphics2D g;

    /** applied transformations */
    protected Map<ElementBox, AffineTransform> savedTransforms;
    
    /**
     * Constructs a renderer using the given graphics contexts.
     * @param g The graphics context used for painting the boxes.
     */
    public GraphicsRenderer(Graphics2D g)
    {
        this.g = g;
        savedTransforms = new HashMap<ElementBox, AffineTransform>();
    }
    
    //====================================================================================================
    
    public void startElementContents(ElementBox elem)
    {
        //setup transformations for the contents
        AffineTransform at = Transform.createTransform(elem);
        if (at != null)
        {
            savedTransforms.put(elem, g.getTransform());
            g.transform(at);
        }
    }

    public void finishElementContents(ElementBox elem)
    {
        //restore the stransformations
        AffineTransform origAt = savedTransforms.get(elem);
        if (origAt != null)
            g.setTransform(origAt);
    }
    
    public void renderElementBackground(ElementBox elem)
    {
        AffineTransform origAt = null;
        AffineTransform at = Transform.createTransform(elem);
        if (at != null)
        {
            origAt = g.getTransform();
            g.transform(at);
        }
        
        elem.drawBackground(g);
        
        if (origAt != null)
            g.setTransform(origAt);
    }

    public void renderTextContent(TextBox text)
    {
        text.drawContent(g);
    }

    public void renderReplacedContent(ReplacedBox box)
    {
        AffineTransform origAt = null;
        if (box instanceof ElementBox)
        {
            AffineTransform at = Transform.createTransform((ElementBox) box);
            if (at != null)
            {
                origAt = g.getTransform();
                g.transform(at);
            }
        }
        
        box.drawContent(g);
        
        if (origAt != null)
            g.setTransform(origAt);
    }

    public void close()
    {
    }    
    
}
