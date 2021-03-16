package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.searchpath.empathy.pojo.serializers.TermBucketSerializer;

import java.util.StringJoiner;

@JsonSerialize(using = TermBucketSerializer.class)
public class TermBucketPojo {

    private final String name;
    private final Long count;

    @JsonCreator
    public TermBucketPojo(@JsonProperty String name, @JsonProperty  long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public Long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TermBucketPojo.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("count=" + count)
                .toString();
    }
}
