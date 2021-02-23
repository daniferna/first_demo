package com.searchpath.empathy.responses;

/**
 * Serializable data class for the Response
 */
public class FirstControllerResponse {
    String query;
    String cluster_name;

    public FirstControllerResponse() {}

    public FirstControllerResponse(String query, String cluster_name) {
        this.query = query;
        this.cluster_name = cluster_name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }
}
