package com.productrecommender.services.scheduled;

import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class Preprocessor {

    private final Jedis conn;

    private HashMap<Long,LinkedList<String>> siteBasedOrderHistory;
    private HashMap<Long,HashMap<String, Integer>> siteBasedProductMappings;

    private String orderHistoryOutputFile;
    private String productCatalogTableName;
    private String siteSetName;

    public Preprocessor(Jedis conn, String orderHistoryOutputFile, String productCatalogTableName, String siteSetName) {
        this.conn = conn;
        this.orderHistoryOutputFile = orderHistoryOutputFile;
        this.productCatalogTableName = productCatalogTableName;
        this.siteSetName = siteSetName;
    }

    public void processFile(String filename) {
        File inputFile = new File(filename);
        try{
            Scanner scanner = new Scanner(inputFile);
            siteBasedOrderHistory = new HashMap<>();
            siteBasedProductMappings = new HashMap<>();
            while (scanner.hasNextLine()) {
                mapToSite(scanner.nextLine());
            }
            writeOrderHistoryBySite();
            writeProductCatalogsBySite();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void mapToSite(String line) {
        // split line into it individual parts which are siteId, contactId, productId
        String [] ids = line.split(",");

        // Check that the contactId is not empty if so don't process line
        if(ids[1].equalsIgnoreCase("\\N")) {
            return;
        }

        // get any existing orderData or initialize a new list based on the current siteId
        long siteId = Long.parseLong(ids[0]);
        LinkedList<String> orderData;
        if(siteBasedOrderHistory.containsKey(siteId)){
            orderData = siteBasedOrderHistory.get(siteId);
        }
        else {
            orderData = new LinkedList<>();
        }

        // add order data to the list
        orderData.add(processLine(siteId, ids[1], ids[2]));

        // save changes to the order data list
        siteBasedOrderHistory.put(siteId, orderData);
    }

    private String processLine(Long siteId, String contactId, String productId ) {
        return contactId + "," + processProductId(siteId, productId);
    }

    private String processProductId(long siteId, String productId) {
        // get any existing productData or initialize a new list based on the current siteId
        HashMap<String,Integer> perSiteProductMappings;
        if(siteBasedProductMappings.containsKey(siteId)) {
            perSiteProductMappings = siteBasedProductMappings.get(siteId);
        } else {
            perSiteProductMappings = new HashMap<>();
            siteBasedProductMappings.put(siteId, perSiteProductMappings);
        }

        // add productData for current item if it does not yet exist
        if(!perSiteProductMappings.containsKey(productId)) {
            perSiteProductMappings.put(productId, perSiteProductMappings.size());
        }

        // return the value mapped to the current productId
        return perSiteProductMappings.get(productId).toString();
    }

    // Takes all processed product data and saves it to redis by the site id
    private void writeProductCatalogsBySite() {
        for (Map.Entry<Long,HashMap<String, Integer>> productMappingPerSite : siteBasedProductMappings.entrySet())
        {
            String redisHashName = getCatalogRedisTableName(productMappingPerSite.getKey());
            for(Map.Entry<String, Integer> product : productMappingPerSite.getValue().entrySet()) {
                conn.hset(redisHashName, product.getValue().toString(), product.getKey());
            }
        }
    }

    // returns redis table name for saving the product data by siteId
    private String getCatalogRedisTableName (Long siteId) {
        return productCatalogTableName + siteId.toString();
    }

    // Takes all processed order data and writes it to individual files by the site id
    private void writeOrderHistoryBySite() {
        for (Map.Entry<Long,LinkedList<String>> entry : siteBasedOrderHistory.entrySet())
        {
            conn.sadd(siteSetName, entry.getKey().toString());
            String siteFileName = siteFileName(entry.getKey());
            File out;
            FileOutputStream fos;
            BufferedWriter bw;
            try {
                out = new File(siteFileName);
                fos = new FileOutputStream(out);
                bw = new BufferedWriter(new OutputStreamWriter(fos));
                for(String line: entry.getValue()) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // return path/filename for the saving the order data by the siteId
    private String siteFileName (Long siteId) {
        return orderHistoryOutputFile + siteId.toString();
    }

}