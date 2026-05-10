package com.recyscan.api;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("product")
    private ApiProduct product;

    public int getStatus() { return status; }
    public ApiProduct getProduct() { return product; }

    public static class ApiProduct {
        @SerializedName("product_name")
        private String productName;

        @SerializedName("_id")
        private String barcode;

        @SerializedName("packaging")
        private String packaging;

        @SerializedName("categories")
        private String categories;

        public String getProductName() { return productName; }
        public String getBarcode() { return barcode; }
        public String getPackaging() { return packaging; }
        public String getCategories() { return categories; }
    }
}
