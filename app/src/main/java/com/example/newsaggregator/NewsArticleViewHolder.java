package com.example.newsaggregator;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewsArticleViewHolder extends RecyclerView.ViewHolder {
    TextView headline;
    TextView date;
    TextView author;
    ImageView image;
    TextView text;
    TextView pageNum;

    public NewsArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        headline = itemView.findViewById(R.id.headline);
        date = itemView.findViewById(R.id.date);
        author = itemView.findViewById(R.id.author);
        image = itemView.findViewById(R.id.articleImage);
        text = itemView.findViewById(R.id.articleText);
        pageNum = itemView.findViewById(R.id.page_num);
    }
}

