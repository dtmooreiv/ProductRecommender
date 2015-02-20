package com.productrecommender;

import com.productrecommender.health.RedisHealthCheck;
import com.productrecommender.resource.RecommendationResource;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

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
    }

    @Override
    public void run(ProductRecommenderConfiguration productRecommenderConfiguration, Environment environment) throws Exception {
        logger.info("Started Recommending");

        final RecommendationResource recommendationResource = new RecommendationResource(conn);
        final RedisHealthCheck redisHealthCheck = new RedisHealthCheck();

        environment.jersey().register(recommendationResource);
        environment.healthChecks().register("Redis", redisHealthCheck);
    }
}
