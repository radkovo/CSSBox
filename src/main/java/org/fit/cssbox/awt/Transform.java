/*
 * Transform.java
 * Copyright (c) 2005-2016 Radek Burget
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
 * Created on 26. 11. 2016, 18:15:15 by burgetr
 */
package org.fit.cssbox.awt;

import java.awt.geom.AffineTransform;

import org.fit.cssbox.layout.CSSDecoder;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Rectangle;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermList;

/**
 * Shared graphical transformation methods that may be shared among renderers.
 * 
 * @author burgetr
 */
public class Transform
{

    /**
     * Creates an AffineTransform that corresponds to the CSS transformations delared
     * for a given element.
     * @param elem The source element box.
     * @return an AffineTransform object describing the transformation or {@code null} when
     * no transformation should be applied on the given element.
     */
    public static AffineTransform createTransform(ElementBox elem)
    {
        if (elem.isBlock() || elem.isReplaced())
        {
            CSSDecoder dec = new CSSDecoder(elem.getVisualContext());
            Rectangle bounds = elem.getAbsoluteBorderBounds();
            //decode the origin
            float ox, oy;
            CSSProperty.TransformOrigin origin = elem.getStyle().getProperty("transform-origin");
            if (origin == CSSProperty.TransformOrigin.list_values)
            {
                TermList values = elem.getStyle().getValue(TermList.class, "transform-origin");
                ox = dec.getLength((TermLengthOrPercent) values.get(0), false, bounds.width / 2, 0, bounds.width);
                oy = dec.getLength((TermLengthOrPercent) values.get(1), false, bounds.height / 2, 0, bounds.height);
            }
            else
            {
                ox = bounds.width / 2;
                oy = bounds.height / 2;
            }
            ox += bounds.x;
            oy += bounds.y;
            //compute the transformation matrix
            AffineTransform ret = null;
            CSSProperty.Transform trans = elem.getStyle().getProperty("transform");
            if (trans == CSSProperty.Transform.list_values)
            {
                ret = new AffineTransform();
                ret.translate(ox, oy);
                TermList values = elem.getStyle().getValue(TermList.class, "transform");
                for (Term<?> term : values)
                {
                    if (term instanceof TermFunction.Rotate)
                    {
                        double theta = dec.getAngle(((TermFunction.Rotate) term).getAngle());
                        ret.rotate(theta);
                    }
                    else if (term instanceof TermFunction.Translate)
                    {
                        float tx = dec.getLength(((TermFunction.Translate) term).getTranslateX(), false, 0, 0, bounds.width);
                        float ty = dec.getLength(((TermFunction.Translate) term).getTranslateY(), false, 0, 0, bounds.height);
                        ret.translate(tx, ty);
                    }
                    else if (term instanceof TermFunction.TranslateX)
                    {
                        float tx = dec.getLength(((TermFunction.TranslateX) term).getTranslate(), false, 0, 0, bounds.width);
                        ret.translate(tx, 0.0);
                    }
                    else if (term instanceof TermFunction.TranslateY)
                    {
                        float ty = dec.getLength(((TermFunction.TranslateY) term).getTranslate(), false, 0, 0, bounds.height);
                        ret.translate(0.0, ty);
                    }
                    else if (term instanceof TermFunction.Scale)
                    {
                        float sx = ((TermFunction.Scale) term).getScaleX();
                        float sy = ((TermFunction.Scale) term).getScaleY();
                        ret.scale(sx, sy);
                    }
                    else if (term instanceof TermFunction.ScaleX)
                    {
                        float sx = ((TermFunction.ScaleX) term).getScale();
                        ret.scale(sx, 1.0);
                    }
                    else if (term instanceof TermFunction.ScaleY)
                    {
                        float sy = ((TermFunction.ScaleY) term).getScale();
                        ret.scale(1.0, sy);
                    }
                    else if (term instanceof TermFunction.Skew)
                    {
                        double ax = dec.getAngle(((TermFunction.Skew) term).getSkewX());
                        double ay = dec.getAngle(((TermFunction.Skew) term).getSkewY());
                        ret.shear(Math.tan(ax), Math.tan(ay));
                    }
                    else if (term instanceof TermFunction.SkewX)
                    {
                        double ax = dec.getAngle(((TermFunction.SkewX) term).getSkew());
                        ret.shear(Math.tan(ax), 0.0);
                    }
                    else if (term instanceof TermFunction.SkewY)
                    {
                        double ay = dec.getAngle(((TermFunction.SkewY) term).getSkew());
                        ret.shear(0.0, Math.tan(ay));
                    }
                    
                    else if (term instanceof TermFunction.Matrix)
                    {
                        float[] vals = ((TermFunction.Matrix) term).getValues();
                        ret.concatenate(new AffineTransform(vals));
                    }
                }
                ret.translate(-ox, -oy);
            }
            
            return ret;
        }
        else
            return null;
    }
    
}
