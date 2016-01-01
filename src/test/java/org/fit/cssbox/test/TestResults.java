/*
 * TestResults.java
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
 * Created on 27. 12. 2015, 12:25:54 by burgetr
 */
package org.fit.cssbox.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author burgetr
 */
public class TestResults
{
    private static Logger log = LoggerFactory.getLogger(TestResults.class);
    
    private static List<String> tagBlacklist;
    static {
        tagBlacklist = new ArrayList<String>();
        tagBlacklist.add("svg");
        tagBlacklist.add("dom/js");
    }
    
    private URL testURL;
    private List<SourceEntry> tests;
    private List<ResultEntry> results;
    
    public TestResults(URL testURL)
    {
        this.testURL = testURL;
        this.tests = new LinkedList<SourceEntry>();
        this.results = new LinkedList<ResultEntry>();
        parseToc();
    }
    
    private void parseToc()
    {
        try
        {
            URL tocURL = new URL(testURL, "reftest-toc.htm");
            DefaultDocumentSource docSource = new DefaultDocumentSource(tocURL.toString());
            DefaultDOMSource parser = new DefaultDOMSource(docSource);
            Document doc = parser.parse();
            
            NodeList tables = doc.getElementsByTagName("table");
            if (tables.getLength() == 1)
            {
                Element table = (Element) tables.item(0);
                NodeList bodies = table.getElementsByTagName("tbody");
                for (int bi = 0; bi < bodies.getLength(); bi++)
                {
                    Element body = (Element) bodies.item(bi);
                    NodeList rows = body.getElementsByTagName("tr");
                    for (int ri = 0; ri < rows.getLength(); ri++)
                    {
                        Element row = (Element) rows.item(ri);
                        //find link to test source
                        NodeList links = row.getElementsByTagName("a");
                        if (links.getLength() > 0)
                        {
                            Element a = (Element) links.item(0);
                            SourceEntry entry = new SourceEntry();
                            entry.name = a.getTextContent().trim();
                            entry.src = a.getAttribute("href");
                            
                            //find tags (if any)
                            NodeList tags = row.getElementsByTagName("abbr");
                            for (int ti = 0; ti < tags.getLength(); ti++)
                            {
                                Element tag = (Element) tags.item(ti);
                                entry.tags.add(tag.getTextContent().trim().toLowerCase());
                            }
                            
                            tests.add(entry);
                        }
                        else
                            log.error("No links in table row");
                    }
                }
                
                log.info("Loaded " + tests.size() + " source entries");
            }
            else
                log.error("Couldn't identify the TOC table");
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    
    public void runTests()
    {
        //ExecutorService exec = Executors.newSingleThreadExecutor();
        ExecutorService exec = Executors.newFixedThreadPool(10);
        List<Callable<Float>> list = getTestList();
        try
        {
            List<Future<Float>> futures = exec.invokeAll(list, list.size() * 5, TimeUnit.SECONDS);
            for (int i = 0; i < list.size(); i++)
            {
                Future<Float> future = futures.get(i);
                ResultEntry entry = new ResultEntry();
                entry.name = ((ReferenceTest) list.get(i)).getName();
                System.err.println("Waiting for " + entry.name);
                try
                {
                    entry.result = future.get();
                } catch (ExecutionException e) {
                    entry.result = 1.0f;
                } catch (CancellationException e) {
                    entry.result = 1.0f;
                }
                results.add(entry);
            }
        } catch (InterruptedException e) {
            log.error("Interrupted: {}", e.getMessage());
        }
    }

    private List<Callable<Float>> getTestList()
    {
        List<Callable<Float>> ret = new LinkedList<Callable<Float>>();
        for (SourceEntry entry : tests)
        {
            boolean blacklisted = false;
            for (String tag : entry.tags)
            {
                if (tagBlacklist.contains(tag))
                    blacklisted = true;
            }
            if (!blacklisted)
            {
                try
                {
                    URL url = new URL(testURL, entry.src);
                    ReferenceTest test = new ReferenceTest(entry.name, url.toString());
                    ret.add(test);
                } catch (MalformedURLException e) {
                    log.error("getListTest: {}", e.getMessage());
                }
            }
            else
                log.info("Skipped " + entry.name);
        }
        return ret;
    }
    
    public ResultEntry runTest(SourceEntry entry)
    {
        try
        {
            URL url = new URL(testURL, entry.src);
            ReferenceTest test = new ReferenceTest(entry.name, url.toString());
            float res = test.performTest();
            
            ResultEntry ret = new ResultEntry();
            ret.name = entry.name;
            ret.result = res;
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveResults(String filename)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(filename));
            for (ResultEntry result : results)
            {
                out.println(result.name + "," + result.result);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public class SourceEntry
    {
        public String name;
        public String src;
        List<String> tags;
        
        public SourceEntry()
        {
            tags = new ArrayList<String>();
        }
    }
    
    public class ResultEntry
    {
        public String name;
        public float result;
    }
    
}
