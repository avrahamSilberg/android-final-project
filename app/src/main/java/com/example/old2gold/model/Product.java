package com.example.old2gold.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Product {
    final public static String PRODUCTS_COLLECTION_NAME = "products";
    @PrimaryKey
    @NonNull
    String id;
    String title;
    String description;
    String gender;
    String productCondition;
    String productCategory;
    String imageUrl;
    String contactId;
    String price;
    Long updateDate = new Long(0);
    Double longitude;
    Double latitude;
    boolean isDeleted = false;
    boolean isSold = false;

    public Product(String id,String title, String description, String gender,
                   String productCondition, String productCategory, String price,
                   String contactId, Double latitude, Double longitude, boolean isDeleted, boolean isSold) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.gender = gender;
        this.productCondition = productCondition;
        this.productCategory = productCategory;
        this.price = price;
        this.contactId = contactId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDeleted = isDeleted;
        this.isSold = isSold;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("id", id);
        json.put("title", title);
        json.put("description", description);
        json.put("gender", gender);
        json.put("condition", productCondition);
        json.put("productCategory", productCategory);
        json.put("imageUrl", imageUrl);
        json.put("contactId", contactId);
        json.put("longitude", longitude);
        json.put("latitude", latitude);
        json.put("updateDate", FieldValue.serverTimestamp());
        json.put("price", price);
        json.put("isDeleted", isDeleted);
        json.put("isSold", isSold);
        return json;
    }

    public static Product create(Map<String, Object> json) {
        String id = (String) json.get("id");
        String title = (String) json.get("title");
        String description = (String) json.get("description");
        String price = (String) json.get("price");
        String gender = (String) json.get("gender");
        String condition = (String) json.get("condition");
        String productCategory = (String) json.get("productCategory");
        String imageUrl = (String) json.get("imageUrl");
        String contactId = (String) json.get("contactId");
        Timestamp ts = (Timestamp) json.get("updateDate");
        Long updateDate = ts.getSeconds();
        Double latitude = (Double) json.get("latitude");
        Double longitude = (Double) json.get("longitude");
        boolean isDeleted = json.containsKey("isDeleted") && (boolean) json.get("isDeleted");
        boolean isSold = json.containsKey("isSold") && (boolean) json.get("isSold");

        Product product = new Product(id ,title, description, gender, condition,
                productCategory, price, contactId, latitude, longitude, isDeleted, isSold);

        product.setImageUrl(imageUrl);
        product.setUpdateDate(updateDate);


        return product;
    }
}
