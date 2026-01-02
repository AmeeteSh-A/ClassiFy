package com.example.spotifymood.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthStore {
    private static final String PREF = "spotify_auth";
    private static final String KEY_VERIFIER = "code_verifier";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_EXPIRY = "expires_at_ms";

    static void saveCodeVerifier(Context ctx, String v) {
        prefs(ctx).edit().putString(KEY_VERIFIER, v).apply();
    }
    public static String getCodeVerifier(Context ctx) {
        return prefs(ctx).getString(KEY_VERIFIER, null);
    }

    public static void saveTokens(Context ctx, String access, String refresh, long expiresAtMs) {
        prefs(ctx).edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .putLong(KEY_EXPIRY, expiresAtMs)
                .apply();
    }

    public static String getAccessToken(Context ctx) {
        return prefs(ctx).getString(KEY_ACCESS, null);
    }
    public static String getRefreshToken(Context ctx) {
        return prefs(ctx).getString(KEY_REFRESH, null);
    }
    public static long getExpiry(Context ctx) {
        return prefs(ctx).getLong(KEY_EXPIRY, 0L);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}
