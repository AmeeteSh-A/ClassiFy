package com.example.spotifymood.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.spotifymood.core.Config;

public class SpotifyAuthManager {
    private String currentCodeVerifier;

    public void startLogin(Context context) {
        // 1. PKCE
        currentCodeVerifier = PkceUtil.generateCodeVerifier();
        String codeChallenge = PkceUtil.generateCodeChallenge(currentCodeVerifier);

        // Persist verifier for the callback step (SharedPrefs simplest)
        AuthStore.saveCodeVerifier(context, currentCodeVerifier);

        // 2. Build auth URL
        Uri uri = Uri.parse("https://accounts.spotify.com/authorize")
                .buildUpon() //to add the uri
                .appendQueryParameter("client_id", Config.CLIENT_ID)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", Config.REDIRECT_URI)
                .appendQueryParameter("scope", Config.SCOPES)
                .appendQueryParameter("code_challenge_method", "S256")
                .appendQueryParameter("code_challenge", codeChallenge)
                .build();

        // 3. Open in Custom Tab (preferred) or fallback to ACTION_VIEW
        CustomTabsIntent intent = new CustomTabsIntent.Builder().build(); //opens  a custom chrome tab within the browser instead of a browser intent
        intent.launchUrl(context, uri);
    }
}
