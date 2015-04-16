package com.productrecommender.resource;

import com.productrecommender.core.Recommendation;
import com.productrecommender.params.LongArrayParam;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/recommend/{siteId}/{contactId}")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    private final static Logger logger = LoggerFactory.getLogger(RecommendationResource.class);

    private final JedisPool pool;
    private final Map<Long, CachingRecommender> recommenders;
    private final String siteSetName;
    private final String productCatalogPrefix;

    public RecommendationResource(JedisPool pool,
                                  Map<Long, CachingRecommender> recommenders,
                                  String siteSetName,
                                  String productCatalogPrefix) {
        this.pool = pool;
        this.recommenders = recommenders;
        this.siteSetName = siteSetName;
        this.productCatalogPrefix = productCatalogPrefix;
    }

    @GET
    public HashMap<Long, ArrayList<Recommendation>> recommend(@PathParam("siteId") LongParam siteId,
                                                              @PathParam("contactId") LongArrayParam contactIds,
                                                              @QueryParam("count") @DefaultValue("10")IntParam count) {

        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + contactIds);


        HashMap<Long, ArrayList<Recommendation>> recommendations = new HashMap<>();

        try (Jedis conn = pool.getResource()){
            //Check to see if we have data for this site
            if(!conn.sismember(siteSetName, siteId.toString())) {
                return recommendations;
            }

            //Get list of recommendations per contact id given
            for (long contactId: contactIds.get()) {
                recommendations.put(contactId, recommendationsForContactId(conn, siteId.get(), contactId, count.get()));
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }

        return recommendations;
    }

    private ArrayList<Recommendation> recommendationsForContactId(Jedis conn,
                                                                  long siteId,
                                                                  long contactId,
                                                                  int count) throws TasteException {

        // Use SiteId to get the recommender and then use the contact and count to get the recommendations
        List<RecommendedItem> recommendedItems = recommenders.get(siteId).recommend(contactId, count);

        // Pull out the itemIds into a string array to pass into the jedis request
        String[] resultIds = new String[recommendedItems.size()];
        for (int i = 0; i < recommendedItems.size(); i++) {
            resultIds[i] = String.valueOf(recommendedItems.get(i).getItemID());
        }
        // Use the itemIds from the recommender to get all the item data from redis
        List<String> resultData = conn.hmget(productCatalogPrefix + siteId, resultIds);

        //Turn list of recommendations and its data into an arraylist of Recommendation objects
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < recommendedItems.size(); i++) {
            String productInfo = resultData.get(i);
            String [] data = productInfo.split("\\t",-1);
            String score = String.valueOf(recommendedItems.get(i).getValue());
            recommendations.add(new Recommendation(data, score));
        }

        //return recommendations list
        return recommendations;
    }
}