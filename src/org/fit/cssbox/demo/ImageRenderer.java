/**
 * Renderer.java
 * Copyright (c) 2005-2009 Radek Burget
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
 * Created on 5.2.2009, 12:00:02 by burgetr
 */
package org.fit.cssbox.demo;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermColor;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.*;
import org.fit.cssbox.misc.Base64Coder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
    
    protected static final short TURN_ALL = 0; //drawing stages
    protected static final short TURN_NONFLOAT = 1;
    protected static final short TURN_FLOAT = 2;
    protected static final short MODE_BOTH = 0; //drawing modes
    protected static final short MODE_FG = 1;
    protected static final short MODE_BG = 2;
    
    /**
     * Renders the URL and prints the result to the specified output stream in the specified
     * format.
     * @param urlstring the source URL
     * @param out output stream
     * @param type output type, one of the TYPE_XXX constants
     * @return true in case of success, false otherwise
     * @throws SAXException 
     */
    public boolean renderURL(String urlstring, OutputStream out, short type) throws IOException, SAXException
    {
        if (!urlstring.startsWith("http:") &&
            !urlstring.startsWith("ftp:") &&
            !urlstring.startsWith("file:"))
                urlstring = "http://" + urlstring;
        
        URL url = new URL(urlstring);
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Transformer/2.x; Linux) CSSBox/2.x (like Gecko)");
        InputStream is = con.getInputStream();
        
        DOMSource parser = new DOMSource(is);
        parser.setContentType(con.getHeaderField("Content-Type")); //use the default encoding provided via HTTP
        Document doc = parser.parse();
        
        DOMAnalyzer da = new DOMAnalyzer(doc, url);
        da.attributesToStyles();
        da.addStyleSheet(null, CSSNorm.stdStyleSheet());
        da.addStyleSheet(null, CSSNorm.userStyleSheet());
        da.getStyleSheets();
        
        is.close();

        if (type == TYPE_PNG)
        {
            ReplacedImage.setLoadImages(true);
            BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, new java.awt.Dimension(1000, 600), url);
            ImageIO.write(contentCanvas.getImage(), "png", out);
        }
        else if (type == TYPE_SVG)
        {
            ReplacedImage.setLoadImages(true);
            BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, new java.awt.Dimension(1000, 600), url);
            PrintWriter w = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
            writeSVG(contentCanvas.getViewport(), w);
            w.close();
        }
        
        return true;
    }
    
    protected void writeSVG(Viewport vp, PrintWriter out) throws IOException
    {
        int w = vp.getContentWidth();
        int h = vp.getContentHeight();
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">");
        out.println("<!-- Rendered by CSSBox http://cssbox.sourceforge.net -->");
        out.println("<svg xmlns=\"http://www.w3.org/2000/svg\"");
        out.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"");
        out.println("         width=\"" + w + "\" height=\"" + h + "px\"");
        out.println("         viewBox=\"0 0 " + w + " " + h + "\"");
        out.println("         zoomAndPan=\"disable\" >");
        
        writeBoxSVG(vp, out);
        
        out.println("</svg>");    
    }
    
    protected void writeBoxSVG(Box box, PrintWriter out) throws IOException
    {
        if (box.isVisible())
        {
            out.println("<!-- ********* OUTPUT TURN 1 - Background ********* -->");
            writeBoxSVG(box, out, TURN_NONFLOAT, MODE_BG);
            out.println("<!-- ********* OUTPUT TURN 2 - Floating boxes ********* -->");
            writeBoxSVG(box, out, TURN_FLOAT, MODE_BOTH);
            out.println("<!-- ********* OUTPUT TURN 3 - Foreground ********* -->");
            writeBoxSVG(box, out, TURN_NONFLOAT, MODE_FG);
        }
    }
    
    protected void writeBoxSVG(Box box, PrintWriter out, int turn, int mode) throws IOException
    {
        if (box instanceof TextBox)
            writeTextBoxSVG((TextBox) box, out, turn, mode);
        else
            writeElementBoxSVG((ElementBox) box, out, turn, mode);
    }

    protected void writeTextBoxSVG(TextBox t, PrintWriter out, int turn, int mode) throws IOException
    {
        if (t.isDisplayed() &&
            (turn == TURN_ALL || turn == TURN_NONFLOAT) &&
            (mode == MODE_BOTH || mode == MODE_FG))
        {
            Rectangle b = t.getAbsoluteBounds();
            VisualContext ctx = t.getVisualContext();
            
            String style = "font-size:" + ctx.getFont().getSize() + "px;" + 
                           "font-weight:" + (ctx.getFont().isBold()?"bold":"normal") + ";" + 
                           "font-variant:" + (ctx.getFont().isItalic()?"italic":"normal") + ";" +
                           "font-family:" + ctx.getFont().getFamily() + ";" +
                           "fill:" + colorString(ctx.getColor()) + ";" +
                           "stroke:none";
                           
            out.println("<text x=\"" + b.x + "\" y=\"" + (b.y + b.height) + "\" width=\"" + b.width + "\" height=\"" + b.height + "\" style=\"" + style + "\">" + htmlEntities(t.getText()) + "</text>");
        }
    }
    
    protected void writeElementBoxSVG(ElementBox eb, PrintWriter out, int turn, int mode) throws IOException
    {
        boolean floating = !(eb instanceof BlockBox) || (((BlockBox) eb).getFloating() != BlockBox.FLOAT_NONE);
        boolean draw = (turn == TURN_ALL || (floating && turn == TURN_FLOAT) || (!floating && turn == TURN_NONFLOAT));
        boolean drawbg = draw && (mode == MODE_BOTH || mode == MODE_BG);
        boolean drawfg = draw && (mode == MODE_BOTH || mode == MODE_FG);
        
        Color bg = eb.getBgcolor();
        Rectangle cb = eb.getAbsoluteContentBounds();
        
        if (drawbg)
        {
            //background
            if (bg != null)
            {
                String style = "stroke:none;fill-opacity:1;fill:" + colorString(bg);
                out.println("<rect x=\"" + cb.x + "\" y=\"" + cb.y + "\" width=\"" + cb.width + "\" height=\"" + cb.height + "\" style=\"" + style + "\" />");
            }
        
            //border
            Rectangle bb = eb.getAbsoluteBorderBounds();
            LengthSet borders = eb.getBorder();
            if (borders.top > 0)
                writeBorderSVG(eb, bb.x, bb.y, bb.x + bb.width, bb.y, "top", borders.top, 0, borders.top/2, out);
            if (borders.right > 0)
                writeBorderSVG(eb, bb.x + bb.width, bb.y, bb.x + bb.width, bb.y + bb.height, "right", borders.right, -borders.right/2, 0, out);
            if (borders.bottom > 0)
                writeBorderSVG(eb, bb.x, bb.y + bb.height, bb.x + bb.width, bb.y + bb.height, "bottom", borders.bottom, 0, -borders.bottom/2, out);
            if (borders.left > 0)
                writeBorderSVG(eb, bb.x, bb.y, bb.x, bb.y + bb.height, "left", borders.left, borders.left/2, 0, out);
        }
        
        //contents
        if (eb.isReplaced()) //replaced boxes
        {
            if (drawfg)
            {
                ReplacedContent cont = ((ReplacedBox) eb).getContentObj();
                if (cont != null && cont instanceof ReplacedImage) //images
                {
                    BufferedImage img = ((ReplacedImage) cont).getImage();
                    if (img != null)
                    {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(img, "png", os);
                        char[] data = Base64Coder.encode(os.toByteArray());
                        String imgdata = "data:image/png;base64," + new String(data);
                        out.println("<image x=\"" + cb.x + "\" y=\"" + cb.y + "\" width=\"" + cb.width + "\" height=\"" + cb.height + "\" xlink:href=\"" + imgdata + "\" />");
                    }
                }
            }
        }
        else
        {
            int nestTurn = turn;
            if (floating && turn == TURN_FLOAT)
                nestTurn = TURN_ALL;
            for (int i = 0; i < eb.getSubBoxNumber(); i++) //contained boxes
                writeBoxSVG(eb.getSubBox(i), out, nestTurn, mode);
        }
    }
    
    protected void writeBorderSVG(ElementBox eb, int x1, int y1, int x2, int y2, String side, int width, int right, int down, PrintWriter out) throws IOException
    {
        TermColor tclr = eb.getStyle().getValue(TermColor.class, "border-"+side+"-color");
        CSSProperty.BorderStyle bst = eb.getStyle().getProperty("border-"+side+"-style");
        if (tclr != null && bst != CSSProperty.BorderStyle.HIDDEN)
        {
            Color clr = tclr.getValue();
            if (clr == null) clr = Color.BLACK;

            String stroke = "";
            if (bst == CSSProperty.BorderStyle.SOLID)
            {
                stroke = "stroke-width:" + width;
            }
            else if (bst == CSSProperty.BorderStyle.DOTTED)
            {
                stroke = "stroke-width:" + width + ";stroke-dasharray:" + width + "," + width;
            }
            else if (bst == CSSProperty.BorderStyle.DASHED)
            {
                stroke = "stroke-width:" + width + ";stroke-dasharray:" + (3*width) + "," + width;
            }
            else if (bst == CSSProperty.BorderStyle.DOUBLE)
            {
                //double is not supported yet, we'll use single
                stroke = "stroke-width:" + width;
            }
            else //default or unsupported - draw a solid line
            {
                stroke = "stroke-width:" + width;
            }
            
            String coords = "M " + (x1+right) + "," + (y1+down) + " L " + (x2+right) + "," + (y2+down);
            String style = "fill:none;stroke:" + colorString(clr) + ";" + stroke;
            out.println("<path style=\"" + style + "\" d=\"" + coords + "\" />");  
        }
        
    }
    
    protected String colorString(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    protected String htmlEntities(String s)
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
