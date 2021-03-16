package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.searchpath.empathy.pojo.serializers.TermAggregationSerializer;

import java.util.Arrays;
import java.util.StringJoiner;

@JsonSerialize(using = TermAggregationSerializer.class)
public class TermAggregationPojo {

    private final String name;
    private final TermBucketPojo[] terms;

    @JsonCreator
    public TermAggregationPojo(@JsonProperty  String name, @JsonProperty TermBucketPojo[] terms) {
        this.name = name;
        this.terms = terms;
    }

    public TermBucketPojo[] getTerms() {
        return terms;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TermAggregationPojo.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("terms=" + Arrays.toString(terms))
                .toString();
    }
}
