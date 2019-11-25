/*
 * CSSNorm.java
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
 * Created on 21. leden 2005, 22:28
 */

package org.fit.cssbox.css;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * This class provides standard style sheets for the browser.
 *
 * @author  radek
 */
public class CSSNorm 
{

    /**
     * Defines a standard HTML style sheet defining the basic style of the individual elements.
     * It corresponds to the CSS2.1 recommendation (Appendix D) with some additions for
     * new HTML5 elements.
     * @return the style string
     */
    public static String stdStyleSheet()
    {
        try {
            return getResource("css/ua-std.css") + getResource("css/ua-html5.css");
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * A style sheet defining the additional basic style not covered by the CSS recommendation.
     * @return The style string
     */
    public static String userStyleSheet()
    {
        try {
            return getResource("css/ua-ext.css");
        } catch (IOException e) {
            return "";
        }
    }
    
    /**
     * A style sheet defining a basic style of form fields. This style sheet may be used
     * for a simple rendering of form fields when their functionality is not implemented
     * in another way.
     * @return The style string
     */
    public static String formsStyleSheet()
    {
        try {
            return getResource("css/ua-forms.css");
        } catch (IOException e) {
            return "";
        }
    }
    
    /**
     * Loads an internal style sheet from resource. 
     * @param name
     * @return
     * @throws IOException
     */
    private static String getResource(String name) throws IOException
    {
        try (InputStream is = CSSNorm.class.getResourceAsStream("/" + name))
        {
            if (is != null) 
            {
                final Scanner scan = new Scanner(is, "UTF-8");
                scan.useDelimiter("\\A");
                final String ret = scan.next();
                scan.close();
                return ret;
            }
            else
                return "";
        }
    }
    
}
