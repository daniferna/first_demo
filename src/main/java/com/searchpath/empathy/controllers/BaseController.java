package com.searchpath.empathy.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import java.io.IOException;

public class BaseController {

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
