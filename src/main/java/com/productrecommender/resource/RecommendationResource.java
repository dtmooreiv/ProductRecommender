package com.productrecommender.resource;

import com.productrecommender.core.Recommendation;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Path("/recommend/{siteId}/{contactId}")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    final static Logger logger = LoggerFactory.getLogger(RecommendationResource.class);

    public RecommendationResource() {

    }

    @GET
    public Recommendation recommend(@PathParam("siteId") LongParam siteId,
                                    @PathParam("contactId") LongParam contactId,
                                    @QueryParam("count") @DefaultValue("10")IntParam count) {
        logger.info(count + " recommendations requested for site id " + siteId + " contact id "  + contactId);


        ArrayList<String> products = new ArrayList<>();

        try {
            DataModel model = new FileDataModel(new File("test_skus_without_site_ids.csv"));
            ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
            Recommender recommender = new GenericBooleanPrefItemBasedRecommender(model, similarity);
            List<RecommendedItem> recommendedItemList = recommender.recommend(contactId.get(), count.get());
            for(RecommendedItem item : recommendedItemList) {
                products.add("Product Id: " + item.getItemID() + " " + " score: " + item.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }


        return new Recommendation(products);
    }

}
