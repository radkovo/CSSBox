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

/**
 * A rendering engine configuration.
 *
 * @author burgetr
 */
public class BrowserConfig
{
    /** Should we load the external images? */
    private boolean loadImages;
    
    /** Should we load the CSS background images? */
    private boolean loadBackgroundImages;
    
    /** Should we interpret the HTML tags? */
    private boolean useHTML;
    
    
    public BrowserConfig()
    {
        loadImages = true;
        loadBackgroundImages = true;
        useHTML = true;
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
    
}
