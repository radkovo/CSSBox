/**
 * TextBoxes.java
 * Copyright (c) 2005-2007 Radek Burget
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
 * Created on 13.11.2007, 14:34:04 by burgetr
 */
package org.fit.cssbox.demo;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.ReplacedImage;
import org.fit.cssbox.layout.TextBox;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * This demo shows how the rendered box tree can be accessed. Itrenders the document 
 * and prints the list of text boxes together with their positions on the page.
 * 
 * @author burgetr
 */
public class TextBoxes
{

    /**
     * Recursively prints the text boxes from the specified tree
     */
    private static void printTextBoxes(Box root)
    {
        if (root instanceof TextBox)
        {
            //text boxes are just printed
            TextBox text = (TextBox) root;
            System.out.println("x=" + text.getBounds().x + " y=" + text.getBounds().y + " text=" + text.getText());
        }
        else if (root instanceof ElementBox)
        {
            //element boxes must be just traversed
            ElementBox el = (ElementBox) root;
            for (int i = el.getStartChild(); i < el.getEndChild(); i++)
                printTextBoxes(el.getSubBox(i));
        }
    }
    
    /**
     * main method
     */
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Usage: TextBoxes <url>");
            System.exit(0);
        }
        
        try {
            //Open the network connection 
            URL url = new URL(args[0]);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            
            //Parse the input document using jTidy
            Tidy tidy = new Tidy();
            tidy.setTrimEmptyElements(false);
            tidy.setAsciiChars(false);
            tidy.setInputEncoding("iso-8859-2");
            tidy.setXHTML(true);
            Document doc = tidy.parseDOM(is, null);
            
            //Create the CSS analyzer
            DOMAnalyzer da = new DOMAnalyzer(doc, url);
            da.createAnonymousBoxes(); //convert anonymous boxes to <span> elements
            da.addStyleSheet(null, CSSNorm.stdStyleSheet()); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet()); //use the additional style sheet
            da.getStyleSheets(); //load the author style sheets
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            
            //Disable image loading
            ReplacedImage.setLoadImages(false);
            
            //Create the browser canvas
            BrowserCanvas browser = new BrowserCanvas(da.getBody(), da, new java.awt.Dimension(1000, 600), url);
            
            //Display the result
            printTextBoxes(browser.getRootBox());
            
            is.close();
            
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
        }

    }
    
}
