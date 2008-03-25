/**
 * BoxBrowser.java 
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
 */
package org.fit.cssbox.demo;

import javax.swing.*;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;

/**
 * This demo implements a browser that displays the rendered box tree and the
 * corresponding page. Each box corresponds to an HTML element or a text node.
 * 
 * @author burgetr
 */
public class BoxBrowser
{
    private DefaultMutableTreeNode root;
    public static BoxBrowser browser;
    
    private JFrame mainWindow = null;  //  @jve:decl-index=0:visual-constraint="67,17"
    private JPanel mainPanel = null;
    private JPanel urlPanel = null;
    private JPanel contentPanel = null;
    private JPanel structurePanel = null;
    private JPanel statusPanel = null;
    private JTextField statusText = null;
    private JLabel jLabel = null;
    private JTextField urlText = null;
    private JButton okButton = null;
    private JScrollPane contentScroll = null;
    private JPanel contentCanvas = null;
    private JSplitPane mainSplitter = null;
    private JToolBar showToolBar = null;
    private JButton redrawButton = null;
    private JPanel toolPanel = null;
    private JScrollPane treeScroll = null;
    private JTree domTree = null;
    
    /**
     * Reads the document, creates the layout and displays it
     */
	private void displayURL(String urlstring)
    {
        try {
            if (!urlstring.startsWith("http:") &&
                !urlstring.startsWith("ftp:") &&
                !urlstring.startsWith("file:"))
                    urlstring = "http://" + urlstring;
            
            URL url = new URL(urlstring);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            
            Tidy tidy = new Tidy();
            tidy.setTrimEmptyElements(false);
            tidy.setAsciiChars(false);
            tidy.setInputEncoding("iso-8859-2");
            //tidy.setInputEncoding("utf-8");
            tidy.setXHTML(true);
            Document doc = tidy.parseDOM(is, null);
            
            DOMAnalyzer da = new DOMAnalyzer(doc, url);
            da.addStyleSheet(null, CSSNorm.stdStyleSheet());
            da.addStyleSheet(null, CSSNorm.userStyleSheet());
            da.getStyleSheets();
            da.attributesToStyles();
            
            is.close();

            contentCanvas = new BrowserCanvas(da.getBody(), da, contentScroll.getSize(), url);
            contentCanvas.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e)
                {
                    System.out.println("Click: " + e.getX() + ":" + e.getY());
                    canvasClick(e.getX(), e.getY());
                }
                public void mousePressed(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mouseEntered(MouseEvent e) { }
                public void mouseExited(MouseEvent e) { }
            });
            contentScroll.setViewportView(contentCanvas);

            //box tree
            root = createBoxTree(((BrowserCanvas) contentCanvas).getRootBox());
            domTree.setModel(new DefaultTreeModel(root));
            
            //=============================================================================
           
        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
	/**
	 * Recursively creates a tree from the box tree
	 */
	private DefaultMutableTreeNode createBoxTree(Box root)
	{
	    DefaultMutableTreeNode ret = new DefaultMutableTreeNode(root);
	    if (root instanceof ElementBox)
	    {
	        ElementBox el = (ElementBox) root;
	        for (int i = el.getStartChild(); i < el.getEndChild(); i++)
	        {
	            ret.add(createBoxTree(el.getSubBox(i)));
	        }
	    }
	    return ret;
	}
	
	/**
	 * Locates a box from its position
	 */
	private DefaultMutableTreeNode locateBox(DefaultMutableTreeNode root, int x, int y)
	{
	    Box box = (Box) root.getUserObject();
	    Rectangle bounds = box.getBounds();
	    if (bounds.contains(x, y))
	    {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                DefaultMutableTreeNode inside = locateBox((DefaultMutableTreeNode) root.getChildAt(i), x, y);
                if (inside != null)
                    return inside;
            }
            return root;
	    }
	    else
	        return null;
	}
	
    /** 
     * This is called when the browser canvas is clicked
     */
    private void canvasClick(int x, int y)
    {
        DefaultMutableTreeNode node = locateBox(root, x, y);
        if (node != null)
        {
            TreePath select = new TreePath(node.getPath());
            domTree.setSelectionPath(select);
            domTree.expandPath(select);
        }
    }
    
    public BrowserCanvas getBrowserCanvas()
    {
        return (BrowserCanvas) contentCanvas;
    }
    
    //===========================================================================
    
    /**
     * This method initializes jFrame	
     * 	
     * @return javax.swing.JFrame	
     */
    private JFrame getMainWindow()
    {
        if (mainWindow == null)
        {
            mainWindow = new JFrame();
            mainWindow.setTitle("Box Browser");
            mainWindow.setVisible(true);
            mainWindow.setBounds(new Rectangle(0, 0, 583, 251));
            mainWindow.setContentPane(getMainPanel());
            mainWindow.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosing(java.awt.event.WindowEvent e)
                {
                    mainWindow.setVisible(false);
                    System.exit(0);
                }
            });
        }
        return mainWindow;
    }

    /**
     * This method initializes jContentPane	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMainPanel()
    {
        if (mainPanel == null)
        {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridy = -1;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.gridx = -1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints11.weighty = 1.0;
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.weightx = 1.0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridwidth = 1;
            gridBagConstraints3.gridy = 3;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridy = 1;
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(getJPanel3(), gridBagConstraints2);
            mainPanel.add(getUrlPanel(), gridBagConstraints);
            mainPanel.add(getMainSplitter(), gridBagConstraints11);
            mainPanel.add(getStatusPanel(), gridBagConstraints3);
        }
        return mainPanel;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getUrlPanel()
    {
        if (urlPanel == null)
        {
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridx = 1;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 3;
            gridBagConstraints7.insets = new java.awt.Insets(4,0,5,7);
            gridBagConstraints7.gridy = 1;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.insets = new java.awt.Insets(0,5,0,5);
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints5.insets = new java.awt.Insets(0,6,0,0);
            gridBagConstraints5.gridx = 0;
            jLabel = new JLabel();
            jLabel.setText("Location :");
            urlPanel = new JPanel();
            urlPanel.setLayout(new GridBagLayout());
            urlPanel.add(jLabel, gridBagConstraints5);
            urlPanel.add(getUrlText(), gridBagConstraints6);
            urlPanel.add(getOkButton(), gridBagConstraints7);
        }
        return urlPanel;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setRows(1);
            contentPanel = new JPanel();
            contentPanel.setLayout(gridLayout1);
            contentPanel.add(getContentScroll(), null);
        }
        return contentPanel;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getStructurePanel()
    {
        if (structurePanel == null)
        {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            structurePanel = new JPanel();
            structurePanel.setPreferredSize(new Dimension(200, 408));
            structurePanel.setLayout(gridLayout);
            structurePanel.add(getTreeScroll(), null);
        }
        return structurePanel;
    }

    /**
     * This method initializes jPanel2	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getStatusPanel()
    {
        if (statusPanel == null)
        {
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.insets = new java.awt.Insets(0,7,0,0);
            gridBagConstraints4.gridy = 2;
            statusPanel = new JPanel();
            statusPanel.setLayout(new GridBagLayout());
            statusPanel.add(getStatusText(), gridBagConstraints4);
        }
        return statusPanel;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getStatusText()
    {
        if (statusText == null)
        {
            statusText = new JTextField();
            statusText.setEditable(false);
            statusText.setText("Browser ready.");
        }
        return statusText;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getUrlText()
    {
        if (urlText == null)
        {
            urlText = new JTextField();
        }
        return urlText;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOkButton()
    {
        if (okButton == null)
        {
            okButton = new JButton();
            okButton.setText("Go!");
            okButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    displayURL(urlText.getText());
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getContentScroll()
    {
        if (contentScroll == null)
        {
            contentScroll = new JScrollPane();
            contentScroll.setViewportView(getContentCanvas());
            contentScroll.addComponentListener(new java.awt.event.ComponentAdapter()
            {
                public void componentResized(java.awt.event.ComponentEvent e)
                {
                    if (contentCanvas != null && contentCanvas instanceof BrowserCanvas)
                    {
                        ((BrowserCanvas) contentCanvas).createLayout(contentScroll.getSize());
                        contentScroll.repaint();
                        //new box tree
                        root = createBoxTree(((BrowserCanvas) contentCanvas).getRootBox());
                        domTree.setModel(new DefaultTreeModel(root));
                    }
                }
            });
        }
        return contentScroll;
    }

    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getContentCanvas()
    {
        if (contentCanvas == null)
        {
            contentCanvas = new JPanel();
        }
        return contentCanvas;
    }
    
    /**
     * This method initializes jSplitPane	
     * 	
     * @return javax.swing.JSplitPane	
     */
    private JSplitPane getMainSplitter()
    {
        if (mainSplitter == null)
        {
            mainSplitter = new JSplitPane();
            mainSplitter.setLeftComponent(getStructurePanel());
            mainSplitter.setRightComponent(getContentPanel());
        }
        return mainSplitter;
    }

    /**
     * This method initializes jToolBar 
     *  
     * @return javax.swing.JToolBar 
     */
    private JToolBar getShowToolBar()
    {
        if (showToolBar == null)
        {
            showToolBar = new JToolBar();
            showToolBar.add(getRedrawButton());
        }
        return showToolBar;
    }
    
    
    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getRedrawButton()
    {
        if (redrawButton == null)
        {
            redrawButton = new JButton();
            redrawButton.setText("Clear");
            redrawButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            redrawButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    ((BrowserCanvas) contentCanvas).redrawBoxes();
                    contentCanvas.repaint();
                }
            });
        }
        return redrawButton;
    }

	/**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel3()
    {
        if (toolPanel == null)
        {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            toolPanel = new JPanel();
            toolPanel.setLayout(flowLayout);
            toolPanel.add(getShowToolBar(), null);
        }
        return toolPanel;
    }

    /**
     * This method initializes treeScroll	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getTreeScroll()
    {
        if (treeScroll == null)
        {
            treeScroll = new JScrollPane();
            treeScroll.setViewportView(getDomTree());
        }
        return treeScroll;
    }

    /**
     * This method initializes domTree	
     * 	
     * @return javax.swing.JTree	
     */
    private JTree getDomTree()
    {
        if (domTree == null)
        {
            domTree = new JTree();
            domTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("(box tree)")));
            domTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
            {
                public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) domTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
                        Box box = (Box) node.getUserObject();
                        if (box != null)
                        {
                            box.drawExtent(((BrowserCanvas) contentCanvas).getImageGraphics());
                            contentCanvas.repaint();
                        }
                    }
                }
            });
        }
        return domTree;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        browser = new BoxBrowser();
        JFrame main = browser.getMainWindow();
        main.setSize(1200,600);
        main.setVisible(true);
    }

}
