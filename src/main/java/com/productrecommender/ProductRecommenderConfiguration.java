package com.productrecommender;

import io.dropwizard.Configuration;
import org.apache.mahout.cf.taste.common.TasteException;
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
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ProductRecommenderConfiguration extends Configuration{

    private final static Logger logger = LoggerFactory.getLogger(ProductRecommenderConfiguration.class);

    public Map<Long,CachingRecommender> getRecommenders(Jedis conn,
                                                        String siteSet,
                                                        String dataFile) throws IOException, TasteException {

        HashMap<Long, CachingRecommender> recommenders = new HashMap<>();

        //For every site id in the site set
        for(String _siteId: conn.smembers(siteSet)) {
            try {
                //try to parse a long
                long siteId = Long.parseLong(_siteId);
                //open the corresponding order_history file
                DataModel model = new FileDataModel(new File(dataFile + siteId));
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

        return recommenders;
    }

    public JedisPool getPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1000);

        return new JedisPool(config, "localhost");
    }
}
