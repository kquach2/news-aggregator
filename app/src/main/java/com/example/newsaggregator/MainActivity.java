package com.example.newsaggregator;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsaggregator.databinding.ActivityMainBinding;
import com.example.newsaggregator.databinding.DrawerItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, HashSet<String>> topicToSources = new HashMap<>();
    private HashMap<String, String> sourcesToTopics = new HashMap<>();

    private HashMap<String, String> sourceToSourceID = new HashMap<>();
    private final ArrayList<NewsArticle> currentArticleList = new ArrayList<>();
    private final ArrayList<String> sourcesDisplayed = new ArrayList<>();
    private HashMap<String, String> topicsToColors = new HashMap<>();

    private final String[] indexColors = new String[]{
            "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059",
            "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
            "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
            "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
            "#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F",
            "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
            "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
            "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",

            "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
            "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
            "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
            "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329",
            "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C",
            "#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800",
            "#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51",
            "#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58"
    };

    private Menu opt_menu;
    private ActionBarDrawerToggle mDrawerToggle;

    private NewsArticleAdapter newsArticleAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_item, sourcesDisplayed) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                DrawerItemBinding binding;
                if (convertView == null) {
                    binding = DrawerItemBinding.inflate(getLayoutInflater(), parent, false);
                    convertView = binding.getRoot();
                    convertView.setTag(R.id.viewBinding, binding);
                } else {
                    binding = ((DrawerItemBinding) convertView.getTag(R.id.viewBinding));
                }

                binding.textView.setText(getItem(position));
                binding.textView.setTextColor(Color.parseColor(topicsToColors.get(sourcesToTopics.get(getItem(position)))));

                return convertView;
            }
        };

        binding.drawerList.setAdapter(arrayAdapter);
        binding.drawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    binding.drawerLayout.closeDrawer(binding.drawerList);
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,            /* host Activity */
                binding.drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        newsArticleAdapter = new NewsArticleAdapter(this, currentArticleList);
        binding.viewpager.setAdapter(newsArticleAdapter);

        new Thread(new SourcesDownloader(this)).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectItem(int position) {

        binding.viewpager.setBackground(null);

        String selectedSource = sourcesDisplayed.get(position);
        setTitle(selectedSource);
        String sourceID = sourceToSourceID.get(selectedSource);
        currentArticleList.clear();

        new Thread(new NewsArticlesDownloader(this, sourceID)).start();

    }

    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //set app title to News Gateway (Number of Sources for Topic)

        sourcesDisplayed.clear();
        HashSet<String> lst = topicToSources.get(item.getTitle().toString());
        if (lst != null) {
            ArrayList<String> sortedLst = new ArrayList<>(lst);
            Collections.sort(sortedLst);
            sourcesDisplayed.addAll(sortedLst);
            setTitle("News Gateway " + "(" + sourcesDisplayed.size() + ")");
        }
        arrayAdapter.notifyDataSetChanged();

        return super.onOptionsItemSelected(item);
    }

    // You need this to set up the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    public void downloadFailed() {
        Log.d(TAG, "downloadFailed: ");
    }

    public void updateData(ArrayList<Source> sList) {
        int colorIndex = 0;
        if (sourcesDisplayed.isEmpty()) {
            HashSet<String> allSources = new HashSet<>();
            topicToSources.put("All", new HashSet<>());
            for (Source source : sList) {
                String topic = source.getTopic();
                String name = source.getName();
                String id = source.getId();
                allSources.add(name);

                Objects.requireNonNull(topicToSources.get("All")).add(name);

                sourcesToTopics.put(name, topic);
                if (!topicToSources.containsKey(topic)) {
                    topicToSources.put(topic, new HashSet<>());
                    topicsToColors.put(topic, indexColors[colorIndex]);
                    colorIndex += 1;
                }
                Objects.requireNonNull(topicToSources.get(topic)).add(name);

                sourceToSourceID.put(name, id);
            }

            ArrayList<String> all = new ArrayList<>(allSources);
            Collections.sort(all);
            sourcesDisplayed.addAll(all);
            setTitle("News Gateway " + "(" + sourcesDisplayed.size() + ")");

            arrayAdapter.notifyDataSetChanged();

        }
        ArrayList<String> tempList = new ArrayList<>(topicToSources.keySet());

        Collections.sort(tempList);
        for (String s : tempList)
            opt_menu.add(s);

        for (int i = 1; i < opt_menu.size(); i++) {
            MenuItem item = opt_menu.getItem(i);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.parseColor(topicsToColors.get(item.getTitle()))), 0, s.length(), 0);
            item.setTitle(s);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        binding.progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateArticles(ArrayList<NewsArticle> aList) {
        Log.d(TAG, "updateArticles: ");
        currentArticleList.addAll(aList);
        newsArticleAdapter.notifyDataSetChanged();
        binding.viewpager.setCurrentItem(0);
        binding.drawerLayout.closeDrawer(binding.drawerList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putSerializable("CURRENTTOPICSELECTION", topicToSources);
        outState.putSerializable("SOURCEIDMAP", sourceToSourceID);
        outState.putSerializable("CURRENTSOURCELIST", sourcesDisplayed);
        outState.putSerializable("DISPLAYEDARTICLES", currentArticleList);
        outState.putSerializable("TOPICSTOCOLORS", topicsToColors);
        outState.putSerializable("SOURCESTOTOPICS", sourcesToTopics);
        outState.putInt("POSITION", binding.viewpager.getCurrentItem());

        // Call super last
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        // Call super first
        super.onRestoreInstanceState(savedInstanceState);
        topicToSources = (HashMap<String, HashSet<String>>)savedInstanceState.getSerializable("CURRENTTOPICSELECTION");
        topicsToColors = (HashMap<String, String>) savedInstanceState.getSerializable("TOPICSTOCOLORS");
        sourcesToTopics = (HashMap<String, String>) savedInstanceState.getSerializable("SOURCESTOTOPICS");
        sourceToSourceID = (HashMap<String, String>)savedInstanceState.getSerializable("SOURCEIDMAP");
        sourcesDisplayed.addAll((ArrayList<String>)savedInstanceState.getSerializable("CURRENTSOURCELIST"));
        setTitle("News Gateway " + "(" + sourcesDisplayed.size() + ")");
        arrayAdapter.notifyDataSetChanged();
        if (!((ArrayList<NewsArticle>)savedInstanceState.getSerializable("DISPLAYEDARTICLES")).isEmpty()) {
            currentArticleList.addAll((ArrayList<NewsArticle>)savedInstanceState.getSerializable("DISPLAYEDARTICLES"));
            newsArticleAdapter.notifyDataSetChanged();
            binding.viewpager.setCurrentItem(savedInstanceState.getInt("POSITION"), false);
            binding.viewpager.setBackground(null);
        }
    }
}