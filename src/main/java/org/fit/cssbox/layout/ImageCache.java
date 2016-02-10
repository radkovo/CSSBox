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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.awt.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A simple cache for storing already loaded images.
 *
 * Changes Done by Leon De Silva.
 * ==============================
 *      * Removed cache which stores failed URLs.
 *      * Changed Image caching technology to Google Guava Cache.
 * 
 * @author Alessandro Tucci
 * @author Leon De Silva.
 */
public class ImageCache
{
    // Cache expire time is set to 1 hour.
    private static final int CACHE_EXPIRY_TIME_IN_HOURS = 1;
    private static Cache<URL, Image> cache = CacheBuilder.newBuilder().expireAfterWrite(CACHE_EXPIRY_TIME_IN_HOURS, TimeUnit.HOURS).build();

    /**
     * Method to store Urls and Images.
     *
     * @param url Url to store.
     * @param image Image to store.
     */
    public static void put(URL url, Image image) {
        cache.put(url, image);
    }

    /**
     * Method to get the Image for a given URL.
     *
     * @param url URL to retrieve Image.
     * @return Image for the given URL.
     */
    public static Image get(URL url) {
        Image image = null;
        if (cache != null) {
            image = cache.asMap().get(url);
        }

        return image;
    }
}
