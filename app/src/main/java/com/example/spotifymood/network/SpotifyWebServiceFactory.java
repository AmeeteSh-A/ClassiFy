package com.example.spotifymood.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class SpotifyWebServiceFactory {
    private static SpotifyWebApi instance;

    public static SpotifyWebApi get() {
        if (instance == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl("https://api.spotify.com/v1/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SpotifyWebApi.class);
        }
        return instance;
    }
}
