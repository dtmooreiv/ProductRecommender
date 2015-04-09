package com.productrecommender;

import com.productrecommender.health.RedisHealthCheck;
import com.productrecommender.resource.ProductRecommendationResource;
import com.productrecommender.resource.RecommendationResource;
import com.productrecommender.services.scheduled.Evaluator;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;

import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class ProductRecommenderApplication extends Application<ProductRecommenderConfiguration>{
    private final static Logger logger = LoggerFactory.getLogger(ProductRecommenderApplication.class);

    private final static Jedis conn = new Jedis("localhost");

    public static void main(String[] args) throws Exception {
        new ProductRecommenderApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProductRecommenderConfiguration> bootstrap) {
        Preprocessor prep = new Preprocessor(conn, ProductRecommenderConfiguration.inputFilesPath, ProductRecommenderConfiguration.outputFilesPath, ProductRecommenderConfiguration.siteSetName);
        prep.processFiles(ProductRecommenderConfiguration.orderHistoryInputFile, ProductRecommenderConfiguration.orderHistoryPrefix, ProductRecommenderConfiguration.productCatalogPrefix);

        Evaluator eval = new Evaluator(conn, ProductRecommenderConfiguration.outputFilesPath, ProductRecommenderConfiguration.orderHistoryPrefix, ProductRecommenderConfiguration.siteSetName);
        eval.evaluateRecommenders();

        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    }

    @Override
    public void run(ProductRecommenderConfiguration productRecommenderConfiguration, Environment environment) throws Exception {
        logger.info("Started Recommending");

        Map<Long, CachingRecommender> recommenders = productRecommenderConfiguration.getRecommenders(conn, ProductRecommenderConfiguration.siteSetName, ProductRecommenderConfiguration.outputFilesPath + ProductRecommenderConfiguration.orderHistoryPrefix);
        Map<Long, GenericBooleanPrefItemBasedRecommender> fullRecommenders = productRecommenderConfiguration.getFullRecommenders(conn, ProductRecommenderConfiguration.siteSetName, ProductRecommenderConfiguration.outputFilesPath + ProductRecommenderConfiguration.orderHistoryPrefix);

        JedisPool pool = productRecommenderConfiguration.getPool();

        final RecommendationResource recommendationResource = new RecommendationResource(pool, recommenders, ProductRecommenderConfiguration.siteSetName, ProductRecommenderConfiguration.productCatalogPrefix);
        final ProductRecommendationResource productRecommendationResource = new ProductRecommendationResource(pool, fullRecommenders, ProductRecommenderConfiguration.siteSetName, ProductRecommenderConfiguration.productCatalogPrefix);
        final RedisHealthCheck redisHealthCheck = new RedisHealthCheck();

        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(recommendationResource);
        environment.jersey().register(productRecommendationResource);
        environment.healthChecks().register("Redis", redisHealthCheck);
    }
}
