package com.searchpath.empathy.pojo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.StringJoiner;

public class Film {

    @JsonProperty("id")
    private final String id;
    @JsonProperty("title")
    private final String title;
    @JsonProperty("genres")
    private final String[] genres;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("start_year")
    private final String start_year;
    @JsonProperty("end_year")
    private final String end_year;

    @JsonCreator
    public Film(String id, String title, String[] genres, String type, String start_year, String end_year) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.type = type;
        this.start_year = start_year;
        this.end_year = end_year;
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

    public String getStartYear() {
        return start_year;
    }

    public String getEndYear() {
        return end_year;
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
                .add("startDate='" + start_year + "'")
                .add("endDate='" + end_year + "'")
                .toString();
    }
}
