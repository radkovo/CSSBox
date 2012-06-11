/*
 * BackgroundImage.java
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
 * Created on 11.6.2012, 14:47:16 by burgetr
 */
package org.fit.cssbox.layout;

import java.net.URL;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;

/**
 * An image placed at the element background together with its position and repeating.
 * 
 * @author burgetr
 */
public class BackgroundImage
{
    private URL url;
    
    private CSSProperty.BackgroundPosition position;
    
    private CSSProperty.BackgroundRepeat repeat;
    
    private CSSProperty.BackgroundAttachment attachment;

    
    
    public BackgroundImage(URL url, BackgroundPosition position,
            BackgroundRepeat repeat, BackgroundAttachment attachment)
    {
        this.url = url;
        this.position = position;
        this.repeat = repeat;
        this.attachment = attachment;
    }

    public URL getUrl()
    {
        return url;
    }

    public CSSProperty.BackgroundPosition getPosition()
    {
        return position;
    }

    public CSSProperty.BackgroundRepeat getRepeat()
    {
        return repeat;
    }

    public CSSProperty.BackgroundAttachment getAttachment()
    {
        return attachment;
    }
    
    //===========================================================================
    
    
    
}
