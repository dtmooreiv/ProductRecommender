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
    public static final String output = "order_history_";
    public static final String productCatalog = "product_catalog_";
    public static final String site_set = "site_set";

    public Preprocessor(Jedis conn) {
        this.conn = conn;
    }

    private String getCatalogRedisTableName (Long siteId) {
        return productCatalog + siteId.toString();
    }

    private String siteFileName (Long siteId) { return output + siteId.toString();}

    private void mapToSite(String line) {
        String [] ids = line.split(",");
        if(ids[1].equalsIgnoreCase("\\N")) {
            return;
        }
        long siteId = Long.parseLong(ids[0]);
        LinkedList<String> orderData;
        if(siteBasedOrderHistory.containsKey(siteId)){
            orderData = siteBasedOrderHistory.get(siteId);
        }
        else {
            orderData = new LinkedList<String>();
        }
        String out = processLine(siteId, ids[1], ids[2]);
        orderData.add(out);
        siteBasedOrderHistory.put(siteId, orderData);
    }

    private String processLine(Long siteId, String contactId, String productId ) {
        return contactId + "," + processProductId(siteId, productId);
    }

    private String processProductId(long siteId, String productId) {

        HashMap<String,Integer> perSiteProductMappings;
        if(siteBasedProductMappings.containsKey(siteId)) {
            perSiteProductMappings = siteBasedProductMappings.get(siteId);
        }
        else {
            perSiteProductMappings = new HashMap<String,Integer>();
            siteBasedProductMappings.put(siteId, perSiteProductMappings);
        }

        if(!perSiteProductMappings.containsKey(productId)) {
            perSiteProductMappings.put(productId, perSiteProductMappings.size());
        }

        return perSiteProductMappings.get(productId).toString();
    }

    public void readFile(String filename) {
        File inputFile = new File(filename);
        try{
            Scanner scanner = new Scanner(inputFile);
            siteBasedOrderHistory = new HashMap<Long,LinkedList<String>>();
            siteBasedProductMappings = new HashMap<Long,HashMap<String,Integer>>();
            while (scanner.hasNextLine()) {
                mapToSite(scanner.nextLine());
            }
            writeOrderHistoryBySite();
            writeProductCatalogs();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void writeProductCatalogs() {
        for (Map.Entry<Long,HashMap<String, Integer>> productMappingPerSite : siteBasedProductMappings.entrySet())
        {
            String redisHashName = getCatalogRedisTableName(productMappingPerSite.getKey());
            for(Map.Entry<String, Integer> product : productMappingPerSite.getValue().entrySet()) {
                conn.hset(redisHashName, product.getValue().toString(), product.getKey());
            }
        }
    }

    private void writeOrderHistoryBySite() {
        for (Map.Entry<Long,LinkedList<String>> entry : siteBasedOrderHistory.entrySet())
        {
            conn.sadd(site_set, entry.getKey().toString());
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
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
