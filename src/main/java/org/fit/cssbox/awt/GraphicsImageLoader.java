/*
 * GraphicsImageLoader.java
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
 * Created on 2. 4. 2020, 19:08:21 by burgetr
 */
package org.fit.cssbox.awt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.fit.cssbox.io.ContentObserver;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.ContentImage;
import org.fit.cssbox.layout.ImageCache;
import org.fit.cssbox.layout.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An image loader that creates the ContentImage instances that are usable
 * in GraphicsRenderer. It is based on ImageIO toolkit and produces
 * the BitmapImage instances for bitmap images.
 * 
 * @author burgetr
 */
public class GraphicsImageLoader implements ImageLoader
{
    protected static final Logger log = LoggerFactory.getLogger(GraphicsImageLoader.class);
    
    private GraphicsVisualContext ctx;
    
    
    protected GraphicsImageLoader(GraphicsVisualContext ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public ContentImage loadImage(URL url)
    {
        if (url != null)
        {
            ContentImage img;
            ImageCache imageCache = ctx.getConfig().getImageCache();
            if (imageCache != null)
            {
                // get image and cache
                img = imageCache.get(url);
                if (img == null && !imageCache.hasFailed(url))
                {
                    img = loadImageFromSource(url);
                    if (img != null)
                        imageCache.put(url, img);
                    else
                        imageCache.putFailed(url);
                }
            }
            else
            {
                // do not cache, just get image
                img = loadImageFromSource(url);
            }
            // observer need to know that resource with this url will be absent.
            // Even if we only check that url has failed earlier.
            if (img == null)
                observeLoadFailed(url);
            return img;
        }
        return null;
    }
    
    /**
     * Loads the image from the given source URL.
     * 
     * @param url the source URL
     * @return the content image or {@code null} when the image could not be loaded or decoded
     */
    public ContentImage loadImageFromSource(URL url)
    {
        ContentImage ret = null;
        try (DocumentSource imgsrc = ctx.getConfig().createDocumentSource(url))
        {
            InputStream urlStream = imgsrc.getInputStream();
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(urlStream);
            try
            {
                Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
                if (!imageReaders.hasNext())
                {
                    log.warn("No image readers for URL: " + url);
                }
                else
                {
                    BufferedImage image = null;
                    do
                    {
                        ImageReader currentImageReader = imageReaders.next();
                        currentImageReader.setInput(imageInputStream);

                        try
                        {
                            image = currentImageReader.read(0);
                        } catch (Exception e) {
                            log.error("Image decoding error: " + e.getMessage() + " with reader " + currentImageReader);
                        } finally {
                            currentImageReader.dispose();
                        }
                    }
                    while (image == null && imageReaders.hasNext());
                    if (image != null)
                        ret = new BitmapImage(url, image);
                }
            } catch (Exception e) {
                log.error("Image decoding error: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Unable to get image from: " + url);
            log.error(e.getMessage());
        }
        return ret;
    }
    
    private void observeLoadFailed(URL url)
    {
        final ContentObserver observer = ctx.getConfig().getContentObserver();
        if (observer != null)
            observer.contentLoadFailed(url);
    }

}
