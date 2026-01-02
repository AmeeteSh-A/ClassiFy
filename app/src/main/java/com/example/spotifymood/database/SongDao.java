package com.example.spotifymood.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.spotifymood.model.Song;
import java.util.List;

@Dao
public interface SongDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Song> songs);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(Song song);


    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();


    @Query("SELECT * FROM songs WHERE track_uri = :trackUri LIMIT 1")
    Song findByTrackUri(String trackUri);


    @Query("SELECT * FROM songs WHERE track_uri IN (:trackUris)")
    List<Song> findByTrackUris(List<String> trackUris);

    @Query("DELETE FROM songs")
    void deleteAll();


    @Query("SELECT * FROM songs WHERE trackName LIKE :query")
    List<Song> searchSongsByName(String query);
}

