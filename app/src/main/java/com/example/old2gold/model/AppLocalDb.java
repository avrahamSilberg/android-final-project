package com.example.fasta.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.fasta.MyApplication;

@Database(entities = {Recipe.class, User.class}, version = 13)
@TypeConverters({Convertors.class})
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract RecipeDao productDao();
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

