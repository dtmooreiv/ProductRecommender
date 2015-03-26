package com.productrecommender.resource;

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

    private final JedisPool pool;
    private final Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap;
    private final String siteSetName;
    private final String productCatalogPrefix;


    public ProductRecommendationResource(JedisPool pool,
                                         Map<Long, GenericBooleanPrefItemBasedRecommender> recommenderMap,
                                         String siteSetName,
                                         String productCatalogPrefix) {
        this.pool = pool;
        this.recommenderMap = recommenderMap;
        this.siteSetName = siteSetName;
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
        try {
            items = recommenderMap.get(siteId).mostSimilarItems(productId, count);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        if(items == null) {
            logger.info("Failed to get similar items for sitedId: " + siteId + " processedProductId: " + productId + " count: " + count);
            return (ArrayList) items;
        }

        for(RecommendedItem item: items) {
            String productInfo = conn.hget(productCatalogPrefix + siteId, Long.toString(item.getItemID()));
            String [] data = productInfo.split("\\t", -1);
            String score = Float.toString(item.getValue());
            recommendations.add(new Recommendation(data, score));
        }

        return recommendations;
    }


}
