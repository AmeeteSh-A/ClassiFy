package com.example.spotifymood.network;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("scope")
    public String scope;

    @SerializedName("expires_in")
    public int expiresIn;

    @SerializedName("refresh_token")
    public String refreshToken;
}
