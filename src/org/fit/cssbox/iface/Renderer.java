/**
 * Renderer.java
 * Copyright (c) 2005-2009 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Created on 5.2.2009, 12:00:02 by burgetr
 */
package org.fit.cssbox.iface;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.ReplacedImage;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * This class provides a rendering interface for obtaining the document image
 * form an URL.
 * 
 * @author burgetr
 */
public class Renderer
{
    public static final short TYPE_PNG = 0;
    public static final short TYPE_SVG = 1;
    
    /**
     * Renders the URL and prints the result to the specified writer in the specified
     * format.
     * @param urlstring the source URL
     * @param out output writer
     * @param type output type, one of the TYPE_XXX constants
     * @return true in case of success, false otherwise
     */
    public boolean renderURL(String urlstring, Writer out, short type)
    {
        try {
            if (!urlstring.startsWith("http:") &&
                !urlstring.startsWith("ftp:") &&
                !urlstring.startsWith("file:"))
                    urlstring = "http://" + urlstring;
            
            URL url = new URL(urlstring);
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Transformer/2.x; Linux) CSSBox/2.x (like Gecko)");
            InputStream is = con.getInputStream();
            
            Tidy tidy = new Tidy();
            tidy.setTrimEmptyElements(false);
            tidy.setAsciiChars(false);
            tidy.setInputEncoding("iso-8859-2");
            //tidy.setInputEncoding("utf-8");
            tidy.setXHTML(true);
            Document doc = tidy.parseDOM(is, null);
            
            DOMAnalyzer da = new DOMAnalyzer(doc, url);
            da.attributesToStyles();
            da.addStyleSheet(null, CSSNorm.stdStyleSheet());
            da.addStyleSheet(null, CSSNorm.userStyleSheet());
            da.getStyleSheets();
            
            is.close();
    
            if (type == TYPE_PNG)
            {
                ReplacedImage.setLoadImages(true);
                BrowserCanvas contentCanvas = new BrowserCanvas(da.getBody(), da, new java.awt.Dimension(1000, 600), url);
                ImageIO.write(contentCanvas.getImage(), "png", out);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
