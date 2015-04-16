package com.productrecommender.params;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class StringArrayParamTest {

    private ArrayList<String> testArray = new ArrayList<>();

    private final static String testArrayString = "555,444,333,222,111";

    @Before
    public void setUp() throws Exception {
        testArray.add("555");
        testArray.add("444");
        testArray.add("333");
        testArray.add("222");
        testArray.add("111");
    }

    @Test
    public void testParse() throws Exception {
        StringArrayParam testParam = new StringArrayParam(testArrayString);
        assertEquals(testArray, testParam.parse(testArrayString));
    }
}