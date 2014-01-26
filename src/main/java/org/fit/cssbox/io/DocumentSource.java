/*
 * DocumentSource.java
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
 * Created on 23.11.2012, 9:18:54 by burgetr
 */
package org.fit.cssbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This class creates an abstraction of a document source that is able to
 * obtain a document based on its URL.
 * 
 * @author burgetr
 */
public abstract class DocumentSource implements java.io.Closeable
{
    
    /**
     * Creates a new document source from an URL.
     * @param url the document URL
     * @throws IOException
     */
    public DocumentSource(URL url) throws IOException
    {
    }
    
    /**
     * Creates a new document source based on a string representation of the URL.
     * @param urlstring the URL string
     * @throws IOException
     */
    public DocumentSource(URL base, String urlstring) throws IOException
    {
    }
    
    /**
     * Obtains the final URL of the obtained document. This URL may be different
     * from the URL used for DocumentSource construction, e.g. when some HTTP redirect
     * occurs.
     * @return the document URL.
     */
    abstract public URL getURL();
    
    /**
     * Obtains the MIME content type of the target document.
     * @return a MIME string.
     */
    abstract public String getContentType();
    
    /**
     * Obtains the input stream for reading the referenced document.
     * @return The input stream.
     * @throws IOException
     */
    abstract public InputStream getInputStream() throws IOException;

    /**
     * Closes the document source.
     */
    abstract public void close() throws IOException;
    
}
