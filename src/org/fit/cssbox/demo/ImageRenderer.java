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
package org.fit.cssbox.demo;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.*;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import sun.misc.BASE64Encoder;

/**
 * This class provides a rendering interface for obtaining the document image
 * form an URL.
 * 
 * @author burgetr
 */
public class ImageRenderer
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
                PrintWriter w = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
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
    
    private void writeSVG(Viewport vp, PrintWriter out) throws IOException
    {
        int w = vp.getContentWidth();
        int h = vp.getContentHeight();
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
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
    
    private void writeBoxSVG(Box box, PrintWriter out) throws IOException
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
                           
            out.println("<text x=\"" + b.x + "\" y=\"" + (b.y + b.height) + "\" width=\"" + b.width + "\" height=\"" + b.height + "\" style=\"" + style + "\">" + htmlEntities(t.getText()) + "</text>"); 
        }
        else if (box.isReplaced())
        {
            ReplacedContent cont = ((ReplacedBox) box).getContentObj();
            if (cont != null && cont instanceof ReplacedImage)
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(((ReplacedImage) cont).getImage(), "png", os);
                BASE64Encoder enc = new BASE64Encoder();
                String imgdata = "data:image/png;base64," + enc.encode(os.toByteArray());
                
                ElementBox eb = (ElementBox) box;
                Rectangle cb = eb.getAbsoluteContentBounds();
                out.println("<image x=\"" + cb.x + "\" y=\"" + cb.y + "\" width=\"" + cb.width + "\" height=\"" + cb.height + "\" xlink:href=\"" + imgdata + "\" />");
            }
        }
        else
        {
            ElementBox eb = (ElementBox) box;
            
            Color bg = eb.getBgcolor();
            Rectangle cb = eb.getAbsoluteContentBounds();
            if (bg != null)
            {
                String style = "stroke:none;fill-opacity:1;fill:" + colorString(bg);
                out.println("<rect x=\"" + cb.x + "\" y=\"" + cb.y + "\" width=\"" + cb.width + "\" height=\"" + cb.height + "\" style=\"" + style + "\" />");
            }
            
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
    
    //=================================================================================
    
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.err.println("Usage: ImageRenderer <url> <output_file> <format>");
            System.err.println();
            System.err.println("Renders a document at the specified URL and stores the document image");
            System.err.println("to the specified file.");
            System.err.println("Supported formats:");
            System.err.println("png: a Portable Network Graphics file (bitmap image)");
            System.err.println("svg: a SVG file (vector image)");
            System.exit(0);
        }
        
        try {
            short type = -1;
            if (args[2].equalsIgnoreCase("png"))
                type = TYPE_PNG;
            else if (args[2].equalsIgnoreCase("svg"))
                type = TYPE_SVG;
            else
            {
                System.err.println("Error: unknown format");
                System.exit(0);
            }
            
            FileOutputStream os = new FileOutputStream(args[1]);
            
            ImageRenderer r = new ImageRenderer();
            r.renderURL(args[0], os, type);
            
            os.close();
            System.err.println("Done.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
}
