package com.searchpath.empathy.controllers;

import com.searchpath.empathy.elastic.ElasticUtil;
import com.searchpath.empathy.responses.FirstControllerResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import javax.inject.Inject;

@Controller("/search")
public class FirstController {

    @Inject
    private ElasticUtil elasticUtil;

    /**
     * Manage the petitions to "/search{query}"
     *
     * @param query The String the petition shall contain with the query info
     * @return The response of the server, serialized as a JSON, right now the query and the ElasticSearch cluster name
     */
    @Get
    public HttpResponse<FirstControllerResponse> search(@QueryValue String query) {
        String cluster_name = elasticUtil.getClusterName();
        var response = new FirstControllerResponse(query, cluster_name);
        return HttpResponse.ok(response);
    }

}