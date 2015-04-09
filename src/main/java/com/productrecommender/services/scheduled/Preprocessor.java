package com.productrecommender.services.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class Preprocessor {

    private final static Logger logger = LoggerFactory.getLogger(Preprocessor.class);
    private final Jedis conn;

    private HashMap<String, LinkedList<String>> siteBasedOrderHistory;
    private HashMap<String, HashMap<String, String>> siteBasedProductCatalog;

    private String inputFilesPath;
    private String outputFilesPath;
    private String siteSetName;

    public Preprocessor(Jedis conn, String inputFilesPath, String outputFilesPath, String siteSetName) {
        this.conn = conn;
        this.inputFilesPath = inputFilesPath;
        this.outputFilesPath = outputFilesPath;
        this.siteSetName = siteSetName;

        this.siteBasedOrderHistory = new HashMap<>();
        this.siteBasedProductCatalog = new HashMap<>();
    }

    public void processFiles(String orderHistoryInput, String orderHistoryPrefix, String productCatalogPrefix) {
        processOrderHistory(orderHistoryInput);
        processProductCatalog(productCatalogPrefix);
        storeOrderHistoryBySite(orderHistoryPrefix);
        storeProductCatalogsBySite(productCatalogPrefix);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++ORDERHISTORY+++++++++++++++++++++++++++++++++++++++++++++++++++++

    private void processOrderHistory(String inputFile) {
        try {
            File input = new File(inputFilesPath + inputFile);
            Scanner scanner = new Scanner(input);
            while (scanner.hasNextLine()) {
                processOrderHistoryLine(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processOrderHistoryLine(String line) {
        // Split line into tokens: siteId, contactId, productId
        String [] token = line.split(",");

        // Check that the contactId is not empty, if so don't process line
        if(token[1].equalsIgnoreCase("\\N")) {
            return;
        }

        // Get any existing data for current site
        LinkedList<String> orderData;
        if(siteBasedOrderHistory.containsKey(token[0])){
            orderData = siteBasedOrderHistory.get(token[0]);
        } else {
            orderData = new LinkedList<>();
        }

        // Add order data to the list
        orderData.add(token[1] + "," + token[2].hashCode());

        // Save changes to the order data list for the current site
        siteBasedOrderHistory.put(token[0], orderData);
        orderHistoryProductData(token[0], token[2]);
    }

    private void orderHistoryProductData(String siteId, String productId) {
        String productInfo = siteId + "\t" + productId + "\t\t\t\t\t";
        processProductCatalogInfo(productInfo);
    }

    // ++++++++++++++++++++++++++++++++++++++++++++++PRODUCTCATALOG+++++++++++++++++++++++++++++++++++++++++++++++++++

    private void processProductCatalog(String inputFilePrefix) {
        try {
            File folder = new File(inputFilesPath);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().startsWith(inputFilePrefix)) {
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            processProductCatalogLine(scanner.nextLine());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processProductCatalogLine(String line) {
        if (line.matches("^1000\\b.*")) {
            line = line.replaceFirst("^1000\\b", "33934");
        } else if (line.matches("^1001\\b.*")) {
            line = line.replaceFirst("^1001\\b", "13703");
        }
        processProductCatalogInfo(line);
    }


    private void processProductCatalogInfo(String productInfo) {
        // split line into it individual parts which are siteId, contactId, productId
        String [] tokens = productInfo.split("\\t",3);

        String catalogKey = tokens[0];
        String productKey = Integer.toString(tokens[1].hashCode());

        // check to see if this product already exists
        HashMap<String, String> productData;
        if(siteBasedProductCatalog.containsKey(catalogKey)) {
            productData = siteBasedProductCatalog.get(catalogKey);
        } else {
            productData = new HashMap<>();
        }

        // add product data to the hashmap
        productData.put(productKey, tokens[1] + "\t" + tokens[2]);

        siteBasedProductCatalog.put(catalogKey, productData);
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // Takes all processed product data and saves it to redis by the site id
    private void storeProductCatalogsBySite(String keyPrefix) {
        for (Map.Entry<String,HashMap<String, String>> entry : siteBasedProductCatalog.entrySet()) {
            String key = keyPrefix + entry.getKey();
            HashMap<String, String> products = entry.getValue();
            logger.info("Key: " + key + " products: " + products);
            conn.hmset(key, products);
        }
    }

    // Takes all processed order data and writes it to individual files by the site id
    private void storeOrderHistoryBySite(String outputFilePrefix) {
        for (Map.Entry<String,LinkedList<String>> entry : siteBasedOrderHistory.entrySet()) {
            conn.sadd(siteSetName, entry.getKey());
            String fileName = outputFilesPath + outputFilePrefix + entry.getKey();
            try {
                File out = new File(fileName);
                FileOutputStream fos = new FileOutputStream(out);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                for(String line: entry.getValue()) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}