package com.searchpath.empathy.pojo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.StringJoiner;

public class Film {

    private final String id;
    private final String title;
    private final String[] genres;
    private final String type;
    private final String startDate;
    private String endDate;

    @JsonCreator
    public Film(@JsonProperty("id") String id,
                @JsonProperty("title") String title,
                @JsonProperty("genres") String[] genres,
                @JsonProperty("type") String type,
                @JsonProperty("startDate") String startDate,
                @JsonProperty("endDate") String endDate) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String[] getGenres() {
        return genres;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Film.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("title='" + title + "'")
                .add("genres=" + Arrays.toString(genres))
                .add("type='" + type + "'")
                .add("startDate='" + startDate + "'")
                .add("endDate='" + endDate + "'")
                .toString();
    }
}
