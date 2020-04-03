/*
 * BitmapImage.java
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
 * Created on 1. 4. 2020, 20:17:19 by burgetr
 */
package org.fit.cssbox.awt;

import java.awt.image.BufferedImage;
import java.net.URL;

import org.fit.cssbox.layout.ContentImage;

/**
 * A ContentImage implementation that represents a bitmap image. It uses BufferedImage
 * for internal image representation.
 * 
 * @author burgetr
 */
public class BitmapImage implements ContentImage
{
    private URL url; //image url
    private BufferedImage bufferedImage;
    
    
    protected BitmapImage(URL url, BufferedImage bufferedImage)
    {
        this.url = url;
        this.bufferedImage = bufferedImage;
    }
    
    @Override
    public URL getUrl()
    {
        return url;
    }

    public BufferedImage getBufferedImage()
    {
        return bufferedImage;
    }

    @Override
    public float getWidth()
    {
        return getBufferedImage().getWidth();
    }

    @Override
    public float getHeight()
    {
        return getBufferedImage().getHeight();
    }

    
}
