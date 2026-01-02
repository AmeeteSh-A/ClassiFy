package com.example.spotifymood.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("tracks")
    public TracksRef tracks;

    @SerializedName("images")
    public List<ImageDto> images;

    public List<ImageDto> getImages() {
        return images != null ? images : new java.util.ArrayList<>();
    }

    public static class TracksRef {
        @SerializedName("href")
        public String href;

        @SerializedName("total")
        public int total;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public TracksRef getTracks() {
        return tracks;
    }
}
