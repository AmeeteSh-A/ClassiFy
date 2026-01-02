package com.example.spotifymood.adapter;

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
import com.example.spotifymood.model.TrackItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Added for filtering

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(TrackItem trackItem);
    }

    private final List<TrackItem> trackList;
    private final List<TrackItem> trackListFull;
    private OnTrackClickListener onTrackClickListener;

    public TrackAdapter(List<TrackItem> initialList) {

        this.trackList = (initialList != null) ? new ArrayList<>(initialList) : new ArrayList<>();
        this.trackListFull = (initialList != null) ? new ArrayList<>(initialList) : new ArrayList<>();
    }


    public List<TrackItem> getTracks() {
        return trackListFull; // Return the full list for recommendation logic
    }

    public void filter(String query) {
        trackList.clear();
        if (query.isEmpty()) {

            trackList.addAll(trackListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();

            List<TrackItem> filteredList = trackListFull.stream()
                    .filter(track ->

                            (track.getName() != null && track.getName().toLowerCase().contains(lowerCaseQuery)) ||
                                    (track.getArtistString() != null && track.getArtistString().toLowerCase().contains(lowerCaseQuery))
                    )
                    .collect(Collectors.toList());
            trackList.addAll(filteredList);
        }

        notifyDataSetChanged();
    }




    public void setOnTrackClickListener(OnTrackClickListener listener) {
        this.onTrackClickListener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);

        return new TrackViewHolder(view, onTrackClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        TrackItem track = trackList.get(position);
        Log.d("TrackAdapter", "Binding: " + track.getName());
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }


    public void addTracks(List<TrackItem> newTracks) {
        if (newTracks != null && !newTracks.isEmpty()) {

            trackListFull.addAll(newTracks);

            if (trackList.size() == (trackListFull.size() - newTracks.size())) {
                int currentSize = trackList.size();
                trackList.addAll(newTracks);
                notifyItemRangeInserted(currentSize, newTracks.size());
            }
        }
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackName, artistName, albumName;
        private final ImageView trackImage;

        public TrackViewHolder(@NonNull View itemView, final OnTrackClickListener listener) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            artistName = itemView.findViewById(R.id.artistName);
            albumName = itemView.findViewById(R.id.albumName);
            trackImage = itemView.findViewById(R.id.trackImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Pass the clicked track back to the activity
                        listener.onTrackClick(trackList.get(position));
                    }
                }
            });
        }

        public void bind(TrackItem track) {
            trackName.setText(track.getName());
            artistName.setText(track.getArtistString());
            albumName.setText(track.getAlbumName());

            String imageUrl = track.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(trackImage);
            } else {
                trackImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}

