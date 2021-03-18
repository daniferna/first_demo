package com.searchpath.empathy.pojo.aggregations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.searchpath.empathy.pojo.aggregations.serializers.BucketSerializer;

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
}
