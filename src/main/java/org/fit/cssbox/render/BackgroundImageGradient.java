/*
 * BackgroundImageGradient.java
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
 * Created on 10. 4. 2020, 18:02:40 by burgetr
 */
package org.fit.cssbox.render;

import org.fit.cssbox.layout.BackgroundImage;
import org.fit.cssbox.layout.ElementBox;

import cz.vutbr.web.css.CSSProperty.BackgroundAttachment;
import cz.vutbr.web.css.CSSProperty.BackgroundOrigin;
import cz.vutbr.web.css.CSSProperty.BackgroundPosition;
import cz.vutbr.web.css.CSSProperty.BackgroundRepeat;
import cz.vutbr.web.css.CSSProperty.BackgroundSize;
import cz.vutbr.web.css.TermList;

/**
 * A background image created by a gradient.
 * 
 * @author burgetr
 */
public class BackgroundImageGradient extends BackgroundImage
{
    private Gradient gradient;
    

    public BackgroundImageGradient(ElementBox owner, BackgroundPosition position, TermList positionValues,
            BackgroundRepeat repeat, BackgroundAttachment attachment, BackgroundOrigin origin, BackgroundSize size,
            TermList sizeValues)
    {
        super(owner, null, position, positionValues, repeat, attachment, origin, size, sizeValues);
    }
    
    public Gradient getGradient()
    {
        return gradient;
    }

    public void setGradient(Gradient gradient)
    {
        this.gradient = gradient;
    }

    @Override
    public float getIntrinsicWidth()
    {
        return 0;
    }

    @Override
    public float getIntrinsicHeight()
    {
        return 0;
    }

    @Override
    public float getIntrinsicRatio()
    {
        return 1;
    }

    @Override
    public boolean hasIntrinsicWidth()
    {
        return false;
    }

    @Override
    public boolean hasIntrinsicHeight()
    {
        return false;
    }

    @Override
    public boolean hasIntrinsicRatio()
    {
        return false;
    }

}
