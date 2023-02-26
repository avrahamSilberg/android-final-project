package com.example.old2gold.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.old2gold.MyApplication;

@Database(entities = {Product.class, User.class}, version = 13)
@TypeConverters({Convertors.class})
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract UserDao userDao();
}

public class AppLocalDb{
    static public AppLocalDbRepository db =
            Room.databaseBuilder(MyApplication.getContext(),
                    AppLocalDbRepository.class,
                    "dbFile.db")
                    .fallbackToDestructiveMigration()
                    .build();
}

