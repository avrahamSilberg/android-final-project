package com.example.old2gold.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("select * from Product where isDeleted=0 and isSold=0 order by updateDate desc")
    List<Product> getAll();

    @Query("select * from Product where contactId=:id and isDeleted=0 order by updateDate desc")
    List<Product> getProductsByContactId(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Product... products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Delete
    void delete(Product product);
}
