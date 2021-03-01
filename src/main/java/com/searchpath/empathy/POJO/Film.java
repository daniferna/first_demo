package com.searchpath.empathy.pojo;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Film {

    private final String id;
    private final String title;
    private final String[] genres;
    private final String startDate;
    private String endDate;

    @JsonCreator
    public Film(@JsonProperty("id") String id,
                @JsonProperty("title") String title,
                @JsonProperty("genres") String[] genres,
                @JsonProperty("startDate") String startDate,
                @JsonProperty("endDate") String endDate) {
        this.id = id;
        this.title = title;
        this.genres = genres;
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

    @Override
    public String toString() {
        return "Film{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", genres=" + Arrays.toString(genres) +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}
