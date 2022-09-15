package com.dfa.imdb_search_api.POJO;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Film {

    private final String id;
    private final String title;
    private final String[] genres;
    private final String type;
    private final String start_year;
    private final String end_year;
    private String original_title;
    @JsonProperty("average_rating")
    private float average_rating;
    @JsonProperty("num_votes")
    private int num_votes;

    @JsonCreator()
    public Film(@JsonProperty("id") String id,
                @JsonProperty("title") String title,
                @JsonProperty("original_title") String originalTitle,
                @JsonProperty("genres") String[] genres,
                @JsonProperty("type") String type,
                @JsonProperty("start_year") String start_year,
                @JsonProperty("end_year") String end_year) {
        this(id, title, genres, type, start_year, end_year);
        this.original_title = originalTitle;
    }

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

    public String getOriginal_title() {
        return original_title;
    }

    public String[] getGenres() {
        return genres;
    }

    public String getStart_year() {
        return start_year;
    }

    public String getEnd_year() {
        return end_year;
    }

    public String getType() {
        return type;
    }

    public float getAverage_rating() {
        return average_rating;
    }

    public void setAverage_rating(float average_rating) {
        this.average_rating = average_rating;
    }

    public int getNum_votes() {
        return num_votes;
    }

    public void setNum_votes(int num_votes) {
        this.num_votes = num_votes;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return getId().equals(film.getId()) && Objects.equals(getTitle(), film.getTitle()) && Arrays.equals(getGenres(), film.getGenres()) && Objects.equals(getType(), film.getType()) && Objects.equals(start_year, film.start_year) && Objects.equals(end_year, film.end_year);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getTitle(), getType(), start_year, end_year);
        result = 31 * result + Arrays.hashCode(getGenres());
        return result;
    }
}
