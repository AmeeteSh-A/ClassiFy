package com.example.spotifymood.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// --- NEW IMPORTS FOR WORKMANAGER ---
//import androidx.work.Constraints;
//import androidx.work.ExistingPeriodicWorkPolicy;
//import androidx.work.NetworkType;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;
//// --- END NEW IMPORTS ---

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.spotifymood.R;
import com.example.spotifymood.adapter.PlaylistAdapter;
import com.example.spotifymood.auth.SpotifyAuthManager;
import com.example.spotifymood.network.PlaylistDto;
import com.example.spotifymood.network.UserPlaylistsDto;
import com.example.spotifymood.repository.AuthRepository;
import com.example.spotifymood.repository.PlaylistRepository;
//import com.example.spotifymood.worker.HistorySyncWorker; // Import the new worker

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit; // Import for time unit

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String HISTORY_SYNC_WORK_NAME = "history_sync_work";

    private SpotifyAuthManager authManager;
    private AuthRepository authRepo;
    private PlaylistAdapter playlistAdapter;
    private RecyclerView playlistRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchPlaylists();

        Button loginBtn = findViewById(R.id.loginBtn);
        Button fetchBtn = findViewById(R.id.fetchBtn);
        Button tunematchbutton = findViewById(R.id.tunematchbutton);
        Button searchButton = findViewById(R.id.searchButton);
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);

        tunematchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intenttunematch = new Intent(MainActivity.this, TuneMatchActivity.class);
                startActivity(intenttunematch);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
                // Tell SearchActivity *why* we are launching it
                intentSearch.putExtra("SEARCH_MODE", "SHOW_RECOMMENDATIONS");
                startActivity(intentSearch);
            }
        });

        authManager = new SpotifyAuthManager();
        authRepo = new AuthRepository();

        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loginBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Spotify Login...", Toast.LENGTH_SHORT).show();
            authManager.startLogin(this);
        });

        fetchBtn.setOnClickListener(v -> fetchPlaylists());

        handleAuthIntent(getIntent());

    }

    private void fetchPlaylists() {
        Log.d(TAG, "FetchPlaylists() called");

        new Thread(() -> {
            try {
                Log.d(TAG, "Creating PlaylistRepository instance...");
                PlaylistRepository repo = new PlaylistRepository();

                Log.d(TAG, "Fetching liked songs as pseudo-playlist...");
                PlaylistDto likedSongs = repo.getLikedSongsAsPlaylist(MainActivity.this);
                if (likedSongs != null) {
                    Log.d(TAG, "Liked songs fetched successfully: " + likedSongs.name);
                } else {
                    Log.w(TAG, "Liked songs fetch returned null");
                }

                Log.d(TAG, "Fetching user playlists from Spotify API...");
                UserPlaylistsDto playlistResponse = repo.getUserPlaylists(MainActivity.this);

                if (playlistResponse == null) {
                    Log.e(TAG, "Playlist response is null!");
                } else {
                    Log.d(TAG, "Playlist response received. Total items: " +
                            (playlistResponse.items != null ? playlistResponse.items.size() : 0));
                }

                List<PlaylistDto> playlists = (playlistResponse != null && playlistResponse.items != null)
                        ? playlistResponse.items
                        : new ArrayList<>();

                if (likedSongs != null) {
                    playlists.add(0, likedSongs);
                    Log.d(TAG, "Added liked songs at top of playlist list.");
                }

                runOnUiThread(() -> {
                    Log.d(TAG, "Updating UI thread with playlists...");
                    if (playlists.isEmpty()) {
                        Log.w(TAG, "No playlists found or fetch failed.");
                        Toast.makeText(this, "No playlists found or fetch failed.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Displaying " + playlists.size() + " playlists in RecyclerView.");
                        playlistAdapter = new PlaylistAdapter(playlists, playlistDto -> {
                            Log.d(TAG, "Playlist clicked: " + playlistDto.name + " (ID: " + playlistDto.id + ")");
                            Intent intent = new Intent(MainActivity.this, TracksActivity.class);
                            intent.putExtra("playlistId", playlistDto.id);
                            intent.putExtra("playlistName", playlistDto.name);
                            startActivity(intent);
                        });

                        playlistRecyclerView.setAdapter(playlistAdapter);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching playlists: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleAuthIntent(intent);
    }

    private void handleAuthIntent(Intent intent) {
        if (intent == null) return;

        String code = intent.getStringExtra("code");
        String error = intent.getStringExtra("error");

        if (error != null) {
            Toast.makeText(this, "Login Error: " + error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (code != null) {
            new Thread(() -> {
                boolean success = authRepo.exchangeCodeForToken(this, code);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show();
                        // --- NEW: This is the perfect place to schedule the worker! ---
                        scheduleHistorySyncWorker();
                        // ---
                    } else {
                        Toast.makeText(this, "Failed to exchange token", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }


    private void scheduleHistorySyncWorker() {
        Log.d(TAG, "Scheduling history sync worker...");

//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build();

//        PeriodicWorkRequest syncRequest =
//                new PeriodicWorkRequest.Builder(HistorySyncWorker.class, 15, TimeUnit.MINUTES)
//                        .setConstraints(constraints)
//                        .build();


//        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
//                HISTORY_SYNC_WORK_NAME,
//                ExistingPeriodicWorkPolicy.KEEP,
//                syncRequest
//        );
    }
}

