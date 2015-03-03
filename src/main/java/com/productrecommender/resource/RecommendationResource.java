package com.productrecommender.resource;

import com.productrecommender.core.Recommendation;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
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
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/recommend/{siteId}/{contactId}")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    final static Logger logger = LoggerFactory.getLogger(RecommendationResource.class);

    private final JedisPool pool;
    private final Map<Long, CachingRecommender> recommenders;

    public RecommendationResource(JedisPool pool, Map<Long, CachingRecommender> recommenders) {
        this.pool = pool;
        this.recommenders = recommenders;
    }

    @GET
    public ArrayList<Recommendation> recommend(@PathParam("siteId") LongParam siteId,
                                               @PathParam("contactId") LongParam contactId,
                                               @QueryParam("count") @DefaultValue("10")IntParam count) {

        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + contactId);

        Jedis conn = pool.getResource();
        ArrayList<Recommendation> recommendations = new ArrayList<>();

        try {
            //Check to see if we have data for this site
            if(!conn.sismember(Preprocessor.site_set, siteId.toString())) {
                return recommendations;
            }

            //Get list of recommendations
            List<RecommendedItem> recommendedItems = recommenders.get(siteId.get()).recommend(contactId.get(), count.get());

            //Turn list of recommendations into a JSON arraylist
            for(RecommendedItem item: recommendedItems) {
                String productId = conn.hget(Preprocessor.productCatalog + siteId, Long.toString(item.getItemID()));
                String score = Float.toString(item.getValue());
                recommendations.add(new Recommendation(productId, score));
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }

        pool.returnResource(conn);
        return recommendations;
    }
}