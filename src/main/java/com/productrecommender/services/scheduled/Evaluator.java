package com.productrecommender.services.scheduled;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by psvinaya on 4/9/15.
 */
public class Evaluator {
    private final static Jedis conn = new Jedis("localhost");
    private final static String evaluationScoresPath = "src/data/evaluation/";

    public void evaluateRecommenders (String siteSet, String siteFilePath) {
        Double sum = new Double(0);
        Integer count = new Integer(0);
        HashMap<Long, Double> evaluatorScores = calculateScores(siteSet, siteFilePath, sum, count);
        storeEvaluationScores(evaluatorScores, sum, count);
    }

    public HashMap<Long, Double> calculateScores(String siteSet, String siteFilePath, Double sum, Integer count)
    {
        HashMap<Long, Double> evaluatorScores = new HashMap<>();
        //For every site id in the site set
        for(String _siteId: conn.smembers(siteSet)) {
            try {
                //try to parse a long
                long siteId = Long.parseLong(_siteId);
                //open the corresponding order_history file
                DataModel dataModel = new FileDataModel(new File(siteFilePath + siteId));
                RecommenderBuilder userSimRecBuilder = new CustomRecommenderBuilder();
                //Creating an AverageAbsoluteDifferenceRecommenderEvaluator()
                RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
                double userSimEvaluationScore = evaluator.evaluate(userSimRecBuilder, null, dataModel, 0.7, 1.0);
                evaluatorScores.put(siteId, userSimEvaluationScore);
                sum += userSimEvaluationScore;
                count++;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TasteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return evaluatorScores;
    }

    private void storeEvaluationScores(HashMap<Long, Double> scores, Double sum, Integer count) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String dateString = dateFormat.format(date);
        String fileName = evaluationScoresPath + dateString;
        try {
            File out = new File(fileName);
            FileOutputStream fos = new FileOutputStream(out);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("SiteId, Score");
            bw.newLine();
            for (Map.Entry<Long, Double> entry : scores.entrySet()) {
                bw.write(entry.getKey() + ", " + entry.getValue());
                bw.newLine();
            }
            bw.newLine();
            bw.write("Number of sites: " + count);
            bw.newLine();
            bw.write("Average Score: " + sum/count);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class CustomRecommenderBuilder implements RecommenderBuilder {
        @Override
        public Recommender buildRecommender(DataModel model)throws TasteException
        {
            //calculate the ItemSimilarity matrix
            ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
            //make a recommender
            Recommender recommender = new GenericBooleanPrefItemBasedRecommender(model, similarity);
            //create a caching recommender
            return recommender;
        }
    }

}
