package com.example.spotifymood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable; // Added
import android.text.TextWatcher; // Added
import android.util.Log;
import android.widget.EditText; // Added
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifymood.R;
import com.example.spotifymood.adapter.TrackAdapter;
import com.example.spotifymood.core.SimilarityCalculator;
import com.example.spotifymood.database.AppDatabase;
import com.example.spotifymood.database.DatabaseInitializer;
import com.example.spotifymood.model.Song;
import com.example.spotifymood.model.TrackItem;
import com.example.spotifymood.network.PlaylistItemsDto;
import com.example.spotifymood.repository.PlaylistRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor; // Added
import java.util.concurrent.Executors; // Added
import java.util.stream.Collectors;

public class TracksActivity extends AppCompatActivity implements TrackAdapter.OnTrackClickListener {

    private static final String TAG = "TracksActivity";

    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;
    private PlaylistRepository playlistRepo;
    private AppDatabase db;
    private SwitchMaterial playlistOnlyToggle;
    private EditText playlistSearchEditText;

    private LinearLayoutManager layoutManager;
    private int offset = 0;
    private final int limit = 50;
    private boolean isLoading = false;
    private boolean hasMoreItems = true;
    private String playlistId;

    private final Executor backgroundLoader = Executors.newSingleThreadExecutor();
    private boolean isInitialLoad = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        db = DatabaseInitializer.getDatabase(getApplicationContext());
        playlistOnlyToggle = findViewById(R.id.playlistOnlyToggle);
        playlistSearchEditText = findViewById(R.id.playlistSearchEditText);

        recyclerView = findViewById(R.id.trackRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        trackAdapter = new TrackAdapter(new ArrayList<>());
        recyclerView.setAdapter(trackAdapter);
        trackAdapter.setOnTrackClickListener(this);
        addScrollListener();

        playlistRepo = new PlaylistRepository();

        playlistId = getIntent().getStringExtra("playlistId");
        if (playlistId == null) {
            Toast.makeText(this, "Playlist ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        playlistSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (trackAdapter != null) {
                    trackAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        loadTracks();
    }

    @Override
    public void onTrackClick(TrackItem trackItem) {

        Toast.makeText(this, "Finding songs similar to: " + trackItem.getName(), Toast.LENGTH_SHORT).show();

        final boolean isPlaylistOnlyMode = playlistOnlyToggle.isChecked();

        new Thread(() -> {
            Song selectedSong = db.songDao().findByTrackUri(trackItem.getUri());

            if (selectedSong == null) {
                runOnUiThread(() -> Toast.makeText(this, "Song not found in local database.", Toast.LENGTH_SHORT).show());
                Log.w(TAG, "Could not find song with URI: " + trackItem.getUri());
                return;
            }
            List<Song> searchSpace;
            if (isPlaylistOnlyMode) {
                Log.d(TAG, "Playlist-only mode is ON. Searching within current playlist.");

                List<String> currentPlaylistUris = trackAdapter.getTracks().stream()
                        .map(TrackItem::getUri)
                        .collect(Collectors.toList());
                searchSpace = db.songDao().findByTrackUris(currentPlaylistUris);
            } else {
                Log.d(TAG, "Playlist-only mode is OFF. Searching entire database.");
                searchSpace = db.songDao().getAllSongs();
            }

            if (searchSpace == null || searchSpace.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No songs available to search.", Toast.LENGTH_SHORT).show());
                return;
            }

            List<Song> recommendations = SimilarityCalculator.findSimilarSongs(selectedSong, searchSpace, 50);

            if (recommendations.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No similar songs found.", Toast.LENGTH_SHORT).show());
                return;
            }

            ArrayList<Song> recommendationList = new ArrayList<>(recommendations);

            Intent intent = new Intent(TracksActivity.this, RecommendationsActivity.class);
            intent.putExtra("originalSongName", selectedSong.getTrackName());
            intent.putExtra("recommendations", recommendationList);
            startActivity(intent);

        }).start();
    }

    private void addScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMoreItems) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= (offset - 1)) {
                        loadTracks();
                    }
                }
            }
        });
    }

    private void loadTracks() {
        if (isLoading || !hasMoreItems) return;
        isLoading = true;
        Log.d(TAG, "Loading tracks for UI, offset: " + offset);

        new Thread(() -> {
            try {
                PlaylistItemsDto itemsDto;

                if ("liked_songs".equals(playlistId)) {
                    itemsDto = playlistRepo.getLikedSongs(this, limit, offset);
                } else {
                    itemsDto = playlistRepo.getPlaylistItems(this, playlistId, limit, offset);
                }

                List<TrackItem> newTracks = itemsDto != null ? itemsDto.getTrackItems() : new ArrayList<>();
                Log.d(TAG, "Fetched " + newTracks.size() + " tracks.");

                if (!newTracks.isEmpty()) {
                    runOnUiThread(() -> {
                        trackAdapter.addTracks(newTracks);
                        offset += newTracks.size();
                        isLoading = false;
                    });
                } else {
                    hasMoreItems = false;
                    runOnUiThread(() -> isLoading = false);
                }

                if (isInitialLoad) {
                    isInitialLoad = false;
                    Log.d(TAG, "First page loaded. Starting silent background load...");
                    startSilentBackgroundLoad();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading tracks", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
            }
        }).start();
    }

    private void startSilentBackgroundLoad() {
        backgroundLoader.execute(() -> {
            while (hasMoreItems) {
                if (isLoading) {
                    try {
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Background loader interrupted", e);
                        break;
                    }
                }

                if (!hasMoreItems) break;

                Log.d(TAG, "Background loading tracks, offset: " + offset);

                try {
                    PlaylistItemsDto itemsDto;
                    if ("liked_songs".equals(playlistId)) {
                        itemsDto = playlistRepo.getLikedSongs(this, limit, offset);
                    } else {
                        itemsDto = playlistRepo.getPlaylistItems(this, playlistId, limit, offset);
                    }

                    List<TrackItem> newTracks = itemsDto != null ? itemsDto.getTrackItems() : new ArrayList<>();
                    Log.d(TAG, "Background fetched " + newTracks.size() + " tracks.");

                    if (!newTracks.isEmpty()) {
                        runOnUiThread(() -> {
                            trackAdapter.addTracks(newTracks);
                            offset += newTracks.size();
                        });
                    } else {
                        hasMoreItems = false;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in background track loader", e);

                    hasMoreItems = false;
                }

            }
            Log.d(TAG, "Silent background loading complete.");
        });
    }

}

