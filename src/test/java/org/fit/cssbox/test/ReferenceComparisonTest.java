/*
 * ReferenceComparisonTest.java
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
 * Created on 2. 1. 2016, 19:20:34 by burgetr
 */
package org.fit.cssbox.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.fit.cssbox.testing.ReferenceResults;
import org.fit.cssbox.testing.TestBatch;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author burgetr
 */
public class ReferenceComparisonTest
{
    private static final int THREADS = 1;

    @Test
    public void checkForRegressions() throws MalformedURLException
    {
        ReferenceResults ref = new ReferenceResults();
        URL url = new URL("file://" + System.getProperty("user.home") + "/tmp/CSSBoxTesting/baseline/nightly-unstable/html4/");
        TestBatch tester = new TestBatch(url, THREADS);
        
        int errorcnt = 0;
        if (tester.getTestCount() > 0)
        {
            ArrayList<String> refNames = new ArrayList<String>(); 
            for (Map.Entry<String, Float> item : ref.entrySet())
            {
                if (item.getValue() <= ReferenceResults.SUCCESS_THRESHOLD)
                    refNames.add(item.getKey());
            }
            
            tester.runTests(refNames);
            Map<String, Float> results = tester.getResults();
            for (Map.Entry<String, Float> item : results.entrySet())
            {
                String name = item.getKey();
                float val = item.getValue();
                float refval = ref.get(name);
                if (val < 0.0f)
                {
                    System.err.println(name + " : execution failed");
                    errorcnt++;
                }
                if (val - refval > ReferenceResults.COMPARISON_THRESHOLD)
                {
                    System.err.println(name + " : regression found (" + val + " > " + refval + ")");
                    if (refval == 0.0f)
                    {
                        errorcnt++; //count as error when the original rate was 0.0 (a broken test)
                        System.err.println("  (broken test)");
                    }
                    else
                        System.err.println("  (but the original value was not 0.0 neither)");
                }
            }
        }
        else
            System.err.println("No tests found, giving up testing.");
        Assert.assertTrue("All results passed", errorcnt == 0);
    }
    
}
