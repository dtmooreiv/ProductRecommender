package com.productrecommender.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recommendation {

    private String productId;
    private String score;

    public Recommendation() {
        // Create Empty Recommendation
    }

    public Recommendation(String productId, String score) {
        this.productId = productId;
        this.score = score;
    }

    @JsonProperty
    public String getProductId() {
        return productId;
    }

    @JsonProperty
    public String getScore() {
        return score;
    }
}
