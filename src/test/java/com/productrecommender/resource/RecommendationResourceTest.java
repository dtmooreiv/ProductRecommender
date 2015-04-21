package com.productrecommender.resource;

import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.LongArrayParam;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RecommendationResourceTest {

    private final static Jedis conn = new Jedis("localhost");
    private static RecommendationResource recommendationResource;

    private final static String inputFilesPath = "src/test/data/input/";
    private final static String outputFilesPath = "src/test/data/output/";
    private final static String orderHistoryInputFile = "recommender_test_order_history_input";
    private final static String orderHistoryPrefix = "recommender_test_order_history_";
    private final static String productCatalogPrefix = "recommender_test_product_catalog_";
    private final static String siteSetName = "recommender_test_site_set";

    private HashMap<Long, ArrayList<Recommendation>> testRecommendations;

    private final static LongParam siteId = new LongParam("111");
    private final static LongParam invalidSiteId = new LongParam("0");
    private final static long[] contactIds = {69750106L, 111547205L, 135946476L, 134531373L, 1L};

    private final static int count = 10;
    private final static int resultsLowerThenCount = 3;
    private final static IntParam countParam = new IntParam(Integer.toString(count));


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Preprocessor prep = new Preprocessor(conn, inputFilesPath, outputFilesPath, siteSetName);
        prep.processFiles(orderHistoryInputFile, orderHistoryPrefix, productCatalogPrefix);
        ProductRecommenderConfiguration productRecommenderConfiguration = new ProductRecommenderConfiguration();
        Map<Long, GenericBooleanPrefItemBasedRecommender> recommenders = productRecommenderConfiguration.getRecommenderMap(conn, siteSetName, outputFilesPath + orderHistoryPrefix);
        Map<Long, CachingRecommender> cachedRecommenders = productRecommenderConfiguration.getCachedRecommenderMap(recommenders);
        JedisPool pool = productRecommenderConfiguration.getPool();
        recommendationResource = new RecommendationResource(pool, cachedRecommenders, productCatalogPrefix);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.del(siteSetName, productCatalogPrefix + siteId);
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
        LongArrayParam ids = new LongArrayParam(Long.toString(contactIds[0]));
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1,testRecommendations.size());
        assertEquals(count,testRecommendations.get(contactIds[0]).size());
    }

    @Test
    public void testRecommendMultipleContactId() throws Exception {
        LongArrayParam ids = new LongArrayParam(contactIds[0] + "," + contactIds[1]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(2,testRecommendations.size());
        assertEquals(count,testRecommendations.get(contactIds[0]).size());
        assertEquals(count,testRecommendations.get(contactIds[1]).size());
    }

    @Test
    public void testResultsLowerThenCount() throws Exception {
        LongArrayParam ids = new LongArrayParam(Long.toString(contactIds[2]));
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(resultsLowerThenCount, testRecommendations.get(contactIds[2]).size());
    }

    @Test
    public void testZeroResults() throws Exception {
        LongArrayParam ids = new LongArrayParam(Long.toString(contactIds[3]));
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(1, testRecommendations.size());
        assertEquals(0, testRecommendations.get(contactIds[3]).size());
    }

    @Test
    public void testRecommendZeroResultsWithMultipleContactId() throws Exception {
        LongArrayParam ids = new LongArrayParam(contactIds[0] + "," + contactIds[3]);
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(2, testRecommendations.size());
        assertEquals(count, testRecommendations.get(contactIds[0]).size());
        assertEquals(0, testRecommendations.get(contactIds[3]).size());
    }

    @Test
    public void testInvalidContactId() throws Exception {
        LongArrayParam ids = new LongArrayParam(Long.toString(contactIds[4]));
        testRecommendations = recommendationResource.recommend(siteId, ids, countParam);
        assertEquals(0, testRecommendations.size());
    }

    @Test
    public void testInvalidSiteId() throws Exception {
        LongArrayParam ids = new LongArrayParam(contactIds[0] + "," + contactIds[1]);
        testRecommendations = recommendationResource.recommend(invalidSiteId, ids, countParam);
        assertEquals(0,testRecommendations.size());
    }
}