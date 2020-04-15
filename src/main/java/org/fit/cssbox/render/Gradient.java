/*
 * LinearGradient.java
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
 * Created on 27. 9. 2019, 12:34:00 by burgetr
 */
package org.fit.cssbox.render;

import java.util.ArrayList;
import java.util.List;

/**
 * A base for all the gradients.
 * 
 * @author burgetr
 */
public abstract class Gradient
{
    private boolean repeating;
    private List<GradientStop> stops;
    private float maxPercentage;
    

    public Gradient()
    {
        repeating = false;
        stops = new ArrayList<>();
        maxPercentage = 0f;
    }
    
    public boolean isRepeating()
    {
        return repeating;
    }

    public void setRepeating(boolean repeating)
    {
        this.repeating = repeating;
    }

    /**
     * Obtains the total gradient length (100%) for computing the stop percentages.
     * @return the gradient length in pixels
     */
    public abstract float getLength();
    
    public List<GradientStop> getStops()
    {
        return stops;
    }
    
    public void addStop(GradientStop stop)
    {
        stops.add(stop);
    }

    /**
     * Returns the absolute length if it is specified for the last step.
     * @return The length converted to pixels or {@code null} if the stop list is empty or there
     * is no absolute length specified for the last stop.
     */
    public Float getLastLengthPx()
    {
        if (!stops.isEmpty())
            return stops.get(stops.size() - 1).getPxLength();
        else
            return null;
    }
    
    /**
     * Computes missing percentages for stops.
     */
    public void recomputeStops()
    {
        recomputePercentages();
        if (!stops.isEmpty())
        {
            //the first one is 0% if missing
            GradientStop first = stops.get(0);
            if (first.getPercentage() == null)
                first.setPercentage(0.0f);
            //the last missing stop is 100%
            GradientStop last = stops.get(stops.size() - 1);
            if (last.getPercentage() == null)
                last.setPercentage(100.0f);
            //remaining stops are half between
            int f = -1; //first unknown percentage position
            for (int i = 1; i < stops.size(); i++)
            {
                GradientStop stop = stops.get(i);
                if (stop.getPercentage() == null)
                {
                    if (f == -1)
                    {
                        f = i;
                    }
                }
                else
                {
                    if (f != -1)
                    {
                        float fPerc = stops.get(f - 1).getPercentage();
                        float lPerc = stop.getPercentage();
                        float frac = (lPerc - fPerc) / (i - f + 1);
                        for (int j = f; j < i; j++)
                        {
                            stops.get(j).setPercentage(frac * (j - f + 1));
                        }
                        f = -1;
                    }
                }
            }
        }
    }
    
    private void recomputePercentages()
    {
        maxPercentage = 0f;
        final float whole = (isRepeating() && getLastLengthPx() != null) ? getLastLengthPx() : getLength();
        //final float whole = getLength();
        for (GradientStop stop : stops)
        {
            //recompute the percentages for the absolute stops
            if (stop.getPxLength() != null)
            {
                stop.setPercentage((stop.getPxLength() / whole) * 100.0f);
            }
            //ensure that the percentages are increasing
            if (stop.getPercentage() != null)
            {
                if (stop.getPercentage() < maxPercentage)
                    stop.setPercentage(maxPercentage);
                else
                    maxPercentage = stop.getPercentage();
            }
        }
    }
    
}
