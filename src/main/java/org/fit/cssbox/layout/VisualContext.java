/*
 * VisualContext.java
 * Copyright (c) 2005-2014 Radek Burget
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
 * Created on 7. z��� 2005, 15:33
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.css.FontDecoder;
import org.fit.cssbox.css.FontSpec;
import org.fit.cssbox.io.DocumentSource;
import org.fit.net.DataURLHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.web.css.*;
import cz.vutbr.web.css.CSSProperty.FontFamily;
import cz.vutbr.web.css.CSSProperty.TextDecoration;
import cz.vutbr.web.csskit.CalcArgs;
import cz.vutbr.web.csskit.TermCalcAngleImpl;

/**
 * The visual context represents the context of the element - the current font properties, EM and EX values,
 * font metrics and color.
 *
 * @author  burgetr
 */
public class VisualContext 
{
    protected static final Logger log = LoggerFactory.getLogger(VisualContext.class);

    private VisualContext parent;
    private VisualContext rootContext; //the visual context of the root element
    private BoxFactory factory; //the factory used for obtaining current configuration
    private Viewport viewport; //the viewport used for obtaining the vw sizes
    private Font font; //current font
    private FontMetrics fm; //current font metrics
    private double fontSize;
    private CSSProperty.FontWeight fontWeight;
    private CSSProperty.FontStyle fontStyle;
    private CSSProperty.FontVariant fontVariant;
    private List<CSSProperty.TextDecoration> textDecoration;
    private double em; //number of pixels in 1em
    private double rem; //number of pixels in 1rem 
    private double ex; //number of pixels in 1ex
    private double ch; //number of pixels in 1ch
    private double dpi; //number of pixels in 1 inch
    
    public Color color; //current text color

    private PxEvaluator pxEval; //expression evaluator for obtaining pixel values of expressions
    private PtEvaluator ptEval; //expression evaluator for obtaining points values of expressions
    private RadEvaluator radEval; //expression evaluator for obtaining radian values of expressions

    
    public VisualContext(VisualContext parent, BoxFactory factory)
    {
        this.parent = parent;
        this.factory = factory;
        rootContext = (parent == null) ? this : parent.rootContext;
        em = CSSUnits.medium_font;
        rem = em;
        ex = 0.6 * em;
        ch = 0.8 * ch; //just an initial guess, updated in updateForGraphics()
        dpi = org.fit.cssbox.css.CSSUnits.dpi;
        font = new Font(Font.SERIF, Font.PLAIN, (int) CSSUnits.medium_font);
        fontSize = CSSUnits.points(CSSUnits.medium_font);
        fontWeight = CSSProperty.FontWeight.NORMAL;
        fontStyle = CSSProperty.FontStyle.NORMAL;
        fontVariant = CSSProperty.FontVariant.NORMAL;
        textDecoration = new ArrayList<CSSProperty.TextDecoration>(2); //it is not very probable to have more than two decorations
        color = Color.BLACK;
    }
    
    public VisualContext create()
    {
        VisualContext ret = new VisualContext(this, this.factory);
        ret.viewport = viewport;
        ret.rootContext = rootContext;
        ret.em = em;
        ret.rem = rem;
        ret.ex = ex;
        ret.ch = ch;
        ret.dpi = dpi;
        ret.font = font;
        ret.fontSize = fontSize;
        ret.fontWeight = fontWeight;
        ret.fontStyle = fontStyle;
        ret.fontVariant = fontVariant;
        ret.textDecoration = new ArrayList<CSSProperty.TextDecoration>(textDecoration);
        ret.color = color;
        return ret;
    }
   
    //=========================================================================
    
    public VisualContext getParentContext()
    {
        return parent;
    }
    
    public Viewport getViewport()
    {
        return viewport;
    }

    public void setViewport(Viewport viewport)
    {
        this.viewport = viewport;
    }

    public boolean isRootContext()
    {
        return (this == rootContext);
    }

    public void makeRootContext()
    {
        if (this.rootContext != null)
            this.rootContext.rootContext = this; //the old root now points to us
        this.rootContext = this; //we also point to us
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
     * Obtains the specified font size in pt.
     * @return the font size in pt
     */
    public double getFontSize()
    {
        return fontSize;
    }
    
    /**
     * The font variant used for the box.
     * @return <code>normal</code> or <code>small-caps</code>
     */
    public String getFontVariant()
    {
        return fontVariant.toString();
    }
    
    /**
     * Returns the text decoration used for the box.
     * @return <code>none</code> or a string of space separated keywords <code>underline</code>, <code>overline</code>, <code>line-through</code> or <code>blink</code>
     */
    public String getTextDecorationString()
    {
        if (textDecoration.isEmpty())
            return "none";
        else
        {
            StringBuilder ret = new StringBuilder();
            for (CSSProperty.TextDecoration dec : textDecoration)
            {
                if (ret.length() > 0)
                    ret.append(' ');
                ret.append(dec.toString());
            }
            return ret.toString();
        }
    }
    
    /**
     * Returns the text decoration used for the box.
     * @return a list of TextDecoration values
     */
    public List<CSSProperty.TextDecoration> getTextDecoration()
    {
        return textDecoration;
    }
    
    /**
     * The text color used for the box.
     * @return color specification
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * @return the em value of the context
     */
    public double getEm()
    {
        return em;
    }

    /**
     * @return the rem value of the context
     */
    public double getRem()
    {
        return rem;
    }

    /**
     * @return the ex value of the context
     */
    public double getEx()
    {
        return ex;
    }

    /**
     * @return the 'ch' value of the context
     */
    public double getCh()
    {
        return ch;
    }
    
    /**
     * @return the dpi value used in the context
     */
    public double getDpi()
    {
        return dpi;
    }

    //=========================================================================
    
    /** 
     * Updates the context according to the given element style. The properties that are not defined 
     * in the style are left unchanged.
     * @param style the style data 
     */
    public void update(NodeData style)
    {
        //setup the font
        CSSProperty.FontWeight weight = style.getProperty("font-weight");
        if (weight != null) fontWeight = weight;
        CSSProperty.FontStyle fstyle =  style.getProperty("font-style");
        if (fstyle != null) fontStyle = fstyle;
        
        String family = null;
        CSSProperty.FontFamily ff = style.getProperty("font-family");
        if (ff == null)
        {
            family = font.getFamily(); //use current
        }
        else if (ff == FontFamily.list_values)
        {
            TermList fmlspec = style.getValue(TermList.class, "font-family");
            if (fmlspec == null)
                family = font.getFamily();
            else
                family = getFontName(fmlspec, fontWeight, fontStyle);
        }
        else
        {
            if (factory != null)
                family = factory.getConfig().getDefaultFont(ff.getAWTValue()); //try to translate to physical font
            if (family == null)
                family = ff.getAWTValue(); //could not translate - use as is
        }
        
        double size;
        double psize = (parent == null) ? CSSUnits.medium_font : parent.getEm();
        CSSProperty.FontSize fsize = style.getProperty("font-size");
        if (fsize == null)
            size = em;
        else if (fsize == CSSProperty.FontSize.length || fsize == CSSProperty.FontSize.percentage)
        {
            TermLengthOrPercent lenspec = style.getValue(TermLengthOrPercent.class, "font-size");
            if (lenspec != null)
            {
                em = psize;
                size = pxLength(lenspec, psize); //pixels are ok here (java is fixed to 72 dpi for font sizes)
            }
            else
                size = em;
        }
        else
            size = CSSUnits.convertFontSize(psize, fsize);
        fontSize = CSSUnits.points(size);
        
        if (rootContext != null)
            rem = rootContext.getEm();
        else
            rem = em; //we don't have a root context?
        
        font = createFont(family, (int) Math.round(size), fontWeight, fontStyle);
        em = size;
        
        CSSProperty.FontVariant variant = style.getProperty("font-variant");
        if (variant != null) fontVariant = variant;
        CSSProperty.TextDecoration decor = style.getProperty("text-decoration");
        textDecoration.clear();
        if (decor != null)
        {
            if (decor == TextDecoration.list_values)
            {
                TermList list = style.getValue(TermList.class, "text-decoration");
                for (Term<?> t : list)
                {
                    if (t.getValue() instanceof CSSProperty.TextDecoration)
                        textDecoration.add((CSSProperty.TextDecoration) t.getValue());
                }
            }
            else if (decor != TextDecoration.NONE)
                textDecoration.add(decor);
        }
        
        //color
        TermColor clr = style.getSpecifiedValue(TermColor.class, "color");
        if (clr != null) color = CSSUnits.convertColor(clr.getValue());
    }
    
    /** 
     * Updates a Graphics according to this context
     * @param g Graphics to be updated
     */
    public void updateGraphics(Graphics2D g)
    {
        g.setFont(font);
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
     
    /** 
     * Updates this context according to the given style. Moreover given Graphics is updated
     * to this style and used for taking the font metrics.
     * @param style the style data to be used
     * @param g Graphics to be updated and used 
     */
    public void updateForGraphics(NodeData style, Graphics2D g)
    {
        if (style != null) update(style);
        updateGraphics(g);
        fm = g.getFontMetrics();
        
        //update the width units
        //em has been updated in update()
        
        FontRenderContext frc = new FontRenderContext(null, false, false);
        TextLayout layout = new TextLayout("x", font, frc);
        ex = layout.getBounds().getHeight();
        
        ch = fm.charWidth('0');
    }
    
    
    //-----------------------------------------------------------------------
    
    /**
     * Computes current text line height.
     * @return the height of the normal text line in pixels
     */
    public int getFontHeight()
    {
        return fm.getHeight();
    }
    
    /**
     * Obtains the maximal distance from the line top to the baseline
     * for the current font.
     * @return the baseline <em>y</em> offset.
     */
    public int getBaselineOffset()
    {
        return fm.getAscent();
    }
    
    /** 
     * Converts a length from a CSS length or percentage to 'pt'.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'pt' 
     */
    public double ptLength(TermLengthOrPercent spec, double whole)
    {
        float nval = spec.getValue();
        if (spec.isPercentage())
        {
            return (whole * nval) / 100;
        }
        else if (spec instanceof TermCalc)
        {
            final CalcArgs args = ((TermCalc) spec).getArgs();
            return args.evaluate(getPtEval().setWhole(whole));
        }
        else
        {
            final TermLength.Unit unit = spec.getUnit();
            if (unit == null)
                return 0;
            switch (unit)
            {
                case pt:
                    return nval;
                case in:
                    return nval * 72;
                case cm:
                    return (nval * 72) / 2.54;
                case mm:
                    return (nval * 72) / 25.4;
                case q:
                    return (nval * 72) / (2.54 * 40.0);
                case pc:
                    return nval * 12;
                case px:
                    return (nval * 72) / dpi;
                case em:
                    return (em * nval * 72) / dpi; //em is in pixels
                case rem:
                    return (rem * nval * 72) / dpi;
                case ex:
                    return (ex * nval * 72) / dpi;
                case ch:
                    return (ch * nval * 72) / dpi;
                case vw:
                    return (viewport.getVisibleRect().getWidth() * nval * 72) / (100.0 * dpi);
                case vh:
                    return (viewport.getVisibleRect().getHeight() * nval * 72) / (100.0 * dpi);
                case vmin:
                    return (Math.min(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval * 72) / (100.0 * dpi);
                case vmax:
                    return (Math.max(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval * 72) / (100.0 * dpi);
                default:
                    return 0;
            }
        }
    }

    /** 
     * Converts a length from a CSS length to 'pt'. Percentages are always evaluated to 0.
     * @param spec the CSS length specification
     * @return font size in 'pt' 
     */
    public double ptLength(TermLengthOrPercent spec)
    {
        return ptLength(spec, 0);
    }
    
    /** 
     * Converts a length from a CSS length or percentage to 'px'.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'px' 
     */
    public double pxLength(TermLengthOrPercent spec, double whole)
    {
        float nval = spec.getValue();
        if (spec.isPercentage())
        {
            return (whole * nval) / 100;
        }
        else if (spec instanceof TermCalc)
        {
            final CalcArgs args = ((TermCalc) spec).getArgs();
            return args.evaluate(getPxEval().setWhole(whole));
        }
        else
        {
            final TermLength.Unit unit = spec.getUnit();
            if (unit == null)
                return 0;
            switch (unit)
            {
                case pt:
                    return (nval * dpi) / 72;
                case in:
                    return nval * dpi;
                case cm:
                    return (nval * dpi) / 2.54;
                case mm:
                    return (nval * dpi) / 25.4;
                case q:
                    return (nval * dpi) / (2.54 * 40.0);
                case pc:
                    return (nval * 12 * dpi) / 72;
                case px:
                    return nval;
                case em:
                    return em * nval; //em is in pixels
                case rem:
                    return rem * nval; //em is in pixels
                case ex:
                    return ex * nval;
                case ch:
                    return ch * nval;
                case vw:
                    return viewport.getVisibleRect().getWidth() * nval / 100.0;
                case vh:
                    return viewport.getVisibleRect().getHeight() * nval / 100.0;
                case vmin:
                    return Math.min(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval / 100.0;
                case vmax:
                    return Math.max(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval / 100.0;
                default:
                    return 0;
            }
        }
    }
    
    /** 
     * Converts a length from a CSS length to 'px'. Percentages are always evaluated to 0.
     * @param spec the CSS length specification
     * @return font size in 'px' 
     */
    public double pxLength(TermLengthOrPercent spec)
    {
        return pxLength(spec, 0);
    }
    
    /**
     * Converts an angle from a CSS angle to 'rad'.
     * @param spec the CSS angle specification
     * @return the corresponding angle in radians
     */
    public double radAngle(TermAngle spec)
    {
        float nval = spec.getValue();
        final TermLength.Unit unit = spec.getUnit();
        if (spec instanceof TermCalcAngleImpl)
        {
            final CalcArgs args = ((TermCalc) spec).getArgs();
            return args.evaluate(getRadEval());
        }
        else
        {
            if (unit == null)
                return 0;
            switch (unit)
            {
                case deg:
                    return (nval * Math.PI) / 180.0; 
                case grad:
                    return (nval * Math.PI) / 200.0;
                case rad:
                    return nval;
                case turn:
                    return nval * 2 * Math.PI;
                default:
                    return 0;
            }
        }
    }
    
    /** 
     * Scans a list of font definitions and chooses the first one that is available
     * @param list of terms obtained from the font-family property
     * @return a font name string according to java.awt.Font
     */
    private String getFontName(TermList list, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
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
                else if (src instanceof RuleFontFace.SourceURL)
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
                                DocumentSource imgsrc = viewport.getConfig().createDocumentSource(url);
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
        if (factory != null)
            return factory.getDecoder().getFontTable().findBestMatch(spec);
        else
            return null; //no factory available, boxes have been created in some alternative way, no font table is available
    }
    
    public Font createFont(String family, int size, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        int fs = Font.PLAIN;
        if (FontSpec.representsBold(weight))
            fs = Font.BOLD;
        if (style == CSSProperty.FontStyle.ITALIC || style == CSSProperty.FontStyle.OBLIQUE)
            fs = fs | Font.ITALIC;
        
        return new Font(family, fs, size);
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

    private PxEvaluator getPxEval()
    {
        if (pxEval == null)
            pxEval = new PxEvaluator(this);
        return pxEval;
    }
    
    private PtEvaluator getPtEval()
    {
        if (ptEval == null)
            ptEval = new PtEvaluator(this);
        return ptEval;
    }
    
    private RadEvaluator getRadEval()
    {
        if (radEval == null)
            radEval = new RadEvaluator(this);
        return radEval;
    }
    
    //============================================================================================================================
    
    /**
     * A base of all the evaluators that use the VisualContext for evaluating the calc() expressions.
     *
     * @author burgetr
     */
    private abstract class UnitEvaluator extends CalcArgs.DoubleEvaluator
    {
        protected VisualContext ctx;
        protected double whole; //whole size used for percentages
        
        public UnitEvaluator(VisualContext ctx)
        {
            this.ctx = ctx;
        }

        public UnitEvaluator setWhole(double whole)
        {
            this.whole = whole;
            return this;
        }
    }
    
    private class PxEvaluator extends UnitEvaluator
    {
        public PxEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public double resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.pxLength((TermLengthOrPercent) val, whole);
            else
                return 0.0; //this should not happen
        }
    }
    
    private class PtEvaluator extends UnitEvaluator
    {
        public PtEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public double resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.ptLength((TermLengthOrPercent) val, whole);
            else
                return 0.0; //this should not happen
        }
    }
    
    private class RadEvaluator extends UnitEvaluator
    {
        public RadEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public double resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.radAngle((TermAngle) val);
            else
                return 0.0; //this should not happen
        }
    }
    
}
