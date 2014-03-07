/**
 * ComputeStyles.java
 * Copyright (c) 2005-2007 Radek Burget
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
 * Created on 13.11.2007, 15:44:42 by burgetr
 */
package org.fit.cssbox.demo;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.NormalOutput;
import org.fit.cssbox.css.Output;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.w3c.dom.Document;


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
            DocumentSource docSource = new DefaultDocumentSource(args[0]);
            
            //Parse the input document
            DOMSource parser = new DefaultDOMSource(docSource);
            Document doc = parser.parse();
            
            //Create the CSS analyzer
            DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
            da.getStyleSheets(); //load the author style sheets
            
            //Compute the styles
            System.err.println("Computing style...");
            da.stylesToDomInherited();
            
            //Save the output
            OutputStream os = new FileOutputStream(args[1]);
            Output out = new NormalOutput(doc);
            out.dumpTo(os);
            os.close();
            
            docSource.close();
            
            System.err.println("Done.");
            
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
        }

    }

}
