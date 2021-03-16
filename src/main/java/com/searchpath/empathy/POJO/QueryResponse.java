package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.StringJoiner;

public class QueryResponse {

    private final long total;
    private final Film[] items;
    private final TermAggregationPojo[] aggregations;

    @JsonCreator()
    public QueryResponse(@JsonProperty long total, @JsonProperty Film[] items
            , @JsonProperty TermAggregationPojo[] aggregations) {
        this.total = total;
        this.items = items;
        this.aggregations = aggregations;
    }

    public long getTotal() {
        return total;
    }

    public Film[] getItems() {
        return items;
    }

    public TermAggregationPojo[] getAggregations() {
        return aggregations;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResponse.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("items=" + Arrays.toString(items))
                .add("aggregations=" + Arrays.toString(aggregations))
                .toString();
    }
}
