package com.example.spotifymood.network;

import com.example.spotifymood.model.TrackItem;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistItemsDto {

    @SerializedName("items")
    public List<Item> items;

    @SerializedName("total")
    public int total;


    public List<TrackItem> getTrackItems() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .filter(item -> item.track != null && item.track.id != null && item.track.uri != null)
                .map(item -> new TrackItem(
                        item.track.id,
                        item.track.name,
                        item.track.uri,
                        item.track.artists,
                        item.track.album
                ))
                .collect(Collectors.toList());
    }


    public static class Item {
        @SerializedName("track")
        public Track track;
    }

    public static class Track {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("uri")
        public String uri;

        @SerializedName("artists")
        public List<TrackItem.Artist> artists;

        @SerializedName("album")
        public TrackItem.Album album;
    }
}

