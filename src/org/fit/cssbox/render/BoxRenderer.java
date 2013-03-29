/*
 * BoxRenderer.java
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
 * Created on 8.3.2013, 11:23:34 by burgetr
 */
package org.fit.cssbox.render;

import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.TextBox;

/**
 * A generic renderer that is able to render box contents and background based on
 * the box type.
 * 
 * @author burgetr
 */
public interface BoxRenderer
{
    /**
     * Initializes the rendering of an element. This is called before any contents of the element
     * or child boxes are rendered. Note that the {@link BoxRenderer#renderElementBackground} method 
     * may be called before this method.
     * @param elem the element
     */
    public void startElementContents(ElementBox elem);
    
    /**
     * Finishes the rendering of an element. This is called after all contents of the element
     * and the child boxes are rendered.
     * @param elem the element
     */
    public void finishElementContents(ElementBox elem);
    
    /**
     * Renders the background and borders of the given element box according to its style.
     * @param elem
     */
    public void renderElementBackground(ElementBox elem);
    
    /**
     * Renders the text represented by a text box according to its computed style.
     * @param text
     */
    public void renderTextContent(TextBox text);
    
    /**
     * Renders the contents of a replaced box.
     * @param box
     */
    public void renderReplacedContent(ReplacedBox box);
    
    /**
     * Finishes and the output.
     */
    public void close();
    
}
