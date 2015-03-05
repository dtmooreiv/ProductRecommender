package com.productrecommender.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recommendation {

    private final static String titleDefault = "Default Title";
    private final static String productUrlDefault = "Default Product Url";
    private final static String imageUrlDefault = "http://bronto.com/files/upload-docs/bronto-logo.png";
    private final static String categoryDefault = "Default Category";
    private final static String descriptionDefault = "Default Description";

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
        this(productId, titleDefault, productUrlDefault, imageUrlDefault, categoryDefault, descriptionDefault, score);
    }

    public Recommendation(String[] productInfo, String score) {
        this(productInfo[0], productInfo[1], productInfo[2], productInfo[3], productInfo[4], productInfo[5], score);
    }

    public Recommendation(String productId, String title, String productUrl, String imageUrl, String category, String description, String score) {
        this.productId = productId;
        this.title = title;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
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
