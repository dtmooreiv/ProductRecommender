package com.productrecommender.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recommendation {

    private final static String[] defaultText = {"Default Title",
            "Default Product Url",
            "http://bronto.com/files/upload-docs/bronto-logo.png",
            "Default Category",
            "Default Description"};

    private String productId;
    private String score;
    private String title;
    private String productUrl;
    private String imageUrl;
    private String category;
    private String description;

    public Recommendation() {
        // Create Empty Recommendation everything is null
    }

    public Recommendation(String productId, String score) {
        this(productId, defaultText[0], defaultText[1], defaultText[2], defaultText[3], defaultText[4], score);
    }

    public Recommendation(String[] product, String score) {
        this(product[0], product[1], product[2], product[3], product[4], product[5], score);
    }

    public Recommendation(String productId, String title, String productUrl, String imageUrl, String category, String description, String score) {
        this.productId = productId;
        this.title = checkString(title,0);
        this.productUrl = checkString(productUrl,1);
        this.imageUrl = checkString(imageUrl,2);
        this.category = checkString(category,3);
        this.description = checkString(description,4);
        this.score = score;
    }

    private String checkString(String str, int defaultIndex) {
        if(str != null && !str.isEmpty()) {
            return str;
        } else {
            return defaultText[defaultIndex];
        }
    }

    @JsonProperty
    public String getProductId() {
        return productId;
    }

    @JsonProperty
    public String getScore() {
        return score;
    }

    @JsonProperty
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty
    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    @JsonProperty
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
