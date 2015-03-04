package com.productrecommender.params;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class LongArrayParamTest {

    private ArrayList<Long> testArray = new ArrayList<>();
    private LongArrayParam testParam;

    private final static String testArrayString = "555,444,333,222,111";

    @Before
    public void setUp() throws Exception {
        testArray.add(555L);
        testArray.add(444L);
        testArray.add(333L);
        testArray.add(222L);
        testArray.add(111L);
    }

    @Test
    public void testParse() throws Exception {
        testParam = new LongArrayParam(testArrayString);
        assertEquals(testArray,testParam.parse(testArrayString));
    }
}