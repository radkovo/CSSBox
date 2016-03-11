/*
 * TestBatch.java
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
package org.fit.cssbox.testing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * A batch of tests in a test folder. The tests are executed in separate threads with
 * a timeout.
 * 
 * @author burgetr
 */
public class TestBatch
{
    private static Logger log = LoggerFactory.getLogger(TestBatch.class);
    private static int DEFAULT_THREADS = 12;
    private static int TASK_TIMEOUT = 30; //seconds
    
    private static List<String> tagBlacklist;
    static {
        tagBlacklist = new ArrayList<String>();
        tagBlacklist.add("svg");
        tagBlacklist.add("dom/js");
    }
    
    private URL testURL;
    private int threadsUsed;
    private List<SourceEntry> tests;
    private Map<String, Float> results;
    private int totalCount;
    private int completedCount;
    
    /**
     * Creates a test batch from a test folder. The folder format must correspond to the
     * CSS WG tests in HTML4 format. It must contain the {@code reftest-toc.htm} index
     * file that is used for obtaining the test names.
     * @param testURL
     */
    public TestBatch(URL testURL)
    {
        this(testURL, DEFAULT_THREADS);
    }
    
    /**
     * Creates a test batch from a test folder. The folder format must correspond to the
     * CSS WG tests in HTML4 format. It must contain the {@code reftest-toc.htm} index
     * file that is used for obtaining the test names.
     * @param testURL
     * @param threadCount the number of threads to be used for parallel testing
     */
    public TestBatch(URL testURL, int threadCount)
    {
        this.threadsUsed = threadCount;
        this.testURL = testURL;
        this.tests = new LinkedList<SourceEntry>();
        this.results = new LinkedHashMap<String, Float>();
        parseToc();
    }
    
    /**
     * Obtain the number of tests available.
     * @return
     */
    public int getTestCount()
    {
        return tests.size();
    }
    
    /**
     * Parses the HTML TOC and extracts the test names and tags. Fills the list of tests.
     */
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
                    for (int ri = 0; ri < 1; ri++) //consider just the first row (the other rows contain some additional references)
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
            System.err.println(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Runs all the test from the TOC. The tests are executed as a single list of tasks
     * passed to the executor service.
     */
    public void runTestsSingleList()
    {
        runTestsSingleList(null);
    }
    
    /**
     * Runs all the test from the TOC. The tests are executed as a single list of tasks
     * passed to the executor service.
     * @param selected the list of selected test to be used or {@code null} to use all the
     * tests that are not blacklisted
     */
    public void runTestsSingleList(List<String> selected)
    {
        //ExecutorService exec = Executors.newSingleThreadExecutor();
        ExecutorService exec = Executors.newFixedThreadPool(threadsUsed);
        List<Callable<Float>> list = getTestList(selected);
        totalCount = list.size();
        completedCount = 0;
        try
        {
            List<Future<Float>> futures = exec.invokeAll(list, list.size() * 5, TimeUnit.SECONDS);
            for (int i = 0; i < list.size(); i++)
            {
                Future<Float> future = futures.get(i);
                String tname = ((ReferenceTestCase) list.get(i)).getName();
                float tvalue;
                try
                {
                    tvalue = future.get();
                } catch (ExecutionException e) {
                    tvalue = 1.0f;
                } catch (CancellationException e) {
                    tvalue = 1.0f;
                }
                results.put(tname, tvalue);
            }
        } catch (InterruptedException e) {
            log.error("Interrupted: {}", e.getMessage());
        }
    }

    /**
     * Runs all the test from the TOC. The tests are executed as separate tasks
     * passed to the executor service.
     */
    public void runTests()
    {
        runTests(null);
    }
    
    /**
     * Runs all the test from the TOC. The tests are executed as separate tasks
     * passed to the executor service.
     * @param selected the list of selected test to be used or {@code null} to use all the
     * tests that are not blacklisted
     */
    public void runTests(List<String> selected)
    {
        if (threadsUsed == 1)
        {
            log.info("Test sequence mode");
            runTestsInSequence(selected);
            return;
        }
        //ExecutorService exec = Executors.newSingleThreadExecutor();
        ExecutorService exec = Executors.newFixedThreadPool(threadsUsed);
        List<Callable<Float>> list = getTestList(selected);
        List<Future<Float>> futures = new ArrayList<Future<Float>>(list.size());
        totalCount = list.size();
        completedCount = 0;
        //exec all tests with time limit; collect the futures
        for (int i = 0; i < list.size(); i++)
        {
            try
            {
                List<Future<Float>> fl = exec.invokeAll(list.subList(i, i + 1), TASK_TIMEOUT, TimeUnit.SECONDS);
                futures.add(fl.get(0));
            } catch (InterruptedException e) {
                log.error("Test " + i + " interrupted: " + e.getMessage());
                futures.add(null);
            }
        }
        //get the results from the futures
        for (int i = 0; i < futures.size(); i++)
        {
            String tname = ((ReferenceTestCase) list.get(i)).getName();
            float tvalue;
            
            Future<Float> future = futures.get(i);
            if (future != null)
            {
                try
                {
                    tvalue = future.get();
                } catch (ExecutionException e) {
                    log.error(tname + " (" + i + "): " + e.getMessage());
                    e.printStackTrace();
                    tvalue = 1.0f;
                } catch (CancellationException e) {
                    log.error(tname + " (" + i + "): " + e.getMessage());
                    e.printStackTrace();
                    tvalue = 1.0f;
                } catch (InterruptedException e) {
                    log.error(tname + " (" + i + "): " + e.getMessage());
                    e.printStackTrace();
                    tvalue = 1.0f;
                }
            }
            else
            {
                log.error(tname + " (" + i + "): result not available");
                tvalue = 1.0f;
            }
            
            results.put(tname, tvalue);
        }
    }
    
    /**
     * Runs all the tests sequentionally.
     * @param selected the list of selected test to be used or {@code null} to use all the
     * tests that are not blacklisted
     */
    public void runTestsInSequence(List<String> selected)
    {
        Runtime runtime = Runtime.getRuntime();
        long minFree = runtime.freeMemory();
        List<Callable<Float>> list = getTestList(selected);
        for (int i = 0; i < list.size(); i++)
        {
            Callable<Float> test = list.get(i);
            String tname = ((ReferenceTestCase) test).getName();
            log.info("Test {}/{} {}", i, list.size(), tname);
            System.out.print("Test " + i + "/" + list.size() + " " + tname);
            try
            {
                Float result = test.call();
                results.put(tname, result);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            long free = runtime.freeMemory();
            if (free < minFree) minFree = free;
            list.set(i, null); //do not hold the tests in memory
            if (i % 10 == 0)
                System.gc();
            System.out.println(" free:" + (free/1000) + " min:" + (minFree/1000));
        }
    }
    
    /**
     * Creates a list of callables for performing the tests based on the TOC while
     * skipping the blacklisted tests.
     * @param selected the list of selected test to be used or {@code null} to use all the
     * tests that are not blacklisted
     * @return The list of corresponding callables.
     */
    private List<Callable<Float>> getTestList(List<String> selected)
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
            if (!blacklisted && (selected == null || selected.contains(entry.name)))
            {
                try
                {
                    URL url = new URL(testURL, entry.src);
                    ReferenceTestCase test = new ReferenceTestCase(entry.name, url.toString());
                    //test.setBatch(this);
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
    
    /**
     * Runs a single test based on the given source entry.
     * @param entry
     * @return test result or a negative value when the execution failed
     */
    public float runTest(SourceEntry entry)
    {
        try
        {
            URL url = new URL(testURL, entry.src);
            ReferenceTestCase test = new ReferenceTestCase(entry.name, url.toString());
            float res = test.performTest();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return -1.0f;
    }
    
    public Map<String, Float> getResults()
    {
        return results;
    }
    
    /**
     * Runs a test specified by its name when it is present in the testing batch.
     * @param name the test name
     * @return test result or a negative value when the execution failed
     */
    public float runTestByName(String name)
    {
        for (SourceEntry test : tests)
        {
            if (test.name.equals(name))
                return runTest(test);
        }
        return -1.0f;
    }
    
    /**
     * Saves all the results to a CSV file.
     * @param filename the destination file path
     */
    public void saveResults(String filename)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(filename));
            for (Entry<String, Float> result : results.entrySet())
            {
                out.println(result.getKey() + "," + result.getValue());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void reportCompletion(ReferenceTestCase testCase)
    {
        completedCount++;
        if (completedCount % 10 == 0)
            log.info("Completed " + completedCount + "/" + totalCount);
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
    
}
