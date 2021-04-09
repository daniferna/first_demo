package com.searchpath.empathy.pojo.aggregations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.searchpath.empathy.pojo.serializers.AggregationSerializer;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

@JsonSerialize(using = AggregationSerializer.class)
public class Aggregation<T> {

    private final String name;
    private final T[] buckets;

    @JsonCreator
    public Aggregation(@JsonProperty String name, @JsonProperty T[] buckets) {
        this.name = name;
        this.buckets = buckets;
    }

    public T[] getBuckets() {
        return buckets;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("terms=" + Arrays.toString(buckets))
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregation<?> that = (Aggregation<?>) o;
        return Objects.equals(name, that.name) && Arrays.equals(buckets, that.buckets);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(buckets);
        return result;
    }
}
