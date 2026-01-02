package com.example.spotifymood.core;

import com.example.spotifymood.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SimilarityCalculator {


    private static double cosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            return 0.0; // Or throw an exception
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            normA += Math.pow(vec1[i], 2);
            normB += Math.pow(vec2[i], 2);
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static List<Song> findSimilarSongs(Song inputSong, List<Song> allSongs, int topN) {
        if (inputSong == null || allSongs == null || allSongs.isEmpty()) {
            return Collections.emptyList();
        }

        double[] inputVector = inputSong.getFeatureVector();
        List<SongSimilarity> similarities = new ArrayList<>();

        for (Song candidateSong : allSongs) {

            if (candidateSong.getTrackUri().equals(inputSong.getTrackUri())) {
                continue;
            }

            double[] candidateVector = candidateSong.getFeatureVector();
            double similarity = cosineSimilarity(inputVector, candidateVector);
            similarities.add(new SongSimilarity(candidateSong, similarity));
        }

        Collections.sort(similarities, new Comparator<SongSimilarity>() {
            @Override
            public int compare(SongSimilarity s1, SongSimilarity s2) {
                return Double.compare(s2.getSimilarity(), s1.getSimilarity());
            }
        });

        List<Song> topSongs = new ArrayList<>();
        int count = Math.min(topN, similarities.size());
        for (int i = 0; i < count; i++) {
            topSongs.add(similarities.get(i).getSong());
        }

        return topSongs;
    }


    private static class SongSimilarity {
        private final Song song;
        private final double similarity;

        public SongSimilarity(Song song, double similarity) {
            this.song = song;
            this.similarity = similarity;
        }

        public Song getSong() {
            return song;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}

