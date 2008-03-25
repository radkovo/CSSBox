/**
 * ComputeStyles.java
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
 * Created on 13.11.2007, 15:44:42 by burgetr
 */
package org.fit.cssbox.demo;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.NormalOutput;
import org.fit.cssbox.css.Output;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * This example computes the effective style of each element and encodes it into the
 * <code>style</code> attribute of this element. The modified HTML document is then saved
 * to the output file.
 * 
 * @author burgetr
 */
public class ComputeStyles
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: ComputeStyles <url> <output_file>");
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
            da.addStyleSheet(null, CSSNorm.stdStyleSheet()); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet()); //use the additional style sheet
            da.getStyleSheets(); //load the author style sheets
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            
            //Compute the styles
            System.err.println("Computing style...");
            da.stylesToDomInherited();
            
            //Save the output
            PrintStream os = new PrintStream(new FileOutputStream(args[1]));
            Output out = new NormalOutput(doc);
            out.dumpTo(os);
            os.close();
            
            is.close();
            
            System.err.println("Done.");
            
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
        }

    }

}
