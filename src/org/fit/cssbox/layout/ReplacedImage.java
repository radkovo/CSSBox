/*
 * ImgBox.java
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
 * Created on 4. prosinec 2005, 21:01
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

/**
 * This class represents an image as the contents of a replaced element
 * 
 * @author  radek
 */
public class ReplacedImage extends ReplacedContent implements ImageObserver
{
    private static boolean LOAD_IMAGES = true;
    
    private URL base; //document base url
    private URL url;  //image url
    private VisualContext ctx; //visual context
    private BufferedImage img; //the loaded image
    
    /** 
     * Creates a new instance of ImgBox 
     */
    public ReplacedImage(ElementBox owner, VisualContext ctx, URL baseurl)
    {
    	super(owner);
        this.ctx = ctx;
        this.base = baseurl;
        try {
            String src = getOwner().getElement().getAttribute("src");
            url = new URL(base, src);
            if (LOAD_IMAGES)
            {
                System.err.println("Loading image: " + url);
                img = javax.imageio.ImageIO.read(url);
            }
         } catch (MalformedURLException e) {
             System.err.println("ImgBox: URL: " + e.getMessage());
             img = null;
             url = null;
         } catch (IOException e) {
             System.err.println("ImgBox: I/O: " + e.getMessage());
             img = null;
         } catch (IllegalArgumentException e) {
             System.err.println("ImgBox: Format error: " + e.getMessage());
             img = null;
         }
    }
    
    /**
     * Switches automatic image data downloading on or off.
     * @param b when set to <code>true</code>, the images are automatically loaded
     * from the server. When set to <code>false</code>, the images are not loaded
     * and the corresponding box is displayed empty. When the image loading is switched
     * off, the box size can be only determined from the element attributes or style.
     * The default value is on.
     */
    public static void setLoadImages(boolean b)
    {
        LOAD_IMAGES = b;
    }

    /**
	 * @return the url of the image
	 */
	public URL getUrl()
	{
		return url;
	}

	public void draw(Graphics g, int width, int height)
    {
        ctx.updateGraphics(g);
        if (img != null)
            g.drawImage(img, getOwner().getAbsoluteContentX(), getOwner().getAbsoluteContentY(), width, height, this);
        else
        {
            g.drawRect(getOwner().getAbsoluteContentX(), getOwner().getAbsoluteContentY(), getOwner().getContentWidth(), getOwner().getContentHeight());
            g.drawLine(getOwner().getAbsoluteContentX(), getOwner().getAbsoluteContentY(), getOwner().getAbsoluteContentX() + getOwner().getContentWidth(), getOwner().getAbsoluteContentY() + getOwner().getContentHeight());
            g.drawLine(getOwner().getAbsoluteContentX() + getOwner().getContentWidth(), getOwner().getAbsoluteContentY(), getOwner().getAbsoluteContentX(), getOwner().getAbsoluteContentY() + getOwner().getContentHeight());
        }
    }

	@Override
	public int getIntrinsicHeight()
	{
		if (img != null)
			return img.getHeight();
		else
			return 20;
	}

	@Override
	public int getIntrinsicWidth()
	{
		if (img != null)
			return img.getWidth();
		else
			return 20;
	}

    @Override
    public float getIntrinsicRatio()
    {
        return (float) getIntrinsicWidth() / (float) getIntrinsicHeight();
    }
    
    
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        return false;
    }
    
}
