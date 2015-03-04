package com.productrecommender.resource;

import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.LongArrayParam;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecommendationResourceTest {

    private final static Jedis conn = new Jedis("localhost");
    private ProductRecommenderConfiguration productRecommenderConfiguration;
    private Map<Long, CachingRecommender> recommenders;
    private JedisPool pool;
    private RecommendationResource recommendationResource;

    private HashMap<Long, ArrayList<Recommendation>> testRecommendations;

    private final static LongParam siteId = new LongParam("9621");
    private final static LongArrayParam singelContactId = new LongArrayParam("69750106");
    private final static IntParam count = new IntParam("10");

    @Before
    public void setUp() throws Exception {
        productRecommenderConfiguration = new ProductRecommenderConfiguration();
        recommenders = productRecommenderConfiguration.getRecommenders(conn);
        pool = productRecommenderConfiguration.getPool();
        recommendationResource = new RecommendationResource(pool, recommenders);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRecommendSingelContactId() throws Exception {
        testRecommendations = recommendationResource.recommend(siteId,singelContactId,count);
        assertEquals(1,testRecommendations.size());
        //TODO
    }
}