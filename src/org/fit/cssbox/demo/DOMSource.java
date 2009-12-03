/**
 * DOMSource.java
 * Copyright (c) 2005-2007 Radek Burget
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
 * Created on 30.9.2009, 12:33:10 by burgetr
 */

package org.fit.cssbox.demo;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * An abstraction of a parser that is able to obtain a DOM. Shared by all the
 * demos.
 * 
 * @author burgetr
 */
public class DOMSource
{
    private InputStream is;
    private Document doc;
    private String charset;

    public DOMSource(InputStream is)
    {
        this.is = is;
    }

    public InputStream getInputStream()
    {
        return is;
    }

    public void setContentType(String type)
    {
        if (type != null)
        {
            String t = type.toLowerCase();

            //extract the charset if specified
            int strt = t.indexOf("charset=");
            if (strt >= 0)
            {
                strt += "charset=".length();
                int stop = t.indexOf(';', strt);
                if (stop == -1)
                    stop = t.length();
                charset = t.substring(strt, stop).trim();
            }
        }
    }

    public Document parse() throws SAXException, IOException
    {
        DOMParser parser = new DOMParser(new HTMLConfiguration());
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        if (charset != null)
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
        parser.parse(new org.xml.sax.InputSource(is));
        doc = parser.getDocument();
        return doc;
    }

}
