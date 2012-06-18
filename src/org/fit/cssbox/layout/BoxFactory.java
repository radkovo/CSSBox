/*
 * BoxFactory.java
 * Copyright (c) 2005-2010 Radek Burget
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

import java.awt.Graphics2D;
import java.net.URL;
import java.util.ListIterator;
import java.util.Vector;

import org.fit.cssbox.css.DOMAnalyzer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.css.Selector.PseudoDeclaration;


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
    protected BrowserConfig config;
    
    protected DOMAnalyzer decoder;
    protected URL baseurl;
    protected Viewport viewport;

    protected int next_order;
    
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
        this.config = new BrowserConfig();
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
     * Reset the factory for creating a new tree.
     */
    public void reset()
    {
        next_order = 0;
    }
    
    /**
     * Create the viewport and the underlying box tree from a DOM tree.
     * 
     * @param root the root element of the source DOM tree.
     * @param g the root graphic context. Copies of this context will be used for the individual boxes. 
     * @param ctx the visual context (computed style). Copies of this context will be used for the individual boxes.
     * @param width preferred viewport width.
     * @param height preferred viewport height.
     * @return the created viewport box with the corresponding box subtrees.
     */
    public Viewport createViewportTree(Element root, Graphics2D g, VisualContext ctx, int width, int height)
    {
        Element vp = createAnonymousElement(root.getOwnerDocument(), "Xdiv", "block");
        viewport = new Viewport(vp, g, ctx, this, root, width, height);
        viewport.setConfig(config);
        BoxTreeCreationStatus stat = new BoxTreeCreationStatus(viewport);
        createSubtree(root, stat);
        System.out.println("Root box is: " + viewport.getRootBox());
        
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
                    addToTree(stat.parent.preadd, stat);
                
                //create :before elements
                if (stat.parent.previousTwin == null)
                {
                    Node n = createPseudoElement(stat.parent, PseudoDeclaration.BEFORE);
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
                    if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE)
                    {
                        stat.curchild = child;
                        createSubtree(n, stat);
                    }
                }
                
                //create :after elements
                if (stat.parent.nextTwin == null)
                {
                    Node n = createPseudoElement(stat.parent, PseudoDeclaration.AFTER);
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
            if (((ElementBox) newbox).mayContainBlocks()) //the new box forms a block context
            {
                BlockBox block = (BlockBox) newbox;
                //A positioned box forms a content box for following absolutely
                //positioned boxes
                if (block.position == BlockBox.POS_ABSOLUTE ||
                    block.position == BlockBox.POS_RELATIVE ||
                    block.position == BlockBox.POS_FIXED)
                     newstat.absbox = block;                
                //Any block box forms a containing box for not positioned elements
                newstat.contbox = block;
                //A box with overflow:hidden creates a clipping box
                if (block.overflow == BlockBox.OVERFLOW_HIDDEN)
                    newstat.clipbox = block;
                //Last inflow box is local for block boxes
                newstat.lastinflow = null;
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
                    }
                    else
                        System.err.println("BoxFactory: warning: grandpa is missing for " + newbox);
                }
            }
            else //positioned box
            {
                newbox.getContainingBlock().addSubBox(newbox);
                ((BlockBox) newbox).absReference = stat.lastinflow; //set the reference box for computing the static position
            }
        }
        else //inline elements -- always in flow
        {
            //System.out.println("For " + newbox + " lastbox is " + lastinflow);
            //spaces may be collapsed when the last inflow box ends with a whitespace and it allows collapsing whitespaces
            boolean lastwhite = (stat.lastinflow == null) || stat.lastinflow.isBlock() || (stat.lastinflow.endsWithWhitespace() && stat.lastinflow.collapsesSpaces());
            //the new box may be collapsed if it allows collapsing whitespaces and it is a whitespace
            boolean collapse = lastwhite && newbox.isWhitespace() && newbox.collapsesSpaces();
            if (!collapse)
            {
                stat.parent.addSubBox(newbox);
                stat.lastinflow = newbox;
            }
        }
        
        //Recursively process the eventual boxes that should be added tohether with the new box
        if (newbox instanceof ElementBox && ((ElementBox) newbox).postadd != null)
        {
            for (Box box : ((ElementBox) newbox).postadd)
                addToTree(box, stat);
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
                        if (subbox.isWhitespace())
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
        ElementBox ret = createBox(stat.parent, (Element) n, null);
        ret.setClipBlock(stat.clipbox);
        if (ret.isBlock())
        {
            BlockBox block = (BlockBox) ret; 
            //Setup my containing box
            if (block.position == BlockBox.POS_ABSOLUTE || block.position == BlockBox.POS_FIXED)
                ret.setContainingBlock(stat.absbox);
            else    
                ret.setContainingBlock(stat.contbox);
        }
        else    
            ret.setContainingBlock(stat.contbox);
        
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
        TextBox text = new TextBox(n, (Graphics2D) stat.parent.getGraphics().create(), stat.parent.getVisualContext().create());
        text.setOrder(next_order++);
        text.setContainingBlock(stat.contbox);
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
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_TABLE_COLUMN_GROUP, ElementBox.DISPLAY_ANY,
                             "table", "table");
        //table row groups require a table parent
        createAnonymousBoxes(root,
                             ElementBox.DISPLAY_TABLE_ROW_GROUP,
                             ElementBox.DISPLAY_TABLE, ElementBox.DISPLAY_ANY, ElementBox.DISPLAY_ANY,
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
            else if (adiv != null || !sub.isWhitespace()) //omit whitespace boxes at the beginning of the blocks
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
        }
        if (adiv != null && !adiv.isempty)
        {
            normalizeBox(adiv); //normalize even the newly created blocks
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
                if (sub instanceof ElementBox)
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
                            adiv.setContainingBlock(sub.getContainingBlock());
                            adiv.setClipBlock(sub.getClipBlock());
                            nest.add(adiv);
                        }
                        if (sub.isDisplayed() && !sub.isEmpty()) 
                        { 
                            adiv.isempty = false;
                            adiv.displayed = true;
                        }
                        sub.setParent(adiv);
                        sub.setContainingBlock((BlockBox) adiv);
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
        if (block)
        {
            Element anelem = createAnonymousElement(child.getNode().getOwnerDocument(), "Xdiv", "block");
            anbox = new BlockBox(anelem, (Graphics2D) child.getGraphics().create(), child.getVisualContext().create());
            anbox.setViewport(viewport);
            anbox.setStyle(createAnonymousStyle("block"));
            ((BlockBox) anbox).contblock = false;
            anbox.isblock = true;
        }
        else
        {
            Element anelem = createAnonymousElement(child.getNode().getOwnerDocument(), "Xspan", "inline");
            anbox = new InlineBox(anelem, (Graphics2D) child.getGraphics().create(), child.getVisualContext().create());
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
        anbox.setContainingBlock(child.getContainingBlock());
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
        ElementBox root;
        
        //New box style
        NodeData style = decoder.getElementStyleInherited(n);
        if (style == null)
                style = createAnonymousStyle(display);
        
        //Special tag names
        if (config.getUseHTML() && n.getNodeName().equals("img"))
        {
            InlineReplacedBox rbox = new InlineReplacedBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
            rbox.setViewport(viewport);
            rbox.setStyle(style);
            rbox.setContentObj(new ReplacedImage(rbox, rbox.getVisualContext(), baseurl));
            root = rbox;
            if (root.isBlock())
                root = new BlockReplacedBox(rbox);
        }
        //Create a box according to the <code>display</code> value
        else
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
    private Node createPseudoElement(ElementBox box, Selector.PseudoDeclaration pseudo) 
    {
        Element n = box.getElement();
        //New box style
        NodeData style = decoder.getElementStyleInherited(n, pseudo);
        if (style != null)
        {
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
                    else if (c instanceof TermFunction)
                    {
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
    private ElementBox createElementInstance(ElementBox parent, Element n, NodeData style)
    {
        ElementBox root = new InlineBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
        root.setViewport(viewport);
        root.setStyle(style);
        if (root.getDisplay() == ElementBox.DISPLAY_LIST_ITEM)
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
    int curchild;
    
    /** 
     * Creates a new initial creation status
     * @param viewport the root viewport box
     */
    public BoxTreeCreationStatus(Viewport viewport)
    {
        parent = contbox = absbox = clipbox = viewport;
        lastinflow = null;
        curchild = 0;
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
    }
    
}
