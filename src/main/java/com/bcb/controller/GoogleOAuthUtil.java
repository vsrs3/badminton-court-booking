/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bcb.controller;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author Nguyen Minh Duc
 */
public class GoogleOAuthUtil {

    private static final String CLIENT_ID = "204559903564-apf9kc8g9k6e5lfgn00r7fr9gpq4ptgp.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/bcb/google-callback";
                                    

    public static String getAccessToken(String code) throws IOException {
        String params =
            "code=" + URLEncoder.encode(code, "UTF-8") +
            "&client_id=" + CLIENT_ID +
            "&client_secret=" + CLIENT_SECRET +
            "&redirect_uri=" + REDIRECT_URI +
            "&grant_type=authorization_code";

        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(params.getBytes());

        JsonObject json =
            JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
                      .getAsJsonObject();

        return json.get("access_token").getAsString();
    }

    public static JsonObject getUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://openidconnect.googleapis.com/v1/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        return JsonParser.parseReader(
                new InputStreamReader(conn.getInputStream())
        ).getAsJsonObject();
    }
}
