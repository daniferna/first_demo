package com.searchpath.empathy.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.searchpath.empathy.pojo.aggregations.Aggregation;
import com.searchpath.empathy.pojo.aggregations.DateHistogramBucket;
import com.searchpath.empathy.pojo.aggregations.TermBucket;

import java.util.Arrays;
import java.util.StringJoiner;

public class QueryResponse {

    private final long total;
    private final Film[] items;
    private final Aggregation<TermBucket>[] termAggregations;
    private final Aggregation<DateHistogramBucket> dateHistogramAggregation;

    @JsonCreator()
    public QueryResponse(@JsonProperty long total, @JsonProperty Film[] items,
                         @JsonProperty Aggregation<TermBucket>[] termAggregations,
                         @JsonProperty Aggregation<DateHistogramBucket> dateHistogram) {
        this.total = total;
        this.items = items;
        this.termAggregations = termAggregations;
        this.dateHistogramAggregation = dateHistogram;
    }

    public long getTotal() {
        return total;
    }

    public Film[] getItems() {
        return items;
    }

    public Aggregation<TermBucket>[] getTermAggregations() {
        return termAggregations;
    }

    public Aggregation<DateHistogramBucket> getDateHistogramAggregation() {
        return dateHistogramAggregation;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResponse.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("items=" + Arrays.toString(items))
                .add("termAggregations=" + Arrays.toString(termAggregations))
                .add("dateHistogramAggregation=" + dateHistogramAggregation)
                .toString();
    }
}
