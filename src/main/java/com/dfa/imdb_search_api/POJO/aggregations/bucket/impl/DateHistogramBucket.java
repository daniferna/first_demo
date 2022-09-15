package com.dfa.imdb_search_api.POJO.aggregations.bucket.impl;

import com.dfa.imdb_search_api.POJO.aggregations.bucket.IBucket;
import com.dfa.imdb_search_api.POJO.serializers.BucketSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;
import java.util.StringJoiner;

@JsonSerialize(using = BucketSerializer.class)
public class DateHistogramBucket implements IBucket {

    private final String decade;
    private final long count;

    public DateHistogramBucket(long docCount, String key) {
        this.count = docCount;
        this.decade = Integer.parseInt(key) + 1 + "-" + (Integer.parseInt(key) + 10);
    }

    @Override
    public String getName() {
        return getDecade();
    }

    public long getCount() {
        return count;
    }

    public String getDecade() {
        return decade;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DateHistogramBucket.class.getSimpleName() + "[", "]")
                .add("docCount=" + count)
                .add("decade='" + decade + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateHistogramBucket that = (DateHistogramBucket) o;
        return count == that.count && Objects.equals(decade, that.decade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decade, count);
    }
}
