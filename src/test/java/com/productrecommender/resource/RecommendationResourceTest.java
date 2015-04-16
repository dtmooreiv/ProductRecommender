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
    private final static String orderHistoryInputFile = "testRecommendationResource";
    private final static String orderHistoryPrefix = "recommender_test_order_history_";
    private final static String productCatalogPrefix = "recommender_test_product_catalog_";
    private final static String siteSetName = "recommender_test_site_set";

    private HashMap<Long, ArrayList<Recommendation>> testRecommendations;

    private final static LongParam siteId = new LongParam("111");
    private final static long[] contactIds = {69750106L,111547205L,1L};
    private final static LongArrayParam singleContactId = new LongArrayParam(Long.toString(contactIds[0]));
    private final static LongArrayParam multipleContactId = new LongArrayParam(contactIds[0] + "," + contactIds[1]);
    private final static LongArrayParam NoResultsContactId = new LongArrayParam(Long.toString(contactIds[2]));

    private final static int count = 10;
    private final static IntParam countParam = new IntParam(Integer.toString(count));


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Preprocessor prep = new Preprocessor(conn, inputFilesPath, outputFilesPath, siteSetName);
        prep.processFiles(orderHistoryInputFile, orderHistoryPrefix, productCatalogPrefix);
        ProductRecommenderConfiguration productRecommenderConfiguration = new ProductRecommenderConfiguration();
        Map<Long, GenericBooleanPrefItemBasedRecommender> recommenders = productRecommenderConfiguration.getRecommenders(conn, siteSetName, outputFilesPath + orderHistoryPrefix);
        Map<Long, CachingRecommender> cachedRecommenders = productRecommenderConfiguration.getCachedContactRecommenders(recommenders);
        JedisPool pool = productRecommenderConfiguration.getPool();
        recommendationResource = new RecommendationResource(pool, cachedRecommenders, siteSetName, productCatalogPrefix);
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
        testRecommendations = recommendationResource.recommend(siteId, singleContactId, countParam);
        assertEquals(1,testRecommendations.size());
        assertEquals(count,testRecommendations.get(contactIds[0]).size());
    }

    @Test
    public void testRecommendMultipleContactId() throws Exception {
        testRecommendations = recommendationResource.recommend(siteId, multipleContactId, countParam);
        assertEquals(2,testRecommendations.size());
        assertEquals(count,testRecommendations.get(contactIds[0]).size());
        assertEquals(count,testRecommendations.get(contactIds[1]).size());
    }

    @Test
    public void testRecommendNoResultsContactId() throws Exception {
        testRecommendations = recommendationResource.recommend(siteId, NoResultsContactId, countParam);
        assertEquals(0,testRecommendations.size());
    }
}