package com.productrecommender;

import com.productrecommender.health.RedisHealthCheck;
import com.productrecommender.resource.RecommendationResource;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class ProductRecommenderApplication extends Application<ProductRecommenderConfiguration>{
    final static Logger logger = LoggerFactory.getLogger(ProductRecommenderApplication.class);
    final static String inputFilename = "skus_without_urls";
    final static Jedis conn = new Jedis("localhost");

    public static void main(String[] args) throws Exception {
        new ProductRecommenderApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProductRecommenderConfiguration> bootstrap) {
        Preprocessor prep = new Preprocessor(conn);
        prep.readFile(inputFilename);
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    }

    @Override
    public void run(ProductRecommenderConfiguration productRecommenderConfiguration, Environment environment) throws Exception {
        logger.info("Started Recommending");

        Map<Long, CachingRecommender> recommenders = productRecommenderConfiguration.getRecommenders(conn);

        JedisPool pool = productRecommenderConfiguration.getPool();

        final RecommendationResource recommendationResource = new RecommendationResource(pool, recommenders);
        final RedisHealthCheck redisHealthCheck = new RedisHealthCheck();

        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(recommendationResource);
        environment.healthChecks().register("Redis", redisHealthCheck);
    }
}
