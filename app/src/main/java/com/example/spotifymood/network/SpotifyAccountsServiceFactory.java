package com.example.spotifymood.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class SpotifyAccountsServiceFactory {
    private static SpotifyAccountsApi instance;

    public static SpotifyAccountsApi get() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl("https://accounts.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SpotifyAccountsApi.class);
        }
        return instance;
    }
}
