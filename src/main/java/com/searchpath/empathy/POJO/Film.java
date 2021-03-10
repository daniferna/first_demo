package com.searchpath.empathy.pojo;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Arrays;
import java.util.StringJoiner;

@JsonNaming(value=PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Film {

    private String id;
    private String title;
    private String[] genres;
    private String type;
    private String start_year;
    private String end_year;

    public Film() {}

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
                .add("start_year=" + start_year)
                .add("end_year=" + end_year)
                .toString();
    }
}
