package com.productrecommender.services.scheduled;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreprocessorTest {

    private final static Jedis conn = new Jedis("localhost");

    private final static String inputFilesPath = "src/test/data/input/";
    private final static String outputFilesPath = "src/test/data/output/";
    private final static String orderHistoryInputFile = "testPreProcessor";
    private final static String orderHistoryPrefix = "processor_test_order_history_";
    private final static String productCatalogPrefix = "processor_test_product_catalog_";
    private final static String siteSetName = "processor_test_site_set";

    private final static String[] siteList = {"111", "222", "333"};
    private final static int[] numLines = {12,24,-1};
    private final static int[] numProducts = {2,24};

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // process the files
        Preprocessor prep = new Preprocessor(conn, inputFilesPath, outputFilesPath, siteSetName);
        prep.processFiles(orderHistoryInputFile, orderHistoryPrefix, productCatalogPrefix);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.del(siteSetName, productCatalogPrefix + siteList[0], productCatalogPrefix + siteList[1], productCatalogPrefix + siteList[2]);
        conn.close();
        removeTestOutput();
    }

    private static void removeTestOutput() {
        File folder = new File(outputFilesPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().startsWith(orderHistoryPrefix)) {
                    file.deleteOnExit();
                }
            }
        }
    }

    @Test
    public void testPreProcessor() throws Exception {
        // Test that the correct number of lines where printed for each site or if 0 that a file was not created
        assertEquals(numLines[0], countFileLines(new File(outputFilesPath + orderHistoryPrefix + siteList[0])));
        assertEquals(numLines[1], countFileLines(new File(outputFilesPath + orderHistoryPrefix + siteList[1])));
        assertEquals(numLines[2], countFileLines(new File(outputFilesPath + orderHistoryPrefix + siteList[2])));
    }

    // Returns File line count or -1 if file does not exist.
    private int countFileLines(File input) {
        try {
            Scanner sc = new Scanner(input);
            int count = 0;
            while (sc.hasNextLine()) {
                count++;
                sc.nextLine();
            }
            sc.close();
            return count;
        } catch (FileNotFoundException e) {
            return -1;
        }
    }

    @Test
    public void testRedisSiteSet() throws Exception {
        // Test that site 1 is stored in siteSet
        assertTrue(conn.sismember(siteSetName, siteList[0]));
        // Test that site 2 is stored in siteSet
        assertTrue(conn.sismember(siteSetName, siteList[1]));
        // Test that site 3 is not stored in siteSet
        assertFalse(conn.sismember(siteSetName, siteList[2]));
    }

    @Test
    public void testRedisProductCatalog() throws Exception {
        int productCatalogSize;
        // Test the data stored for the first site
        assertTrue(conn.exists(productCatalogPrefix + siteList[0]));
        productCatalogSize = conn.hgetAll(productCatalogPrefix + siteList[0]).size();
        assertEquals(productCatalogSize, numProducts[0]);
        // Test the data stored for the second site
        assertTrue(conn.exists(productCatalogPrefix + siteList[1]));
        productCatalogSize = conn.hgetAll(productCatalogPrefix + siteList[1]).size();
        assertEquals(productCatalogSize, numProducts[1]);
        // Test that there was no data stored for the third site
        assertFalse(conn.exists(productCatalogPrefix + siteList[2]));
    }
}