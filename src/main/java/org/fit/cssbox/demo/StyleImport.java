/*
 * StyleImport.java
 * Copyright (c) 2005-2014 Radek Burget
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
 * Created on 28. 2. 2014, 14:21:23 by burgetr
 */
package org.fit.cssbox.demo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.NormalOutput;
import org.fit.cssbox.css.Output;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This demo imports all the linked style sheets into the HTML document and prints the resulting code.
 * 
 * @author burgetr
 */
public class StyleImport
{
    private Document doc;

    public StyleImport(String urlstring) throws IOException, SAXException
    {
        //Open the network connection 
        DocumentSource docSource = new DefaultDocumentSource(urlstring);
        
        //Parse the input document
        DOMSource parser = new DefaultDOMSource(docSource);
        doc = parser.parse();
        
        //Create the CSS analyzer
        DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
        da.getStyleSheets(); //load the author style sheets
        da.localizeStyles(); //put the style sheets into the document header
        
        docSource.close();
    }
    
    public void dumpTo(OutputStream ostream)
    {
        Output out = new NormalOutput(doc, false);
        out.dumpTo(ostream);
    }
    
    public void dumpTo(PrintWriter writer)
    {
        Output out = new NormalOutput(doc, false);
        out.dumpTo(writer);
    }
    
    //==================================================================================================
    
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: StyleImport <url> <output_file>");
            System.exit(0);
        }
        
        try
        {
            StyleImport si = new StyleImport(args[0]);
            FileOutputStream os = new FileOutputStream(args[1]); 
            si.dumpTo(os);
            os.close();
            System.out.println("Done.");
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
        }

    }

}
