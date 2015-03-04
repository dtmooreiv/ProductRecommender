package com.productrecommender.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecommendationTest {
    private Recommendation testEmptyVariable;
    private Recommendation testInitializedVariable;
    private final static String productId = "TESTID";
    private final static String score = "TESTSCORE";

    @Before
    public void setUp() throws Exception {
        testEmptyVariable = new Recommendation();
        testInitializedVariable = new Recommendation(productId,score);
    }

    @Test
    public void testGetProductIdEmpty() throws Exception {
        assertEquals(null,testEmptyVariable.getProductId());
    }

    @Test
    public void testGetProductId() throws Exception {
        assertEquals(productId,testInitializedVariable.getProductId());
    }

    @Test
    public void testGetScoreEmpty() throws Exception {
        assertEquals(null,testEmptyVariable.getScore());
    }

    @Test
    public void testGetScore() throws Exception {
        assertEquals(score,testInitializedVariable.getScore());
    }
}