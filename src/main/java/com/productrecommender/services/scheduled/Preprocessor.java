package com.productrecommender.services.scheduled;

import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class Preprocessor {

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
        // split line into it individual parts which are siteId, contactId, productId
        String [] token = line.split("\\t",-1);

        if (token[0].equals("1000")) {
            token[0] = "33934";
        } else {
            token[0] = "13703";
        }

        String key = token[0] + "_" + token[1].hashCode();

        // check to see if this product already exists
        HashMap<String, String> productData;
        if(siteBasedProductCatalog.containsKey(key)) {
            productData = siteBasedProductCatalog.get(key);
        } else {
            productData = new HashMap<>();
        }

        // add product data to the hashmap
        productData.put("productId",token[1]);
        productData.put("title",token[2]);
        productData.put("productUrl",token[3]);
        productData.put("imageUrl",token[4]);
        productData.put("category",token[5]);
        productData.put("description",token[6]);

        siteBasedProductCatalog.put(key, productData);
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // Takes all processed product data and saves it to redis by the site id
    private void storeProductCatalogsBySite(String keyPrefix) {
        for (Map.Entry<String,HashMap<String, String>> entry : siteBasedProductCatalog.entrySet()) {
            String key = keyPrefix + entry.getKey();
            HashMap<String, String> product = entry.getValue();
            conn.hmset(key, product);
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