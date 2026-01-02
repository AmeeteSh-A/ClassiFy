package com.example.spotifymood.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserPlaylistsDto {
    @SerializedName("items")
    public List<PlaylistDto> items;

    @SerializedName("total")
    public int total;

    @SerializedName("next")
    public String next;

    public List<PlaylistDto> getItems() {
        return items != null ? items : new java.util.ArrayList<>();
    }
}
