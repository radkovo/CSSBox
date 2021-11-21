/*
 * BoxFactory.java
 * Copyright (c) 2005-2019 Radek Burget
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
 * Created on 10.4.2010, 17:13:30 by burgetr
 */

package org.fit.cssbox.layout;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.fit.cssbox.css.Counters;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.HTMLNorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Selector.PseudoElementType;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.css.CSSProperty.Overflow;


/**
 * A factory for creating the box tree. The usual way of creating the box tree is creating the viewport using the 
 * {@link BoxFactory#createViewportTree(Element, Graphics2D, VisualContext, int, int)}. However, the factory can be used for creating
 * the individual nodes or subtrees.
 * 
 * <p>Usually, a single factory is created for each viewport. Then, this factory is accessible using
 * the {@link Viewport#getFactory()} method.
 * 
 * @author burgetr
 */
public class BoxFactory
{
    private static Logger log = LoggerFactory.getLogger(BoxFactory.class);

    private static final Set<CSSProperty.Display> properTableChild;
    static {
        properTableChild = new HashSet<>(7);
        properTableChild.add(ElementBox.DISPLAY_TABLE_ROW);
        properTableChild.add(ElementBox.DISPLAY_TABLE_ROW_GROUP);
        properTableChild.add(ElementBox.DISPLAY_TABLE_HEADER_GROUP);
        properTableChild.add(ElementBox.DISPLAY_TABLE_FOOTER_GROUP);
        properTableChild.add(ElementBox.DISPLAY_TABLE_COLUMN);
        properTableChild.add(ElementBox.DISPLAY_TABLE_COLUMN_GROUP);
        properTableChild.add(ElementBox.DISPLAY_TABLE_CAPTION);
    }
    
    private static final Set<CSSProperty.Display> properTableRowGroupChild;
    static {
        properTableRowGroupChild = new HashSet<>(1);
        properTableRowGroupChild.add(ElementBox.DISPLAY_TABLE_ROW);
    }
    
    private static final Set<CSSProperty.Display> properTableRowChild;
    static {
        properTableRowChild = new HashSet<>(1);
        properTableRowChild.add(ElementBox.DISPLAY_TABLE_CELL);
    }
    
    protected BrowserConfig config;
    protected HTMLBoxFactory html;
    
    protected DOMAnalyzer decoder;
    protected URL baseurl;
    protected Viewport viewport;

    protected int next_order;
    protected boolean overflowPropagated;
    
    
    /**
     * Create a new factory.
     * @param decoder The CSS decoder used for obtaining the DOM styles.
     * @param baseurl Base URL used for completing the relative URLs in the document.
     */
    public BoxFactory(DOMAnalyzer decoder, URL baseurl)
    {
        this.decoder = decoder;
        this.baseurl = baseurl;
        this.next_order = 0;
        this.overflowPropagated = false;
        this.config = new BrowserConfig();
        this.html = new HTMLBoxFactory(this);
    }
    
    /**
     * Obtains the current browser configuration.
     * @return current configuration.
     */
    public BrowserConfig getConfig()
    {
        return config;
    }

    /**
     * Sets the browser configuration used for rendering.
     * @param config the new configuration.
     */
    public void setConfig(BrowserConfig config)
    {
        this.config = config;
    }

    /**
     * Sets whether the engine should use the HTML extensions or not. Currently, the HTML
     * extensions include following:
     * <ul>
     * <li>Creating replaced boxes for <code>&lt;img&gt;</code> elements
     * <li>Using the <code>&lt;body&gt;</code> element background for the whole canvas according to the HTML specification
     * </ul> 
     * @param useHTML <code>false</code> if the extensions should be switched off (default is on)
     */
    public void setUseHTML(boolean useHTML)
    {
        config.setUseHTML(useHTML);
    }
    
    /**
     * Checks if the HTML extensions are enabled for the factory.
     * @return <code>true</code> if the HTML extensions are enabled
     * @see #setUseHTML(boolean) 
     */
    public boolean getUseHTML()
    {
        return config.getUseHTML();
    }
    
    /**
     * Obtains the base URL used by this factory.
     * @return the base URL.
     */
    public URL getBaseURL()
    {
        return baseurl;
    }
    
    /**
     * Obtains the CSS analyzer and decoder used by the boxes.
     * @return A DOMAnalyzer
     */
    public DOMAnalyzer getDecoder()
    {
        return decoder;
    }
    
    /**
     * Reset the factory for creating a new tree.
     */
    public void reset()
    {
        next_order = 0;
        overflowPropagated = false;
    }
    
    /**
     * Create the viewport and the underlying box tree from a DOM tree.
     * 
     * @param root the root element of the source DOM tree.
     * @param ctx the visual context (computed style). Copies of this context will be used for the individual boxes.
     * @param width preferred viewport width.
     * @param height preferred viewport height.
     * @return the created viewport box with the corresponding box subtrees.
     */
    public Viewport createViewportTree(Element root, VisualContext ctx, float width, float height)
    {
        Element vp = createAnonymousElement(root.getOwnerDocument(), "Xdiv", "block");
        viewport = new Viewport(vp, ctx, this, root, width, height);
        viewport.setConfig(config);
        overflowPropagated = false;
        BoxTreeCreationStatus stat = new BoxTreeCreationStatus(viewport);
        createSubtree(root, stat);
        log.debug("Root box is: " + viewport.getRootBox());
        
        return viewport;
    }
    
    /**
     * Creates the box subtrees for all the child nodes of the DOM node corresponding to the box creatin status. Recursively creates the child boxes 
     * from the child nodes.
     * @param stat current tree creation status used for determining the parents
     */
    public void createBoxTree(BoxTreeCreationStatus stat)
    {
        boolean generated = false;
        do
        {
            if (stat.parent.isDisplayed())
            {
                //add previously created boxes (the rest from the last twin)
                if (stat.parent.preadd != null)
                {
                    addToTree(stat.parent.preadd, stat);
                    stat.parent.preadd = null; //don't need to keep this anymore
                }
                
                //create :before elements
                if (stat.parent.previousTwin == null)
                {
                    //create the artificial node and update counters
                    Node n = createPseudoElement(stat.parent, PseudoElementType.BEFORE, stat.counters);
                    if (n != null && (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE))
                    {
                        stat.curchild = -1;
                        createSubtree(n, stat);
                    }
                }
                
                //create normal elements
                NodeList children = stat.parent.getElement().getChildNodes();
                for (int child = stat.parent.firstDOMChild; child < stat.parent.lastDOMChild; child++)
                {
                    Node n = children.item(child);
                    //update the counters
                    if (n.getNodeType() == Node.ELEMENT_NODE)
                    {
                        NodeData style = decoder.getElementStyleInherited((Element) n);
                        stat.counters.applyStyle(style);
                    }
                    //create the subtree
                    if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE)
                    {
                        stat.curchild = child;
                        createSubtree(n, stat);
                    }
                }
                
                //create :after elements
                if (stat.parent.nextTwin == null)
                {
                    //create the artificial node and update counters
                    Node n = createPseudoElement(stat.parent, PseudoElementType.AFTER, stat.counters);
                    if (n != null && (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE))
                    {
                        stat.curchild = children.getLength();
                        createSubtree(n, stat);
                    }
                }
                
                normalizeBox(stat.parent);
            }
            
            //if a twin box has been created, continue creating the unprocessed boxes in the twin box
            if (stat.parent.nextTwin != null)
            {
                stat.parent = stat.parent.nextTwin;
                generated = true;
            }
            else
                generated = false;
            
        } while (generated);
    }

    /**
     * Creates a subtree of a parent box that corresponds to a single child DOM node of this box and adds the subtree to the complete tree.
     * 
     * @param n the root DOM node of the subtree being created
     * @param stat curent box creation status for obtaining the containing boxes 
     */
    private void createSubtree(Node n, BoxTreeCreationStatus stat)
    {
        //store current status for the parent
        stat.parent.curstat = new BoxTreeCreationStatus(stat);
        
        //Create the new box for the child
        Box newbox;
        boolean istext = false;
        if (n.getNodeType() == Node.TEXT_NODE)
        {
            newbox = createTextBox((Text) n, stat);
            istext = true;
        }
        else
            newbox = createElementBox((Element) n, stat);
        
        //Create the child subtree
        if (!istext) 
        {
            //Determine the containing boxes of the children
            BoxTreeCreationStatus newstat = new BoxTreeCreationStatus(stat);
            newstat.parent = (ElementBox) newbox;
            newstat.counters = new Counters(newstat.counters); //new counters scope
            if (((ElementBox) newbox).mayContainBlocks()) //the new box forms a block context
            {
                BlockBox block = (BlockBox) newbox;
                //propagate overflow if necessary
                if (!overflowPropagated)
                    overflowPropagated = viewport.checkPropagateOverflow(block);
                //positioned element?
                if (block.position == BlockBox.POS_ABSOLUTE ||
                    block.position == BlockBox.POS_RELATIVE ||
                    block.position == BlockBox.POS_FIXED)
                {
                    //A positioned box forms a content box for following absolutely positioned boxes
                     newstat.absbox = block;
                     //update clip box for the block
                     ElementBox cblock = block.getContainingBlockBox();
                     if (cblock instanceof BlockBox && (cblock.getClipBlock() == null || ((BlockBox) cblock).getOverflowX() != Overflow.VISIBLE))
                         block.setClipBlock((BlockBox) cblock);
                     else
                         block.setClipBlock(cblock.getClipBlock());
                     //A box with overflow:hidden creates a clipping box
                     if (block.overflowX != BlockBox.OVERFLOW_VISIBLE || block.clipRegion != null)
                         newstat.clipbox = block;
                     else
                         newstat.clipbox = block.getClipBlock();
                }
                else //not positioned element
                {
                    //A box with overflow:hidden creates a clipping box
                    if (block.overflowX != BlockBox.OVERFLOW_VISIBLE)
                        newstat.clipbox = block;
                }
                //Any block box forms a containing box for not positioned elements
                newstat.contbox = block;
                //Last inflow box is local for block boxes
                newstat.lastinflow = null; //TODO this does not work in some cases (absolute.html)
                //create the subtree
                createBoxTree(newstat);
                //remove trailing whitespaces in blocks
                removeTrailingWhitespaces(block);
            }
            else
                createBoxTree(newstat);
        }

        //Add the new box to the parent according to its type
        addToTree(newbox, stat);
    }
    
    /**
     * Adds a bew box to the tree according to its type and the tree creation status.
     * @param newbox the box to be added
     * @param stat current box tree creation status used for determining the appropriate parent boxes
     */
    private void addToTree(Box newbox, BoxTreeCreationStatus stat)
    {
        if (newbox.isBlock())  
        {
            if (!((BlockBox) newbox).isPositioned())
            {
                if (stat.parent.mayContainBlocks()) //block in block
                {
                    stat.parent.addSubBox(newbox);
                    stat.lastinflow = newbox;
                }
                else //block in inline box -- split the inline box
                {
                    ElementBox iparent = null; //last inline ancestor
                    ElementBox grandpa = stat.parent; //first block ancestor
                    ElementBox prev = null;
                    do
                    {
                        //start next level
                        iparent = grandpa;
                        grandpa = iparent.getParent();
                        //finish inline parent and create another one
                        int lastchild = iparent.lastDOMChild;
                        iparent.lastDOMChild = iparent.curstat.curchild; //this will finish the iteration just now
                        if (iparent.curstat.curchild + 1 < lastchild || prev != null) //some children are remaning or there is some content already created -- split the inline boxes up to the block level
                        {
                            ElementBox newparent = iparent.copyBox();
                            newparent.removeAllSubBoxes();
                            newparent.firstDOMChild = iparent.curstat.curchild + 1;
                            iparent.nextTwin = newparent;
                            newparent.previousTwin = iparent;
                            if (prev != null) //queue the previously created child to be added to the new box
                                newparent.preadd = prev;
                            prev = newparent;
                        }
                    } while (grandpa != null && !grandpa.mayContainBlocks());
                        
                    if (grandpa != null)
                    {
                        //queue the block box and the next twin to be put to the block level
                        iparent.postadd = new Vector<Box>(2);
                        iparent.postadd.add(newbox);
                        if (iparent.nextTwin != null)
                            iparent.postadd.add(iparent.nextTwin);
                        stat.lastinflow = null; //we have started a new block box
                    }
                    else
                        log.error("(internal error) grandpa is missing for %s", newbox);
                }
            }
            else //positioned box
            {
                ((BlockBox) newbox).domParent = newbox.getParent(); //set the DOM parent
                ((BlockBox) newbox).absReference = stat.lastinflow; //set the reference box for computing the static position
                newbox.getContainingBlockBox().addSubBox(newbox);
            }
        }
        else //inline elements -- always in flow
        {
            //spaces may be collapsed when the last inflow box ends with a whitespace and it allows collapsing whitespaces
            boolean lastwhite = (stat.lastinflow == null) || stat.lastinflow.isBlock() || (stat.lastinflow.endsWithWhitespace() && stat.lastinflow.collapsesSpaces());
            //the new box may be collapsed if it allows collapsing whitespaces and it is a whitespace
            boolean collapse = lastwhite && newbox.isWhitespace() && newbox.collapsesSpaces() && !newbox.isSticky() && !(newbox instanceof InlineBlockBox);
            if (!collapse)
            {
                stat.parent.addSubBox(newbox);
                stat.lastinflow = newbox;
            }
            else
                newbox.setContainingBlockBox(null); //indicate that the box is not part of the box tree (collapsed)
        }
        
        //Recursively process the eventual boxes that should be added tohether with the new box
        if (newbox instanceof ElementBox && ((ElementBox) newbox).postadd != null)
        {
            for (Box box : ((ElementBox) newbox).postadd)
                addToTree(box, stat);
            ((ElementBox) newbox).postadd = null; //don't need to keep this anymore
        }
        
    }
    
    /**
     * Removes the block box trailing inline whitespace child boxes if allowed by the white-space values. 
     * @param block the block box to be processed
     */
    private void removeTrailingWhitespaces(ElementBox block)
    {
        if (block.collapsesSpaces())
        {
            for (ListIterator<Box> it = block.getSubBoxList().listIterator(block.getSubBoxNumber()); it.hasPrevious();)
            {
                Box subbox = it.previous();
                if (subbox.isInFlow())
                {
                    if (!subbox.isBlock() && subbox.collapsesSpaces())
                    {
                        if (subbox.isWhitespace() && !(subbox instanceof InlineBlockBox))
                            it.remove();
                        else if (subbox instanceof ElementBox)
                        {
                            removeTrailingWhitespaces((ElementBox) subbox);
                            break; //the whole box is not whitespace
                        }
                        else if (subbox instanceof TextBox)
                        {
                            ((TextBox) subbox).removeTrailingWhitespaces();
                            break;
                        }
                    }
                    else
                        break;
                }
            }
            block.setEndChild(block.getSubBoxList().size());
        }
    }
    
    /**
     * Creates a new box for an element node and sets the containing boxes accordingly.
     * @param n The element node
     * @param stat The box tree creation status used for obtaining the containing boxes
     * @return the newly created element box
     */
    public ElementBox createElementBox(Element n, BoxTreeCreationStatus stat)
    {
        ElementBox ret = createBox(stat.parent, n, null);
        ret.setClipBlock(stat.clipbox);
        if (ret.isBlock())
        {
            BlockBox block = (BlockBox) ret; 
            //Setup my containing box
            if (block.position == BlockBox.POS_ABSOLUTE)
                ret.setContainingBlockBox(stat.absbox);
            else if (block.position == BlockBox.POS_FIXED)
                ret.setContainingBlockBox(viewport);
            else    
                ret.setContainingBlockBox(stat.contbox);
        }
        else    
            ret.setContainingBlockBox(stat.contbox);
        
        //mark the root visual context
        if (n.getOwnerDocument().getDocumentElement() == n)
            ret.getVisualContext().makeRootContext();
        
        return ret;
    }
    
    /**
     * Creates a new box for a text node and sets the containing boxes accordingly.
     * @param n The element node
     * @param stat Current box tree creation status for obtaining the containing boxes
     * @return the newly created text box
     */
    private TextBox createTextBox(Text n, BoxTreeCreationStatus stat)
    {
        //TODO: in some whitespace processing modes, multiple boxes may be created
        TextBox text = new TextBox(n, stat.parent.getVisualContext().create());
        text.setOrder(next_order++);
        text.setContainingBlockBox(stat.contbox);
        text.setClipBlock(stat.clipbox);
        text.setViewport(viewport);
        text.setBase(baseurl);
        text.setParent(stat.parent);
        return text;
    }

    /**
     * Checks the newly created box and creates anonymous block boxes above the children if necessary.
     * @param root the box to be checked
     * @return the modified root box
     */
    private ElementBox normalizeBox(ElementBox root)
    {
        //anonymous inline and block elements if necessary
        if (root.mayContainBlocks() && ((BlockBox) root).containsBlocks())
            createAnonymousBlocks((BlockBox) root);
        else if (root.containsMixedContent())
            createAnonymousInline(root);
        //generate missing child wrappers
        // https://www.w3.org/TR/CSS22/tables.html#anonymous-boxes
        if (root.getDisplay() == ElementBox.DISPLAY_TABLE || root.getDisplay() == ElementBox.DISPLAY_INLINE_TABLE)
            createAnonymousWrappers(root, "tr", "table-row", properTableChild);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_ROW_GROUP)
            createAnonymousWrappers(root, "tr", "table-row", properTableRowGroupChild);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_ROW)
            createAnonymousWrappers(root, "td", "table-cell", properTableRowChild);
        //table cells require a row parent
        createAnonymousBoxes(root, 
                             ElementBox.DISPLAY_TABLE_CELL,
                             ElementBox.DISPLAY_TABLE_ROW, ElementBox.DISPLAY_ANY, ElementBox.DISPLAY_ANY, 
                             "tr", "table-row");
        //table rows require a group parent
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_ROW,
                             ElementBox.DISPLAY_TABLE_ROW_GROUP, ElementBox.DISPLAY_TABLE_HEADER_GROUP, ElementBox.DISPLAY_TABLE_FOOTER_GROUP, 
                             "tbody", "table-row-group");
        //table columns require a table parent
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_COLUMN,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_INLINE_TABLE, ElementBox.DISPLAY_TABLE_COLUMN_GROUP,
                             "table", "table");
        //table row groups require a table parent
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_ROW_GROUP,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_INLINE_TABLE, ElementBox.DISPLAY_ANY,
                             "table", "table");
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_HEADER_GROUP,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_INLINE_TABLE, ElementBox.DISPLAY_ANY,
                             "table", "table");
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_FOOTER_GROUP,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_INLINE_TABLE, ElementBox.DISPLAY_ANY,
                             "table", "table");
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_CAPTION,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_INLINE_TABLE, ElementBox.DISPLAY_ANY,
                             "table", "table");
        return root;
    }
    
    /**
     * Creates anonymous inline boxes if the a block box contains both the inline
     * and the text child boxes. The child boxes of the specified root
     * are processed and the text boxes are grouped in a newly created
     * anonymous <code>span</code> boxes.
     * @param root the root box
     */
    private void createAnonymousInline(ElementBox root)
    {
        Vector<Box> nest = new Vector<Box>();
        for (int i = 0; i < root.getSubBoxNumber(); i++)
        {
            Box sub = root.getSubBox(i);
            if (sub instanceof ElementBox)
                nest.add(sub);
            else
            {
                ElementBox anbox = createAnonymousBox(root, sub, false);
                anbox.addSubBox(sub);
                nest.add(anbox);
            }   
        }
        root.nested = nest;
        root.endChild = nest.size();
    }
    
    /**
     * Creates anonymous block boxes if the a block box contains both the inline
     * and the block child boxes. The child boxes of the specified root
     * are processed and the inline boxes are grouped in a newly created
     * anonymous <code>div</code> boxes.
     * @param root the root box
     */
    private void createAnonymousBlocks(BlockBox root)
    {
        Vector<Box> nest = new Vector<Box>();
        ElementBox adiv = null;
        for (int i = 0; i < root.getSubBoxNumber(); i++)
        {
            Box sub = root.getSubBox(i);
            if (sub.isBlock())
            {
                if (adiv != null && !adiv.isempty)
                {
                    normalizeBox(adiv); //normalize even the newly created blocks
                    removeTrailingWhitespaces(adiv);
                }
                adiv = null;
                nest.add(sub);
            }
            else if (adiv != null || !(sub instanceof InlineBox) || !sub.isWhitespace()) //omit whitespace inline boxes at the beginning of the blocks
            {
                if (adiv == null)
                {
                    adiv = createAnonymousBox(root, sub, true);
                    nest.add(adiv);
                }
                if (sub.isDisplayed() && !sub.isEmpty()) 
                { 
                    adiv.isempty = false;
                    adiv.displayed = true;
                }
                adiv.addSubBox(sub);
            }
            else
                sub.setContainingBlockBox(null);
        }
        if (adiv != null && !adiv.isempty)
        {
            normalizeBox(adiv); //normalize even the newly created blocks
            removeTrailingWhitespaces(adiv);
        }
        root.nested = nest;
        root.endChild = nest.size();
    }
    
    private void createAnonymousWrappers(ElementBox root, String name, String display, Set<CSSProperty.Display> allowed)
    {
        Vector<Box> nest = new Vector<Box>();
        ElementBox adiv = null;
        for (int i = 0; i < root.getSubBoxNumber(); i++)
        {
            Box sub = root.getSubBox(i);
            if (sub.isBlock() && allowed.contains(((BlockBox) sub).getDisplay()))
            {
                if (adiv != null)
                {
                    normalizeBox(adiv); //normalize even the newly created blocks
                    if (!adiv.isempty)
                        removeTrailingWhitespaces(adiv);
                }
                adiv = null;
                nest.add(sub);
            }
            else if (adiv != null || !(sub instanceof InlineBox) || !sub.isWhitespace()) //omit whitespace inline boxes at the beginning of the blocks
            {
                if (adiv == null)
                {
                    Element elem = createAnonymousElement(root.getElement().getOwnerDocument(), name, display);
                    adiv = createBox(root, elem, display);
                    adiv.isblock = true;
                    adiv.isempty = true;
                    adiv.setContainingBlockBox(sub.getContainingBlockBox());
                    adiv.setClipBlock(sub.getClipBlock());
                    nest.add(adiv);
                }
                if (sub.isDisplayed() && !sub.isEmpty()) 
                { 
                    adiv.isempty = false;
                    adiv.displayed = true;
                }
                adiv.addSubBox(sub);
                sub.setContainingBlockBox(adiv);
            }
            else
                sub.setContainingBlockBox(null);
        }
        if (adiv != null)
        {
            normalizeBox(adiv); //normalize even the newly created blocks
            if (!adiv.isempty)
                removeTrailingWhitespaces(adiv);
        }
        root.nested = nest;
        root.endChild = nest.size();
    }

    /**
     * Checks the child boxes of the specified root box wheter they require creating an anonymous
     * parent box.
     * @param root the box whose child boxes are checked
     * @param type the required display type of the child boxes. The remaining child boxes are skipped.
     * @param reqtype1 the first required display type of the root. If the root type doesn't correspond
     *      to any of the required types, an anonymous parent is created for the selected children.
     * @param reqtype2 the second required display type of the root.
     * @param reqtype3 the third required display type of the root.
     * @param name the element name of the created anonymous box
     * @param display the display type of the created anonymous box
     */
    private void createAnonymousBoxes(ElementBox root, 
                                      CSSProperty.Display type,
                                      CSSProperty.Display reqtype1, 
                                      CSSProperty.Display reqtype2, 
                                      CSSProperty.Display reqtype3, 
                                      String name, String display)
    {
        if (root.getDisplay() != reqtype1 && root.getDisplay() != reqtype2 && root.getDisplay() != reqtype3)
        {
            Vector<Box> nest = new Vector<Box>();
            ElementBox adiv = null;
            for (int i = 0; i < root.getSubBoxNumber(); i++)
            {
                Box sub = root.getSubBox(i);
                if (sub instanceof BlockBox && ((BlockBox) sub).isPositioned())
                {
                    //positioned boxes are left untouched
                    nest.add(sub);
                }
                else if (sub instanceof ElementBox)
                {
                    ElementBox subel = (ElementBox) sub;
                    if (subel.getDisplay() != type)
                    {
                        adiv = null;
                        nest.add(sub);
                    }
                    else
                    {
                        if (adiv == null)
                        {
                            Element elem = createAnonymousElement(root.getElement().getOwnerDocument(), name, display);
                            adiv = createBox(root, elem, display);
                            adiv.isblock = true;
                            adiv.isempty = true;
                            adiv.setContainingBlockBox(sub.getContainingBlockBox());
                            adiv.setClipBlock(sub.getClipBlock());
                            nest.add(adiv);
                        }
                        if (sub.isDisplayed() && !sub.isEmpty()) 
                        { 
                            adiv.isempty = false;
                            adiv.displayed = true;
                        }
                        sub.setParent(adiv);
                        sub.setContainingBlockBox(adiv);
                        adiv.addSubBox(sub);
                    }
                }
                else
                    return; //first box is TextBox => all the boxes are TextBox, nothing to do. 
            }
            root.nested = nest;
            root.endChild = nest.size();
        }
    }
    
    /**
     * Creates an empty anonymous block or inline box that can be placed between an optional parent and its child.
     * The corresponding properties of the box are taken from the child. The child is inserted NOT as the child box of the new box. 
     * The new box is NOT inserted as a subbox of the parent. 
     * @param parent an optional parent node. When used, the parent of the new box is set to this node and the style is inherited from the parent. 
     * @param child the child node
     * @param block when set to <code>true</code>, a {@link BlockBox} is created. Otherwise, a {@link InlineBox} is created.
     * @return the new created block box
     */
    protected ElementBox createAnonymousBox(ElementBox parent, Box child, boolean block)
    {
        ElementBox anbox;
        if (block) {
            Element anelem = createAnonymousElement(child.getNode().getOwnerDocument(), "Xdiv", "block");
            if (parent.display == ElementBox.DISPLAY_GRID) {
                anbox = new GridItem(anelem, child.getVisualContext().create());
            } else if (parent.display == ElementBox.DISPLAY_FLEX) {
                anbox = new FlexItem(anelem, child.getVisualContext().create());
            } else {
                anbox = new BlockBox(anelem, child.getVisualContext().create());
            }
            anbox.setViewport(viewport);
            anbox.setStyle(createAnonymousStyle("block"));
            ((BlockBox) anbox).contblock = false;
            anbox.isblock = true;
        }
        else
        {
            Element anelem = createAnonymousElement(child.getNode().getOwnerDocument(), "Xspan", "inline");
            anbox = new InlineBox(anelem, child.getVisualContext().create());
            anbox.setViewport(viewport);
            anbox.setStyle(createAnonymousStyle("inline"));
            anbox.isblock = false;
        }
        if (parent != null)
        {
            computeInheritedStyle(anbox, parent);
            anbox.setParent(parent);
        }
        anbox.setOrder(next_order++);
        anbox.isempty = true;
        anbox.setBase(child.getBase());
        anbox.setContainingBlockBox(child.getContainingBlockBox());
        anbox.setClipBlock(child.getClipBlock());
        return anbox;
    }
    
    /**
     * Creates a single new box from an element.
     * @param n The source DOM element
     * @param display the display: property value that is used when the box style is not known (e.g. anonymous boxes)
     * @return A new box of a subclass of {@link ElementBox} based on the value of the 'display' CSS property
     */
    public ElementBox createBox(ElementBox parent, Element n, String display)
    {
        ElementBox root = null;
        
        //New box style
        NodeData style = decoder.getElementStyleInherited(n);
        if (style == null)
                style = createAnonymousStyle(display);
        
        //Special (HTML) tag names
        if (config.getUseHTML() && html.isTagSupported(n))
        {
            root = html.createBox(parent, n, viewport, style);
        }
        //Not created yet -- create a box according to the display value
        if (root == null)
        {
            root = createElementInstance(parent, n, style);
        }
        root.setBase(baseurl);
        root.setViewport(viewport);
        root.setParent(parent);
        root.setOrder(next_order++);
        return root;
    }

    /**
     * Creates a new box for a pseudo-element.
     * @param box the parent box of the pseudo element
     * @param pseudo The pseudo element name
     * @return A new box of a subclass of ElementBox based on the value of the 'display' CSS property
     */
    private Node createPseudoElement(ElementBox box, PseudoElementType pseudo, Counters counters) 
    {
        Element n = box.getElement();
        //New box style
        NodeData style = decoder.getElementStyleInherited(n, pseudo);
        if (style != null)
        {
            counters.applyStyle(style);
            TermList cont = style.getValue(TermList.class, "content");
            if (cont != null && cont.size() > 0)
            {
                //create the DOM tree for the pseudo element
                //parent
                Element pelem = createAnonymousElement(n.getOwnerDocument(), "XPspan", "inline"); 
                //content elements
                for (Term<?> c : cont)
                {
                    if (c instanceof TermIdent)
                    {
                    }
                    else if (c instanceof TermString)
                    {
                        Text txt = n.getOwnerDocument().createTextNode(((TermString) c).getValue());
                        pelem.appendChild(txt);
                    }
                    else if (c instanceof TermURI)
                    {
                    }
                    else if (c instanceof TermFunction.Attr)
                    {
                        final TermFunction.Attr f = (TermFunction.Attr) c;
                        String val = HTMLNorm.getAttribute(n, f.getName());
                        Text txt = n.getOwnerDocument().createTextNode(val);
                        pelem.appendChild(txt);
                    }
                    else if (c instanceof TermFunction.Counter)
                    {
                        final TermFunction.Counter f = (TermFunction.Counter) c;
                        Integer val = counters.getCounter(f.getName());
                        CSSProperty.ListStyleType cstyle = f.getStyle();
                        if (cstyle == null)
                            cstyle = CSSProperty.ListStyleType.DECIMAL;
                        Text txt = n.getOwnerDocument().createTextNode(ListItemBox.formatItemNumber(val, cstyle));
                        pelem.appendChild(txt);
                    }
                    else if (c instanceof TermFunction.Counters)
                    {
                        final TermFunction.Counters f = (TermFunction.Counters) c;
                        CSSProperty.ListStyleType cstyle = f.getStyle();
                        if (cstyle == null)
                            cstyle = CSSProperty.ListStyleType.DECIMAL;
                        final String sep = f.getSeparator();
                        final List<Integer> items = counters.getCounters(f.getName());
                        String val = "";
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (i != 0)
                                val += sep;
                            val += ListItemBox.formatItemNumber(items.get(i), cstyle);
                        }
                        final Text txt = n.getOwnerDocument().createTextNode(val);
                        pelem.appendChild(txt);
                    }
                }

                //use the pseudo element style for the new (main) element
                decoder.useStyle(pelem, null, style);

                return pelem;
            }
            else
                return null; //no contents
        }
        else
            return null; //no pseudo declaration
    }
    
    /**
     * Creates an instance of ElementBox. According to the display: property of the style, the appropriate
     * subclass of ElementBox is created (e.g. BlockBox, TableBox, etc.)
     * @param n The source DOM element
     * @param style Style definition for the node
     * @return The created instance of ElementBox
     */
    public ElementBox createElementInstance(ElementBox parent, Element n, NodeData style)
    {
        ElementBox root = new InlineBox(n, parent.getVisualContext().create());
        root.setViewport(viewport);
        root.setStyle(style);

        if (root.getDisplay() == ElementBox.DISPLAY_GRID)
            root = new GridBox((InlineBox) root);
        else if(root.getDisplay() == ElementBox.DISPLAY_FLEX)
            root = new FlexBox((InlineBox) root);


        else if (root.getDisplay() == ElementBox.DISPLAY_LIST_ITEM)
            root = new ListItemBox((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE)
            root = new BlockTableBox((InlineBox) root);
        /*else if (root.getDisplay() == ElementBox.DISPLAY_INLINE_TABLE)
            root = new InlineTableBox((InlineBox) root);*/
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_CAPTION)
            root = new TableCaptionBox((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_ROW_GROUP
                 || root.getDisplay() == ElementBox.DISPLAY_TABLE_HEADER_GROUP
                 || root.getDisplay() == ElementBox.DISPLAY_TABLE_FOOTER_GROUP)
            root = new TableBodyBox((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_ROW)
            root = new TableRowBox((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_CELL)
            root = new TableCellBox((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_COLUMN)
            root = new TableColumn((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_TABLE_COLUMN_GROUP)
            root = new TableColumnGroup((InlineBox) root);
        else if (root.getDisplay() == ElementBox.DISPLAY_INLINE_BLOCK)
            root = new InlineBlockBox((InlineBox) root);
//        else if (root.getDisplay() == ElementBox.DISPLAY_INLINE_GRID)
//            root = new InlineGridBox((InlineBox) root);
//        else if (root.getDisplay() == ElementBox.DISPLAY_INLINE_FLEX)
//            root = new InlineFlexBox((InlineBox) root);
        else if (parent instanceof GridBox)
            root = new GridItem((InlineBox) root);
        else if (parent instanceof FlexBox)
            root = new FlexItem((InlineBox) root);
        else if (root.isBlock())
            root = new BlockBox((InlineBox) root);
        return root;
    }
    
    /**
     * Creates a new DOM element that represents an anonymous box in a document.
     * @param doc the document
     * @param name the anonymous element name (generally arbitrary)
     * @param display the display style value for the block
     * @return the new element
     */
    public Element createAnonymousElement(Document doc, String name, String display)
    {
        Element div = doc.createElement(name);
        div.setAttribute("class", "Xanonymous");
        div.setAttribute("style", "display:" + display);
        return div;
    }
    
    /**
     * Creates the style definition for an anonymous box. It contains only the class name set to "Xanonymous"
     * and the display: property set according to the parametres.
     * @param display <code>display:</code> property value of the resulting style.
     * @return Resulting style definition
     */
    public NodeData createAnonymousStyle(String display)
    {
        NodeData ret = CSSFactory.createNodeData();
        
        Declaration cls = CSSFactory.getRuleFactory().createDeclaration();
        cls.unlock();
        cls.setProperty("class");
        cls.add(CSSFactory.getTermFactory().createString("Xanonymous"));
        ret.push(cls);
        
        Declaration disp = CSSFactory.getRuleFactory().createDeclaration();
        disp.unlock();
        disp.setProperty("display");
        disp.add(CSSFactory.getTermFactory().createIdent(display));
        ret.push(disp);
        
        return ret;
    }
    
    /**
     * Computes the style of a node based on its parent using the CSS inheritance.
     * @param dest the box whose style should be computed
     * @param parent the parent box
     */ 
    private void computeInheritedStyle(ElementBox dest, ElementBox parent)
    {
        NodeData newstyle = dest.getStyle().inheritFrom(parent.getStyle()); 
        dest.setStyle(newstyle);
    }
    
}

/**
 * The box tree creation status holds all the ancestor boxes that might be necessary for creating the child boxes
 * and adding them to the resulting tree
 *
 * @author burgetr
 */
class BoxTreeCreationStatus
{
    /** Normal flow parent box */
    public ElementBox parent;
    
    /** Containing block for normal flow */
    public BlockBox contbox;
    
    /** Containing block for absolutely positioned boxes */
    public BlockBox absbox;
    
    /** Clipping box based on overflow property */
    public BlockBox clipbox;
    
    /** Last in-flow box */
    public Box lastinflow;
    
    /** The index of the DOM node within its parent node */
    public int curchild;
    
    public Counters counters;
    
    /** 
     * Creates a new initial creation status
     * @param viewport the root viewport box
     */
    public BoxTreeCreationStatus(Viewport viewport)
    {
        parent = contbox = absbox = clipbox = viewport;
        lastinflow = null;
        curchild = 0;
        counters = new Counters();
    }
    
    /** 
     * Creates a copy of the status
     * @param stat original status
     */
    public BoxTreeCreationStatus(BoxTreeCreationStatus stat)
    {
        this.parent = stat.parent;
        this.contbox = stat.contbox;
        this.absbox = stat.absbox;
        this.clipbox = stat.clipbox;
        this.lastinflow = stat.lastinflow;
        this.curchild = stat.curchild;
        this.counters =stat.counters;
    }
    
}
