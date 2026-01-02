package com.example.spotifymood.network;

import java.util.List;

public class TrackDto {
    public List<Item> items;

    public static class Item {
        public Track track;

        public static class Track {
            public String name;
            public List<Artist> artists;

            public static class Artist {
                public String name;
            }
        }
    }
}
