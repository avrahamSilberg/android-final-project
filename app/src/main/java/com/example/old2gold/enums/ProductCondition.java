package com.example.old2gold.enums;

public enum ProductCondition {
    GOOD_AS_NEW("Good as new"),
    GOOD("Good"),
    OK("Ok");

    private final String productCondition;

    ProductCondition(String productCondition) {
        this.productCondition = productCondition;
    }

    @Override
    public String toString() {
        return productCondition;
    }
}
