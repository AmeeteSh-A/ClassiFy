package com.example.spotifymood.model;

import java.util.List;
import java.util.stream.Collectors;

public class TrackItem {
    private final String id;
    private final String name;
    private final String uri;
    private final List<Artist> artists;
    private final Album album;

    public TrackItem(String id, String name, String uri, List<Artist> artists, Album album) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.artists = artists;
        this.album = album;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public String getAlbumName() {
        return album != null ? album.getName() : "Unknown Album";
    }

    public String getImageUrl() {
        if (album != null && album.getImages() != null && !album.getImages().isEmpty()) {

            return album.getImages().get(0).getUrl();
        }
        return null;
    }

    public String getArtistString() {
        if (artists == null || artists.isEmpty()) return "Unknown Artist";
        return artists.stream().map(Artist::getName).collect(Collectors.joining(", "));
    }

    public static class Artist {
        private String name;
        public String getName() { return name; }
    }

    public static class Album {
        private String name;
        private List<Image> images;
        public String getName() { return name; }
        public List<Image> getImages() { return images; }
    }

    public static class Image {
        private String url;
        public String getUrl() { return url; }
    }
}

