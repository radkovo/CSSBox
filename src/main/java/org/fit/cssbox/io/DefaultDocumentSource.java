/*
 * DefaultDocumentSource.java
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
 * Created on 23.11.2012, 9:28:33 by burgetr
 */
package org.fit.cssbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.fit.net.DataURLHandler;

/**
 * This class implements the document source with the standard {@link java.net.URLConnection}
 * with an additional support for the data: URLs.
 *  
 * @author burgetr
 */
public class DefaultDocumentSource extends DocumentSource
{
    /** The user-agent string used for HTTP connection */
    private static String USER_AGENT = "Mozilla/5.0 (compatible; BoxBrowserTest/4.x; Linux) CSSBox/4.x (like Gecko)";

    private URLConnection con;
    private InputStream is;
    
    /**
     * Creates a network data source based on the target document URL.
     * @param url the document URL
     * @throws IOException
     */
    public DefaultDocumentSource(URL url) throws IOException
    {
        super(url);
        con = createConnection(url);
        is = null;
    }
    
    /**
     * Creates a data source based on the URL string. The data: urls are automatically
     * recognized and  processed.
     * @param urlstring The URL string
     * @throws IOException
     */
    public DefaultDocumentSource(String urlstring) throws IOException
    {
        super(null, urlstring);
        URL url = DataURLHandler.createURL(null, urlstring);
        con = createConnection(url);
        is = null;
    }
    
    /**
     * Creates a data source based on the URL string. The data: urls are automatically
     * recognized and  processed.
     * @param base The base URL to be used for the relative URLs in the urlstring
     * @param urlstring The URL string
     * @throws IOException
     */
    public DefaultDocumentSource(URL base, String urlstring) throws IOException
    {
        super(base, urlstring);
        URL url = DataURLHandler.createURL(base, urlstring);
        con = createConnection(url);
        is = null;
    }
    
    /**
     * Creates and configures the URL connection.
     * @param url the target URL
     * @return the created connection instance
     * @throws IOException
     */
    protected URLConnection createConnection(URL url) throws IOException
    {
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);
        return con;
    }
    
    @Override
    public URL getURL()
    {
        return con.getURL();
    }
    
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (is == null)
            is = con.getInputStream();
        return is;
    }
    
    @Override
    public String getContentType()
    {
        return con.getHeaderField("Content-Type");
    }

    /**
     * Obtains the current User-agent string used for new connections.
     * @return the user-agent string.
     */
    public static String getUserAgent()
    {
        return USER_AGENT;
    }

    /**
     * Sets the user agent string that will be used for new connections.
     * @param userAgent the user-agent string
     */
    public static void setUserAgent(String userAgent)
    {
        USER_AGENT = userAgent;
    }

    @Override
    public void close() throws IOException
    {
        if (is != null)
            is.close();
    }
    

}
