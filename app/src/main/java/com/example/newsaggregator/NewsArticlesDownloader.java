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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewsArticlesDownloader implements Runnable {
    private static MainActivity mainActivity;
    private static String sourceID;
    private static RequestQueue queue;
    private static ArrayList<NewsArticle> articleList = new ArrayList<>();
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines";

    private static final String yourAPIKey = "b8cbbba40bcf4a9496e764723f7d9bfb";


    public NewsArticlesDownloader(MainActivity mainActivity, String sourceID) {
        this.mainActivity = mainActivity;
        this.sourceID = sourceID;
    }

    @Override
    public void run() {
        queue = Volley.newRequestQueue(mainActivity);
        Uri.Builder buildURL = Uri.parse(BASE_URL).buildUpon();
        buildURL.appendQueryParameter("sources", sourceID);
        buildURL.appendQueryParameter("apiKey", yourAPIKey);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            articleList.clear();
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
        Log.d(TAG, "handleResults: ");
        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        final ArrayList<NewsArticle> articleList = parseJSON(s);
        if (articleList == null) {
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        mainActivity.runOnUiThread(
                () -> mainActivity.updateArticles(articleList));
    }

    private ArrayList<NewsArticle> parseJSON(JSONObject s) {
        try {
            JSONArray articles = s.getJSONArray("articles");

            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                String headline = "";
                String date = "";
                String author = "";
                String image = "";
                String text = "";
                String url = "";
                if (article.has("title") && !article.getString("title").equals("null")) headline = article.getString("title");
                if (article.has("publishedAt") && !article.getString("publishedAt").equals("null")) {
                    SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    SimpleDateFormat dateFormat = null;
                    String dateString = article.getString("publishedAt");
                    if (article.getString("publishedAt").contains(".")) {
                        int dotPos = article.getString("publishedAt").indexOf(".");
                        int zPos = article.getString("publishedAt").indexOf("Z");
                        StringBuilder milliseconds = new StringBuilder();
                        for (int j = dotPos+1; j < zPos; j++) {
                            milliseconds.append("S");
                            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss." + milliseconds + "'Z'", Locale.US);
                        }
                    } else if (article.getString("publishedAt").contains("+00:00")) {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US);
                    } else {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    }
//                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date myDate = dateFormat.parse(dateString);
                    date = newFormat.format(myDate);
                }
                if (article.has("author") && !article.getString("author").equals("null")) author = article.getString("author");
                if (article.has("url") && !article.getString("url").equals("null")) url = article.getString("url");
                if (article.has("urlToImage") && !article.getString("urlToImage").equals("null")) image = article.getString("urlToImage");
                if (article.has("description") && !article.getString("description").equals("null")) text = article.getString("description");

                articleList.add(new NewsArticle(headline, date, author, url, image, text));
            }

            return articleList;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
