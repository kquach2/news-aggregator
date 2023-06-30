package com.example.newsaggregator;

import java.io.Serializable;

public class NewsArticle implements Serializable{
    private String headline;
    private String date;
    private String author;
    private String url;
    private String image;
    private String text;

    public NewsArticle(String headline, String date, String author, String url, String image, String text) {
        this.headline = headline;
        this.date = date;
        this.author = author;
        this.url = url;
        this.image = image;
        this.text = text;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public String getText() {
        return text;
    }
}
