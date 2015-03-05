package com.productrecommender.resource;

import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.LongArrayParam;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecommendationResourceTest {

    private final static Jedis conn = new Jedis("localhost");
    private static ProductRecommenderConfiguration productRecommenderConfiguration;
    private static Map<Long, CachingRecommender> recommenders;
    private static JedisPool pool;
    private static RecommendationResource recommendationResource;

    private HashMap<Long, ArrayList<Recommendation>> testRecommendations;

    private final static LongParam siteId = new LongParam("9621");

    private final static long[] contactIds = {69750106L,111547205L,1L};
    private final static LongArrayParam singleContactId = new LongArrayParam(Long.toString(contactIds[0]));
    private final static LongArrayParam multipleContactId = new LongArrayParam(contactIds[0] + "," + contactIds[1]);
    private final static LongArrayParam NoResultsContactId = new LongArrayParam(Long.toString(contactIds[2]));

    private final static int count = 10;
    private final static IntParam countParam = new IntParam(Integer.toString(count));


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        productRecommenderConfiguration = new ProductRecommenderConfiguration();
        recommenders = productRecommenderConfiguration.getRecommenders(conn);
        pool = productRecommenderConfiguration.getPool();
        recommendationResource = new RecommendationResource(pool, recommenders);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        conn.close();
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