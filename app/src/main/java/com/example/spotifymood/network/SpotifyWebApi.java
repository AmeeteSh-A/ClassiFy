package com.example.spotifymood.network;

import java.util.Map; // Needed for POST request body

import retrofit2.Call;
import retrofit2.http.Body; // Needed for POST request body
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST; // Needed for POST requests
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyWebApi {
    @GET("playlists/{playlist_id}")
    Call<PlaylistDto> getPlaylist(
            @Header("Authorization") String bearerToken,
            @Path("playlist_id") String playlistId
    );

    @GET("playlists/{playlist_id}/tracks")
    Call<PlaylistItemsDto> getPlaylistItems(
            @Header("Authorization") String bearerToken,
            @Path("playlist_id") String playlistId,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset,
            @Query("fields") String fields
    );

    @GET("me/playlists")
    Call<UserPlaylistsDto> getCurrentUserPlaylists(
            @Header("Authorization") String accessToken
    );


    @GET("me/tracks")
    Call<PlaylistItemsDto> getLikedSongs(
            @Header("Authorization") String auth,
            @Query("limit") int limit,
            @Query("offset") int offset
    );


    @GET("tracks/{id}")
    Call<PlaylistItemsDto.Track> getTrack(
            @Header("Authorization") String auth,
            @Path("id") String trackId
    );

    @GET("me")
    Call<UserProfileDto> getCurrentUserProfile(
            @Header("Authorization") String auth
    );


    @POST("users/{user_id}/playlists")
    Call<PlaylistDto> createPlaylist(
            @Header("Authorization") String auth,
            @Path("user_id") String userId,
            @Body Map<String, Object> body
    );


    @POST("playlists/{playlist_id}/tracks")
    Call<Void> addTracksToPlaylist(
            @Header("Authorization") String auth,
            @Path("playlist_id") String playlistId,
            @Body Map<String, Object> body
    );


}

