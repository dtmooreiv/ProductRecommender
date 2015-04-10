package com.productrecommender.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productrecommender.ProductRecommenderConfiguration;
import com.productrecommender.core.Recommendation;
import com.productrecommender.params.LongArrayParam;
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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("product-recommend/{siteId}/{productId}")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class ProductRecommendationResource {

    private final static Logger logger = LoggerFactory.getLogger(ProductRecommendationResource.class);
    public static final String PRODUCT_CACHES = "product_caches_";

    private final JedisPool pool;
    private final Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap;
    private final String siteSetName;
    private final String productCatalogPrefix;
    private final ObjectMapper mapper;



    public ProductRecommendationResource(JedisPool pool,
                                         Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap,
                                         String siteSetName,
                                         String productCatalogPrefix) {
        this.pool = pool;
        this.recommenderMap = recommenderMap;
        this.siteSetName = siteSetName;
        this.productCatalogPrefix = productCatalogPrefix;
        this.mapper = new ObjectMapper();
    }

    @GET
    public HashMap<String, ArrayList<Recommendation>> recommend(@PathParam("siteId") LongParam siteId,
                                                              @PathParam("productId")StringArrayParam productIds,
                                                              @QueryParam("count") @DefaultValue("10") IntParam count ) {
        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + productIds);


        HashMap<String, ArrayList<Recommendation>> recommendations = new HashMap<>();

        try (Jedis conn = pool.getResource()){
            //Check to see if we have data for this site
            if(!conn.sismember(siteSetName, siteId.toString())) {
                return recommendations;
            }

            //Get list of recommendations per contact id given
            for (String productId: productIds.get()) {
                recommendations.put(productId, recommendationsForProductId(conn, siteId.get(), productId.hashCode(), count.get()));
            }
        }

        return recommendations;
    }

    private ArrayList<Recommendation> recommendationsForProductId(Jedis conn, long siteId, long productId, int count) {
        List<RecommendedItem> items = null;
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        //
        if(conn.hexists(PRODUCT_CACHES + siteId, productId + "")) {
            String recs = conn.hget(PRODUCT_CACHES + siteId, productId + "").replaceAll("[\\[|\\]]", "");
            String[] recArray = recs.split(",");

            for(int i = 0; i < count && i < recArray.length; i++) {
                String rec = recArray[i];
                String[] recVals = rec.replaceAll("[{|\"|}]", "").split(" ");
                long recProductId = Long.parseLong(recVals[0]);
                String productInfo = conn.hget(productCatalogPrefix + siteId, recProductId +"");
                String [] data = productInfo.split("\\t", -1);
                recommendations.add(new Recommendation(data, recVals[1]));
            }
        }
        else {
            try {
                items = recommenderMap.get(siteId).mostSimilarItems(productId, ProductRecommenderConfiguration.NUM_CACHED_SIMILAR_PRODUCT_IDS);
            } catch (TasteException e) {
                e.printStackTrace();
            }
            if(items == null) {
                logger.info("Failed to get similar items for sitedId: " + siteId + " processedProductId: " + productId + " count: " + count);
                return (ArrayList) items;
            }
            List<String> similarItemStrings = new ArrayList<>();
            for(int i = 0; i < count && i < items.size(); i++) {
                RecommendedItem item = items.get(i);
                String productInfo = conn.hget(productCatalogPrefix + siteId, Long.toString(item.getItemID()));
                String [] data = productInfo.split("\\t", -1);
                String score = Float.toString(item.getValue());
                recommendations.add(new Recommendation(data, score));
                similarItemStrings.add("{" + item.getItemID()+ " " + item.getValue() + "}");
            }

            String similarItemsString = null;
            try {
                similarItemsString = mapper.writeValueAsString(similarItemStrings);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            if(similarItemsString != null) {
                logger.info(similarItemsString);
                conn.hset(PRODUCT_CACHES + siteId, productId + "", similarItemsString);
            }
        }

        return recommendations;
    }


}
