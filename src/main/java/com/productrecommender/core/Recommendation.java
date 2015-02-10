package com.productrecommender.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Recommendation {

    private ArrayList<String> productIds;

    public Recommendation() {
        //Jackson Serialization Apparently
    }

    public Recommendation(ArrayList<String> productIds) {
        this.productIds = productIds;
    }

    @JsonProperty
    public ArrayList<String> getProductIds() {
        return productIds;
    }
}
