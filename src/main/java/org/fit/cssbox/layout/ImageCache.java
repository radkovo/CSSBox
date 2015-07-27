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

import java.awt.Image;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple cache for storing already loaded images.
 * 
 * @author Alessandro Tucci
 */
public class ImageCache
{
    private static ConcurrentHashMap<URL, Image> cache;
    static {
        cache = new ConcurrentHashMap<URL, Image>();
    }

    private static ConcurrentHashMap<URL, Boolean> failed;
    static {
        failed = new ConcurrentHashMap<URL, Boolean>();
    }
    
    public static Image put(URL uri, Image image)
    {
        return cache.put(uri, image);
    }

    public static Image get(URL uri)
    {
        return cache.get(uri);
    }

    public static Image remove(URL uri)
    {
        return cache.remove(uri);
    }
    
    public static void putFailed(URL uri)
    {
        failed.put(uri, true);
    }
    
    public static boolean hasFailed(URL uri)
    {
        Boolean b = failed.get(uri);
        return (b != null && b == true);
    }
    
}
