package com.example.spotifymood.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SpotifyAccountsApi {
    @FormUrlEncoded
    @POST("api/token") // jo url ham log build kiye the usme /api/token pe post hoga
    Call<TokenResponse> exchangeCode(
            @Field("client_id") String clientId,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri,
            @Field("code_verifier") String codeVerifier
    );

    @FormUrlEncoded
    @POST("api/token")
    Call<TokenResponse> refreshToken(
            @Field("client_id") String clientId,
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken
    );
}
