/*
 * BrowserConfig.java
 * Copyright (c) 2005-2012 Radek Burget
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
 * Created on 18.6.2012, 9:40:57 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fit.cssbox.io.ContentObserver;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rendering engine configuration.
 *
 * @author burgetr
 */
public class BrowserConfig
{
    private static Logger log = LoggerFactory.getLogger(BrowserConfig.class);

    /** The viewport background color */
    private Color viewportBackgroundColor;
    
    /** Should we load the external images? */
    private boolean loadImages;
    
    /** Should we load the CSS background images? */
    private boolean loadBackgroundImages;
    
    /** Image loading timeout [ms] */
    private int imageLoadTimeout;
    
    /** Should we interpret the HTML tags? */
    private boolean useHTML;
    
    /** Should we replace the images with their ALT attribute? */
    private boolean replaceImagesWithAlt;
    
    /** Should the viewport clip its contents? */
    private boolean clipViewport;
    
    /** Registered DocumentSource implementation */
    private Class<? extends DocumentSource> documentSourceClass;
    
    /** Registered DOMSource implementation */
    private Class<? extends DOMSource> domSourceClass;
    
    /** Registered content observer that tracks the image loading */
    private ContentObserver contentObserver;

    private ImageCache imageCache;
    
    /** Default font families */
    private Map<String, String> defaultFonts;
    
    /**
     * Creates a new config with default values of the options.
     */
    public BrowserConfig()
    {
        viewportBackgroundColor = Color.WHITE;
        loadImages = true;
        loadBackgroundImages = true;
        imageLoadTimeout = 500;
        useHTML = true;
        replaceImagesWithAlt = false;
        clipViewport = false;
        documentSourceClass = DefaultDocumentSource.class;
        domSourceClass = DefaultDOMSource.class;
        contentObserver = null;
        initDefaultFonts();
    }

    public Color getViewportBackgroundColor()
    {
        return viewportBackgroundColor;
    }

    /**
     * Sets the background color of the viewport. This color is used when no background color
     * is defined for the root element (and neither the body element for HTML mode). Default
     * walue is white.
     * @param viewportBackgroundColor
     */
    public void setViewportBackgroundColor(Color viewportBackgroundColor)
    {
        this.viewportBackgroundColor = viewportBackgroundColor;
    }

    public boolean getLoadImages()
    {
        return loadImages;
    }

    /**
     * Sets whether to load the referenced content images automatically. The default value is <code>true</code>.
     * @param loadImages
     */
    public void setLoadImages(boolean loadImages)
    {
        this.loadImages = loadImages;
    }

    public boolean getLoadBackgroundImages()
    {
        return loadBackgroundImages;
    }

    /**
     * Sets whether to load the CSS background images automatically. The default value is <code>true</code>.
     * @param loadBackgroundImages
     */
    public void setLoadBackgroundImages(boolean loadBackgroundImages)
    {
        this.loadBackgroundImages = loadBackgroundImages;
    }

    public int getImageLoadTimeout()
    {
        return imageLoadTimeout;
    }

    /**
     * Configures the timeout for loading images. The default value is 500ms.
     * @param imageLoadTimeout The timeout for loading images in miliseconds.
     */
    public void setImageLoadTimeout(int imageLoadTimeout)
    {
        this.imageLoadTimeout = imageLoadTimeout;
    }

    /**
     * Registers the content observer that tracks the image loading.
     * @param contentObserver the content observer to be used or {@code null} for none.
     */
    public void setContentObserver(ContentObserver contentObserver)
    {
        this.contentObserver = contentObserver;
    }

    public ContentObserver getContentObserver()
    {
        return contentObserver;
    }

    public ImageCache getImageCache()
    {
        return imageCache;
    }

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    public boolean getUseHTML()
    {
        return useHTML;
    }

    /**
     * Sets whether the engine should use the HTML extensions or not. Currently, the HTML
     * extensions include the following:
     * <ul>
     * <li>Creating replaced boxes for <code>&lt;img&gt;</code> elements
     * <li>Using the <code>&lt;body&gt;</code> element background for the whole canvas according to the HTML specification
     * </ul> 
     * @param useHTML <code>false</code> if the extensions should be switched off (default is on)
     */
    public void setUseHTML(boolean useHTML)
    {
        this.useHTML = useHTML;
    }

    public boolean getReplaceImagesWithAlt()
    {
        return replaceImagesWithAlt;
    }

    /**
     * Sets whether the images should be replaced by their 'alt' text.
     * @param replaceImagesWithAlt when set to {@code true}, the images will be treated as their 'alt' text.
     */
    public void setReplaceImagesWithAlt(boolean replaceImagesWithAlt)
    {
        this.replaceImagesWithAlt = replaceImagesWithAlt;
    }

    public boolean getClipViewport()
    {
        return clipViewport;
    }

    /**
     * Configures whether the rendered page should be clipped by the viewport. When set to {@code true},
     * the content outside the viewport won't be rendered (the generated boxes won't be visible).
     * When set to {@code false}, the whole page will be rendered and the viewport size is only
     * used for layout computation (positioned boxes etc.) The default is {@code false}.
     * @param clipViewport The configuration value.
     */
    public void setClipViewport(boolean clipViewport)
    {
        this.clipViewport = clipViewport;
    }

    /**
     * Sets the class used by CSSBox for obtaining documents based on their URLs.
     * @param documentSourceClass the new document source class
     */
    public void registerDocumentSource(Class<? extends DocumentSource> documentSourceClass)
    {
        this.documentSourceClass = documentSourceClass;
    }
    
    /**
     * Obtains the class used by CSSBox for obtaining documents based on their URLs.
     * @return the used class
     */
    public Class<? extends DocumentSource> getDocumentSourceClass()
    {
        return documentSourceClass;
    }
    
    /**
     * Creates a new instance of the {@link org.fit.cssbox.io.DocumentSource} class registered in the browser configuration.
     * @param url the URL to be given to the document source.
     * @return the document source.
     * @throws IOException 
     */
    public DocumentSource createDocumentSource(URL url) throws IOException
    {
        try
        {
            Constructor<? extends DocumentSource> constr = getDocumentSourceClass().getConstructor(URL.class);
            return constr.newInstance(url);
        } catch (Exception e) {
            Throwable cause = e; //find if there is an IOException cause and throw it
            while (cause != null && !(cause instanceof IOException))
                cause = e.getCause();
            if (cause != null && cause instanceof IOException)
                throw (IOException) cause;
            //no IO exception cause, this should not happen (some internal reflection problem)
            log.error("Could not create the DocumentSource instance: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Creates a new instance of the {@link org.fit.cssbox.io.DocumentSource} class registered in the browser configuration.
     * @param base the base URL
     * @param urlstring the URL suffix
     * @return the document source.
     */
    public DocumentSource createDocumentSource(URL base, String urlstring)
    {
        try
        {
            Constructor<? extends DocumentSource> constr = getDocumentSourceClass().getConstructor(URL.class, String.class);
            return constr.newInstance(base, urlstring);
        } catch (Exception e) {
            log.warn("Could not create the DocumentSource instance: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sets the class used by CSSBox for the DOM tree from documents.
     * @param domSourceClass the new DOM source class
     */
    public void registerDOMSource(Class<? extends DOMSource> domSourceClass)
    {
        this.domSourceClass = domSourceClass;
    }
    
    /**
     * Obtains the class used by CSSBox for the DOM tree from documents.
     * @return the used class
     */
    public Class<? extends DOMSource> getDOMSourceClass()
    {
        return domSourceClass;
    }

    /**
     * Creates a new instance of the {@link org.fit.cssbox.io.DOMSource} class registered in the browser configuration
     * ({@link org.fit.cssbox.layout.BrowserConfig}).
     * @param src the document source to be given to the DOM source.
     * @return the DOM source.
     */
    public DOMSource createDOMSource(DocumentSource src)
    {
        try
        {
            Constructor<? extends DOMSource> constr = getDOMSourceClass().getConstructor(DocumentSource.class);
            return constr.newInstance(src);
        } catch (Exception e) {
            log.warn("BoxFactory: Warning: could not create the DOMSource instance: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sets a default physical font to be used for a logical name.
     * @param logical the logical font name
     * @param physical the physical font to be used
     */
    public void setDefaultFont(String logical, String physical)
    {
        defaultFonts.put(logical, physical);
    }
    
    /**
     * Obtains the physical font name used for a logical name.
     * @param logical the logical name
     * @return the physicel font name or <code>null</code> if no default is defined for the logical name.
     */
    public String getDefaultFont(String logical)
    {
        return defaultFonts.get(logical);
    }
    
    /**
     * Initializes the default fonts. Current implementation just defines the same physical names for basic
     * AWT logical fonts.
     */
    protected void initDefaultFonts()
    {
        defaultFonts = new HashMap<String, String>(3);
        defaultFonts.put(Font.SERIF, Font.SERIF);
        defaultFonts.put(Font.SANS_SERIF, Font.SANS_SERIF);
        defaultFonts.put(Font.MONOSPACED, Font.MONOSPACED);
    }
    
}
