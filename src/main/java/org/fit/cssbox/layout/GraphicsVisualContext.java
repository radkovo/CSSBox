/*
 * GraphicsVisualContext.java
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
 * Created on 10. 11. 2019, 13:53:14 by burgetr
 */
package org.fit.cssbox.layout;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.css.FontDecoder;
import org.fit.cssbox.css.FontSpec;
import org.fit.cssbox.css.FontTable;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.render.GraphicsRenderer;
import org.fit.net.DataURLHandler;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermURI;

/**
 * A visual context implementation based on a Graphics2D context
 * 
 * @author burgetr
 */
public class GraphicsVisualContext extends VisualContext
{
    private Graphics2D g;
    private Font font; //current font
    private FontMetrics fm; //current font metrics
    private float ex; // 1ex length in points
    private float ch; // 1ch length in points
    
    /** Default font attributes used when creating fonts */
    private HashMap<TextAttribute, Object> defaultFontAttributes;
    

    /**
     * Creates a new visual context for a AWT graphics context.
     * 
     * @param g The graphics context
     * @param parent the parent visual context or {@code null} for the root context
     * @param config used browser configuration
     * @param fontTable CSS font table or {@code null} when no CSS-defined fonts are used
     */
    public GraphicsVisualContext(Graphics2D g, VisualContext parent, BrowserConfig config, FontTable fontTable)
    {
        super(parent, config, fontTable);
        this.g = g;
        font = new Font(Font.SERIF, Font.PLAIN, (int) CSSUnits.medium_font);
        defaultFontAttributes = new HashMap<>();
        updateMetrics(g);
    }

    @Override
    public VisualContext create()
    {
        GraphicsVisualContext ret = new GraphicsVisualContext(g, this, this.getConfig(), this.getFontTable());
        ret.copyVisualContext(this);
        ret.updateMetrics(g);
        return ret;
    }
    
    @Override
    public void copyVisualContext(VisualContext src)
    {
        super.copyVisualContext(src);
        if (src instanceof GraphicsVisualContext)
        {
            font = ((GraphicsVisualContext) src).font;
            defaultFontAttributes = ((GraphicsVisualContext) src).defaultFontAttributes;
            ex = src.getEx();
            ch = src.getCh();
        }
    }
    
    //=========================================================================
    
    @Override
    public void update(NodeData style)
    {
        super.update(style);
        updateMetrics(g);
    }
    
    /**
     * Gets the 1ex length in points
     * @return the ex size in points
     */
    @Override
    public float getEx()
    {
        return ex;
    }

    /**
     * Gets the 1ch length in points
     * @return the ch size in points
     */
    @Override
    public float getCh()
    {
        return ch;
    }
    
    //=========================================================================
    
    /** 
     * Updates a Graphics according to this context
     * @param g Graphics to be updated
     */
    public void updateGraphics(Graphics2D g)
    {
        g.setFont(font);
        g.setColor(GraphicsRenderer.convertColor(color));
    }
     
    /**
     * Updates the font metrics and the ex, and ch values. 
     * @param g Graphics to be used for computing the font metrics 
     */
    private void updateMetrics(Graphics2D g)
    {
        // create a working copy of the graphic context
        final Graphics2D cg = (Graphics2D) g.create(); 
        // set the graphics font to the current font
        updateGraphics(cg);
        // get the font metrics
        fm = cg.getFontMetrics();
        
        //update the width units
        //em and rem are maintained by the parent class (VisualContext)
        FontRenderContext frc = new FontRenderContext(null, false, false);
        TextLayout layout = new TextLayout("x", font, frc);
        ex = CSSUnits.points((float) layout.getBounds().getHeight());
        ch = CSSUnits.points(fm.charWidth('0'));
    }
    
    @Override
    public float stringWidth(String text)
    {
        if (text.isEmpty())
            return 0;
        else
        {
            final Graphics2D cg = (Graphics2D) g.create(); 
            updateGraphics(cg);
            return (float) fm.getStringBounds(text, cg).getWidth();
        }
    }
    
    //=========================================================================
    
    @Override
    public void setCurrentFont(String family, float size, CSSProperty.FontWeight weight, CSSProperty.FontStyle style, float spacing)
    {
        // AWT specifies the font sizes px (in points with 72dpi)
        font = createFont(family, Math.round(CSSUnits.pixels(size)), weight, style, CSSUnits.pixels(spacing));
    }
    
    @Override
    public String getFontFamily()
    {
        return font.getFamily();
    }

    @Override
    public FontInfo getFontInfo()
    {
        return new FontInfo(font.getFamily(), getFontSize(), font.isBold(), font.isItalic());
    }
    
    @Override
    public float getFontHeight()
    {
        return fm.getHeight();
    }
    
    @Override
    public float getBaselineOffset()
    {
        return fm.getAscent();
    }
    
    /**
     * The AWT font used for the box.
     * @return current font
     */
    public Font getFont()
    {
        return font;
    }
    
    protected Font createBaseFont(String family, int size, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        int fs = Font.PLAIN;
        if (FontSpec.representsBold(weight))
            fs = Font.BOLD;
        if (style == CSSProperty.FontStyle.ITALIC || style == CSSProperty.FontStyle.OBLIQUE)
            fs = fs | Font.ITALIC;
        
        return new Font(family, fs, size);
    }
    
    protected Font createFont(String family, int size, CSSProperty.FontWeight weight,
            CSSProperty.FontStyle style, float spacing)
    {
        Font base = createBaseFont(family, size, weight, style);
        Map<TextAttribute, Object> attributes = new HashMap<>(defaultFontAttributes);
        // add tracking when needed
        if (spacing >= 0.0001)
        {
            // TRACKING value is multiplied by font size in AWT. 
            // (0.75 has been empiricaly determined by comparing with other browsers) 
            final float tracking = spacing / getFontSize() * 0.75f;
            
            attributes.put(TextAttribute.TRACKING, tracking);
        }
        // derive the font when some attributes have been set
        if (attributes.isEmpty())
            return base;
        else
            return base.deriveFont(attributes);
    }
    
    @Override
    protected String fontAvailable(String family)
    {
        final String avail[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (int i = 0; i < avail.length; i++)
        {
            if (avail[i].equalsIgnoreCase(family))
                return avail[i];
        }
        return null;
    }
    
    @Override
    protected String getFallbackFont()
    {
        final String avail[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        if (avail.length == 0)
            return "Serif"; //no physical fonts available, give up
        //first try: helvetica
        String ret = fontAvailable("Helvetica");
        if (ret == null) //second try: anything containing "Serif" or "Sans" to avoid strange fonts
        {
            for (int i = 0; i < avail.length; i++)
            {
                if (avail[i].toLowerCase().contains("sans") || avail[i].toLowerCase().contains("serif"))
                {
                    ret = avail[i];
                    break;
                }
            }
        }
        if (ret == null) //third try: use the first available font
        {
            ret = avail[0];
        }
        return ret;
    }
    
    @Override
    protected String registerExternalFont(TermURI urlstring, String format)
            throws MalformedURLException, IOException
    {
        String nameFound = null;
        if (format == null || FontDecoder.supportedFormats.contains(format))
        {
            URL url = DataURLHandler.createURL(urlstring.getBase(), urlstring.getValue());
            String regName = FontDecoder.findRegisteredFont(url);
            if (regName == null)
            {
                DocumentSource imgsrc = getViewport().getConfig().createDocumentSource(url);
                Font newFont;
                try {
                    newFont = FontDecoder.decodeFont(imgsrc, format);
                } catch (FontFormatException e) {
                    throw new IOException(e);
                }
                if (GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(newFont))
                    log.debug("Registered font: {}", newFont.getFontName());
                else
                    log.debug("Failed to register font: {} (not fatal, probably already existing)", newFont.getFontName());
                regName = newFont.getFontName();
                FontDecoder.registerFont(url, regName);
            }
            nameFound = regName;
        }
        return nameFound;
    }
    
    /**
     * Gets the map of default font attributes used when creating new fonts.
     * By modifying this map, the new font creation may be adjusted.
     * @return The font attribute map
     */
    public HashMap<TextAttribute, Object> getDefaultFontAttributes()
    {
        return defaultFontAttributes;
    }
    
    //=========================================================================
    
    /**
     * Computes the line metrics for the particular text content and graphical context.
     * @return the line metrics
     */
    public LineMetrics getLineMetrics(String text)
    {
        return getFont().getLineMetrics(text, g.getFontRenderContext());
    }
    
}
