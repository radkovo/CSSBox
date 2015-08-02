/*
 * DefaultDOMSource.java
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
 * Created on 22.11.2012, 19:44:34 by burgetr
 */
package org.fit.cssbox.io;

import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLElements;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A default CSSBox DOM parser implementation using the NekoHTML parser.
 *
 * @author burgetr
 */
public class DefaultDOMSource extends DOMSource
{
    static boolean neko_fixed = false;

    public DefaultDOMSource(DocumentSource src)
    {
        super(src);
    }

    @Override
    public Document parse() throws SAXException, IOException
    {
        //temporay NekoHTML fix until nekohtml gets fixed
        if (!neko_fixed)
        {
            HTMLElements.Element li = HTMLElements.getElement(HTMLElements.LI);
            HTMLElements.Element[] oldparents = li.parent;
            li.parent = new HTMLElements.Element[oldparents.length + 1];
            for (int i = 0; i < oldparents.length; i++)
                li.parent[i] = oldparents[i];
            li.parent[oldparents.length] = HTMLElements.getElement(HTMLElements.MENU);
            neko_fixed = true;
        }
        
        DOMParser parser = new DOMParser(new HTMLConfiguration());
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        if (charset != null)
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
        parser.parse(new org.xml.sax.InputSource(getDocumentSource().getInputStream()));
        return parser.getDocument();
    }

}
