package com.example.newsaggregator;

public class Source {
    private String id;
    private String name;
    private String topic;

    public Source(String id, String name, String topic) {
        this.id = id;
        this.name = name;
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }
}
