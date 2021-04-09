package com.searchpath.empathy.pojo.aggregations.bucket.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.searchpath.empathy.pojo.aggregations.bucket.IBucket;
import com.searchpath.empathy.pojo.serializers.BucketSerializer;

import java.util.Objects;
import java.util.StringJoiner;

@JsonSerialize(using = BucketSerializer.class)
public class TermBucket implements IBucket {

    private final String name;
    private final Long count;

    @JsonCreator
    public TermBucket(@JsonProperty String name, @JsonProperty long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TermBucket.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("count=" + count)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermBucket that = (TermBucket) o;
        return Objects.equals(name, that.name) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, count);
    }
}
