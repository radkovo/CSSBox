/*
 * ContentImage.java
 * Copyright (c) 2005-2020 Radek Burget
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
 * Created on 12.6.2012, 9:30:12 by burgetr
 */
package org.fit.cssbox.layout;

import java.net.URL;

/**
 * Generic image used in the page content (used by ReplacedImage and BackgroundImage).
 * 
 * @author burgetr
 */
public interface ContentImage
{
    
    /**
     * Obtains the source URL of the image.
     * @return the source URL
     */
    public URL getUrl();

    /**
     * Obtains the original width of the image.
     * @return the original width (depending on the image format)
     */
    public abstract float getWidth();
    
    /**
     * Obtains the original height of the image.
     * @return the original height (depending on the image format)
     */
    public abstract float getHeight();
    
}
