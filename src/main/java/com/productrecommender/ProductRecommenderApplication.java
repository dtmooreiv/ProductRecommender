package com.productrecommender;

import com.productrecommender.health.RedisHealthCheck;
import com.productrecommender.resource.RecommendationResource;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductRecommenderApplication extends Application<ProductRecommenderConfiguration>{
    final static Logger logger = LoggerFactory.getLogger(ProductRecommenderApplication.class);
    final static String inputFilename = "skus_without_urls";

    public static void main(String[] args) throws Exception {
        new ProductRecommenderApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProductRecommenderConfiguration> bootstrap) {
        Jedis conn = new Jedis("localhost");
        Preprocessor prep = new Preprocessor(conn);
        prep.readFile(inputFilename);
    }

    @Override
    public void run(ProductRecommenderConfiguration productRecommenderConfiguration, Environment environment) throws Exception {
        logger.info("Started Recommending");

        final RecommendationResource recommendationResource = new RecommendationResource();
        final RedisHealthCheck redisHealthCheck = new RedisHealthCheck();

        environment.jersey().register(recommendationResource);
        environment.healthChecks().register("Redis", redisHealthCheck);
    }
}
