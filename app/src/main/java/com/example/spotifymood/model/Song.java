package com.example.spotifymood.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "songs")
public class Song implements java.io.Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "track_uri")
    private String trackUri;

    private String trackName;
    private String artistNames;
    private String imageUrl;
    private float danceability;
    private float energy;
    private float valence;
    private float acousticness;
    private float instrumentalness;
    private float speechiness;
    private float liveness;
    private float tempo;
    private float loudness;
    private int key;
    private int mode;


    public Song() {

    }



    @NonNull
    public String getTrackUri() {
        return trackUri;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtistNames() {
        return artistNames;
    }

    public String getImageUrl() { // <-- NEW GETTER
        return imageUrl;
    }

    public float getDanceability() {
        return danceability;
    }

    public float getEnergy() {
        return energy;
    }

    public float getValence() {
        return valence;
    }

    public float getAcousticness() {
        return acousticness;
    }

    public float getInstrumentalness() {
        return instrumentalness;
    }

    public float getSpeechiness() {
        return speechiness;
    }

    public float getLiveness() {
        return liveness;
    }

    public float getTempo() {
        return tempo;
    }

    public float getLoudness() {
        return loudness;
    }

    public int getKey() {
        return key;
    }

    public int getMode() {
        return mode;
    }




    public void setTrackUri(@NonNull String trackUri) {
        this.trackUri = trackUri;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setArtistNames(String artistNames) {
        this.artistNames = artistNames;
    }

    public void setImageUrl(String imageUrl) { // <-- NEW SETTER
        this.imageUrl = imageUrl;
    }

    public void setDanceability(float danceability) {
        this.danceability = danceability;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public void setValence(float valence) {
        this.valence = valence;
    }

    public void setAcousticness(float acousticness) {
        this.acousticness = acousticness;
    }

    public void setInstrumentalness(float instrumentalness) {
        this.instrumentalness = instrumentalness;
    }

    public void setSpeechiness(float speechiness) {
        this.speechiness = speechiness;
    }

    public void setLiveness(float liveness) {
        this.liveness = liveness;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public void setLoudness(float loudness) {
        this.loudness = loudness;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double[] getFeatureVector() {
        return new double[]{
                this.danceability,
                this.energy,
                this.valence,
                this.acousticness,
                this.instrumentalness,
                this.speechiness,
                this.liveness,
        };
    }
}

