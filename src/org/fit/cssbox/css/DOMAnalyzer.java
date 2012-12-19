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
import cz.vutbr.web.css.Selector.PseudoDeclaration;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.StyleMap;


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
    private String encoding; //default character encoding for style sheet parsing
    
    private Vector<StyleSheet> styles;  //vector of StyleSheet sheets
    private Analyzer analyzer; //style sheet analyzer
    private StyleMap stylemap; //style map for DOM nodes
    private StyleMap istylemap; //style map with inheritance
    
    /** The origin of a style sheet */
    public enum Origin 
    {
    	/** An author style sheet (this is the default in most cases) */
    	AUTHOR, 
    	/** A user agent style sheet */
    	AGENT, 
    	/** A user tstyle sheet */
    	USER 
    };
    
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     */
    public DOMAnalyzer(org.w3c.dom.Document doc) 
    {
        this.doc = doc;
        this.media = DEFAULT_MEDIA;
        baseUrl = null;
        encoding = null;
        styles = new Vector<StyleSheet>();
        stylemap = null;
        istylemap = null;
    }
        
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     * @param baseUrl the base URL for loading the style sheets. This URL may be redefined by the <code>&lt;base&gt;</code> tag used in the
     * document header.
     */
    public DOMAnalyzer(org.w3c.dom.Document doc, URL baseUrl) 
    {
        this(doc, baseUrl, true);
    }
    
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     * @param baseUrl the base URL for loading the style sheets. If <code>detectBase</code>, this URL may be redefined by the <code>&lt;base&gt;</code> tag used in the
     * document header.
     * @param detectBase sets whether to try to accept the <code>&lt;base&gt;</code> tags in the document header.
     */
    public DOMAnalyzer(org.w3c.dom.Document doc, URL baseUrl, boolean detectBase) 
    {
        this.doc = doc;
        this.encoding = null;
        this.media = DEFAULT_MEDIA;
        styles = new Vector<StyleSheet>();
        this.baseUrl = baseUrl;
        if (detectBase)
        {
            String docbase = getDocumentBase();
            if (docbase != null)
            {
                try {
                    this.baseUrl = new URL(baseUrl, docbase);
                    System.err.println("DOMAnalyzer: Using specified document base " + this.baseUrl);
                } catch (MalformedURLException e) {
                    System.err.println("DOMAnalyzer: error: malformed base URL " + docbase);
                }
            }
        }
        stylemap = null;
        istylemap = null;
    }

    /**
     * Obtains the default character encoding used for parsing the style sheets. This encoding is used when there is
     * no other encoding specified inside the style sheet using the <code>@charset</code> rule. When there
     * is no default encoding specified, this method returns <code>null</code> and the system default encoding
     * is used for style sheets.
     * @return The encoding name or <code>null</code> when there is no default encoding specified.
     */
    public String getDefaultEncoding()
    {
        return encoding;
    }

    /**
     * Sets the default character encoding used for parsing the referenced style sheets. This encoding is used when there is
     * no other encoding specified inside the style sheet using the <code>@charset</code> rule.
     * @param encoding The encoding string or <code>null</code> for using the system default encoding.
     */
    public void setDefaultEncoding(String encoding)
    {
        this.encoding = encoding;
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
     * Finds the explicitly specified base URL in the document according to the HTML specification
     * @return the base URL string, when present in the document or null when not present
     * @see <a href="http://www.w3.org/TR/html4/struct/links.html#edef-BASE">the BASE element definition</a>
     */
    public String getDocumentBase()
    {
        Element head = getHead();
        if (head != null)
        {
            NodeList bases = head.getElementsByTagName("base");
            if (bases != null && bases.getLength() > 0)
            {
                Element base = (Element) bases.item(0);
                if (base.hasAttribute("href"))
                    return base.getAttribute("href");
                else
                    return null;
            }
            else
                return null;
        }
        else
            return null;
    }
    
    /**
     * Finds the explicitly specified character encoding using the <code>&lt;meta&gt;</code> tag according
     * to the HTML specifiaction.
     * @return the encoding name when present in the document or <code>null</code> if not present. 
     */
    public String getCharacterEncoding()
    {
        Element head = getHead();
        if (head != null)
        {
            NodeList metas = head.getElementsByTagName("meta");
            if (metas != null)
            {
                for (int i = 0; i < metas.getLength(); i++)
                {
                    Element meta = (Element) metas.item(i);
                    if (meta.hasAttribute("http-equiv")
                            && meta.getAttribute("http-equiv").equalsIgnoreCase("content-type") 
                            && meta.hasAttribute("content"))
                    {
                        String ctype = meta.getAttribute("content").toLowerCase(); 
                        int cpos = ctype.indexOf("charset=");
                        if (cpos != -1)
                            return ctype.substring(cpos + 8);
                    }
                    else if (meta.hasAttribute("charset"))
                    {
                        return meta.getAttribute("charset");
                    }
                }
                return null;
            }
            else
                return null;
        }
        else
            return null;
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
        StyleSheet newsheet = CSSFactory.getUsedStyles(doc, encoding, baseUrl, media);
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
     * Loads a stylesheet from an URL as na author style sheet.
     * Imports all the imported style sheets before storing it.
     * @param base the document base url
     * @param href the href specification
     * @param encoding the default encoding
     */
    public void loadStyleSheet(URL base, String href, String encoding)
    {
    	loadStyleSheet(base, href, encoding, Origin.AUTHOR);
    }
    
    /**
     * Loads a stylesheet from an URL and specifies the origin.
     * Imports all the imported style sheets before storing it.
     * @param base the document base url
     * @param href the href specification
     * @param encoding the default character encoding for the style sheet (use <code>null</code> for default system encoding)
     * @param origin the style sheet origin (author, user agent or user)
     */
    public void loadStyleSheet(URL base, String href, String encoding, Origin origin)
    {
        try {
            StyleSheet newsheet = CSSFactory.parse(new URL(base, href), encoding);
            newsheet.setOrigin(translateOrigin(origin));
            styles.add(newsheet);
        } catch (IOException e) {
            System.err.println("DOMAnalyzer: I/O Error: "+e.getMessage());
        } catch (CSSException e) {
            System.err.println("DOMAnalyzer: CSS Error: "+e.getMessage());
        }
    }
    
    /**
     * Parses and adds an author style sheet represented as a string to the end of the used
     * stylesheet lists. It imports all the imported style sheets before storing
     * it.
     * @param base the document base URL
     * @param cssdata the style string
     * @deprecated This method does not specify the style sheet origin; use {@link #addStyleSheet(URL, String, Origin)} instead.
     */
	public void addStyleSheet(URL base, String cssdata)
    {
		addStyleSheet(base, cssdata, Origin.AUTHOR);
    }
    
    /**
     * Parses and adds an author style sheet represented as a string to the end of the used
     * stylesheet lists. It imports all the imported style sheets before storing
     * it. The origin of the style sheet is set to the author, agent or user. The origin
     * influences the rule priority according to the <a href="http://www.w3.org/TR/CSS21/cascade.html#cascading-order">CSS specification</a>.
     * @param base the document base URL
     * @param cssdata the style string
     * @param origin the style sheet origin (AUTHOR, AGENT or USER)
     */
	public void addStyleSheet(URL base, String cssdata, Origin origin)
    {
	    try {
    	    StyleSheet newsheet = CSSFactory.parse(cssdata);
            newsheet.setOrigin(translateOrigin(origin));
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
    	if (analyzer == null)
    		analyzer = new Analyzer(styles);
    	
    	if (stylemap == null)
    		stylemap = analyzer.evaluateDOM(doc, media, false);
    	
    	return stylemap.get(el);
    }
    
    /**
     * Checks whether the inhetited style has been computed and computes it when necessary.
     */
    private void checkStylesInherited()
    {
    	if (analyzer == null)
    		analyzer = new Analyzer(styles);
        
        if (istylemap == null)
            istylemap = analyzer.evaluateDOM(doc, media, true);
    }
    
    /**
     * Gets all the style declarations for a particular element and computes 
     * the resulting element style including the inheritance from the parent.
     * @param el the element for which the style should be computed
     * @return the resulting style declaration 
     */
    public NodeData getElementStyleInherited(Element el)
    {
        checkStylesInherited();
    	return istylemap.get(el);
    }
    
    /**
     * Gets all the style declarations for a particular element and a particular pseudo-element and computes 
     * the resulting pseudo-element style including the inheritance from the parent.
     * @param el the element for which the style should be computed
     * @param pseudo the pseudo class or element used for style computation
     * @return the resulting style declaration 
     */
    public NodeData getElementStyleInherited(Element el, PseudoDeclaration pseudo)
    {
        checkStylesInherited();
        return istylemap.get(el, pseudo);
    }
    
    /**
     * Checks whether the element has some style defined for a specified pseudo-element. 
     * @param el The element to be checked
     * @param pseudo The pseudo element specification
     * @return <code>true</code> if there is some style defined for the given pseudo-element
     */
    public boolean hasPseudoDef(Element el, PseudoDeclaration pseudo)
    {
        checkStylesInherited();
        return istylemap.hasPseudo(el, pseudo);
    }
    
    /**
     * Assigns the given style for the specified pseudo-element.
     * @param el the element to be assigned the style
     * @param pseudo The pseudo-element or <code>null</code> if none is required
     * @param style the assigned style
     */
    public void useStyle(Element el, PseudoDeclaration pseudo, NodeData style)
    {
        checkStylesInherited();
        istylemap.put(el, pseudo, style);
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
    
    /**
     * Translates the origin from the CSSBox API to jStyleParser API
     * (in order not to expose the jStyleParser API in CSSBox)
     */
    private StyleSheet.Origin translateOrigin(Origin origin)
    {
    	if (origin == Origin.AUTHOR)
    		return StyleSheet.Origin.AUTHOR;
    	else if (origin == Origin.AGENT)
    		return StyleSheet.Origin.AGENT;
    	else
    		return StyleSheet.Origin.USER;
    }
}
