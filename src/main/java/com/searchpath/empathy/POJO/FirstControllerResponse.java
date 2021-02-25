package com.searchpath.empathy.POJO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message class for the FirstController Response.
 * Uses Jackson to being serialized into JSON
 */
public class FirstControllerResponse {
    private final String query;
    private final String cluster_name;

    @JsonCreator
    public FirstControllerResponse(@JsonProperty("query") String query,
                                   @JsonProperty("cluster_name") String cluster_name) {
        this.query = query;
        this.cluster_name = cluster_name;
    }

    public String getQuery() {
        return query;
    }

    public String getCluster_name() {
        return cluster_name;
    }

}
