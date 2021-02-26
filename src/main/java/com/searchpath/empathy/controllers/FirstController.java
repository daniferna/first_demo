package com.searchpath.empathy.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.POJO.FirstControllerResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Controller("/search")
public class FirstController {

    private IElasticUtil elasticUtil;

    @Inject FirstController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    /**
     * Manage the petitions to "/search{query}"
     *
     * @param query The String the petition shall contain with the query info
     * @return The response of the server, serialized as a JSON, right now the query and the ElasticSearch cluster name
     */
    @Get
    public HttpResponse<FirstControllerResponse> search(@QueryValue String query) throws IOException {
        String cluster_name = elasticUtil.getClusterName();
        var response = new FirstControllerResponse(query, cluster_name);
        return HttpResponse.ok(response);
    }

    /**
     * ERROR HANDLING CODE FOR THIS CONTROLLER BELOW
     */

    @Error
    public HttpResponse<JsonError> elasticIOError(HttpRequest request, IOException ioException) {
        JsonError error = new JsonError("IO Exception: " + ioException.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>status(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error accessing ElasticSearch").body(error);
    }

    @Error
    public HttpResponse<JsonError> jsonError(HttpRequest request, JsonParseException jsonParseException) {
        JsonError error = new JsonError("INVALID JSON: " + jsonParseException.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Error in your JSON").body(error);
    }

}