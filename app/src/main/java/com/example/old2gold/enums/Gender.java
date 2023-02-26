package com.example.old2gold.enums;

public enum Gender {
    FEMALE("Female"),
    MALE("Male"),
    OTHER("Other");

    private final String genderString;

    Gender(String genderString) {
        this.genderString = genderString;
    }

    @Override
    public String toString() {
        return genderString;
    }
}