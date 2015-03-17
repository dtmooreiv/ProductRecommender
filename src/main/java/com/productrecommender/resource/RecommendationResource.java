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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    private final String productCatalogTableName;

    public RecommendationResource(JedisPool pool,
                                  Map<Long, CachingRecommender> recommenders,
                                  String siteSetName,
                                  String productCatalogTableName) {
        this.pool = pool;
        this.recommenders = recommenders;
        this.siteSetName = siteSetName;
        this.productCatalogTableName = productCatalogTableName;
    }

    @GET
    public HashMap<Long, ArrayList<Recommendation>> recommend(@PathParam("siteId") LongParam siteId,
                                                              @PathParam("contactId") LongArrayParam contactIds,
                                                              @QueryParam("count") @DefaultValue("10")IntParam count) {

        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + contactIds);

        Jedis conn = pool.getResource();
        HashMap<Long, ArrayList<Recommendation>> recommendations = new HashMap<>();

        try {
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

        pool.returnResource(conn);
        return recommendations;
    }

    private ArrayList<Recommendation> recommendationsForContactId(Jedis conn, long siteId, long contactId, int count) throws TasteException {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        List<RecommendedItem> recommendedItems = recommenders.get(siteId).recommend(contactId, count);

        //Turn list of recommendations into a JSON arraylist
        for (RecommendedItem item : recommendedItems) {
            String productId = conn.hget(productCatalogTableName + siteId, Long.toString(item.getItemID()));
            String score = Float.toString(item.getValue());
            recommendations.add(new Recommendation(productId, score));
        }
        return recommendations;
    }
}