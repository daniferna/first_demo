package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class Rating {

    private final String id;
    private final float average_rating;
    private final int num_votes;

    @JsonCreator()
    public Rating(@JsonProperty("id") String id,
                  @JsonProperty("average_rating") float averageRating,
                  @JsonProperty("num_votes") int numVotes) {
        this.id = id;
        this.average_rating = averageRating;
        this.num_votes = numVotes;
    }

    public String getId() {
        return id;
    }

    public float getAverageRating() {
        return average_rating;
    }

    public int getNumVotes() {
        return num_votes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Float.compare(rating.getAverageRating(), getAverageRating()) == 0 && getNumVotes() == rating.getNumVotes() && getId().equals(rating.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAverageRating(), getNumVotes());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Rating.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("avgRating=" + average_rating)
                .add("numVotes=" + num_votes)
                .toString();
    }
}
