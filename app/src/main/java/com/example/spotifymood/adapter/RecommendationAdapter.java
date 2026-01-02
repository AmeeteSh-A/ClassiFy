package com.example.spotifymood.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotifymood.R;
import com.example.spotifymood.model.Song;
import com.example.spotifymood.model.TrackItem; // Import TrackItem for nested classes
import com.example.spotifymood.network.PlaylistItemsDto;
import com.example.spotifymood.repository.PlaylistRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {

    private static final String TAG = "RecAdapter";

    private final List<Song> songList;
    private PlaylistRepository playlistRepository;
    private Context context;

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public RecommendationAdapter(List<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        // Initialize the repository
        this.playlistRepository = new PlaylistRepository();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_track, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackName, artistName, albumName;
        private final ImageView trackImage;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            artistName = itemView.findViewById(R.id.artistName);
            albumName = itemView.findViewById(R.id.albumName);
            trackImage = itemView.findViewById(R.id.trackImage);
        }

        public void bind(Song song) {

            trackName.setText(song.getTrackName());
            artistName.setText(song.getArtistNames());

            albumName.setText("Loading album...");
            trackImage.setImageResource(R.drawable.ic_launcher_background);

            backgroundExecutor.execute(() -> {
                String trackId = null;
                if (song.getTrackUri() != null && song.getTrackUri().contains("spotify:track:")) {
                    trackId = song.getTrackUri().split("spotify:track:")[1];
                }

                if (trackId == null) {
                    Log.e(TAG, "Invalid track URI, cannot fetch details: " + song.getTrackUri());
                    mainThreadHandler.post(() -> albumName.setText("Unknown Album"));
                    return;
                }

                PlaylistItemsDto.Track trackDetails = playlistRepository.getTrackDetails(context, trackId);

                mainThreadHandler.post(() -> {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION || !songList.get(getAdapterPosition()).getTrackUri().equals(song.getTrackUri())) {
                        return;
                    }

                    if (trackDetails != null && trackDetails.album != null) {
                        albumName.setText(trackDetails.album.getName());

                        String imageUrl = null;
                        List<TrackItem.Image> images = trackDetails.album.getImages();
                        if (images != null && !images.isEmpty()) {
                            imageUrl = images.get(0).getUrl();
                        }


                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(itemView.getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(trackImage);
                        } else {

                            trackImage.setImageResource(R.drawable.ic_launcher_background);
                        }
                    } else {

                        albumName.setText("Unknown Album");
                        trackImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                });
            });
        }
    }
}

