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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.css.FontSpec;
import org.fit.cssbox.css.FontTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFontFace;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermAngle;
import cz.vutbr.web.css.TermCalc;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermFloatValue;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.csskit.CalcArgs;
import cz.vutbr.web.csskit.Color;
import cz.vutbr.web.csskit.TermCalcAngleImpl;

/**
 * The visual context represents the context of the element - the current font properties, EM and EX values,
 * font metrics and color.
 *
 * @author  burgetr
 */
public abstract class VisualContext 
{
    protected static final Logger log = LoggerFactory.getLogger(VisualContext.class);

    private VisualContext parent;
    private VisualContext rootContext; //the visual context of the root element
    private BrowserConfig config; //used engine configuration
    private FontTable fontTable; //a table of CSS-defined fonts
    private Viewport viewport; //the viewport used for obtaining the vw sizes
    private float fontSize; //font size in pt
    private CSSProperty.FontWeight fontWeight;
    private CSSProperty.FontStyle fontStyle;
    private CSSProperty.FontVariant fontVariant;
    private List<CSSProperty.TextDecoration> textDecoration;
    private float letterSpacing; //additional letter spacing in pt
    private float rem; // 1rem length in points 
    
    public Color color; //current text color

    private PxEvaluator pxEval; //expression evaluator for obtaining pixel values of expressions
    private PtEvaluator ptEval; //expression evaluator for obtaining points values of expressions
    private DegEvaluator degEval; //expression evaluator for obtaining degree values of expressions
    private RadEvaluator radEval; //expression evaluator for obtaining radian values of expressions

    
    public VisualContext(VisualContext parent, BrowserConfig config, FontTable fontTable)
    {
        this.parent = parent;
        this.config = config;
        this.fontTable = fontTable;
        rootContext = (parent == null) ? this : parent.rootContext;
        fontSize = CSSUnits.medium_font;
        fontWeight = CSSProperty.FontWeight.NORMAL;
        fontStyle = CSSProperty.FontStyle.NORMAL;
        fontVariant = CSSProperty.FontVariant.NORMAL;
        rem = fontSize;
        textDecoration = new ArrayList<CSSProperty.TextDecoration>(2); //it is not very probable to have more than two decorations
        letterSpacing = 0.0f;
        color = new Color(0, 0, 0);
    }
    
    public void copyVisualContext(VisualContext src)
    {
        viewport = src.viewport;
        rootContext = src.rootContext;
        rem = src.rem;
        fontSize = src.fontSize;
        fontWeight = src.fontWeight;
        fontStyle = src.fontStyle;
        fontVariant = src.fontVariant;
        textDecoration = new ArrayList<CSSProperty.TextDecoration>(src.textDecoration);
        letterSpacing = src.letterSpacing;
        color = src.color;
    }
   
    abstract public VisualContext create();
    
    //=========================================================================
    
    public VisualContext getParentContext()
    {
        return parent;
    }
    
    public void setParentContext(VisualContext parent)
    {
        this.parent = parent;
    }
    
    public BrowserConfig getConfig()
    {
        return config;
    }

    public FontTable getFontTable()
    {
        return fontTable;
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
     * Gets the information about the current font.
     * @return current font information
     */
    abstract public FontInfo getFontInfo();
    
    /**
     * Obtains the specified font size in pt.
     * @return the font size in pt
     */
    public float getFontSize()
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
     * Returns the letter spacing used for the box.
     * @return letter spacing
     */
    public float getLetterSpacing()
    {
        return letterSpacing;
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
     * Gets the em value for the current context. It is always equal to FontSize.
     * @return the em value of the context
     */
    public float getEm()
    {
        return fontSize;
    }

    /**
     * Gets the 1rem length in points
     * @return the rem size in points
     */
    public float getRem()
    {
        return rem;
    }

    /**
     * Gets the 1ex length in points
     * @return the ex size in points
     */
    abstract public float getEx();

    /**
     * Gets the 1ch length in points
     * @return the ch size in points
     */
    abstract public float getCh();
    
    protected void setRem(float rem)
    {
        this.rem = rem;
    }

    /**
     * Gets the current font family.
     * @return The font family name
     */
    abstract public String getFontFamily();
    
    /**
     * Computes the pixel width of the given string in the visual context.
     * @param text The text sting
     * @return the resulting widtg in pixels
     */
    abstract public float stringWidth(String text);
    
    //=========================================================================
    
    /** 
     * Updates the context according to the given element style. The properties that are not defined 
     * in the style are left unchanged.
     * @param style the style data 
     */
    public void update(NodeData style)
    {
        // font style and weight
        CSSProperty.FontWeight weight = style.getProperty("font-weight");
        if (weight != null) fontWeight = weight;
        CSSProperty.FontStyle fstyle =  style.getProperty("font-style");
        if (fstyle != null) fontStyle = fstyle;
        
        // font family
        String family = null;
        CSSProperty.FontFamily ff = style.getProperty("font-family");
        if (ff == null)
        {
            family = getFontFamily(); //use current
        }
        else if (ff == CSSProperty.FontFamily.list_values)
        {
            TermList fmlspec = style.getValue(TermList.class, "font-family");
            if (fmlspec == null)
                family = getFontFamily();
            else
                family = findFontName(fmlspec, fontWeight, fontStyle);
        }
        else
        {
            family = findLogicalFont(ff, fontWeight, fontStyle);
        }
        if (family == null) {
            //no alternative was usable try 'Serif' logical font
            family = findLogicalFont(CSSProperty.FontFamily.SERIF, fontWeight, fontStyle);
            //even Serif is not available - try the last fallback
            if (family == null)
            {
                family = getFallbackFont();
                log.warn("Couldn't find any usable font for {}, using {} as the last option", ff, family);
            }
        }
        
        // font size in pt
        float size;
        final float psize = (parent == null) ? CSSUnits.medium_font : parent.getEm();
        final CSSProperty.FontSize fsize = style.getProperty("font-size");
        if (fsize == null) //no specification - use the parent size
            size = psize;
        else if (fsize == CSSProperty.FontSize.length || fsize == CSSProperty.FontSize.percentage)
        {
            TermLengthOrPercent lenspec = style.getValue(TermLengthOrPercent.class, "font-size");
            if (lenspec != null)
            {
                if (parent != null)
                    size = parent.ptLength(lenspec, psize);
                else
                    size = rootContext.ptLength(lenspec, psize);
            }
            else
                size = psize;
        }
        else // size keywords
            size = CSSUnits.convertFontSize(psize, fsize);
        fontSize = size;
        rem = rootContext.getEm();
        
        // font variant
        CSSProperty.FontVariant variant = style.getProperty("font-variant");
        if (variant != null) fontVariant = variant;

        // text decoration
        CSSProperty.TextDecoration decor = style.getProperty("text-decoration");
        textDecoration.clear();
        if (decor != null)
        {
            if (decor == CSSProperty.TextDecoration.list_values)
            {
                TermList list = style.getValue(TermList.class, "text-decoration");
                for (Term<?> t : list)
                {
                    if (t.getValue() instanceof CSSProperty.TextDecoration)
                        textDecoration.add((CSSProperty.TextDecoration) t.getValue());
                }
            }
            else if (decor != CSSProperty.TextDecoration.NONE)
                textDecoration.add(decor);
        }
        
        // letter spacing
        CSSProperty.LetterSpacing spacing = style.getProperty("letter-spacing");
        if (spacing != null)
        {
            if (spacing == CSSProperty.LetterSpacing.NORMAL)
                letterSpacing = 0.0f;
            else
            {
                TermLength lenspec = style.getValue(TermLength.class, "letter-spacing");
                if (lenspec != null)
                    letterSpacing = ptLength(lenspec);
            }
        }
        
        // color
        TermColor clr = style.getSpecifiedValue(TermColor.class, "color");
        if (clr != null) color = clr.getValue();
        
        // update the font settings
        setCurrentFont(family, size, fontWeight, fontStyle, letterSpacing);
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Sets current font according to its given parametres.
     *  
     * @param family font family
     * @param size font size in pt
     * @param weight font weight
     * @param style font style
     * @param spacing letter spacing in pt
     */
    abstract public void setCurrentFont(String family, float size, CSSProperty.FontWeight weight, CSSProperty.FontStyle style, float spacing);
    
    /**
     * Computes current text line height.
     * @return the height of the normal text line in pixels
     */
    abstract public float getFontHeight();
    
    /**
     * Obtains the maximal distance from the line top to the baseline
     * for the current font.
     * @return the baseline <em>y</em> offset.
     */
    abstract public float getBaselineOffset();
    
    /** 
     * Converts a length from a CSS length or percentage to 'pt'. The current font size is used for em units.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'pt' 
     */
    public float ptLength(TermLengthOrPercent spec, float whole)
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
                    return (nval * 72) / 2.54f;
                case mm:
                    return (nval * 72) / 25.4f;
                case q:
                    return (nval * 72) / (2.54f * 40.0f);
                case pc:
                    return nval * 12;
                case px:
                    return CSSUnits.points(nval);
                case em:
                    return getFontSize() * nval;
                case rem:
                    return getRem() * nval;
                case ex:
                    return getEx() * nval;
                case ch:
                    return getCh() * nval;
                case vw:
                    return CSSUnits.points(viewport.getVisibleRect().getWidth()) * nval / 100.0f;
                case vh:
                    return CSSUnits.points(viewport.getVisibleRect().getWidth()) * nval / 100.0f;
                case vmin:
                    return CSSUnits.points(Math.min(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight())) * nval / 100.0f;
                case vmax:
                    return CSSUnits.points(Math.max(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight())) * nval / 100.0f;
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
    public float ptLength(TermLengthOrPercent spec)
    {
        return ptLength(spec, 0);
    }
    
    /** 
     * Converts a length from a CSS length or percentage to 'px'. The current font size is used for em units.
     * @param spec the CSS length specification
     * @param whole the value that corresponds to 100%. It is used only when spec is a percentage.
     * @return the length in 'px' 
     */
    public float pxLength(TermLengthOrPercent spec, float whole)
    {
        float nval = spec.getValue();
        if (spec.isPercentage())
        {
            return (whole * nval) / 100.0f;
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
                    return nval * CSSUnits.dpi / 72.0f;
                case in:
                    return nval * CSSUnits.dpi;
                case cm:
                    return (nval * CSSUnits.dpi) / 2.54f;
                case mm:
                    return (nval * CSSUnits.dpi) / 25.4f;
                case q:
                    return (nval * CSSUnits.dpi) / (2.54f * 40.0f);
                case pc:
                    return (nval * 12 * CSSUnits.dpi) / 72;
                case px:
                    return nval;
                case em:
                    return CSSUnits.pixels(getFontSize() * nval); //font size is in pt
                case rem:
                    return CSSUnits.pixels(getRem() * nval);
                case ex:
                    return CSSUnits.pixels(getEx() * nval);
                case ch:
                    return CSSUnits.pixels(getCh() * nval);
                case vw:
                    return viewport.getVisibleRect().getWidth() * nval / 100.0f;
                case vh:
                    return viewport.getVisibleRect().getHeight() * nval / 100.0f;
                case vmin:
                    return Math.min(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval / 100.0f;
                case vmax:
                    return Math.max(viewport.getVisibleRect().getWidth(), viewport.getVisibleRect().getHeight()) * nval / 100.0f;
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
    public float pxLength(TermLengthOrPercent spec)
    {
        return pxLength(spec, 0);
    }
    
    /**
     * Converts an angle from a CSS angle to 'deg'.
     * @param spec the CSS angle specification
     * @return the corresponding angle in radians
     */
    public float degAngle(TermAngle spec)
    {
        float nval = spec.getValue();
        final TermLength.Unit unit = spec.getUnit();
        if (spec instanceof TermCalcAngleImpl)
        {
            final CalcArgs args = ((TermCalc) spec).getArgs();
            return args.evaluate(getDegEval());
        }
        else
        {
            if (unit == null)
                return 0;
            switch (unit)
            {
                case deg:
                    return nval; 
                case grad:
                    return (nval * 200.0f) / (float) Math.PI;
                case rad:
                    return (nval * 180.0f) / (float) Math.PI;
                case turn:
                    return nval * 360.0f;
                default:
                    return 0;
            }
        }
    }
    
    /**
     * Converts an angle from a CSS angle to 'rad'.
     * @param spec the CSS angle specification
     * @return the corresponding angle in radians
     */
    public float radAngle(TermAngle spec)
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
                    return (nval * (float) Math.PI) / 180.0f; 
                case grad:
                    return (nval * (float) Math.PI) / 200.0f;
                case rad:
                    return nval;
                case turn:
                    return nval * 2.0f * (float) Math.PI;
                default:
                    return 0.0f;
            }
        }
    }
    
    //===================================================================================
    
    /** 
     * Scans a list of font definitions and chooses the first one that is available
     * @param list of terms obtained from the font-family property
     * @return a font name string according to java.awt.Font
     */
    protected String findFontName(TermList list, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        String ret = null;
        for (Term<?> term : list)
        {
            Object value = term.getValue();
            if (value instanceof CSSProperty.FontFamily) //logical font
            {
                ret = findLogicalFont((CSSProperty.FontFamily) value, weight, style);
            }
            else //physical font
            {
                ret = lookupFont(value.toString(), weight, style);
            }
            if (ret != null)
                break;
        }
        return ret;
    }

    /**
     * Tries to find a physical font name for a given logical font
     * @param ff the logical font family according to CSS specification
     * @param weight the font weight to match
     * @param style the font style to match
     * @return physical font family name or {@code null} when nothing has been found
     */
    protected String findLogicalFont(CSSProperty.FontFamily ff, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        final List<String> flist = getConfig().getLogicalFont(ff.toString());
        for (String cand : flist)
        {
            String found = lookupFont(cand, weight, style);
            if (found != null)
                return found;
        }
        return null;
    }
    
    /**
     * Checks if the font family is available either among the CSS defined fonts or the system fonts.
     * If found, registers a system font with the given name. 
     * @param family Required font family
     * @param weight Required font weight
     * @param style Required font style
     * @return The corresponding system font name or {@code null} when no candidates have been found. 
     */
    protected String lookupFont(String family, CSSProperty.FontWeight weight, CSSProperty.FontStyle style)
    {
        //try to look in the style font table
        String nameFound = null;
        FontSpec spec = new FontSpec(family, weight, style);
        boolean isItalic = (style == FontStyle.ITALIC || style == FontStyle.OBLIQUE);
        boolean isBold = FontSpec.representsBold(weight);
        List<RuleFontFace.Source> srcs = findMatchingFontSources(spec);
        if (srcs != null)
        {
            for (RuleFontFace.Source src : srcs)
            {
                if (src instanceof RuleFontFace.SourceLocal)
                {
                    String name = fontAvailable(((RuleFontFace.SourceLocal) src).getName(), isItalic, isBold);
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
                        final TermURI urlstring = ((RuleFontFace.SourceURL) src).getURI();
                        final String format = ((RuleFontFace.SourceURL) src).getFormat();
                        nameFound = registerExternalFont(urlstring, format);
                    } catch (MalformedURLException e) {
                        log.error("Couldn't load font with URI {} ({})", ((RuleFontFace.SourceURL) src).getURI(), e.getMessage());
                    } catch (IOException e) {
                        log.error("Couldn't load font with URI {} ({})", ((RuleFontFace.SourceURL) src).getURI(), e.getMessage());
                    }
                }
            }
        }
        //if nothing found, try the system font names
        if (nameFound == null)
        {
            nameFound = fontAvailable(family, isItalic, isBold);
        }
        //create the font when found
        return nameFound;
    }

    /**
     * Finds the CSS font definitions matching the font specification.
     * @param spec The font specification to match
     * @return a list of available font sources or {@code null} when the font tables are not available
     */
    protected List<RuleFontFace.Source> findMatchingFontSources(FontSpec spec)
    {
        if (getFontTable() != null)
            return getFontTable().findBestMatch(spec);
        else
            return null;
    }
    
    /** 
     * Find the font family available with the given family name, bold and italic properties,
     * or return {@code null} if the font is not available
     * @return The exact name of the font family or {@code null} if the font is not available
     */
    protected abstract String fontAvailable(String family, boolean isBold, boolean isItalic);

    /**
     * Gets the name of any usable font in the system. This is used as the last fallback
     * if all other alternatives have failed.
     * @return Any usable physical font name
     */
    protected abstract String getFallbackFont();
    
    /**
     * Decodes and registers a new font based on its URL.
     * @param urlstring the external font URL
     * @param format the font format according to CSS spec (e.g. 'truetype')
     * @return The registed font name or {@code null} when failed
     * @throws MalformedURLException
     * @throws IOException
     */
    protected abstract String registerExternalFont(TermURI urlstring, String format)
            throws MalformedURLException, IOException;
    
    //============================================================================================================================
    
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
    
    private DegEvaluator getDegEval()
    {
        if (degEval == null)
            degEval = new DegEvaluator(this);
        return degEval;
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
    private abstract class UnitEvaluator extends CalcArgs.FloatEvaluator
    {
        protected VisualContext ctx;
        protected float whole; //whole size used for percentages
        
        public UnitEvaluator(VisualContext ctx)
        {
            this.ctx = ctx;
        }

        public UnitEvaluator setWhole(float whole)
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
        public float resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.pxLength((TermLengthOrPercent) val, whole);
            else
                return 0.0f; //this should not happen
        }
    }
    
    private class PtEvaluator extends UnitEvaluator
    {
        public PtEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public float resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.ptLength((TermLengthOrPercent) val, whole);
            else
                return 0.0f; //this should not happen
        }
    }
    
    private class RadEvaluator extends UnitEvaluator
    {
        public RadEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public float resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.radAngle((TermAngle) val);
            else
                return 0.0f; //this should not happen
        }
    }
    
    private class DegEvaluator extends UnitEvaluator
    {
        public DegEvaluator(VisualContext ctx)
        {
            super(ctx);
        }

        @Override
        public float resolveValue(TermFloatValue val)
        {
            if (val instanceof TermLengthOrPercent)
                return ctx.degAngle((TermAngle) val);
            else
                return 0.0f; //this should not happen
        }
    }

    //===================================================================================

    /**
     * Gets the image loader that can be used for obtaining ContentImage instances from URLs.
     * @return the image loader
     */
    public abstract ImageLoader getImageLoader();
    
}
