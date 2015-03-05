package com.productrecommender.services.scheduled;

import org.junit.*;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.*;

public class PreprocessorTest {

    private final static Jedis conn = new Jedis("localhost");
    private static Preprocessor prep;

    private final static String testFileName = "src/data/test/testPreProcessor";
    private final static String[] CreatedTestFileNames = {"order_history_111","order_history_222","order_history_333"};

    private final static int numLines111 = 12;
    private final static int numLines222 = 24;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        prep = new Preprocessor(conn);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.close();
    }

    @Test
    public void testReadFile() throws Exception {
        // process the files
        prep.readFile(testFileName);

        File testFile111 = new File(CreatedTestFileNames[0]);
        File testFile222 = new File(CreatedTestFileNames[1]);
        File testFile333 = new File(CreatedTestFileNames[2]);

        // check that the right ones exist
        Boolean ProperFilesExists = testFile111.isFile() && testFile222.isFile() && !testFile333.isFile();
        assertTrue(ProperFilesExists);

        // check line count of the ones that exist
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
    }
}