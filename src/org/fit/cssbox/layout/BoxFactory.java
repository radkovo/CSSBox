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
 * <p>Usually, a single factory is created using the constructor. The last created factory is then accessible using
 * the {@link #getInstance()} method.
 * 
 * @author burgetr
 */
public class BoxFactory
{
    private static BoxFactory instance = null; //last created instance
    
    /** whether to use HTML */
    private boolean useHTML = true;
    
    protected DOMAnalyzer decoder;
    protected URL baseurl;
    protected Viewport viewport;

    protected int next_order;
    
    /**
     * Create a new factory. From this point, the new factory will be accessible using the {@link #getInstance()} method.
     * @param decoder The CSS decoder used for obtaining the DOM styles.
     * @param baseurl Base URL used for completing the relative URLs in the document.
     */
    public BoxFactory(DOMAnalyzer decoder, URL baseurl)
    {
        this.decoder = decoder;
        this.baseurl = baseurl;
        this.next_order = 0;
        instance = this;
    }
    
    /**
     * Get the latest created instance of the factory.
     * @return A box factory object or <code>null</code> when no factory has been created yet.
     */
    public static BoxFactory getInstance()
    {
        return instance;
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
        this.useHTML = useHTML;
    }
    
    /**
     * Checks if the HTML extensions are enabled for the factory.
     * @return <code>true</code> if the HTML extensions are enabled
     * @see #setUseHTML(boolean) 
     */
    public boolean getUseHTML()
    {
        return useHTML;
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
        
        createSubtree(viewport, viewport, viewport, viewport, null, root, 0);
        System.out.println("Root box is: " + viewport.getRootBox());
        
        return viewport;
    }
    
    /**
     * Creates the box subtrees for all the child nodes of the DOM node corresponding to the specified parent box. Recursively creates the child boxes 
     * from the child nodes.
     * @param parent the parent box
     * @param contbox the containing box of the new box when not absolutley positioned
     * @param absbox the containing box of the new box when absolutley positioned
     * @param clipbox the clipping block of this subtree
     */
    public void createBoxTree(ElementBox parent, BlockBox contbox, BlockBox absbox, BlockBox clipbox)
    {
        NodeList children = parent.getElement().getChildNodes();
        if (parent.isDisplayed())
        {
            //a reference box for possible absolutely positioned boxes
            //normally, it is the previous in-flow box, or null, if this is the first box
            Box inflow_reference = null;
            
            //create :before elements
            if (parent.previousTwin == null)
            {
                Node n = createPseudoElement(parent, PseudoDeclaration.BEFORE);
                if (n != null && (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE))
                    inflow_reference = createSubtree(parent, contbox, absbox, clipbox, inflow_reference, n, -1);
            }
            
            //create normal elements
            for (int child = parent.firstDOMChild; child < parent.lastDOMChild; child++)
            {
                Node n = children.item(child);
                if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE)
                    inflow_reference = createSubtree(parent, contbox, absbox, clipbox, inflow_reference, n, child);
            }
            
            //create :after elements
            if (parent.nextTwin == null)
            {
                Node n = createPseudoElement(parent, PseudoDeclaration.AFTER);
                if (n != null && (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE))
                    inflow_reference = createSubtree(parent, contbox, absbox, clipbox, inflow_reference, n, children.getLength());
            }
            
            normalizeBox(parent);
        }
    }

    /**
     * Creates a subtree of a parent box that corresponds to a single child DOM node of this box. During the creation, the in-flow boxes
     * are tracked for computing the static positions of absolutely positioned boxes.
     * 
     * @param parent parent node of the subtree being created
     * @param contbox the containing box of the new box when not absolutley positioned
     * @param absbox the containing box of the new box when absolutley positioned
     * @param clipbox the clipping block of this subtree
     * @param inflow_reference the last in-flow box before this subree
     * @param n the root DOM node of the subtree being created
     * @param child_index the index of the DOM node within its parent node
     * @return the new value of inflow_reference, i.e. the last in-flow box
     */
    private Box createSubtree(ElementBox parent, BlockBox contbox, BlockBox absbox, BlockBox clipbox, Box inflow_reference, Node n, int child_index)
    {
        //Create the new box for the child
        Box newbox;
        if (n.getNodeType() == Node.TEXT_NODE)
            newbox = createTextBox(parent, (Text) n, contbox, clipbox);
        else
            newbox = createElementBox(parent, (Element) n, contbox, absbox, clipbox);
        
        ElementBox newparent = null;
        
        //Add the new box to the parent according to its type
        if (newbox.isBlock())  
        {
            if (newbox.isInFlow())
            {
                if (parent.isBlock()) //block in block
                {
                    parent.addSubBox(newbox);
                    inflow_reference = newbox;
                }
                else //block in inline box -- split the inline box
                {
                    ElementBox grandpa = parent.getParent();
                    if (grandpa != null)
                    {
                        //finish inline parent and create another one
                        parent.lastDOMChild = child_index; //this will finish the iteration just now
                        newparent = parent.copyBox();
                        newparent.removeAllSubBoxes();
                        newparent.firstDOMChild = child_index + 1;
                        //put the new block at the same level as the parent
                        grandpa.addSubBox(newbox);
                    }
                    else
                        System.err.println("BoxFactory: warning: grandpa is missing for " + newbox);
                }
            }
            else //out-of-flow box
            {
                newbox.getContainingBlock().addSubBox(newbox);
                ((BlockBox) newbox).absReference = inflow_reference; //set the reference box for computing the static position
            }
        }
        else //inline elements -- always in flow
        {
            parent.addSubBox(newbox);
            inflow_reference = newbox;
        }

        if (newbox instanceof ElementBox) 
        {
            //Determine the containing boxes of the children
            BlockBox newcont = contbox;
            BlockBox newabs = absbox;
            BlockBox newclip = clipbox;
            if (newbox.isBlock())
            {
                BlockBox block = (BlockBox) newbox;
                //A positioned box forms a content box for following absolutely
                //positioned boxes
                if (block.position == BlockBox.POS_ABSOLUTE ||
                    block.position == BlockBox.POS_RELATIVE ||
                    block.position == BlockBox.POS_FIXED)
                     newabs = block;                
                //Any block box forms a containing box for not positioned elements
                newcont = block;
                //A box with overflow:hidden creates a clipping box
                if (block.overflow == BlockBox.OVERFLOW_HIDDEN)
                    newclip = block;
            }
            createBoxTree((ElementBox) newbox, newcont, newabs, newclip);
        }

        if (newparent != null && newparent.firstDOMChild < newparent.lastDOMChild)
        {
            //put another parent for the rest on the same level
            parent.getParent().addSubBox(newparent);
            parent.nextTwin = newparent;
            newparent.previousTwin = parent;
            //process the new parent
            createBoxTree(newparent, contbox, absbox, clipbox);
            //if the new parent generated no children, remove it again
            if (newparent.getSubBoxNumber() == 0)
                parent.getParent().removeSubBox(newparent);
        }
        
        return inflow_reference;
    }

    /**
     * Creates a new box for an element node and sets the containing boxes accordingly.
     * @param parent the parent box of the created box
     * @param n The element node
     * @param contbox the containing box of the new box when not absolutley positioned
     * @param absbox the containing box of the new box when absolutley positioned
     * @param clipbox the clipping block of this subtree
     * @return the newly created element box
     */
    public ElementBox createElementBox(ElementBox parent, Element n, BlockBox contbox, BlockBox absbox, BlockBox clipbox)
    {
        ElementBox ret = createBox(parent, (Element) n, null);
        ret.setClipBlock(clipbox);
        if (ret.isBlock())
        {
            BlockBox block = (BlockBox) ret; 
            //Setup my containing box
            if (block.position == BlockBox.POS_ABSOLUTE || block.position == BlockBox.POS_FIXED)
                ret.setContainingBlock(absbox);
            else    
                ret.setContainingBlock(contbox);
        }
        else    
            ret.setContainingBlock(contbox);
        
        return ret;
    }
    
    /**
     * Creates a new box for a text node and sets the containing boxes accordingly.
     * @param parent the parent box of the created box
     * @param n The element node
     * @param contbox the containing box of the new box when not absolutley positioned
     * @param clipbox the clipping block of this subtree
     * @return the newly created text box
     */
    private TextBox createTextBox(ElementBox parent, Text n, BlockBox contbox, BlockBox clipbox)
    {
        //TODO: in some whitespace processing modes, multiple boxes may be created
        TextBox text = new TextBox(n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
        text.setOrder(next_order++);
        text.setContainingBlock(contbox);
        text.setClipBlock(clipbox);
        text.setViewport(viewport);
        text.setBase(baseurl);
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
        if (root.isBlock() && ((BlockBox) root).containsBlocks())
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
            if (sub.isblock)
            {
                if (adiv != null && !adiv.isempty)
                    normalizeBox(adiv); //normalize even the newly created blocks
                adiv = null;
                nest.add(sub);
            }
            else if (!sub.isWhitespace()) //omit whitespace boxes
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
            normalizeBox(adiv); //normalize even the newly created blocks
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
            anbox = new BlockBox(anelem, child.getGraphics(), child.getVisualContext());
            anbox.setStyle(createAnonymousStyle("block"));
            ((BlockBox) anbox).contblock = false;
            anbox.isblock = true;
        }
        else
        {
            Element anelem = createAnonymousElement(child.getNode().getOwnerDocument(), "Xspan", "inline");
            anbox = new InlineBox(anelem, child.getGraphics(), child.getVisualContext());
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
        anbox.setViewport(child.getViewport());
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
        if (useHTML && n.getNodeName().equals("img"))
        {
            InlineReplacedBox rbox = new InlineReplacedBox((Element) n, (Graphics2D) parent.getGraphics().create(), parent.getVisualContext().create());
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
     * @param n The source DOM element
     * @param pseudo The pseudo element name
     * @param viewport The used viewport
     * @param parent the root element from which the style will be inherited
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
