/**
 * StreamDocumentSource.java
 *
 * Created on 19.12.2012, 15:07:56 by burgetr
 */
package org.fit.cssbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.fit.cssbox.io.DocumentSource;

/**
 * A dummy implementation of the document source for encapsulating an already created input stream
 * and the additional parametres.
 * 
 * @author burgetr
 */
public class StreamDocumentSource extends DocumentSource
{
    private URL url;
    private String ctype;
    private InputStream is;
    
    /**
     * Creates the document source from the input stream.
     * @param is the input stream
     * @param url the base URL to be used with the data source
     * @param contentType stream content type
     * @throws IOException
     */
    public StreamDocumentSource(InputStream is, URL url, String contentType) throws IOException
    {
        super(url);
        this.url = url;
        this.ctype = contentType;
        this.is = is;
    }

    @Override
    public URL getURL()
    {
        return url;
    }

    @Override
    public String getContentType()
    {
        return ctype;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return is;
    }

    @Override
    public void close() throws IOException
    {
        is.close();
    }

}
