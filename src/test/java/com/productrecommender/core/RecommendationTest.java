package com.productrecommender.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecommendationTest {

    private Recommendation testEmptyVariable;
    private Recommendation testInitializedVariable;
    private Recommendation testDefaultVariable;

    private final static String productIdTest = "TESTID";
    private final static String scoreTest = "TESTSCORE";
    private final static String titleTest = "TESTSTITLE";
    private final static String productUrlTest = "TESTSPRODUCTURL";
    private final static String imageUrlTest = "TESTSIMAGEURL";
    private final static String categoryTest = "TESTSCATAGORY";
    private final static String descriptionTest = "TESTSDESCRIPTION";

    private final static String titleSetTest = "SETTESTSTITLE";
    private final static String productUrlSetTest = "SETTESTSPRODUCTURL";
    private final static String imageUrlSetTest = "SETTESTSIMAGEURL";
    private final static String categorySetTest = "SETTESTSCATAGORY";
    private final static String descriptionSetTest = "SETTESTSDESCRIPTION";

    private final static String titleDefault = "Default Title";
    private final static String productUrlDefault = "Default Product Url";
    private final static String imageUrlDefault = "http://bronto.com/files/upload-docs/bronto-logo.png";
    private final static String categoryDefault = "Default Category";
    private final static String descriptionDefault = "Default Description";

    @Before
    public void setUp() throws Exception {
        testEmptyVariable = new Recommendation();
        testDefaultVariable = new Recommendation(productIdTest, scoreTest);
        testInitializedVariable = new Recommendation(productIdTest,titleTest,productUrlTest,imageUrlTest,categoryTest,descriptionTest,scoreTest);
    }

    // Test initialized variables

    @Test
    public void testGetProductId() throws Exception {
        assertEquals(productIdTest,testInitializedVariable.getProductId());
    }

    @Test
    public void testGetScore() throws Exception {
        assertEquals(scoreTest,testInitializedVariable.getScore());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(titleTest,testInitializedVariable.getTitle());
    }

    @Test
    public void testSetTitle() throws Exception {
        testInitializedVariable.setTitle(titleSetTest);
        assertEquals(titleSetTest,testInitializedVariable.getTitle());
    }

    @Test
    public void testGetProductUrl() throws Exception {
        assertEquals(productUrlTest,testInitializedVariable.getProductUrl());
    }

    @Test
    public void testSetProductUrl() throws Exception {
        testInitializedVariable.setProductUrl(productUrlSetTest);
        assertEquals(productUrlSetTest, testInitializedVariable.getProductUrl());
    }

    @Test
    public void testGetImageUrl() throws Exception {
        assertEquals(imageUrlTest,testInitializedVariable.getImageUrl());
    }

    @Test
    public void testSetImageUrl() throws Exception {
        testInitializedVariable.setImageUrl(imageUrlSetTest);
        assertEquals(imageUrlSetTest, testInitializedVariable.getImageUrl());
    }

    @Test
    public void testGetCategory() throws Exception {
        assertEquals(categoryTest, testInitializedVariable.getCategory());
    }

    @Test
    public void testSetCategory() throws Exception {
        testInitializedVariable.setCategory(categorySetTest);
        assertEquals(categorySetTest,testInitializedVariable.getCategory());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(descriptionTest,testInitializedVariable.getDescription());
    }

    @Test
    public void testSetDescription() throws Exception {
        testInitializedVariable.setDescription(descriptionSetTest);
        assertEquals(descriptionSetTest, testInitializedVariable.getDescription());
    }

    // Test null variables

    @Test
    public void testGetProductIdEmpty() throws Exception {
        assertEquals(null,testEmptyVariable.getProductId());
    }

    @Test
    public void testGetScoreEmpty() throws Exception {
        assertEquals(null,testEmptyVariable.getScore());
    }

    // Test Default Values

    @Test
    public void testGetTitleDefault() throws Exception {
        assertEquals(titleDefault,testDefaultVariable.getTitle());
    }

    @Test
    public void testGetProductUrlDefault() throws Exception {
        assertEquals(productUrlDefault,testDefaultVariable.getProductUrl());
    }

    @Test
    public void testGetImageUrlDefault() throws Exception {
        assertEquals(imageUrlDefault,testDefaultVariable.getImageUrl());
    }


    @Test
    public void testGetCategoryDefault() throws Exception {
        assertEquals(categoryDefault,testDefaultVariable.getCategory());
    }


    @Test
    public void testGetDescriptionDefault() throws Exception {
        assertEquals(descriptionDefault,testDefaultVariable.getDescription());
    }
}