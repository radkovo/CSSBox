/*
 * ReferenceResults.java
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
 * Created on 1. 1. 2016, 13:59:34 by burgetr
 */
package org.fit.cssbox.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

/**
 * Representation of the saved reference results.
 * 
 * @author burgetr
 */
public class ReferenceResults extends LinkedHashMap<String, Float>
{
    private static final long serialVersionUID = 1L;

    public static final float SUCCESS_THRESHOLD = 0.001f;
    public static final float COMPARISON_THRESHOLD = 0.001f;
    
    private int successCnt;
    private int failCnt;
    private int fatalCnt;
    
    public ReferenceResults()
    {
        try
        {
            successCnt = failCnt = fatalCnt = 0;
            loadCSV(getClass().getResourceAsStream("/test_reference.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getSuccessCnt()
    {
        return successCnt;
    }

    public int getFailCnt()
    {
        return failCnt;
    }

    public int getFatalCnt()
    {
        return fatalCnt;
    }

    private void loadCSV(InputStream is) throws IOException
    {
        BufferedReader read = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = read.readLine()) != null)
        {
            String[] vals = line.split(",");
            float val = Float.parseFloat(vals[1]);
            put(vals[0], val);
            if (val <= SUCCESS_THRESHOLD)
                successCnt++;
            else
                failCnt++;
            if (val == 1.0f)
                fatalCnt++;
        }
    }
    
}
