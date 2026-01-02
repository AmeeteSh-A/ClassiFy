package com.example.spotifymood.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import com.example.spotifymood.model.Song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DatabaseInitializer {

    private static final String TAG = "DatabaseInitializer";
    private static AppDatabase database;

    public static synchronized AppDatabase getDatabase(final Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "song-database")
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                prePopulateDatabase(context.getApplicationContext(), database);
                            });
                        }
                    })
                    .build();
        }
        return database;
    }

    private static void prePopulateDatabase(Context context, AppDatabase db) {
        List<Song> songsToInsert = new ArrayList<>();
        String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        int lineCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("dataset(features)_cleaned.csv")))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] tokens = line.split(regex, -1);

                if (tokens.length >= 23) {
                    try {
                        Song song = new Song();
                        song.setTrackUri(tokens[0]);
                        song.setTrackName(tokens[1]);
                        song.setArtistNames(tokens[3]);
                        song.setDanceability(Float.parseFloat(tokens[12]));
                        song.setEnergy(Float.parseFloat(tokens[13]));
                        song.setKey((int) Float.parseFloat(tokens[14]));
                        song.setLoudness(Float.parseFloat(tokens[15]));
                        // --- Also applied fix here for safety ---
                        song.setMode((int) Float.parseFloat(tokens[16]));
                        song.setSpeechiness(Float.parseFloat(tokens[17]));
                        song.setAcousticness(Float.parseFloat(tokens[18]));
                        song.setInstrumentalness(Float.parseFloat(tokens[19]));
                        song.setLiveness(Float.parseFloat(tokens[20]));
                        song.setValence(Float.parseFloat(tokens[21]));
                        song.setTempo(Float.parseFloat(tokens[22]));

                        songsToInsert.add(song);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, "Skipping row " + lineCount + " due to parsing error.", e);
                        skippedCount++;
                    }
                } else {
                    if (lineCount <= 5) {
                        Log.e(TAG, "Incorrect token count on line " + lineCount + ". Expected >= 23, but found " + tokens.length + ".");
                        Log.e(TAG, "Line content: " + line);
                    }
                    skippedCount++;
                }
            }

            if (!songsToInsert.isEmpty()) {
                db.songDao().insertAll(songsToInsert);
                Log.d(TAG, "Database pre-population complete. " + songsToInsert.size() + " songs inserted. " + skippedCount + " rows skipped.");
            } else {
                Log.e(TAG, "Database pre-population failed. No songs were inserted after processing " + lineCount + " lines.");
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file or pre-populating database", e);
        }
    }
}

