package com.example.spotifymood.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.spotifymood.R;
import com.example.spotifymood.core.SimilarityCalculator;
import com.example.spotifymood.database.AppDatabase;
import com.example.spotifymood.database.DatabaseInitializer;
import com.example.spotifymood.model.Song;
import com.example.spotifymood.model.TrackItem;
import com.example.spotifymood.network.PlaylistDto;
import com.example.spotifymood.network.PlaylistItemsDto;
import com.example.spotifymood.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TuneMatchActivity extends AppCompatActivity {

    private static final String TAG = "TuneMatchActivity";
    private static final int SEARCH_SONG_REQUEST = 1;

    private Button btnCreatePlaylist;
    private CardView songCard;
    private ImageView albumArtImageView;
    private TextView trackNameTextView;
    private TextView artistNameTextView;
    private ImageButton btnLike, btnDislike;
    private Button btnChooseNewSeed;


    private AppDatabase db;
    private PlaylistRepository playlistRepository;
    private Song currentSong;
    private Song activeSeedSong;
    private List<Song> likedSongsList = new ArrayList<>();
    private Set<String> shownSongUris = new HashSet<>();
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private List<Song> allSongsCache;


    private float initialX;
    private float dX;
    private static final float SWIPE_THRESHOLD_PERCENT = 0.4f;
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunematch);

        db = DatabaseInitializer.getDatabase(getApplicationContext());
        playlistRepository = new PlaylistRepository();

        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        songCard = findViewById(R.id.songCard);
        albumArtImageView = findViewById(R.id.albumArtImageView);
        trackNameTextView = findViewById(R.id.trackNameTextView);
        artistNameTextView = findViewById(R.id.artistNameTextView);
        btnDislike = findViewById(R.id.btnDislike);
        btnLike = findViewById(R.id.btnLike);
        btnChooseNewSeed = findViewById(R.id.btnChooseNewSeed);

        setupClickListeners();
        setupSwipeListener();
        loadInitialData();
    }

    private void setupClickListeners() {
        btnCreatePlaylist.setOnClickListener(v -> {
            if (likedSongsList.isEmpty()) {
                Toast.makeText(this, "You haven't liked any songs yet!", Toast.LENGTH_SHORT).show();
                return;
            }

            String playlistName = activeSeedSong != null
                    ? "Mix - " + activeSeedSong.getTrackName()
                    : "TuneMatch Mix";
            String playlistDescription = "A mix of songs created with SpotifyMood's TuneMatch feature.";

            Toast.makeText(this, "Creating playlist '" + playlistName + "'...", Toast.LENGTH_SHORT).show();
            setLoadingState(true);

            backgroundExecutor.execute(() -> {
                PlaylistDto newPlaylist = playlistRepository.createSpotifyPlaylist(this, playlistName, playlistDescription);

                if (newPlaylist != null && newPlaylist.id != null) {
                    List<String> trackUris = likedSongsList.stream()
                            .map(Song::getTrackUri)
                            .collect(Collectors.toList());
                    boolean addedSuccessfully = playlistRepository.addTracksToSpotifyPlaylist(this, newPlaylist.id, trackUris);

                    mainThreadHandler.post(() -> {
                        Toast.makeText(this,
                                addedSuccessfully
                                        ? "Playlist created and songs added!"
                                        : "Playlist created, but failed to add songs.",
                                Toast.LENGTH_LONG).show();
                        setLoadingState(false);
                    });
                } else {
                    mainThreadHandler.post(() -> {
                        Toast.makeText(this, "Failed to create playlist.", Toast.LENGTH_LONG).show();
                        setLoadingState(false);
                    });
                }
            });
        });

        btnChooseNewSeed.setOnClickListener(v -> showNewSeedDialog());

        btnLike.setOnClickListener(v -> {
            if (!isLoading) animateSwipe(true);
        });

        btnDislike.setOnClickListener(v -> {
            if (!isLoading) animateSwipe(false);
        });
    }

    private void setupSwipeListener() {
        songCard.setOnTouchListener((view, event) -> {
            if (isLoading) return false;
            float cardWidth = view.getWidth();
            float swipeThreshold = cardWidth * SWIPE_THRESHOLD_PERCENT;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = view.getX();
                    dX = event.getRawX() - view.getTranslationX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() - dX;
                    view.setTranslationX(newX);
                    view.setRotation(newX / 20.0f);
                    view.setAlpha(1.0f - Math.abs(newX) / cardWidth);
                    break;
                case MotionEvent.ACTION_UP:
                    float translationX = view.getTranslationX();
                    if (translationX > swipeThreshold) commitSwipe(true, cardWidth);
                    else if (translationX < -swipeThreshold) commitSwipe(false, cardWidth);
                    else resetCardPosition();
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    private void resetCardPosition() {
        songCard.animate()
                .translationX(0f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(200)
                .start();
    }

    private void commitSwipe(boolean isLike, float screenWidth) {
        setLoadingState(true);

        float targetX = isLike ? screenWidth * 2 : -screenWidth * 2;
        float targetRotation = isLike ? 30f : -30f;

        songCard.animate()
                .translationX(targetX)
                .rotation(targetRotation)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (isLike && currentSong != null) {
                        if (!likedSongsList.contains(currentSong)) {
                            likedSongsList.add(currentSong);
//                            Toast.makeText(this,
//                                    "Added: " + currentSong.getTrackName() +
//                                            "\nTotal liked: " + likedSongsList.size(),
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    loadNextSong(isLike);
                })
                .start();
    }

    private void animateSwipe(boolean isLike) {
        setLoadingState(true);

        float screenWidth = songCard.getWidth();
        float targetX = isLike ? screenWidth * 2 : -screenWidth * 2;
        float targetRotation = isLike ? 30f : -30f;

        // Small button press feedback
        songCard.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
            songCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(targetX)
                    .rotation(targetRotation)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (isLike && currentSong != null) {
                            if (!likedSongsList.contains(currentSong)) {
                                likedSongsList.add(currentSong);
//                                Toast.makeText(this,
//                                        "Added: " + currentSong.getTrackName() +
//                                                "\nTotal liked: " + likedSongsList.size(),
//                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        loadNextSong(isLike);
                    })
                    .start();
        }).start();
    }

    private void loadInitialData() {
        setLoadingState(true);
        backgroundExecutor.execute(() -> {
            allSongsCache = db.songDao().getAllSongs();

            if (allSongsCache == null || allSongsCache.isEmpty()) {
                mainThreadHandler.post(() -> {
                    Toast.makeText(this, "Error: No songs found in database.", Toast.LENGTH_LONG).show();
                    finish();
                });
                return;
            }

            activeSeedSong = allSongsCache.get(random.nextInt(allSongsCache.size()));
            currentSong = activeSeedSong;
            shownSongUris.add(currentSong.getTrackUri());

            mainThreadHandler.post(() -> {
                updateCardUI(currentSong);
                songCard.setTranslationX(0f);
                songCard.setRotation(0f);
                songCard.setAlpha(1f);
                setLoadingState(false);
            });
        });
    }

    private void loadNextSong(boolean useLikedSongAsNewSeed) {
        setLoadingState(true);
        backgroundExecutor.execute(() -> {
            Song seed;
            if (useLikedSongAsNewSeed && currentSong != null) {
                activeSeedSong = currentSong;
                seed = activeSeedSong;
            } else if (activeSeedSong != null) {
                seed = activeSeedSong;
            } else {
                mainThreadHandler.post(() -> {
                    Toast.makeText(this, "Error finding next song.", Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                });
                return;
            }

            List<Song> recommendations = SimilarityCalculator.findSimilarSongs(seed, allSongsCache, 50);
            Song nextSong = null;
            for (Song s : recommendations) {
                if (!shownSongUris.contains(s.getTrackUri())) {
                    nextSong = s;
                    break;
                }
            }

            if (nextSong != null) {
                currentSong = nextSong;
                shownSongUris.add(currentSong.getTrackUri());
                mainThreadHandler.post(() -> {
                    updateCardUI(currentSong);
                    setLoadingState(false);
                });
            } else {
                mainThreadHandler.post(() -> {
                    Toast.makeText(this, "No more songs for this vibe!", Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                });
            }
        });
    }

    private void updateCardUI(Song song) {
        songCard.setTranslationX(0f);
        songCard.setRotation(0f);
        songCard.setAlpha(1f);

        if (song == null) {
            trackNameTextView.setText("All songs seen");
            artistNameTextView.setText("Try a new seed!");
            return;
        }

        trackNameTextView.setText(song.getTrackName());
        artistNameTextView.setText(song.getArtistNames());
        albumArtImageView.setImageResource(R.drawable.ic_launcher_background);

        backgroundExecutor.execute(() -> {
            String trackId = null;
            if (song.getTrackUri() != null && song.getTrackUri().contains("spotify:track:")) {
                trackId = song.getTrackUri().split("spotify:track:")[1];
            }
            if (trackId == null) return;

            PlaylistItemsDto.Track trackDetails = playlistRepository.getTrackDetails(this, trackId);

            mainThreadHandler.post(() -> {
                if (currentSong != null && currentSong.getTrackUri().equals(song.getTrackUri())) {
                    String imageUrl = null;
                    if (trackDetails != null && trackDetails.album != null &&
                            trackDetails.album.getImages() != null &&
                            !trackDetails.album.getImages().isEmpty()) {
                        imageUrl = trackDetails.album.getImages().get(0).getUrl();
                    }

                    if (imageUrl != null) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(albumArtImageView);
                    } else {
                        albumArtImageView.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            });
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (this.isLoading && isLoading) return;
        this.isLoading = isLoading;
        btnCreatePlaylist.setEnabled(!isLoading);
        btnChooseNewSeed.setEnabled(!isLoading);
        btnLike.setEnabled(!isLoading);
        btnDislike.setEnabled(!isLoading);
        songCard.setAlpha(isLoading ? 0.5f : 1f);
    }

    private void showNewSeedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a new seed song");
        builder.setItems(new CharSequence[]{"Go Random", "Search for a song"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    likedSongsList.clear();
                    shownSongUris.clear();
                    loadInitialData();
                    break;
                case 1:
                    Intent intent = new Intent(TuneMatchActivity.this, SearchActivity.class);
                    intent.putExtra("SEARCH_MODE", "PICK_SEED");
                    startActivityForResult(intent, SEARCH_SONG_REQUEST);
                    break;
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_SONG_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedSong")) {
                Song selectedSeed = (Song) data.getSerializableExtra("selectedSong");

                if (selectedSeed != null) {
                    Toast.makeText(this, "New seed: " + selectedSeed.getTrackName(), Toast.LENGTH_SHORT).show();
                    likedSongsList.clear();
                    shownSongUris.clear();
                    setLoadingState(true);
                    activeSeedSong = selectedSeed;
                    currentSong = selectedSeed;
                    shownSongUris.add(currentSong.getTrackUri());
                    mainThreadHandler.post(() -> {
                        updateCardUI(currentSong);
                        setLoadingState(false);
                    });
                }
            }
        }
    }
}
