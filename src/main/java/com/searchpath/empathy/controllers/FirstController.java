package com.searchpath.empathy.controllers;

import com.searchpath.empathy.responses.FirstControllerResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;

import java.io.IOException;
import java.util.Optional;

@Controller("/search")
public class FirstController {

    /**
     * Manage the petitions to "/search/{query}"
     * @param query The String the petition can contain
     * @return  The response of the server, right now the query and the ElasticSearch cluster name
     */
    @Get("/{query}")
    public HttpResponse<FirstControllerResponse> search(@PathVariable String query) {
        String cluster_name = getClusterName();
        var response = new FirstControllerResponse(query, cluster_name);
        return HttpResponse.ok(response);
    }

    /**
     * Connects with ElasticSearch and ask for the info of the ElasticSearch cluster. Then get the cluster name
     * @return Name of the ElasticSearch cluster
     */
    private String getClusterName() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9300, "http"))
        );
        MainResponse response = null;
        try {
            response = client.info(RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getClusterName();
    }

}