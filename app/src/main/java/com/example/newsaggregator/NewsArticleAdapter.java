package com.example.newsaggregator;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class NewsArticleAdapter extends
        RecyclerView.Adapter<NewsArticleViewHolder> {

    private final MainActivity mainActivity;
    private final ArrayList<NewsArticle> articleList;

    public NewsArticleAdapter(MainActivity mainActivity, ArrayList<NewsArticle> articleList) {
        this.mainActivity = mainActivity;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public NewsArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsArticleViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.article_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsArticleViewHolder holder, int position) {
        holder.headline.setVisibility(View.VISIBLE);
        holder.date.setVisibility(View.VISIBLE);
        holder.author.setVisibility(View.VISIBLE);
        holder.text.setVisibility(View.VISIBLE);
        NewsArticle a = articleList.get(position);
        if (!Objects.equals(a.getHeadline(), "")) {
            holder.headline.setText(a.getHeadline());
            holder.headline.setOnClickListener(v -> openArticle(a.getUrl()));
        } else holder.headline.setVisibility(View.GONE);
        if (!Objects.equals(a.getDate(), "")) {
            holder.date.setText(a.getDate());
        } else holder.date.setVisibility(View.GONE);
        if (!Objects.equals(a.getAuthor(), "")) {
            holder.author.setText(a.getAuthor());
        } else holder.author.setVisibility(View.GONE);
        if (!Objects.equals(a.getImage(), "")) {  // url has to be https
            Glide.with(mainActivity)
                    .load(a.getImage())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.brokenimage)
                    .into(holder.image);
        }
        else {
            Glide.with(mainActivity).clear(holder.image);
            holder.image.setImageResource(R.drawable.noimage);
        }
        holder.image.setOnClickListener(v -> openArticle(a.getUrl()));
        if (!Objects.equals(a.getText(), "")) {
            holder.text.setText(a.getText());
            holder.text.setOnClickListener(v -> openArticle(a.getUrl()));
        } else holder.text.setVisibility(View.GONE);

        holder.pageNum.setText(String.format(
                Locale.getDefault(),"%d of %d", (position+1), articleList.size()));
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    private void openArticle(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(mainActivity.getPackageManager()) != null) {
            mainActivity.startActivity(intent);
        }
    }
}



