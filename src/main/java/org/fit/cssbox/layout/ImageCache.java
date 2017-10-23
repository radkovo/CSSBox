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

/**
 * Image cache interface. Simple singleton implementation is not enough.
 * For example we can try to load failed image later, set some limits to memory storage or store them on local FS.
 * 
 * @author dedrakot
 */
public interface ImageCache
{
    void put(URL uri, Image image);
    Image get(URL uri);
    void putFailed(URL uri);
    boolean hasFailed(URL uri);
}
