package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.StringJoiner;

public class QueryResponse {

    private final long total;
    private final Film[] items;

    @JsonCreator
    public QueryResponse(@JsonProperty long total, @JsonProperty Film[] items) {
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public Film[] getItems() {
        return items;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResponse.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("items=" + Arrays.toString(items))
                .toString();
    }
}
