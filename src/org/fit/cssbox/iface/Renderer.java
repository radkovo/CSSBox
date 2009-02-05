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

import java.awt.Color;
import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.*;
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
     * Renders the URL and prints the result to the specified output stream in the specified
     * format.
     * @param urlstring the source URL
     * @param out output stream
     * @param type output type, one of the TYPE_XXX constants
     * @return true in case of success, false otherwise
     */
    public boolean renderURL(String urlstring, OutputStream out, short type)
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
            else if (type == TYPE_SVG)
            {
                ReplacedImage.setLoadImages(true);
                BrowserCanvas contentCanvas = new BrowserCanvas(da.getBody(), da, new java.awt.Dimension(1000, 600), url);
                PrintWriter w = new PrintWriter(out);
                writeSVG(contentCanvas.getViewport(), w);
                w.close();
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void writeSVG(Viewport vp, PrintWriter out)
    {
        int w = vp.getContentWidth();
        int h = vp.getContentHeight();
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
        out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">");
        out.println("<svg xmlns=\"http://www.w3.org/2000/svg\"");
        out.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"");
        out.println("         width=\"" + w + "\" height=\"" + h + "px\"");
        out.println("         viewBox=\"0 0 " + w + " " + h + "\"");
        out.println("         zoomAndPan=\"disable\" >");
        
        for (int i = 0; i < vp.getSubBoxNumber(); i++)
            writeBoxSVG(vp.getSubBox(i), out);
        
        out.println("</svg>");    
    }
    
    private void writeBoxSVG(Box box, PrintWriter out)
    {
        Rectangle b = box.getAbsoluteBounds();
        if (box instanceof TextBox)
        {
            TextBox t = (TextBox) box;
            VisualContext ctx = t.getVisualContext();
            String style = "font-size:" + ctx.getFont().getSize() + "px;" + 
                           "font-weight:" + (ctx.getFont().isBold()?"bold":"normal") + ";" + 
                           "font-variant:" + (ctx.getFont().isItalic()?"italic":"normal") + ";" +
                           "font-family:" + ctx.getFont().getFamily() + ";" +
                           "fill:" + colorString(ctx.getColor()) + ";" +
                           "stroke:none";
                           
            out.println("<text x=\"" + b.x + "\" y=\"" + b.y + "\" width=\"" + b.width + "\" height=\"" + b.height + "\" style=\"" + style + "\">" + htmlEntities(t.getText()) + "</text>"); 
        }
        else
        {
            ElementBox eb = (ElementBox) box;
            for (int i = 0; i < eb.getSubBoxNumber(); i++)
                writeBoxSVG(eb.getSubBox(i), out);
        }
    }

    
    private String colorString(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    private String htmlEntities(String s)
    {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("&", "&amp;");
    }
}
