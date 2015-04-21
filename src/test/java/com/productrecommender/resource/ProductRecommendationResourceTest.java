package com.productrecommender.resource;

import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.StringArrayParam;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ProductRecommendationResourceTest {

    private final static Jedis conn = new Jedis("localhost");
    private static ProductRecommendationResource recommendationResource;
    private static String productCachePrefix;

    private final static String inputFilesPath = "src/test/data/input/";
    private final static String outputFilesPath = "src/test/data/output/";
    private final static String orderHistoryInputFile = "product_recommender_test_order_history_input";
    private final static String orderHistoryPrefix = "product_recommender_test_order_history_";
    private final static String productCatalogPrefix = "product_recommender_test_product_catalog_";
    private final static String siteSetName = "product_recommender_test_site_set";

    private HashMap<String, ArrayList<Recommendation>> testRecommendations;

    private final static LongParam siteId = new LongParam("111");
    private final static LongParam invalidSiteId = new LongParam("0");
    private final static String[] productIds = {"706390", "706392", "226821", "657830", "FAKE_PRODUCT_ID"};

    private final static int count = 10;
    private final static int resultsLowerThenCount = 6;
    private final static IntParam countParam = new IntParam(Integer.toString(count));


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Preprocessor prep = new Preprocessor(conn, inputFilesPath, outputFilesPath, siteSetName);
        prep.processFiles(orderHistoryInputFile, orderHistoryPrefix, productCatalogPrefix);
        ProductRecommenderConfiguration productRecommenderConfiguration = new ProductRecommenderConfiguration();
        Map<Long, GenericBooleanPrefItemBasedRecommender> recommenders = productRecommenderConfiguration.getRecommenderMap(conn, siteSetName, outputFilesPath + orderHistoryPrefix);
        JedisPool pool = productRecommenderConfiguration.getPool();
        recommendationResource = new ProductRecommendationResource(pool, recommenders, productCatalogPrefix);
        productCachePrefix = recommendationResource.PRODUCT_CACHES;
    }

    @After
    public void tearDown() throws Exception {
        conn.del(productCachePrefix + siteId);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.del(siteSetName, productCatalogPrefix + siteId, productCachePrefix + siteId);
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
    public void testRecommendSingleContactId() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[0]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(count, testRecommendations.get(productIds[0]).size());
    }

    @Test
    public void testRecommendMultipleContactId() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[0] + "," + productIds[1]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(2, testRecommendations.size());
        assertEquals(count, testRecommendations.get(productIds[0]).size());
        assertEquals(count, testRecommendations.get(productIds[1]).size());
    }

    @Test
    public void testResultsLowerThenCount() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[2]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(resultsLowerThenCount, testRecommendations.get(productIds[2]).size());
    }

    @Test
    public void testZeroResults() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[3]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(0, testRecommendations.get(productIds[3]).size());
    }

    @Test
    public void testCaching() throws Exception {
        // Check that it hasn't been cached
        assertFalse(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[0].hashCode())));

        // make the request which should cache the results
        StringArrayParam ids = new StringArrayParam(productIds[0]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(count, testRecommendations.get(productIds[0]).size());

        // Check that it was cached
        assertTrue(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[0].hashCode())));
    }

    @Test
    public void testNoCachingZeroResults() throws Exception {
        // Check that it hasn't been cached
        assertFalse(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[3].hashCode())));

        // make the request which should cache the results
        StringArrayParam ids = new StringArrayParam(productIds[3]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(0, testRecommendations.get(productIds[3]).size());

        // Check that it was cached
        assertFalse(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[3].hashCode())));
    }

    @Test
    public void testCachingSpeed() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[1]);

        // Check that it hasn't been cached and time the request
        assertFalse(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[1].hashCode())));
        long startNonCached = System.nanoTime();
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        long endNonCached = System.nanoTime();
        assertEquals(1, testRecommendations.size());
        assertEquals(count, testRecommendations.get(productIds[1]).size());


        // Check that it was cached and time the request
        assertTrue(conn.hexists(productCachePrefix + siteId, String.valueOf(productIds[1].hashCode())));
        long startCached = System.nanoTime();
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        long endCached = System.nanoTime();
        assertEquals(1, testRecommendations.size());
        assertEquals(count, testRecommendations.get(productIds[1]).size());

        // Check that the cached time was faster then the non cached time
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println((endNonCached - startNonCached) + " vs " + (endCached - startCached));
        assertTrue((endNonCached - startNonCached) > (endCached - startCached));
    }

    @Test
    public void testRecommendInvalidProductId() throws Exception {
        StringArrayParam ids = new StringArrayParam(ProductRecommendationResourceTest.productIds[4]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(0, testRecommendations.size());
    }

    @Test
    public void testRecommendInvalidSiteId() throws Exception {
        StringArrayParam ids = new StringArrayParam(productIds[0] + "," + productIds[1]);
        testRecommendations = recommendationResource.recommend(invalidSiteId, ids, countParam);
        assertEquals(0, testRecommendations.size());
    }
}