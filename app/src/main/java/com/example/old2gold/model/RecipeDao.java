package com.example.fasta.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {
    @Query("select * from Recipe where isDeleted=0 order by updateDate desc")
    List<Recipe> getAll();

    @Query("select * from Recipe where contactId=:id and isDeleted=0 order by updateDate desc")
    List<Recipe> getProductsByContactId(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Recipe... recipes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Recipe> recipes);

    @Delete
    void delete(Recipe recipe);
}
