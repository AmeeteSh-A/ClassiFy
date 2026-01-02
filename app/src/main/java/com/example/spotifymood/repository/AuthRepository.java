package com.example.spotifymood.repository;

import android.content.Context;
import android.util.Log;

import com.example.spotifymood.auth.AuthStore;
import com.example.spotifymood.core.Config;
import com.example.spotifymood.network.SpotifyAccountsApi;
import com.example.spotifymood.network.SpotifyAccountsServiceFactory;
import com.example.spotifymood.network.TokenResponse;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepo";
    private final SpotifyAccountsApi accountsApi = SpotifyAccountsServiceFactory.get();

    public boolean exchangeCodeForToken(Context ctx, String code) {
        Log.d(TAG, "exchangeCodeForToken() called. code len=" + (code == null ? 0 : code.length()));

        String verifier = AuthStore.getCodeVerifier(ctx);
        if (verifier == null) {
            Log.e(TAG, "Missing PKCE code_verifier! Did AuthStore fail to save before login?");
            return false;
        }

        Log.d(TAG, "Using code_verifier len=" + verifier.length());

        Response<TokenResponse> resp;
        try {
            resp = accountsApi.exchangeCode(
                    Config.CLIENT_ID,
                    "authorization_code",
                    code,
                    Config.REDIRECT_URI,
                    verifier
            ).execute();
        } catch (IOException e) {
            Log.e(TAG, "Network IO error during token exchange", e);
            return false;
        }

        Log.d(TAG, "Token exchange HTTP=" + resp.code());

        if (!resp.isSuccessful()) {
            logErrorBody(TAG, resp.errorBody(), "exchange error");
            return false;
        }

        TokenResponse body = resp.body();
        if (body == null) {
            Log.e(TAG, "Token exchange success but body null.");
            return false;
        }

        Log.d(TAG, "accessToken prefix=" + safePrefix(body.accessToken));
        Log.d(TAG, "refreshToken prefix=" + safePrefix(body.refreshToken));
        Log.d(TAG, "expiresIn=" + body.expiresIn + "s  scope=" + body.scope + "  type=" + body.tokenType);

        long expiresAt = System.currentTimeMillis() + (body.expiresIn * 1000L);
        AuthStore.saveTokens(ctx, body.accessToken, body.refreshToken, expiresAt);
        Log.d(TAG, "Tokens saved. ExpiresAt(ms)=" + expiresAt);

        return true;
    }

    public String getValidAccessToken(Context ctx) {
        long now = System.currentTimeMillis();
        long exp = AuthStore.getExpiry(ctx);
        String token = AuthStore.getAccessToken(ctx);

        if (token != null) {
            long remainMs = exp - now;
            if (remainMs > 60_000) {
                Log.d(TAG, "Access token still valid (" + remainMs + "ms left).");
                return token;
            } else {
                Log.d(TAG, "Access token expiring soon (" + remainMs + "ms). Will refresh.");
            }
        } else {
            Log.d(TAG, "No cached access token. Will try refresh (if refresh token exists).");
        }

        String refresh = AuthStore.getRefreshToken(ctx);
        if (refresh == null) {
            Log.e(TAG, "No refresh token available; user must log in again.");
            return null;
        }

        Response<TokenResponse> resp;
        try {
            resp = accountsApi.refreshToken(
                    Config.CLIENT_ID,
                    "refresh_token",
                    refresh
            ).execute();
        } catch (IOException e) {
            Log.e(TAG, "Network IO error during token refresh", e);
            return null;
        }

        Log.d(TAG, "Token refresh HTTP=" + resp.code());

        if (!resp.isSuccessful()) {
            logErrorBody(TAG, resp.errorBody(), "refresh error");
            return null;
        }

        TokenResponse body = resp.body();
        if (body == null) {
            Log.e(TAG, "Refresh success but body null.");
            return null;
        }

        String newRefresh = body.refreshToken != null ? body.refreshToken : refresh;

        long expiresAt = System.currentTimeMillis() + (body.expiresIn * 1000L);
        AuthStore.saveTokens(ctx, body.accessToken, newRefresh, expiresAt);

        Log.d(TAG, "Refresh saved. New access prefix=" + safePrefix(body.accessToken) +
                " expiresIn=" + body.expiresIn + "s");

        return body.accessToken;
    }

    private static void logErrorBody(String tag, ResponseBody errorBody, String label) {
        if (errorBody == null) {
            Log.e(tag, label + ": <no error body>");
            return;
        }
        try {
            String err = errorBody.string();
            Log.e(tag, label + ": " + err);
        } catch (IOException e) {
            Log.e(tag, label + ": <error reading body>", e);
        }
    }

    private static String safePrefix(String s) {
        if (s == null) return "null";
        return s.length() <= 6 ? s : s.substring(0, 6) + "...";
    }
}
