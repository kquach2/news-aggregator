package com.example.newsaggregator;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SourcesDownloader implements Runnable {
    private static MainActivity mainActivity;
    private static RequestQueue queue;
    private static ArrayList<Source> sourceList = new ArrayList<>();
    private static final String BASE_URL = "https://newsapi.org/v2/sources?apiKey=b8cbbba40bcf4a9496e764723f7d9bfb";

    public SourcesDownloader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: ");
        queue = Volley.newRequestQueue(mainActivity);
        Uri dataUri = Uri.parse(BASE_URL);
        String urlToUse = dataUri.toString();

        Response.Listener<JSONObject> listener = response -> {
            sourceList.clear();
            handleResults(mainActivity, response);
        };
        Response.ErrorListener error = error1 -> handleResults(mainActivity, null);

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("User-Agent", "News-App");
                        return headers;
                    }
                };
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private void handleResults(MainActivity mainActivity, JSONObject s) {
        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        final ArrayList<Source> sourceList = parseJSON(s);
        if (sourceList == null) {
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        mainActivity.runOnUiThread(
                () -> mainActivity.updateData(sourceList));
    }

    private ArrayList<Source> parseJSON(JSONObject s) {
        try {
            JSONArray sources = s.getJSONArray("sources");

            for (int i = 0; i < sources.length(); i++) {
                String id = sources.getJSONObject(i).getString("id");
                String name = sources.getJSONObject(i).getString("name");
                String topic = sources.getJSONObject(i).getString("category");
                sourceList.add(new Source(id, name, topic));
            }

            return sourceList;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
