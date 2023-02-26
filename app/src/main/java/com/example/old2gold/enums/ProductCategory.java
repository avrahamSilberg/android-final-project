package com.example.old2gold.enums;

public enum ProductCategory {
    BAKING("Baking"),
    COOKING("Cooking"),
    DESERT("Dessert"),
    OTHER("Other");

    private final String productCategory;

    ProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    @Override
    public String toString() {
        return productCategory;
    }
}
