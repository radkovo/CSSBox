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
import java.util.List;
import java.util.Map;

import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.css.FontDecoder;
import org.fit.cssbox.css.FontSpec;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.render.GraphicsRenderer;
import org.fit.net.DataURLHandler;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFontFace;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
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
    

    public GraphicsVisualContext(Graphics2D g, VisualContext parent, BoxFactory factory)
    {
        super(parent, factory);
        this.g = g;
        font = new Font(Font.SERIF, Font.PLAIN, (int) CSSUnits.medium_font);
        defaultFontAttributes = new HashMap<>();
        updateMetrics(g);
    }

    @Override
    public VisualContext create()
    {
        GraphicsVisualContext ret = new GraphicsVisualContext(g, this, this.getFactory());
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
        return fm.stringWidth(text);
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
    
    @Override
    protected String getFontName(TermList list, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        for (Term<?> term : list)
        {
            Object value = term.getValue();
            if (value instanceof CSSProperty.FontFamily)
                return ((CSSProperty.FontFamily) value).getAWTValue();
            else
            {
                String name = lookupFont(value.toString(), weight, style);
                if (name != null) return name;
            }
        }
        //nothing found, use Serif
        return java.awt.Font.SERIF;
    }
    
    /**
     * The AWT font used for the box.
     * @return current font
     */
    public Font getFont()
    {
        return font;
    }
    
    /**
     * Check if the font family is available either among the CSS defined fonts or the system fonts.
     * If found, registers a system font with the given name. 
     * @param family Required font family
     * @param weight Required font weight
     * @param style Required font style
     * @return The corresponding system font name or {@code null} when no candidates have been found. 
     */
    private String lookupFont(String family, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        final String systemFontNames[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        //try to look in the style font table
        String nameFound = null;
        FontSpec spec = new FontSpec(family, weight, style);
        List<RuleFontFace.Source> srcs = findMatchingFontSources(spec);
        if (srcs != null)
        {
            for (RuleFontFace.Source src : srcs)
            {
                if (src instanceof RuleFontFace.SourceLocal)
                {
                    String name = fontAvailable(((RuleFontFace.SourceLocal) src).getName(), systemFontNames);
                    if (name != null)
                    {
                        nameFound = name;
                        break;
                    }
                }
                else if (src instanceof RuleFontFace.SourceURL && getViewport().getConfig().isLoadFonts())
                {
                    try
                    {
                        TermURI urlstring = ((RuleFontFace.SourceURL) src).getURI();
                        String format = ((RuleFontFace.SourceURL) src).getFormat();
                        if (format == null || FontDecoder.supportedFormats.contains(format))
                        {
                            URL url = DataURLHandler.createURL(urlstring.getBase(), urlstring.getValue());
                            String regName = FontDecoder.findRegisteredFont(url);
                            if (regName == null)
                            {
                                DocumentSource imgsrc = getViewport().getConfig().createDocumentSource(url);
                                Font newFont = FontDecoder.decodeFont(imgsrc, format);
                                if (GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(newFont))
                                    log.debug("Registered font: {}", newFont.getFontName());
                                else
                                    log.debug("Failed to register font: {} (not fatal, probably already existing)", newFont.getFontName());
                                regName = newFont.getFontName();
                                FontDecoder.registerFont(url, regName);
                            }
                            nameFound = regName;
                        }
                    } catch (MalformedURLException e) {
                        log.error("Couldn't load font with URI {} ({})", ((RuleFontFace.SourceURL) src).getURI(), e.getMessage());
                    } catch (IOException e) {
                        log.error("Couldn't load font with URI {} ({})", ((RuleFontFace.SourceURL) src).getURI(), e.getMessage());
                    } catch (FontFormatException e) {
                        log.error("Couldn't decode font with URI {} ({})", ((RuleFontFace.SourceURL) src).getURI(), e.getMessage());
                    }
                }
            }
        }
        //if nothing found, try the system font names
        if (nameFound == null)
        {
            nameFound = fontAvailable(family, systemFontNames);
        }
        //create the font when found
        return nameFound;
    }
    
    private List<RuleFontFace.Source> findMatchingFontSources(FontSpec spec)
    {
        if (getFactory() != null)
            return getFactory().getDecoder().getFontTable().findBestMatch(spec);
        else
            return null; //no factory available, boxes have been created in some alternative way, no font table is available
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
    
    /** Returns true if the font family is available.
     * @return The exact name of the font family or null if it's not available
     */
    private String fontAvailable(String family, String[] avail)
    {
        for (int i = 0; i < avail.length; i++)
            if (avail[i].equalsIgnoreCase(family)) return avail[i];
        return null;
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
