package com.productrecommender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
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
import java.util.Set;


public class ProductRecommenderConfiguration extends Configuration{

    private final static Logger logger = LoggerFactory.getLogger(ProductRecommenderConfiguration.class);

    public final static int NUM_CACHED_SIMILAR_PRODUCT_IDS = 50;
    final static String inputFilesPath = "src/data/input/";
    final static String outputFilesPath = "src/data/output/";
    final static String orderHistoryInputFile = "skus_without_urls";
    final static String orderHistoryPrefix = "order_history_";
    final static String productCatalogPrefix = "product_catalog_";
    final static String siteSetName = "site_set";

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

    public Map<Long,GenericBooleanPrefItemBasedRecommender> getFullRecommenders(Jedis conn,
                                                        String siteSet,
                                                        String dataFile) throws IOException, TasteException {

        HashMap<Long, GenericBooleanPrefItemBasedRecommender> recommenders = new HashMap<>();

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
                GenericBooleanPrefItemBasedRecommender recommender = new GenericBooleanPrefItemBasedRecommender(model, similarity);
                //store it in the recommenders map
                recommenders.put(siteId, recommender);
            } catch (NumberFormatException e) {
                logger.info("Could not parse site id from member: " + _siteId + " of site_set");
            }
        }

        return recommenders;
    }

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
                    String productInfo = productMap.get(itemId);
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
