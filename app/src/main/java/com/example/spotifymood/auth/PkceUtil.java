package com.example.spotifymood.auth;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class PkceUtil {
    private static final String PKCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final SecureRandom RAND = new SecureRandom();
    // secureRandom = java class to generate very strong random numbers.
    /*
    * static mtlb sab koi bhi dekh sakta hai (within the same class), i.e. shared class-wide, no instance needed. i.e. it aint tied to an object
    * final mtlb change nhi hoga
    * private kyuki bhai isi me use krna hai bas, yani can be used only in PKCEutil
    * */

    private PkceUtil(){}
    /*mtlb ki koi bhi isko use nhi kar payega like it cant be new pkceUtil or some
    * safe otherwise too but still a general good practice to do kyuki if you dont dp it in the ode it wont happen otherwise regardless*/

    // length between 43 and 128; Spotify example uses 64
    public static String generateCodeVerifier() {
        int len = 64;
        StringBuilder sb = new StringBuilder(len); // why sb? becos strings are immutable so if i do str = str+ "a", im making a new string. so its slow and wastes memory
        for (int i=0;i<len;i++) {
            int idx = RAND.nextInt(PKCE_CHARS.length());
            sb.append(PKCE_CHARS.charAt(idx));
        }
        return sb.toString(); // now this runs after the loop so the random 64 length-ed thing is returned as a string. ab isko use karna hai then we can do something like String verifier = PkceUtil.generateCodeVerifier(); in some other file to access this random codeverifier
    }

    public static String generateCodeChallenge(String codeVerifier) {
        /*
        * public: Anyone (from any other class) can call this method.
        * static: You can call it without creating an object → PkceUtil.generateCodeChallenge(...)
        * */
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // this is a built in java class jo bhai hashing me help krta hai, s256 ki jagah it can be sha-1 or 512 or md5 (case insensitive btw)
            byte[] digest = md.digest(codeVerifier.getBytes() /*this andar wala part converts the string (verifier) to bytes since hashing requires bytes*/);  //md.digest does the hashing
            // Base64 URL-safe, no padding, no wraps
            return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING); //this is a good practice too but here ye spotify docs me mentuoned hai
            /*
            * Base64.URL_SAFE → replaces + with - and / with _ so it works in URLs.
            * Base64.NO_WRAP → ensures the output is a single-line (no \n).
            * Base64.NO_PADDING → removes the = at the end, since Spotify requires it.
            * */
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e); //in case kisi phone me kaam na kare to crash ho jayega but with a reason
        }
    }
}
