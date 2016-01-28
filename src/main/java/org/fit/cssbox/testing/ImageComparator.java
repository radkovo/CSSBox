/*
 * ImageComparator.java
 * Copyright (c) 2005-2015 Radek Burget
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
 * Created on 27. 12. 2015, 0:14:29 by burgetr
 */
package org.fit.cssbox.testing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author burgetr
 */
public class ImageComparator
{
    private BufferedImage diff;
    private int totalcnt; //total number of pixels
    private int diffcnt; //number of different pixels
    private String errorDescr; //error description
    
    public ImageComparator(BufferedImage img1, BufferedImage img2)
    {
        errorDescr = null;
        diff = createDifferenceImage(img1, img2);
    }

    public float getErrorRate()
    {
        return (float) diffcnt / (float) totalcnt;
    }
    
    public String getErrorDescription()
    {
        return errorDescr;
    }
    
    public BufferedImage getDifferenceImage()
    {
        return diff;
    }
    
    private BufferedImage createDifferenceImage(BufferedImage img1, BufferedImage img2) 
    {
        // convert images to pixel arrays...
        final int w1 = img1.getWidth();
        final int h1 = img1.getHeight(); 
        final int w2 = img2.getWidth();
        final int h2 = img2.getHeight();
        if (w1 != w2 || h1 != h2)
        {
            errorDescr = "Image sizes don't match";
            diffcnt = totalcnt = 1;
            return img1;
        }
        else
        {
            final int highlight = Color.MAGENTA.getRGB();
            final int[] p1 = img1.getRGB(0, 0, w1, h1, null, 0, w1);
            final int[] p2 = img2.getRGB(0, 0, w1, h1, null, 0, w1);
            totalcnt = p1.length;
            diffcnt = 0;
            // compare img1 to img2, pixel by pixel. If different, highlight img1's pixel...
            for (int i = 0; i < p1.length; i++)
            {
                if (p1[i] != p2[i])
                {
                    p1[i] = highlight;
                    diffcnt++;
                }
            }
            // save img1's pixels to a new BufferedImage, and return it...
            // (May require TYPE_INT_ARGB)
            final BufferedImage out = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
            out.setRGB(0, 0, w1, h1, p1, 0, w1);
            return out;
        }
    }
}
