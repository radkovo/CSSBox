/*
 * ImageLoader.java
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
 * Created on 2. 4. 2020, 19:03:28 by burgetr
 */
package org.fit.cssbox.layout;

import java.net.URL;

/**
 * A factory for loading ContentImage instances.
 * 
 * @author burgetr
 */
public interface ImageLoader
{

    /**
     * Loads an image from URL and creates the corresponding ContentImage instance
     * that can be later displayed by the renderer.
     * 
     * @param url source URL
     * @return the resulting ContentImage instance or {@code null} when the image cannot be
     * loaded.
     */
    public ContentImage loadImage(URL url);
    
}
