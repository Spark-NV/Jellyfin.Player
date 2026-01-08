package com.brouken.player;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JellyfinApiService {
    private static final String TAG = "JellyfinApiService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String baseUrl;
    private final String userToken;
    private final String userId;

    public JellyfinApiService(String baseUrl, String userToken, String userId) {
        this.baseUrl = baseUrl.replaceAll("/$", ""); // Remove trailing slash
        this.userToken = userToken;
        this.userId = userId;

        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder createAuthenticatedRequest(String endpoint) {
        return new Request.Builder()
                .url(baseUrl + endpoint)
                .addHeader("Authorization", "MediaBrowser Token=\"" + userToken + "\"")
                .addHeader("X-Emby-Authorization", "MediaBrowser Client=\"JustPlayer\", Device=\"Android\", DeviceId=\"\", Version=\"1.0.0\", Token=\"" + userToken + "\"")
                .addHeader("Content-Type", "application/json");
    }

    public void reportPlaybackStart(String itemId, long positionTicks, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("ItemId", itemId);
            json.put("PositionTicks", positionTicks);
            json.put("IsPaused", false);
            json.put("IsMuted", false);
            json.put("VolumeLevel", 100);
            json.put("PlayMethod", "DirectStream");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating playback start JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = createAuthenticatedRequest("/Sessions/Playing")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void reportPlaybackProgress(String itemId, long positionTicks, boolean isPaused, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("ItemId", itemId);
            json.put("PositionTicks", positionTicks);
            json.put("IsPaused", isPaused);
            json.put("IsMuted", false);
            json.put("VolumeLevel", 100);
            json.put("PlayMethod", "DirectStream");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating playback progress JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = createAuthenticatedRequest("/Sessions/Playing/Progress")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void reportPlaybackStop(String itemId, long positionTicks, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("ItemId", itemId);
            json.put("PositionTicks", positionTicks);
            json.put("PlayMethod", "DirectStream");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating playback stop JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = createAuthenticatedRequest("/Sessions/Playing/Stopped")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void markItemPlayed(String itemId, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("Played", true);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating mark played JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = createAuthenticatedRequest("/Users/" + userId + "/PlayedItems/" + itemId)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
