package com.example.fasta.model;

import com.example.fasta.enums.RecipeCategory;

import java.util.ArrayList;
import java.util.List;

public class ProductFilterCB {
    private String productType;

    public String getProductType() {
        return productType;
    }

    public void setProductTypes(String productType) {
        this.productType = productType;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    private ProductFilterCB(String productType) {
        this.productType = productType;
        this.flag = false;
    }

    private boolean flag;

    public static List<ProductFilterCB> getAllCheckboxTypes() {
        List<ProductFilterCB> productSizeFilterCBList = new ArrayList<>();
        RecipeCategory[] productTypes = RecipeCategory.values();
        for (int i = 0; i < productTypes.length; i++) {
            productSizeFilterCBList.add(i, new ProductFilterCB(productTypes[i].toString()));
        }

        return productSizeFilterCBList;
    }

}