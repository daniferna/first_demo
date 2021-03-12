package com.searchpath.empathy.controllers;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.QueryResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

@Controller("/search")
public class SearchController extends BaseController{

    private final IElasticUtil elasticUtil;

    @Inject
    SearchController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
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

}