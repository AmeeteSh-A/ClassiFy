package com.example.spotifymood.network;

import com.google.gson.annotations.SerializedName;

public class ImageDto {
    @SerializedName("url")
    public String url;

    @SerializedName("height")
    public int height;

    @SerializedName("width")
    public int width;
}
