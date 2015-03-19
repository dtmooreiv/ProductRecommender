package com.productrecommender.services.scheduled;

import org.junit.*;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.*;

public class PreprocessorTest {

    private final static Jedis conn = new Jedis("localhost");

    private final static String inputFilesPath = "src/test/data/input/";
    private final static String outputFilesPath = "src/test/data/output/";
    private final static String orderHistoryInputFile = "testPreProcessor";
    private final static String orderHistoryPrefix = "proctest_order_history_";
    private final static String productCatalogPrefix = "proctest_product_catalog_";
    private final static String siteSetName = "proctest_site_set";

    private final static int numLines111 = 12;
    private final static int numLines222 = 24;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // process the files
        Preprocessor prep = new Preprocessor(conn, inputFilesPath, outputFilesPath, siteSetName);
        prep.processFiles(orderHistoryInputFile, orderHistoryPrefix, productCatalogPrefix);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.flushAll();
        conn.close();
    }

    @Test
    public void testReadFile() throws Exception {

        File testFile111 = new File(outputFilesPath + orderHistoryPrefix + 111);
        File testFile222 = new File(outputFilesPath + orderHistoryPrefix + 222);
        File testFile333 = new File(outputFilesPath + orderHistoryPrefix + 333);

        // check that the right ones exist
        Boolean ProperFilesExists = testFile111.isFile() && testFile222.isFile() && !testFile333.isFile();
        assertTrue(ProperFilesExists);

        // check line count of the files that should exist
        Scanner test111 = new Scanner(testFile111);
        int countLines111 = 0;
        while (test111.hasNextLine()) {
            countLines111++;
            test111.nextLine();
        }
        test111.close();
        assertEquals(numLines111,countLines111);

        Scanner test222 = new Scanner(testFile222);
        int countLines222 = 0;
        while (test222.hasNextLine()) {
            countLines222++;
            test222.nextLine();
        }
        test222.close();
        assertEquals(numLines222, countLines222);

        // remove created files
        testFile111.delete();
        testFile222.delete();
    }
}