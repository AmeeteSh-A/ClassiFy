package com.example.spotifymood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifymood.R;
import com.example.spotifymood.adapter.SearchAdapter;
import com.example.spotifymood.core.SimilarityCalculator; // Added
import com.example.spotifymood.database.AppDatabase;
import com.example.spotifymood.database.DatabaseInitializer;
import com.example.spotifymood.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnSongSelectedListener {

    private static final String TAG = "SearchActivity";

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private SearchAdapter searchAdapter;
    private AppDatabase db;
    private List<Song> allSongsCache;


    private String searchMode;
    public static final String MODE_PICK_SEED = "PICK_SEED";
    public static final String MODE_SHOW_RECOMMENDATIONS = "SHOW_RECOMMENDATIONS";

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final Handler searchDelayHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchMode = getIntent().getStringExtra("SEARCH_MODE");
        if (searchMode == null) {
            Log.e(TAG, "Search mode not provided! Defaulting to PICK_SEED.");
            searchMode = MODE_PICK_SEED;
        }


        db = DatabaseInitializer.getDatabase(getApplicationContext());

        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this);
        searchResultsRecyclerView.setAdapter(searchAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchDelayHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                searchRunnable = () -> {
                    if (query.length() > 2) {
                        performSearch(query);
                    } else {
                        searchAdapter.setSongs(new ArrayList<>());
                    }
                };
                searchDelayHandler.postDelayed(searchRunnable, 300);
            }
        });

        if (searchMode.equals(MODE_SHOW_RECOMMENDATIONS)) {
            backgroundExecutor.execute(() -> {
                allSongsCache = db.songDao().getAllSongs();
                Log.d(TAG, "All songs cached for recommendation mode.");
            });
        }

    }


    private void performSearch(String query) {
        String searchQuery = "%" + query + "%";

        backgroundExecutor.execute(() -> {
            List<Song> results = db.songDao().searchSongsByName(searchQuery);

            mainThreadHandler.post(() -> {
                searchAdapter.setSongs(results);
            });
        });
    }


    @Override
    public void onSongSelected(Song song) {
        if (searchMode.equals(MODE_PICK_SEED)) {
            Log.d(TAG, "Mode: PICK_SEED. Returning selected song.");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedSong", song);
            setResult(RESULT_OK, resultIntent);
            finish();

        } else if (searchMode.equals(MODE_SHOW_RECOMMENDATIONS)) {
            Log.d(TAG, "Mode: SHOW_RECOMMENDATIONS. Finding similar songs.");
            Toast.makeText(this, "Finding songs similar to: " + song.getTrackName(), Toast.LENGTH_SHORT).show();

            backgroundExecutor.execute(() -> {
                if (allSongsCache == null || allSongsCache.isEmpty()) {
                    allSongsCache = db.songDao().getAllSongs();
                }

                if (allSongsCache == null || allSongsCache.isEmpty()) {
                    mainThreadHandler.post(() -> Toast.makeText(this, "Error: Could not load songs from database.", Toast.LENGTH_SHORT).show());
                    return;
                }

                List<Song> recommendations = SimilarityCalculator.findSimilarSongs(song, allSongsCache, 50);

                if (recommendations.isEmpty()) {
                    mainThreadHandler.post(() -> Toast.makeText(this, "No similar songs found.", Toast.LENGTH_SHORT).show());
                    return;
                }

                ArrayList<Song> recommendationsList = new ArrayList<>(recommendations);
                Intent intent = new Intent(SearchActivity.this, RecommendationsActivity.class);
                intent.putExtra("originalSongName", song.getTrackName());
                intent.putExtra("recommendations", recommendationsList);

                mainThreadHandler.post(() -> {
                    startActivity(intent);
                    finish();
                });
            });
        }

    }
}

