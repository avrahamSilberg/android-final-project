package com.example.fasta.model;


import androidx.annotation.NonNull;
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
public class Recipe {
    final public static String PRODUCTS_COLLECTION_NAME = "recipes";
    @PrimaryKey
    @NonNull
    String id;
    String title;
    String description;
    String productCondition;
    String productCategory;
    String imageUrl;
    String contactId;
    Long updateDate = new Long(0);
    Double longitude;
    Double latitude;
    boolean isDeleted = false;

    public Recipe(String id, String title, String description,
                  String productCondition, String productCategory,
                  String contactId, boolean isDeleted) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.productCondition = productCondition;
        this.productCategory = productCategory;
        this.contactId = contactId;
        this.isDeleted = isDeleted;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("id", id);
        json.put("title", title);
        json.put("description", description);
        json.put("condition", productCondition);
        json.put("productCategory", productCategory);
        json.put("imageUrl", imageUrl);
        json.put("contactId", contactId);
        json.put("updateDate", FieldValue.serverTimestamp());
        json.put("isDeleted", isDeleted);
        return json;
    }

    public static Recipe create(Map<String, Object> json) {
        String id = (String) json.get("id");
        String title = (String) json.get("title");
        String description = (String) json.get("description");
        String condition = (String) json.get("condition");
        String productCategory = (String) json.get("productCategory");
        String imageUrl = (String) json.get("imageUrl");
        String contactId = (String) json.get("contactId");
        Timestamp ts = (Timestamp) json.get("updateDate");
        Long updateDate = ts.getSeconds();
        boolean isDeleted = json.containsKey("isDeleted") && (boolean) json.get("isDeleted");

        Recipe recipe = new Recipe(id ,title, description, condition,
                productCategory, contactId, isDeleted);

        recipe.setImageUrl(imageUrl);
        recipe.setUpdateDate(updateDate);


        return recipe;
    }
}
