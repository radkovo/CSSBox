/**
 * ReplacedBox.java
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
 * Created on 6.2.2009, 16:48:52 by burgetr
 */
package org.fit.cssbox.layout;

/**
 * Common interface for both the inline and block replaced boxes
 * 
 * @author burgetr
 */
public interface ReplacedBox
{
    
    /**
     * @return the content object
     */
    public ReplacedContent getContentObj();

    /**
     * @param obj the obj to set
     */
    public void setContentObj(ReplacedContent obj);
    

}
