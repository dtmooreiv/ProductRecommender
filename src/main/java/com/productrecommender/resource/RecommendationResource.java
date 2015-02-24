package com.productrecommender.resource;

import com.productrecommender.core.Recommendation;
import com.productrecommender.services.scheduled.Preprocessor;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.common.TasteException;
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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/recommend/{siteId}/{contactId}")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    final static Logger logger = LoggerFactory.getLogger(RecommendationResource.class);

    private final Jedis conn;
    private final Map<Long, CachingRecommender> recommenders;

    public RecommendationResource(Jedis conn, Map<Long, CachingRecommender> recommenders) {
        this.conn = conn;
        this.recommenders = recommenders;
    }

    @GET
    public Recommendation recommend(@PathParam("siteId") LongParam siteId,
                                    @PathParam("contactId") LongParam contactId,
                                    @QueryParam("count") @DefaultValue("10")IntParam count) {
        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + contactId);


        ArrayList<String> products = new ArrayList<>();

        try {
            //Check to see if we have data for this site
            if(!conn.sismember(Preprocessor.site_set, siteId.toString())) {
                return new Recommendation(products);
            }

            List<RecommendedItem> recommendedItems = recommenders.get(siteId.get()).recommend(contactId.get(), count.get());
            for(RecommendedItem item: recommendedItems) {
                products.add("{\"productId\": \"" + conn.hget(Preprocessor.productCatalog + siteId, item.getItemID() + "") + "\", " + " \"score\": \"" + item.getValue() + "\"}");
            }

        } catch (TasteException e) {
            e.printStackTrace();
        }


        return new Recommendation(products);
    }

}
