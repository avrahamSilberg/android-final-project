package com.example.old2gold.enums;

public enum Size {
    Y("Y"),
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    XXL("XXL");

    private final String sizeString;

    public String getProductSize() {
        return sizeString;
    }

    Size(String genderString) {
        this.sizeString = genderString;
    }

    @Override
    public String toString() {
        return sizeString;
    }
    }
