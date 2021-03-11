package com.searchpath.empathy.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.QueryResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

@Controller("/search")
public class FirstController {

    private IElasticUtil elasticUtil;

    @Inject FirstController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    /**
     * Call which initiates the process of indexing of the IMDB data stored in the resources folder.
     * @return A response with OK status and the returned String of the called method if everything works well.
     * @see IElasticUtil#loadIMDBData()
     * @throws IOException If something went wrong
     */
    @Get("/index")
    public HttpResponse<String> index() throws IOException, ParseException {
        return HttpResponse.ok(elasticUtil.loadIMDBData());
    }

    /**
     * Manage the petitions to "/search{...}"
     *
     * @param query,title The String the petition shall contain with the query info
     * @return The response of the server, serialized as a JSON {@link QueryResponse}
     */
    @Get("{?query}{?title}")
    public HttpResponse<QueryResponse> search(Optional<String> title, Optional<String> query) throws IOException {

        if (query.isPresent())
            return HttpResponse.ok(elasticUtil.search(query.get()));
        else if (title.isPresent())
            return HttpResponse.ok(elasticUtil.searchByTitle(title.get()));

        return HttpResponse.badRequest();
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