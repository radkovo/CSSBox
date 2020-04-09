/*
 * ImageCache.java
 * Copyright (c) 2005-2015 Radek Burget
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
 * Created on 27. 7. 2015, 15:37:26 by burgetr
 */
package org.fit.cssbox.layout;

import java.net.URL;

/**
 * A generic cache for storing and re-using downloaded images. It uses the image URI as a unique
 * identifier of the image.
 * 
 * @author dedrakot
 * @author burgetr
 */
public interface ImageCache
{
    /**
     * Adds a new image to the cache.
     * @param uri source URI
     * @param image the image
     */
    void put(URL uri, ContentImage image);
    
    /**
     * Retrieves an image from the cache based on its URI.
     * @param uri the image URI
     * @return the retrieved image or {@code null} when there is no such image in the cache.
     */
    ContentImage get(URL uri);
    
    /**
     * Stores an information about that the image downloading has failed before in order to avoid
     * further attempts to download the image again.
     * @param uri the image URL
     */
    void putFailed(URL uri);
    
    /**
     * Checks whether the image downloading or decoding has failed before in order to avoid
     * further attempts to download the image again.
     * @param uri the image URI
     * @return {@code true} when the image could not be downloaded or decoded in the past
     */
    boolean hasFailed(URL uri);
}
