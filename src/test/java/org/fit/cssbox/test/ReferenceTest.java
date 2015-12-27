/*
 * ReferenceTest.java
 * Copyright (c) 2005-2015 Radek Burget
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
 * Created on 26. 12. 2015, 23:35:53 by burgetr
 */
package org.fit.cssbox.test;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.vutbr.web.css.MediaSpec;

/**
 *
 * @author burgetr
 */
public class ReferenceTest
{
    private static Logger log = LoggerFactory.getLogger(ReferenceTest.class);
    
    private String urlstring;
    
    private DocumentSource docSource;
    private DOMSource parser;
    
    //default rendering config
    private String mediaType = "screen";
    private Dimension windowSize = new Dimension(1200, 600);
    private boolean cropWindow = false;
    private boolean loadImages = true;
    private boolean loadBackgroundImages = true;

    public ReferenceTest(String urlstring)
    {
        this.urlstring = urlstring;
    }

    public float performTest() throws IOException, SAXException
    {
        log.info("Loading test {}", urlstring);
        Document doc = loadDocument(urlstring);
        URL srcurl = docSource.getURL();
        URL refurl = extractReference(doc, docSource.getURL());
        BufferedImage testImg = renderDocument(doc);
        closeDocument();
        
        log.info("  -- reference result {}", refurl);
        Document refDoc = loadDocument(refurl.toString());
        BufferedImage refImg = renderDocument(refDoc);
        closeDocument();
        
        ImageComparator ic = new ImageComparator(testImg, refImg);
        log.info(" -- error rate {}", ic.getErrorRate());
        if (ic.getErrorRate() > 0.0f)
        {
            File path = new File(srcurl.getFile());
            saveImage(testImg, "/tmp/test-" + path.getName() + ".srcA.png");
            saveImage(refImg, "/tmp/test-" + path.getName() + ".srcB.png");
            saveImage(ic.getDifferenceImage(), "/tmp/test-" + path.getName() + ".diff.png");
        }
        
        return ic.getErrorRate();
    }
    
    private Document loadDocument(String urlstring) throws IOException, SAXException
    {
        docSource = new DefaultDocumentSource(urlstring);
        parser = new DefaultDOMSource(docSource);
        Document doc = parser.parse();
        return doc;
    }
    
    private void closeDocument() throws IOException
    {
        if (docSource != null)
            docSource.close();
    }
    
    private BufferedImage renderDocument(Document doc)
    {
        //create the media specification
        MediaSpec media = new MediaSpec(mediaType);
        media.setDimensions(windowSize.width, windowSize.height);
        media.setDeviceDimensions(windowSize.width, windowSize.height);

        //Create the CSS analyzer
        DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
        da.setMediaSpec(media);
        da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
        da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT); //render form fields using css
        da.getStyleSheets(); //load the author style sheets
        
        BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
        contentCanvas.setAutoMediaUpdate(false); //we have a correct media specification, do not update
        contentCanvas.getConfig().setClipViewport(cropWindow);
        contentCanvas.getConfig().setLoadImages(loadImages);
        contentCanvas.getConfig().setLoadBackgroundImages(loadBackgroundImages);

        contentCanvas.createLayout(windowSize);
        return contentCanvas.getImage();
    }
    
    /**
     * Finds the URL of the reference document from the META tags.
     * @param doc the source document
     * @return the URL or {@code null} when the URL was not found
     */
    private URL extractReference(Document doc, URL baseURL)
    {
        NodeList links = doc.getElementsByTagName("link");
        String href = null;
        for (int i = 0; i < links.getLength(); i++)
        {
            Element link = (Element) links.item(i);
            if ("match".equalsIgnoreCase(link.getAttribute("rel")))
            {
                href = link.getAttribute("href");
                break;
            }
        }
        if (href != null && !href.trim().isEmpty())
        {
            href = href.trim();
            try
            {
                return new URL(baseURL, href);
            } catch (MalformedURLException e) {
                log.warn("Invalid reference URL: {}", href);
                return null;
            }
        }
        else
            return null;
    }
    
    private void saveImage(BufferedImage img, String outfile)
    {
        try
        {
            FileOutputStream os = new FileOutputStream(outfile);
            ImageIO.write(img, "png", os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
