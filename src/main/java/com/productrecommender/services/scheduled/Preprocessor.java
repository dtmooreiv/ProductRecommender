package com.productrecommender.services.scheduled;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Preprocessor {
    HashMap<Long,LinkedList<String>> siteBasedOrderHistory;
    HashMap<Long,HashMap<String, Integer>> siteBasedProductMappings;

    private void readFile(String filename) {
        File inputFile = new File(filename);
        try{
            Scanner scanner = new Scanner(inputFile);
            siteBasedOrderHistory = new HashMap<Long,LinkedList<String>>();
            siteBasedProductMappings = new HashMap<Long,HashMap<String,Integer>>();
            while (scanner.hasNextLine()) {
                mapToSite(scanner.nextLine());
            }
        }
        catch (FileNotFoundException e) {

        }

    }

    private void mapToSite(String line) {
        String [] ids = line.split(",");
        if(ids[1].equals("\n")) {
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
        orderData.add(processLine(siteId, ids[1], ids[2]));
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
        }

        if(!perSiteProductMappings.containsKey(productId)) {
            perSiteProductMappings.put(productId, perSiteProductMappings.size());
        }

        return perSiteProductMappings.get(productId).toString();
    }


}
