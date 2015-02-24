package com.productrecommender;

import com.productrecommender.health.RedisHealthCheck;
import com.productrecommender.resource.RecommendationResource;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.HashMap;

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

        HashMap<Long, CachingRecommender> recommenders = new HashMap<>();

        //For every site id in the site set
        for(String _siteId: conn.smembers(Preprocessor.site_set)) {
            try {
                //try to parse a long
                long siteId = Long.parseLong(_siteId);
                //open the corresponding order_history file
                DataModel model = new FileDataModel(new File(Preprocessor.output + siteId));
                //calculate the ItemSimilarity matrix
                ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
                //make a recommender
                Recommender recommender = new GenericBooleanPrefItemBasedRecommender(model, similarity);
                //create a caching recommender
                CachingRecommender cachingRecommender = new CachingRecommender(recommender);
                //store it in the recommenders map
                recommenders.put(siteId, cachingRecommender);
            } catch (NumberFormatException e) {
                logger.info("Could not parse site id from member: " + _siteId + " of site_set");
            }
        }

        final RecommendationResource recommendationResource = new RecommendationResource(conn, recommenders);
        final RedisHealthCheck redisHealthCheck = new RedisHealthCheck();


        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(recommendationResource);
        environment.healthChecks().register("Redis", redisHealthCheck);
    }
}
