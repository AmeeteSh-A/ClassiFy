package com.example.spotifymood.core;

public final class Config {
    private Config(){}

    public static final String CLIENT_ID = "Client ID goes here";

    public static final String REDIRECT_URI = "spotifymood://callback";

    public static final String SCOPES =
            "playlist-read-private " +
                    "playlist-read-collaborative " +
                    "user-library-read " +
                    "playlist-modify-private " +
                    "playlist-modify-public " +
                    "user-read-recently-played";
}

