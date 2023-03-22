package com.example.fasta.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User implements Serializable  {
    final public static String COLLECTION_NAME = "users";
    @NonNull
    @PrimaryKey
    String id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String address;
    String userImageUrl;
    ArrayList<String> favoriteProducts;

    public User(String firstName, String lastName, String email, String phoneNumber, String address, ArrayList<String> favoriteProducts) {
        this.address = address;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.favoriteProducts = favoriteProducts;
    }

    public static User create(Map<String, Object> json) {
        JSONObject currentUser = new JSONObject(json);
        String id = (String) json.get("id");
        String firstName = (String) json.get("firstName");
        String lastName = (String) json.get("lastName");
        String address = (String) json.get("address");
        String email = (String) json.get("email");
        String phoneNumber = (String) json.get("phoneNumber");
        String userImageUrl = (String) json.get("userImageUrl");

        ArrayList<String> favoriteProducts = new ArrayList<>();

        try {
            final JSONArray userFavoriteProducts = currentUser.getJSONArray("favoriteProducts");
            for  (int index =0 ;index < userFavoriteProducts.length(); index++) {
                favoriteProducts.add((String) userFavoriteProducts.get(index));
            }
        } catch (JSONException e) {
            Log.d("error", "failed getting user favorite product") ;
        }

        User user = new User(id, firstName, lastName, email, phoneNumber, address, userImageUrl, favoriteProducts);

        return user;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("email", email);
        json.put("phoneNumber", phoneNumber);
        json.put("userImageUrl", userImageUrl);
        json.put("favoriteProducts", Collections.emptyList());
        return json;
    }
}
