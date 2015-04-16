package com.productrecommender.resource;

import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.StringArrayParam;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("product-recommend/{siteId}/{productId}")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class ProductRecommendationResource {

    private final static Logger logger = LoggerFactory.getLogger(ProductRecommendationResource.class);

    public final String PRODUCT_CACHES = "product_caches_";

    private final JedisPool pool;
    private final Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap;
    private final String productCatalogPrefix;

    public ProductRecommendationResource(JedisPool pool,
                                         Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap,
                                         String productCatalogPrefix) {
        this.pool = pool;
        this.recommenderMap = recommenderMap;
        this.productCatalogPrefix = productCatalogPrefix;
    }

    @GET
    public HashMap<String, ArrayList<Recommendation>> recommend(@PathParam("siteId") LongParam siteId,
                                                              @PathParam("productId")StringArrayParam productIds,
                                                              @QueryParam("count") @DefaultValue("10") IntParam count ) {

        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + productIds);

        HashMap<String, ArrayList<Recommendation>> recommendations = new HashMap<>();

        try (Jedis conn = pool.getResource()){
            //Check to see if we have data for this site
            if (!recommenderMap.containsKey(siteId.get())) {
                return recommendations;
            }

            //Get list of recommendations per product id given
            for (String productId: productIds.get()) {
                recommendations.put(productId, recommendationsForProductId(conn, siteId.get(), productId.hashCode(), count.get()));
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }

        return recommendations;
    }

    private ArrayList<Recommendation> recommendationsForProductId(Jedis conn, long siteId, long productId, int count) throws TasteException {
        String[] resultIds;
        String[] resultScores;

        if (conn.hexists(PRODUCT_CACHES + siteId, String.valueOf(productId))) {
            // Get cached recommendations and alter count bases on available recommendations
            String[] recommendedItems = conn.hget(PRODUCT_CACHES + siteId, String.valueOf(productId)).split(";");
            count = (recommendedItems.length > count) ? count : recommendedItems.length;

            // This is where the recommendations to be returned will be stored for processing
            resultIds = new String[count];
            resultScores = new String[count];
            for (int i = 0; i < count; i++) {
                String[] token = recommendedItems[i].split(",");
                resultIds[i] = token[0];
                resultScores[i] = token[1];
            }
        } else {
            // Compute recommendations and alter count bases on available recommendations
            List<RecommendedItem> recommendedItems = recommenderMap.get(siteId).mostSimilarItems(productId, ProductRecommenderConfiguration.NUM_CACHED_SIMILAR_PRODUCT_IDS);
            if (recommendedItems.size() == 0) {
                return new ArrayList<>();
            }
            count = (recommendedItems.size() > count) ? count : recommendedItems.size();

            // This is where the recommendations to be returned will be stored for processing
            resultIds = new String[count];
            resultScores = new String[count];

            // This is where all the recommendations to be cached will be stored for processing
            String similarItemsString = "";
            String fence = "";

            int i = 0;
            for (RecommendedItem item : recommendedItems) {
                // Pull out the given number of items in order to return them later
                if (i < count) {
                    resultIds[i] = String.valueOf(item.getItemID());
                    resultScores[i] = String.valueOf(item.getValue());
                    i++;
                }

                // Place every recommendation in a string to cache in redis
                similarItemsString += fence + item.getItemID() + "," + item.getValue();
                fence = ";";
            }

            // Cache the recommendations to redis, max is set by ProductRecommenderConfiguration.NUM_CACHED_SIMILAR_PRODUCT_IDS
            conn.hset(PRODUCT_CACHES + siteId, String.valueOf(productId), similarItemsString);
        }

        // Use the itemIds from the recommender to get all the item data from redis
        List<String> resultData = conn.hmget(productCatalogPrefix + siteId, resultIds);

        //Turn list of recommendations and its data into an array list of Recommendation objects
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String productInfo = resultData.get(i);
            String[] data = productInfo.split("\\t", -1);
            String score = resultScores[i];
            recommendations.add(new Recommendation(data, score));
        }

        //return recommendations list
        return recommendations;
    }
}
