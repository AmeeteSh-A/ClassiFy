package com.example.spotifymood.repository;

import android.content.Context;
import android.util.Log;

import com.example.spotifymood.auth.AuthStore;
import com.example.spotifymood.network.ImageDto;
import com.example.spotifymood.network.PlaylistDto;
import com.example.spotifymood.network.PlaylistItemsDto;
import com.example.spotifymood.network.SpotifyWebApi;
import com.example.spotifymood.network.SpotifyWebServiceFactory;
import com.example.spotifymood.network.TrackDto;
import com.example.spotifymood.network.UserProfileDto;
import com.example.spotifymood.network.UserPlaylistsDto;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistRepository {
    private final SpotifyWebApi webApi = SpotifyWebServiceFactory.get();
    private final AuthRepository authRepo = new AuthRepository();
    private static final String TAG = "PlaylistRepository";

    private String bearer(Context ctx) {
        String token = authRepo.getValidAccessToken(ctx);
        return token == null ? null : "Bearer " + token;
    }


    public PlaylistDto getPlaylist(Context ctx, String playlistId) throws IOException {
        String auth = bearer(ctx);
        if (auth == null) return null;
        Response<PlaylistDto> resp = webApi.getPlaylist(auth, playlistId).execute();
        return resp.isSuccessful() ? resp.body() : null;
    }

    public PlaylistItemsDto getPlaylistItems(Context ctx, String playlistId, int limit, int offset) {
        String auth = bearer(ctx);
        if (auth == null) {
            Log.e(TAG, "No auth token!");
            return null;
        }
        try {
            Response<PlaylistItemsDto> resp = webApi
                    .getPlaylistItems(auth, playlistId, limit, offset, null)
                    .execute();

            if (!resp.isSuccessful()) {
                String err = resp.errorBody() != null ? resp.errorBody().string() : "no error body";
                Log.e(TAG, "Tracks HTTP " + resp.code() + ": " + err);
                return null;
            }

            PlaylistItemsDto dto = resp.body();
            Log.d(TAG,
                    "Fetched tracks page: total=" + (dto!=null?dto.total:"null")
                            + ", items.size=" + (dto!=null && dto.items!=null?dto.items.size():"null"));
            return dto;

        } catch (IOException ioe) {
            Log.e(TAG, "IO error fetching tracks", ioe);
            return null;
        }
    }

    public void fetchUserPlaylists(Context context, Callback<UserPlaylistsDto> callback) {
        String token = "Bearer " + AuthStore.getAccessToken(context);
        webApi.getCurrentUserPlaylists(token).enqueue(callback);
    }

    public UserPlaylistsDto getUserPlaylists(Context ctx) {
        String auth = bearer(ctx);
        if (auth == null) {
            Log.e(TAG, "No valid access token; cannot fetch playlists");
            return null;
        }
        try {
            Response<UserPlaylistsDto> response =
                    webApi.getCurrentUserPlaylists(auth).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                Log.e(TAG, "Failed to fetch playlists: " + (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while fetching playlists", e);
        }
        return null;
    }

    public PlaylistDto getLikedSongsAsPlaylist(Context ctx) {
        String auth = bearer(ctx);
        if (auth == null) {
            Log.e(TAG, "No auth token for liked songs!");
            return null;
        }
        try {
            Response<PlaylistItemsDto> response = webApi.getLikedSongs(auth, 1, 0).execute();

            if (response.isSuccessful() && response.body() != null) {
                int totalLikedSongs = response.body().total;
                PlaylistDto likedSongsPlaylist = new PlaylistDto();
                likedSongsPlaylist.id = "liked_songs";
                likedSongsPlaylist.name = "Liked Songs";
                PlaylistDto.TracksRef tracks = new PlaylistDto.TracksRef();
                tracks.total = totalLikedSongs;
                likedSongsPlaylist.tracks = tracks;
                ImageDto image = new ImageDto();
                image.url = "https://t.scdn.co/images/3099b3803ad9496896c43f2212be1f27.png";
                likedSongsPlaylist.images = Collections.singletonList(image);
                return likedSongsPlaylist;
            } else {
                Log.e(TAG, "Failed to fetch liked songs count: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error fetching liked songs", e);
        }
        return null;
    }

    public PlaylistItemsDto getLikedSongs(Context ctx, int limit, int offset) {
        String auth = bearer(ctx);
        if (auth == null) {
            Log.e(TAG, "No auth token!");
            return null;
        }
        try {
            Response<PlaylistItemsDto> resp = webApi.getLikedSongs(auth, limit, offset).execute();

            if (!resp.isSuccessful()) {
                String err = resp.errorBody() != null ? resp.errorBody().string() : "no error body";
                Log.e(TAG, "Liked Songs HTTP " + resp.code() + ": " + err);
                return null;
            }
            return resp.body();
        } catch (IOException ioe) {
            Log.e(TAG, "IO error fetching liked songs", ioe);
            return null;
        }
    }

    public PlaylistItemsDto.Track getTrackDetails(Context ctx, String trackId) {
        String auth = bearer(ctx);
        if (auth == null) {
            Log.e(TAG, "No auth token for getTrackDetails!");
            return null;
        }
        try {
            Response<PlaylistItemsDto.Track> resp = webApi.getTrack(auth, trackId).execute();
            if (resp.isSuccessful()) {
                return resp.body();
            } else {
                String err = resp.errorBody() != null ? resp.errorBody().string() : "no error body";
                Log.e(TAG, "getTrackDetails HTTP " + resp.code() + ": " + err);
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error fetching track details", e);
            return null;
        }
    }


    private String getCurrentUserId(Context ctx) {
        String auth = bearer(ctx);
        if (auth == null) return null;
        try {
            Response<UserProfileDto> resp = webApi.getCurrentUserProfile(auth).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                return resp.body().id;
            } else {
                Log.e(TAG, "Failed to get user profile: " + (resp.errorBody() != null ? resp.errorBody().string() : resp.message()));
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error getting user profile", e);
        }
        return null;
    }


    public PlaylistDto createSpotifyPlaylist(Context ctx, String name, String description) {
        String auth = bearer(ctx);
        if (auth == null) return null;

        String userId = getCurrentUserId(ctx);
        if (userId == null) return null;

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        if (description != null && !description.isEmpty()) {
            body.put("description", description);
        }
        body.put("public", false); // Default to private

        try {
            Response<PlaylistDto> resp = webApi.createPlaylist(auth, userId, body).execute();
            if (resp.isSuccessful()) {
                Log.d(TAG, "Successfully created playlist: " + name);
                return resp.body();
            } else {
                Log.e(TAG, "Failed to create playlist: " + (resp.errorBody() != null ? resp.errorBody().string() : resp.message()));
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error creating playlist", e);
        }
        return null;
    }

    public boolean addTracksToSpotifyPlaylist(Context ctx, String playlistId, List<String> trackUris) {
        String auth = bearer(ctx);
        if (auth == null || trackUris == null || trackUris.isEmpty()) return false;

        Map<String, Object> body = new HashMap<>();
        body.put("uris", trackUris);

        try {
            Response<Void> resp = webApi.addTracksToPlaylist(auth, playlistId, body).execute();
            if (resp.isSuccessful()) {
                Log.d(TAG, "Successfully added " + trackUris.size() + " tracks to playlist: " + playlistId);
                return true;
            } else {
                Log.e(TAG, "Failed to add tracks to playlist: " + (resp.errorBody() != null ? resp.errorBody().string() : resp.message()));
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error adding tracks to playlist", e);
        }
        return false;
    }


//    public RecentlyPlayedDto getRecentlyPlayedTracks(Context ctx, long afterTimestamp) {
//        String auth = bearer(ctx);
//        if (auth == null) {
//            Log.e(TAG, "No auth token for getRecentlyPlayedTracks!");
//            return null;
//        }
//
//        try {
//            Response<RecentlyPlayedDto> resp = webApi.getRecentlyPlayed(auth, 50, afterTimestamp).execute();
//            if (resp.isSuccessful()) {
//                return resp.body();
//            } else {
//                String err = resp.errorBody() != null ? resp.errorBody().string() : "no error body";
//                Log.e(TAG, "getRecentlyPlayedTracks HTTP " + resp.code() + ": " + err);
//                return null;
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "IO error fetching recently played tracks", e);
//            return null;
//        }
//    }
}

