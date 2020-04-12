/*
 * RadialGradient.java
 * Copyright (c) 2005-2020 Radek Burget
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
 */
package org.fit.cssbox.render;

import java.util.ArrayList;
import java.util.Collections;

import org.fit.cssbox.layout.Rectangle;

/**
 * A radial gradient representation.
 *
 * @author safar
 * @author burgetr
 */
public class RadialGradient extends Gradient
{
    // an elipse/circle switch
    private boolean circle;

    public float rxy;

    public double xAxis;
    public double yAxis;
    public double xAxisPerc;
    public double yAxisPerc;

    // center point
    public float sx;
    public float sy;
    public float sxPerc;
    public float syPerc;

    // SVG element values TODO
    public double cx;
    public double cy;
    public double fx;
    public double fy;
    public double r;

    public Rectangle rect;
    public Rectangle newRect;

    public double newHeight;
    public double newWidth;

    public enum RadLengths
    {
        CLOSEST_CORNER, CLOSEST_SIDE, FARTHEST_CORNER, FARTHEST_SIDE
    }

    /**
     * Creates a radial gradient with the given size.
     *
     * @param rect
     */
    public RadialGradient(Rectangle rect)
    {
        super();
        circle = true;
        this.rect = rect;
        newRect = new Rectangle();
        newRect.x = rect.x;
        newRect.y = rect.y;
        newRect.width = rect.width;
        newRect.height = rect.height;
    }

    /**
     * Checks whether this is a circle gradient.
     * @return {@code true} for circle, {@code false} for ellipse.
     */
    public boolean isCircle()
    {
        return circle;
    }

    /**
     * tato metoda slouzi k vypoctu SVG parametru podle zadanych parametru z CSS
     * radialniho gradientu probiha zde prepocet z absolutnich vzdalenosti v
     * pixelech na procentualni vyjadreni, ktere se pouziva v SVG
     *
     * @param rx
     * @param ry
     * @param sx
     * @param sy
     */
    public void setEllipseData(double rx, double ry, float sx, float sy)
    {
        circle = false;

        cx = (double) sx / (double) rect.width * 100;
        fx = (double) sx / (double) rect.width * 100;
        cy = (double) sy / (double) rect.height * 100;
        fy = (double) sy / (double) rect.height * 100;

        xAxis = (double) rx / (double) rect.width * 100;
        yAxis = (double) ry / (double) rect.height * 100;

        // kvuli odlisnosti SVG a CSS gradientu (viz technicka zprava) je nutne dopocitat rozmery elementu, na ktery bude gradient apliovan
        if (ry > rx)
        {
            r = xAxis;
            newHeight = rect.width * (double) ry / (double) rx;
            newWidth = rect.width;
        }
        else
        {
            r = yAxis;
            newWidth = rect.height * (double) rx / (double) ry;
            newHeight = rect.height;
        }
    }

    /**
     * metoda slouzi pro zpracovani procentualnich hodnot z CSS gradientu
     *
     * @param rxp
     * @param ryp
     * @param sxp
     * @param syp
     */
    public void setEllipseDataPercent(double rxp, double ryp, float sxp, float syp)
    {
        setEllipseData(rxp * rect.width / 100, ryp * rect.height / 100, sxp * rect.width / 100,
                syp * rect.height / 100);
    }

    /**
     * tato metoda dopocitava rozmery pro elipticky gradient v pripade, ze jsou
     * rozmery zadany pomoci klicovych slov vsechny hodnoty jsou vypocitany v
     * pixelech a pak je volana metoda setEllipseData, ktera dopocita prislusne
     * hodnoty pro SVG
     *
     * @param rl
     * @param sx
     * @param sy
     */
    public void setEllipseDataRadLengths(RadLengths rl, float sx, float sy)
    {
        double rx = 0;
        double ry = 0;
        double distTop;
        double distLeft;
        double distRight;
        double distBot;
        double ratio;
        float xx, yy;
        double pom;
        int i;

        switch (rl)
        {
            // vypocet vzdalenosti k nejbl
            case CLOSEST_CORNER:
                i = getIndexOfMinCornerDistance(sx, sy);
                if (i == 0)
                { //A
                    xx = 0;
                    yy = 0;
                }
                else if (i == 1)
                { //D
                    xx = 0;
                    yy = rect.height;
                }
                else if (i == 2)
                { // B
                    xx = rect.width;
                    yy = rect.height;
                }
                else
                { // C
                    xx = rect.width;
                    yy = 0;
                }
                ratio = (double) sx / (double) sy;

                pom = (xx - sx) * (xx - sx) / (ratio * ratio) + (yy - sy) * (yy - sy);
                ry = Math.sqrt(pom);
                rx = ratio * ry;
                break;

            case FARTHEST_CORNER:
                i = getIndexOfMaxCornerDistance(sx, sy);
                if (i == 0)
                { //A
                    xx = 0;
                    yy = 0;
                }
                else if (i == 1)
                { //D
                    xx = 0;
                    yy = rect.height;
                }
                else if (i == 2)
                { // B
                    xx = rect.width;
                    yy = rect.height;
                }
                else
                { // C
                    xx = rect.width;
                    yy = 0;
                }
                ratio = (double) sx / (double) sy;

                pom = (xx - sx) * (xx - sx) / (ratio * ratio) + (yy - sy) * (yy - sy);
                ry = Math.sqrt(pom);
                rx = ratio * ry;
                break;

            case CLOSEST_SIDE:
                distTop = getCoordinateDistance(sy, 0);
                distBot = getCoordinateDistance(sy, rect.height);
                distLeft = getCoordinateDistance(sx, 0);
                distRight = getCoordinateDistance(sx, rect.width);
                rx = Math.min(distLeft, distRight);
                ry = Math.min(distTop, distBot);
                break;
            case FARTHEST_SIDE:
                distTop = getCoordinateDistance(sy, 0);
                distBot = getCoordinateDistance(sy, rect.height);
                distLeft = getCoordinateDistance(sx, 0);
                distRight = getCoordinateDistance(sx, rect.width);
                rx = Math.max(distLeft, distRight);
                ry = Math.max(distTop, distBot);
                break;
        }
        setEllipseData(rx, ry, sx, sy);
    }

    private int getIndexOfMinCornerDistance(float sx, float sy)
    {
        ArrayList<Double> l = getAllCornersDistance(sx, sy);
        return l.indexOf(Collections.min(l));
    }

    private int getIndexOfMaxCornerDistance(float sx, float sy)
    {
        ArrayList<Double> l = getAllCornersDistance(sx, sy);
        return l.indexOf(Collections.max(l));

    }

    /**
     * prepocet zadanych rozmeru a souradnic stredu gradientu na procentualni
     * vyjadreni vzhledem k rozmerum elementu
     * 
     * @param rxy
     * @param sx
     * @param sy
     */
    public void setCircleData(double rxy, float sx, float sy)
    {
        circle = true;

        cx = (double) sx / (double) rect.width * 100;
        fx = (double) sx / (double) rect.width * 100;
        cy = (double) sy / (double) rect.height * 100;
        fy = (double) sy / (double) rect.height * 100;

        // polomer je vybran podle delsi ze stran
        r = (double) rxy / (double) Math.max(rect.width, rect.height) * 100;
    }

    /**
     * stejne jako setCircleData, ale hodnoty stredu gradientu jsou zadany v
     * procentech.
     * 
     * @param rxy
     * @param sxp
     * @param syp
     */
    public void setCircleDataPercent(double rxy, float sxp, float syp)
    {
        setCircleData(rxy, sxp * rect.width / 100, syp * rect.height / 100);
    }

    /**
     * vypocet pro pripad, ze je rozmer gradientu zadan jednim z klicovych slov
     * 
     * @param rl
     * @param sx
     * @param sy
     */
    public void setCircleDataRadLengths(RadLengths rl, float sx, float sy)
    {
        double rxy = 0;
        ArrayList<Double> l;
        switch (rl)
        {
            case CLOSEST_CORNER:
                l = getAllCornersDistance(sx, sy);
                rxy = Collections.min(l);
                break;
            case CLOSEST_SIDE:
                l = getAllSidesDistance(sx, sy);
                rxy = Collections.min(l);
                break;
            case FARTHEST_CORNER:
                l = getAllCornersDistance(sx, sy);
                rxy = Collections.max(l);
                break;
            case FARTHEST_SIDE:
                l = getAllSidesDistance(sx, sy);
                rxy = Collections.max(l);
                break;
        }
        setCircleData(rxy, sx, sy);
    }

    public void setCircleDataPercentRadLengths(RadLengths rl, float sxp, float syp)
    {
        setCircleDataRadLengths(rl, sxp * rect.width / 100, syp * rect.height / 100);
    }

    private ArrayList<Double> getAllCornersDistance(float centerX, float centerY)
    {
        ArrayList<Double> l = new ArrayList<Double>();

        l.add(getPointsDistance(centerX, centerY, 0, 0)); // A
        l.add(getPointsDistance(centerX, centerY, 0, rect.height)); // D
        l.add(getPointsDistance(centerX, centerY, rect.width, 0)); // B
        l.add(getPointsDistance(centerX, centerY, rect.width, rect.height)); // C

        return l;
    }

    private ArrayList<Double> getAllSidesDistance(float centerX, float centerY)
    {
        ArrayList<Double> l = new ArrayList<Double>();

        l.add(getCoordinateDistance(centerX, 0));
        l.add(getCoordinateDistance(centerX, rect.width));
        l.add(getCoordinateDistance(centerY, 0));
        l.add(getCoordinateDistance(centerY, rect.height));
        return l;
    }

    private double getPointsDistance(double x1, double y1, double x2, double y2)
    {
        double dx = Math.abs(x1 - x2);
        double dy = Math.abs(y1 - y2);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double getCoordinateDistance(double x1, double x2)
    {
        return Math.abs(x1 - x2);
    }

}
