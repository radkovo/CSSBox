/*
 * FontMap.java
 * Copyright (c) 2005-2017 Radek Burget
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
 * Created on 1. 11. 2017, 16:20:14 by burgetr
 */
package org.fit.cssbox.css;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.RuleFontFace;
import cz.vutbr.web.css.RuleFontFace.Source;

/**
 * A table of registered font sources.
 * 
 * @author burgetr
 */
public class FontTable extends LinkedHashMap<FontSpec, List<RuleFontFace.Source>>
{
    private static final long serialVersionUID = 1L;

    
    public List<RuleFontFace.Source> findBestMatch(FontSpec font)
    {
        List<RuleFontFace.Source> ret = null;
        int max = 0;
        for (Map.Entry<FontSpec, List<Source>> entry : entrySet())
        {
            if (entry.getKey().match(font) > max)
            {
                ret = entry.getValue();
                max = entry.getKey().match(font);
            }
        }
        return ret;
    }
    
}
