package com.example.old2gold.model;

import com.example.old2gold.enums.Size;

import java.util.ArrayList;
import java.util.List;

public class ProductSizeCB {
    private String productSize;

    public String getProductSize() {
        return productSize;
    }

    public void setProductTypes(String productType) {
        this.productSize = productType;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    private boolean flag;

    private ProductSizeCB(String productSize) {
        this.productSize = productSize;
        this.flag = false;
    }

    public static List<ProductSizeCB> getAllCheckboxSizes() {
        List<ProductSizeCB> productFilterCBList = new ArrayList<>();
        Size[] productSizes = Size.values();
        for (int i = 0; i < productSizes.length; i++) {
            productFilterCBList.add(i, new ProductSizeCB(productSizes[i].getProductSize()));
        }

        return productFilterCBList;
    }
}
