/*
 * ReferenceCase.java
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
package org.fit.cssbox.testing;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

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
 * A callable process that represents a single reference test.
 * 
 * @author burgetr
 */
public class ReferenceTestCase implements Callable<Float>
{
    private static Logger log = LoggerFactory.getLogger(ReferenceTestCase.class);
    
    private String name;
    private String urlstring;
    private boolean saveImages;
    private TestBatch batch;
    
    private DocumentSource docSource;
    private DOMSource parser;
    
    //default rendering config
    private String mediaType = "screen";
    private Dimension windowSize = new Dimension(1200, 600);
    private boolean cropWindow = false;
    private boolean loadImages = true;
    private boolean loadBackgroundImages = true;

    /**
     * Creates a reference test.
     * @param name the test name (e.g. absolute-replaced-width-001)
     * @param urlstring the source test file URL (only the HTML tests are supported right now)
     */
    public ReferenceTestCase(String name, String urlstring)
    {
        this.name = name;
        this.urlstring = urlstring;
        this.saveImages = false;
    }
    
    /**
     * Creates a reference test.
     * @param name the test name (e.g. absolute-replaced-width-001)
     * @param urlstring the source test file URL (only the HTML tests are supported right now)
     * @param saveImages {@code true} to save the difference images when the test fails
     */
    public ReferenceTestCase(String name, String urlstring, boolean saveImages)
    {
        this.name = name;
        this.urlstring = urlstring;
        this.saveImages = saveImages;
    }
    
    public void setBatch(TestBatch batch)
    {
        this.batch = batch;
    }
    
    /**
     * Obtains the test name.
     * @return the test name
     */
    public String getName()
    {
        return name;
    }

    @Override
    public Float call() throws Exception
    {
        return performTest();
    }
    
    /**
     * Runs the test and returns the result.
     * @return The resulting failure rate (0.0 means passed, >0.0 means fail)
     * @throws IOException
     * @throws SAXException
     */
    public float performTest() throws IOException, SAXException
    {
        log.debug("Loading test {}", urlstring);
        Document doc = loadDocument(urlstring);
        URL srcurl = docSource.getURL();
        URL refurl = extractReference(doc, docSource.getURL());
        BufferedImage testImg = renderDocument(doc);
        closeDocument();
        
        log.debug("  -- reference result {}", refurl);
        Document refDoc = loadDocument(refurl.toString());
        BufferedImage refImg = renderDocument(refDoc);
        closeDocument();
        
        ImageComparator ic = new ImageComparator(testImg, refImg);
        log.debug(" -- error rate {}", ic.getErrorRate());
        if (saveImages && ic.getErrorRate() > 0.0f)
        {
            File path = new File(srcurl.getFile());
            saveImage(testImg, "/tmp/test-" + path.getName() + ".srcA.png");
            saveImage(refImg, "/tmp/test-" + path.getName() + ".srcB.png");
            saveImage(ic.getDifferenceImage(), "/tmp/test-" + path.getName() + ".diff.png");
        }
        if (ic.getErrorDescription() != null)
            log.error(name + ": " + ic.getErrorDescription());
        
        if (batch != null)
            batch.reportCompletion(this);
        
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
