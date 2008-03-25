/*
 * DOMAnalyzer.java
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
 * Created on 20. leden 2005, 14:08
 */

package org.fit.cssbox.css;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import org.w3c.dom.css.*;
//import org.w3c.css.sac.InputSource;
import com.steadystate.css.*;
import com.steadystate.css.dom.*;
//import com.steadystate.css.parser.*;

/**
 * This class implements methods for the DOM tree style analysis.
 * 
 * @author  burgetr
 */
public class DOMAnalyzer 
{
    private Document doc;   //the root node of the DOM tree
    private Vector<StyleSheet> styles;  //vector of StyleSheet sheets
    private URL baseUrl;
    
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     */
    public DOMAnalyzer(org.w3c.dom.Document doc) 
    {
        this.doc = doc;
        styles = new Vector<StyleSheet>();
        baseUrl = null;
    }
        
    /**
     * Creates a new DOM analyzer.
     * @param doc the document to be analyzed
     * @param baseUrl the base URL for loading the style sheets
     */
    public DOMAnalyzer(org.w3c.dom.Document doc, URL baseUrl) 
    {
        this.doc = doc;
        styles = new Vector<StyleSheet>();
        this.baseUrl = baseUrl;
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
        recursiveStylesToDomInherited(getBody(), null);
    }

    /**
     * Converts the HTML presentation attributes in the document body to inline styles.
     */
    public void attributesToStyles()
    {
        HTMLNorm.attributesToStyles(getBody(), "");
    }
    
    /**
     * Creates anonymous &lt;span&gt; elements from the text nodes which
     * have some sibling elements
     * @deprecated
     */
    public void createAnonymousBoxes()
    {
        recursiveCreateAnonymousBoxes(getBody());
    }
    
    /** 
     * Returns a vector of CSSStyleSheet objects referenced from the document. The internal
     * style sheets are read from the document directly, the external ones are downloaded and
     * parsed automatically.
     */
    public void getStyleSheets()
    {
        NodeList nodes = getHead().getChildNodes();        
        for (int i = 0; i < nodes.getLength(); i++)
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                Element elem = (Element) nodes.item(i);
                //Internal style sheet using <style>
                if (elem.getNodeName().equals("style"))
                {
                    String media = elem.getAttribute("media");
                    System.err.println("Internal, media="+media);
                    if (allowedMedia(media))
                    {
                        Node text = elem.getFirstChild();
                        if (text != null && text.getNodeType() == Node.TEXT_NODE)
                        {
                            String cssdata = ((Text) text).getData();
                            addStyleSheet(baseUrl, cssdata);
                        }
                    }
                }
                //External style sheet using <link>
                else if (elem.getNodeName().equals("link"))
                {
                    if (elem.getAttribute("rel").equalsIgnoreCase("stylesheet") &&
                        elem.getAttribute("type").equalsIgnoreCase("text/css"))
                    {
                        String media = elem.getAttribute("media");
                        System.err.println("External: "+elem.getAttribute("href")+" media="+media);
                        if (allowedMedia(media))
                        {
                            if (baseUrl != null)
                                loadStyleSheet(baseUrl, elem.getAttribute("href"));
                            else
                                System.err.println("No base URL.");
                        }
                    }
                }
            }
    }

    /**
     * Loads a stylesheet from an URL.
     * Imports all the imported style sheets before storing it.
     * @param base the document base url
     * @param href the href specification
     */
    public void loadStyleSheet(URL base, String href)
    {
        String cssdata = "";
        try {
            URL url = new URL(base, href);
            URLConnection con = url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = r.readLine()) != null)
                cssdata = cssdata + line + "\n";
            System.err.println("Load: "+url.toString());
            addStyleSheet(url, cssdata);
            r.close();
        } catch (IOException e) {
            System.err.println("I/O Error: "+e.getMessage());
        }
    }
    
    /**
     * Parses and adds a style sheet represented as a string to the end of the used
     * stylesheet lists. It imports all the imported style sheets before storing
     * it.
     * @param base the document base URL
     * @param cssdata the style string
     */
    @SuppressWarnings("deprecation")
	public void addStyleSheet(URL base, String cssdata)
    {
        //old style
        CSS2Parser parser = new CSS2Parser(new StringReader(cssdata));
        CSSStyleSheet stylesheet = parser.styleSheet();
        if (baseUrl != null)
            importStyles(base, stylesheet);
        else
            System.err.println("Import: no base URL specified");
        StyleSheet newsheet = new StyleSheet(CSSNorm.normalizeStyleSheet(stylesheet));
        styles.add(newsheet);
    }
    
    /**
     * Scans a stylesheet and loads all the imported style sheets.
     * @param base the document base URL
     * @param sheet the style sheet to be scanned
     */
    public void importStyles(URL base, CSSStyleSheet sheet)
    {
        CSSRuleList rules = sheet.getCssRules();
        for (int i = 0; i < rules.getLength(); i++)
        {
            if (rules.item(i).getType() == CSSRule.IMPORT_RULE)
            {
                CSSImportRule rule = (CSSImportRule) rules.item(i);
                
                String media = rule.getMedia().getMediaText();
                System.err.println("Import: " + rule.getHref() + " media=" + media);
                if (allowedMedia(media))
                    loadStyleSheet(base, rule.getHref());
            }
        }
    }
    
    /**
     * Finds all CSS rules that match the specified DOM element.
     * @param elem The DOM element that is matched to the rule selectors
     * @return a vector of matching rules
     */
    public Vector<RuleSpec> getMatchingRules(Element elem)
    {
        Vector<RuleSpec> ret = new Vector<RuleSpec>();
        for (int si = 0; si < styles.size(); si++)
        {
            StyleSheet sheet = (StyleSheet) styles.elementAt(si);
            for (int ri = 0; ri < sheet.size(); ri++)
            {
                RuleSpec rule = sheet.getRule(ri);
                if (rule.getSelector().matches(elem))
                {
                    ret.add(new RuleSpec(rule));
                }
            }
        }
        return ret;
    }
    
    /**
     * Joins multiple CSS rules to a single one.
     * @param rules The vector of rules to be joined
     * @return the resulting declaration
     */
    public CSSStyleDeclaration joinRules(Vector<RuleSpec> rules)
    {
        CSSStyleDeclaration dest = null;
        for (int i = 0; i < rules.size(); i++)
        {
            RuleSpec rule = rules.elementAt(i);
            dest = CSSNorm.joinDeclaration(rule.getStyle(), dest);
        }
        return dest;
    }
    
    /**
     * Gets all the style declarations for a particular element and computes 
     * the resulting element style.
     * @param el the element for which the style should be computed
     * @return the resulting declaration
     */
    public CSSStyleDeclaration getElementStyle(Element el)
    {
        //Get matching style rules from stylesheets
        Vector<RuleSpec> match = getMatchingRules(el);
                
        //Apply the inline style
        if (el.getAttributes().getNamedItem("style") != null)
        {
            RuleSpec irule = new RuleSpec(getStyleDeclaration(el));
            match.add(irule);
        }
        return joinRules(match);
    }
    
    /**
     * Get all the style declarations for a particular element and compute 
     * the resulting element style including the inheritance from a parent.
     * @param el the element for which the style should be computed
     * @param parent the parent element style from which the values are inherited
     * @return the resulting style declaration 
     */
    public CSSStyleDeclaration getElementStyleInherited(Element el, CSSStyleDeclaration parent)
    {
        CSSStyleDeclaration decl = getElementStyle(el);
        if (decl != null)
        {
            if (parent != null)
                decl = CSSNorm.computeInheritedStyle(parent, decl);
            else
                CSSUnits.normalizeUnits(null, decl);
        }
        return decl;
    }
    
    //========================================================================

    /**
     * Creates a style declaration from the inline style of an element.
     * @param el the element
     * @return the style declaration 
     */
    public static CSSStyleDeclaration getStyleDeclaration(Element el)
    {
        String stylespec = el.getAttribute("style");
        CSSStyleDeclaration decl = new CSSStyleDeclarationImpl(null);
        decl.setCssText(stylespec);
        return CSSNorm.normalizeDeclaration(decl);
    }

    /**
     * Creates a style declaration from a CSS style string
     * @param stylespec the style string
     * @return the style declaration 
     */
    public static CSSStyleDeclaration getStyleDeclaration(String stylespec)
    {
        CSSStyleDeclaration decl = new CSSStyleDeclarationImpl(null);
        decl.setCssText(stylespec);
        return CSSNorm.normalizeDeclaration(decl);
    }
        
    //====================================================================
    
    private void recursiveStylesToDom(Node n)
    {
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            Element el = (Element) n;
            CSSStyleDeclaration decl = getElementStyle(el);
            if (decl != null)
            {
                String decls = decl.getCssText();
                decls = decls.substring(1, decls.length()-1);
                el.setAttribute("style", quote(decls));
            }
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveStylesToDom(child.item(i));
    }

    private void recursiveStylesToDomInherited(Node n, CSSStyleDeclaration parent)
    {
        CSSStyleDeclaration nextlevel = parent;
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            Element el = (Element) n;
            CSSStyleDeclaration decl = getElementStyle(el);
            if (decl != null)
            {
                if (parent != null)
                    decl = CSSNorm.computeInheritedStyle(parent, decl);
                else
                    CSSUnits.normalizeUnits(null, decl);
                String decls = decl.getCssText();
                decls = decls.substring(1, decls.length()-1);
                el.setAttribute("style", quote(decls));
            }
            nextlevel = decl;
        }                
        NodeList child = n.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            recursiveStylesToDomInherited(child.item(i), nextlevel);
    }

    private void recursiveCreateAnonymousBoxes(Element n)
    {
        boolean text = false;
        boolean elem = false;
        NodeList child = n.getChildNodes();

        //check if there is any text node and any element node
        for (int i = 0; i < child.getLength(); i++)
        {
            if (child.item(i).getNodeType() == Node.TEXT_NODE)
                text = true;
            if (child.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                elem = true;
                recursiveCreateAnonymousBoxes((Element) child.item(i));
            }
        }
        
        //if there are both text and element nodes, conver the text nodes
        //to <span> elements
        if (text && elem)
        {
            for (int i = 0; i < child.getLength(); i++)
            {
                Node c = child.item(i);
                if (c.getNodeType() == Node.TEXT_NODE)
                {
                    Element span = doc.createElement("span");
                    Node t = doc.createTextNode(c.getNodeValue());
                    
                    span.setAttribute("class", "Xanonymous");
                    span.setAttribute("style", "display:inline;");
                    n.replaceChild(span, c);
                    span.appendChild(t);
                }
            }
        }
    }
    
    private String quote(String s)
    {
        return s.replace('"', '\'');
    }

    private boolean allowedMedia(String media)
    {
        return media.equals("") || 
               media.indexOf("screen") != -1 || 
               media.indexOf("all") != -1;
    }
    
    //========================================================================
    
    private void recursivePrintTags(Node n, int level, java.io.PrintStream p)
    {
        String mat = "";
        
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            CSSStyleDeclaration decl = getElementStyle((Element) n);
            if (decl != null)
                mat = "style:\"" + decl.getCssText() + "\"";
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
        
}
