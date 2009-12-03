/*
 * DOMAnalyzer.java
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
 * Created on 20. leden 2005, 14:08
 */

package org.fit.cssbox.css;

import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.domassign.Analyzer;


/**
 * This class implements methods for the DOM tree style analysis.
 * 
 * @author  burgetr
 */
public class DOMAnalyzer 
{
	private static final String DEFAULT_MEDIA = "screen";
	
    private Document doc;   //the root node of the DOM tree
    private URL baseUrl;    //base URL
    private String media;   //media type
    
    private Vector<StyleSheet> styles;  //vector of StyleSheet sheets
    private StyleSheet mainsheet; //the stylesheet containing all the rules, if it is null, it must be recomputed
    private Analyzer analyzer; //style sheet analyzer
    private Map<Element, NodeData> stylemap; //style map for DOM nodes
    private Map<Element, NodeData> istylemap; //style map with inheritance
    
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     */
    public DOMAnalyzer(org.w3c.dom.Document doc) 
    {
        this.doc = doc;
        this.media = DEFAULT_MEDIA;
        baseUrl = null;
        styles = new Vector<StyleSheet>();
        mainsheet = null;
        stylemap = null;
        istylemap = null;
    }
        
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     * @param baseUrl the base URL for loading the style sheets
     */
    public DOMAnalyzer(org.w3c.dom.Document doc, URL baseUrl) 
    {
        this.doc = doc;
        this.media = DEFAULT_MEDIA;
        styles = new Vector<StyleSheet>();
        this.baseUrl = baseUrl;
        mainsheet = null;
        stylemap = null;
        istylemap = null;
    }
    
    /**
     * Returns current medium used.
	 * @return the media type according to CSS
	 */
	public String getMedia()
	{
		return media;
	}

	/**
	 * Set the medium used for computing the style. Default is "screen".
	 * @param media the medium to set
	 */
	public void setMedia(String media)
	{
		this.media = media;
	}

	/**
	 * Returns the root element of the document.
	 */
	public Element getRoot()
	{
	    return doc.getDocumentElement();
	}
	
	/**
     * Returns the &lt;head&gt; element.
     */
    public Element getHead()
    {
        return (Element) doc.getElementsByTagName("head").item(0);
    }
    
    /**
     * Returns the &lt;body&gt; element.
     */
    public Element getBody()
    {
        return (Element) doc.getElementsByTagName("body").item(0);
    }
    
    /**
     * Formats the tag tree to an output stream. This is used mainly for debugging purposes.
     */
    public void printTagTree(java.io.PrintStream out)
    {
        recursivePrintTags(doc, 0, out);
    }

    /**
     * Encodes the efficient style of all the elements to their <code>style</code> attributes.
     * Inheritance is not applied.
     * This is currently not necessary for the rendering. It is practical mainly together with
     * the <code>printTagTree</code> method for debugging the resulting style. 
     */
    public void stylesToDom()
    {
        recursiveStylesToDom(getBody());
    }
    
    /**
     * Encodes the efficient style of all the elements to their <code>style</code> attributes
     * while applying the inheritance.
     * This is currently not necessary for the rendering. It is practical mainly together with
     * the <code>printTagTree</code> method for debugging the resulting style. 
     */
    public void stylesToDomInherited()
    {
        recursiveStylesToDomInherited(getBody());
    }

    /**
     * Converts the HTML presentation attributes in the document body to inline styles.
     */
    public void attributesToStyles()
    {
        //fix the DOM tree structure according to HTML syntax
        HTMLNorm.normalizeHTMLTree(doc);
        //convert attributes to style definitions
        HTMLNorm.attributesToStyles(getBody(), "");
    }
    
    /** 
     * Returns a vector of CSSStyleSheet objects referenced from the document for the specified
     * media type. The internal style sheets are read from the document directly, the external
     * ones are downloaded and parsed automatically.
     * @param media the media type string
     */
    public void getStyleSheets(String media)
    {
    	this.media = new String(media);
        StyleSheet newsheet = CSSFactory.getUsedStyles(doc, baseUrl, media);
        styles.add(newsheet);
    }

    /** 
     * Returns a vector of CSSStyleSheet objects referenced from the document for the media
     * type set by <code>setMedia()</code> (or "screen" by default). The internal style 
     * sheets are read from the document directly, the external ones are downloaded
     * and parsed automatically.
     */
    public void getStyleSheets()
    {
    	getStyleSheets(media);
    }

    /**
     * Loads a stylesheet from an URL.
     * Imports all the imported style sheets before storing it.
     * @param base the document base url
     * @param href the href specification
     */
    public void loadStyleSheet(URL base, String href, String encoding)
    {
        try {
            StyleSheet newsheet = CSSFactory.parse(new URL(base, href), encoding); 
            styles.add(newsheet);
        } catch (IOException e) {
            System.err.println("DOMAnalyzer: I/O Error: "+e.getMessage());
        } catch (CSSException e) {
            System.err.println("DOMAnalyzer: CSS Error: "+e.getMessage());
        }
    }
    
    /**
     * Parses and adds a style sheet represented as a string to the end of the used
     * stylesheet lists. It imports all the imported style sheets before storing
     * it.
     * @param base the document base URL
     * @param cssdata the style string
     */
	public void addStyleSheet(URL base, String cssdata)
    {
	    try {
    	    StyleSheet newsheet = CSSFactory.parse(cssdata);
            styles.add(newsheet);
	    } catch (IOException e) {
            System.err.println("DOMAnalyzer: I/O Error: "+e.getMessage());
        } catch (CSSException e) {
            System.err.println("DOMAnalyzer: CSS Error: "+e.getMessage());
        }
    }
    
    /**
     * Gets all the style declarations for a particular element and computes 
     * the resulting element style.
     * @param el the element for which the style should be computed
     * @return the resulting declaration
     */
    public NodeData getElementStyle(Element el)
    {
    	if (mainsheet == null)
    	{
    		mainsheet = computeMainSheet();
    		analyzer = new Analyzer(mainsheet);
    	}
    	
    	if (stylemap == null)
    		stylemap = analyzer.evaluateDOM(doc, media, false);
    	
    	return stylemap.get(el);
    }
    
    /**
     * Gets all the style declarations for a particular element and computes 
     * the resulting element style including the inheritance from the parent.
     * @param el the element for which the style should be computed
     * @return the resulting style declaration 
     */
    public NodeData getElementStyleInherited(Element el)
    {
    	if (mainsheet == null)
    	{
    		mainsheet = computeMainSheet();
    		analyzer = new Analyzer(mainsheet);
    	}
    	
    	if (istylemap == null)
    		istylemap = analyzer.evaluateDOM(doc, media, true);
    	
    	return istylemap.get(el);
    }
    
    //====================================================================
    
    private void recursiveStylesToDom(Node n)
    {
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            Element el = (Element) n;
            NodeData decl = getElementStyle(el);
            if (decl != null)
            {
                String decls = decl.toString().replace("\n", "");
                el.setAttribute("style", quote(decls));
            }
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveStylesToDom(child.item(i));
    }

    private void recursiveStylesToDomInherited(Node n)
    {
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            Element el = (Element) n;
            NodeData decl = getElementStyleInherited(el);
            if (decl != null)
            {
                String decls = decl.toString().replace("\n", "");
                el.setAttribute("style", quote(decls));
            }
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveStylesToDomInherited(child.item(i));
    }

    
    private String quote(String s)
    {
        return s.replace('"', '\'');
    }

    //========================================================================
    
    private void recursivePrintTags(Node n, int level, java.io.PrintStream p)
    {
        String mat = "";
        
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            NodeData decl = getElementStyle((Element) n);
            if (decl != null)
                mat = "style:\"" + decl.toString() + "\"";
        }
        else if (n.getNodeType() == Node.TEXT_NODE)
        {
            mat = n.getNodeValue();
        }
                
        String ind = "";
        for (int i = 0; i < level*4; i++) ind = ind + ' ';
        
        p.println(ind + n.getNodeName() + " " + mat);
        
        
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursivePrintTags(child.item(i), level+1, p);   
    }
        
    
    private StyleSheet computeMainSheet()
    {
        StyleSheet newstyle = (StyleSheet) CSSFactory.getRuleFactory().createStyleSheet().unlock();
        for (StyleSheet style : styles)
            newstyle.addAll(style);
        return newstyle;
    }
    
}
