package com.productrecommender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRecommenderConfiguration extends Configuration{

    private final static Logger logger = LoggerFactory.getLogger(ProductRecommenderConfiguration.class);

    public final static int NUM_CACHED_SIMILAR_PRODUCT_IDS = 50;
    final static String inputFilesPath = "src/data/input/";
    final static String outputFilesPath = "src/data/output/";
    final static String orderHistoryInputFile = "skus_without_urls";
    final static String orderHistoryPrefix = "order_history_";
    final static String productCatalogPrefix = "product_catalog_";
    final static String siteSetName = "site_set";

    public Map<Long, GenericBooleanPrefItemBasedRecommender> getRecommenderMap(Jedis conn) throws IOException {
        return getRecommenderMap(conn, siteSetName, outputFilesPath + orderHistoryPrefix);
    }

    // This was made for testing purposes
    public Map<Long, GenericBooleanPrefItemBasedRecommender> getRecommenderMap(Jedis conn, String siteSet, String input) throws IOException {

        HashMap<Long, GenericBooleanPrefItemBasedRecommender> recommenders = new HashMap<>();

        //For every site id in the site set
        for(String _siteId: conn.smembers(siteSet)) {
            try {
                //try to parse a long
                long siteId = Long.parseLong(_siteId);
                //open the corresponding order_history file
                DataModel model = new FileDataModel(new File(input + siteId));
                //calculate the ItemSimilarity matrix
                ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
                //make a recommender
                GenericBooleanPrefItemBasedRecommender recommender = new GenericBooleanPrefItemBasedRecommender(model, similarity);
                //store it in the recommenders map
                recommenders.put(siteId, recommender);
            } catch (NumberFormatException e) {
                logger.info("Could not parse site id from member: " + _siteId + " of site_set");
            }
        }

        return recommenders;
    }

    public Map<Long, CachingRecommender> getCachedRecommenderMap(Map<Long, GenericBooleanPrefItemBasedRecommender> recommenders) throws TasteException {
        HashMap<Long, CachingRecommender> cachedRecommenders = new HashMap<>();

        //For every entry in the recommenders map cache the customer recommendations
        for (Map.Entry<Long, GenericBooleanPrefItemBasedRecommender> entry : recommenders.entrySet()) {
            // Caching the current recommender
            CachingRecommender cachedRecommender = new CachingRecommender(entry.getValue());
            // Store it in the recommenders map
            cachedRecommenders.put(entry.getKey(), cachedRecommender);
        }

        return cachedRecommenders;
    }

    // This can be used to cache all product recommendations during initialization.
    public boolean cacheRecommenderProductSimilarities(GenericBooleanPrefItemBasedRecommender recommender, Jedis conn, long siteId) {
        logger.info("CachingProductSimilarities");
        DataModel model = recommender.getDataModel();
        boolean val = true;
        try {
            LongPrimitiveIterator itemIDs = model.getItemIDs();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> productMap = conn.hgetAll(productCatalogPrefix + siteId);
            logger.info("ItemIds: " + itemIDs.toString() + " hasNext: " + itemIDs.hasNext());
            logger.info("ProductMap: " + productMap.entrySet().size());
            while(itemIDs.hasNext()) {
                long itemId = itemIDs.nextLong();

                List<RecommendedItem> similarItems = recommender.mostSimilarItems(itemId, NUM_CACHED_SIMILAR_PRODUCT_IDS);
                List<String> similarItemStrings = new ArrayList<>();
                for(RecommendedItem item: similarItems) {
                    similarItemStrings.add("[" + item.getItemID()+ ", " + item.getValue() + "]");
                }

                String similarItemsString = null;
                try {
                    similarItemsString = mapper.writeValueAsString(similarItemStrings);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                if(similarItemsString != null) {
                    String productInfo = productMap.get(String.valueOf(itemId));
                    productInfo = productInfo + "\\t" + similarItemsString;
                    logger.info("SiteId: " + siteId + " ItemId: " + itemId +" ProductInfo: " + productInfo);
                    //productMap.put(itemId, productInfo);
                    //conn.hset(productCatalogPrefix + siteId, itemId + "", productInfo);
                }

            }
        } catch (TasteException e) {
            e.printStackTrace();
            val = false;
        }
        return val;
    }

    public JedisPool getPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1000);

        return new JedisPool(config, "localhost");
    }
}
