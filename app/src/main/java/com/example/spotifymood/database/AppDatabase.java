package com.example.spotifymood.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.spotifymood.model.Song;

@Database(entities = {Song.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {


    public abstract SongDao songDao();


}

