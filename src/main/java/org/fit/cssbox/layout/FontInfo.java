/*
 * FontInfo.java
 * Copyright (c) 2005-2019 Radek Burget
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
 * Created on 10. 11. 2019, 19:50:14 by burgetr
 */
package org.fit.cssbox.layout;

/**
 * General font information used mainly for describing the current font. 
 * 
 * @author burgetr
 */
public class FontInfo
{
    private String family;
    private float size;
    private boolean bold;
    private boolean italic;
    
    public FontInfo(String family, float size, boolean bold, boolean italic)
    {
        this.family = family;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
    }

    public String getFamily()
    {
        return family;
    }

    public void setFamily(String family)
    {
        this.family = family;
    }

    public float getSize()
    {
        return size;
    }

    public void setSize(float size)
    {
        this.size = size;
    }

    public boolean isBold()
    {
        return bold;
    }

    public void setBold(boolean bold)
    {
        this.bold = bold;
    }

    public boolean isItalic()
    {
        return italic;
    }

    public void setItalic(boolean italic)
    {
        this.italic = italic;
    }
}
