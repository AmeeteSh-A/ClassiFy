package com.example.spotifymood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spotifymood.R;
import com.example.spotifymood.model.Song;
import java.util.ArrayList;
import java.util.List;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Song> songList = new ArrayList<>();
    private final OnSongSelectedListener listener;


    public interface OnSongSelectedListener {
        void onSongSelected(Song song);
    }

    public SearchAdapter(OnSongSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We re-use item_track.xml. Make sure all IDs match.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.bind(songList.get(position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }


    public void setSongs(List<Song> newSongs) {
        this.songList.clear();
        if (newSongs != null) {
            this.songList.addAll(newSongs);
        }
        notifyDataSetChanged();
    }


    class SearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackName;
        private final TextView artistName;
        private final TextView albumName;


        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackName);
            artistName = itemView.findViewById(R.id.artistName);
            albumName = itemView.findViewById(R.id.albumName);

            if (albumName != null) {
                albumName.setVisibility(View.GONE);
            }
            View trackImage = itemView.findViewById(R.id.trackImage);
            if (trackImage != null) {
                trackImage.setVisibility(View.GONE);
            }
        }

        public void bind(final Song song) {
            trackName.setText(song.getTrackName());
            artistName.setText(song.getArtistNames());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongSelected(song);
                }
            });
        }
    }
}

