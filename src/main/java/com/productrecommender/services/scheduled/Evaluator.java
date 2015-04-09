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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(Preprocessor.class);
    private final Jedis conn;
    private String outputFilesPath;
    private String siteSetName;
    private String orderHistoryPrefix;
    private String evaluationScoresPath;
    private double sum;
    private int count;


    public Evaluator (Jedis conn, String outputFilesPath, String orderHistoryPrefix, String siteSetName) {
        this.conn = conn;
        this.outputFilesPath = outputFilesPath;
        this.siteSetName = siteSetName;
        this.orderHistoryPrefix = orderHistoryPrefix;
        evaluationScoresPath = "src/data/evaluation/";
        sum = 0;
        count = 0;
    }

    public void evaluateRecommenders () {
        HashMap<Long, Double> evaluatorScores = calculateScores();
        storeEvaluationScores(evaluatorScores);
    }

    public HashMap<Long, Double> calculateScores()
    {
        HashMap<Long, Double> evaluatorScores = new HashMap<>();
        //For every site id in the site set
        for(String _siteId: conn.smembers(siteSetName)) {
            try {
                //try to parse a long
                long siteId = Long.parseLong(_siteId);
                //open the corresponding order_history file
                DataModel dataModel = new FileDataModel(new File(outputFilesPath + orderHistoryPrefix + siteId));
                RecommenderBuilder userSimRecBuilder = new UserRecommenderBuilder();
                //Creating an AverageAbsoluteDifferenceRecommenderEvaluator()
                RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
                double userSimEvaluationScore = evaluator.evaluate(userSimRecBuilder, null, dataModel, 0.7, 1.0);
                if (Double.isNaN(userSimEvaluationScore)) {
                    continue;
                }
                evaluatorScores.put(siteId, userSimEvaluationScore);
                sum += userSimEvaluationScore;
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
        return evaluatorScores;
    }

    private void storeEvaluationScores(HashMap<Long, Double> scores) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
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

    public class UserRecommenderBuilder implements RecommenderBuilder {
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
