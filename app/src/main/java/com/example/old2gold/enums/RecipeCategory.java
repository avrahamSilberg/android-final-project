package com.example.old2gold.enums;

public enum RecipeCategory {
    BAKING("Baking"),
    COOKING("Cooking"),
    DESERT("Dessert"),
    OTHER("Other");

    private final String recipeCategory;

    RecipeCategory(String recipeCategory) {
        this.recipeCategory = recipeCategory;
    }

    @Override
    public String toString() {
        return recipeCategory;
    }
}
