package com.example.spotifymood.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SpotifyRedirectActivity extends Activity {
    private static final String TAG = "SpotifyRedirectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        Log.d(TAG, "onCreate: redirect uri = " + uri);

        String code = null;
        String error = null;

        if (uri != null && "spotifymood".equals(uri.getScheme()) && "callback".equals(uri.getHost())) {
            code = uri.getQueryParameter("code");
            error = uri.getQueryParameter("error");
            Log.d(TAG, "Parsed code=" + code + " error=" + error);
        } else {
            Log.w(TAG, "Redirect URI did not match expected scheme/host.");
        }

        if (code != null) {
            Toast.makeText(this, "Auth code received", Toast.LENGTH_SHORT).show();
        } else if (error != null) {
            Toast.makeText(this, "Auth error: " + error, Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (code != null) intent.putExtra("code", code);
        if (error != null) intent.putExtra("error", error);
        startActivity(intent);

        finish();
    }
}
