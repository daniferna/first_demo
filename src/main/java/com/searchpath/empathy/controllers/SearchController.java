package com.searchpath.empathy.controllers;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import com.searchpath.empathy.pojo.QueryResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

@Controller("/search")
public class SearchController extends BaseController {

    private final IElasticUtil elasticUtil;

    @Inject
    SearchController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    /**
     * Manage the petitions to "/search?query=queryText&{...}"
     *
     * @param query,title The String the petition shall contain with the query info
     * @return The response of the server, serialized as a JSON {@link QueryResponse}
     * @throws IOException  If there's a problem when accessing ElasticSearch
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Get
    public QueryResponse search(@QueryValue("query") String query, @QueryValue("genre") Optional<String> genres,
                                @QueryValue("type") Optional<String> type,
                                @QueryValue("date") Optional<String> date,
                                @QueryValue("filter*") Optional<String> filter) throws IOException {

        var params = new String[]{query, genres.orElse(""),
                type.orElse(""), date.orElse(""), filter.orElse("")};
        return elasticUtil.searchByParams(params);

    }

    /**
     * Manage the petitions to "/search/titles/:titleID"
     * @param titleID The ID of the title you're looking for
     * @return The title you were looking for
     * @throws IOException  If there's a problem when accessing ElasticSearch
     */
    @Get("/titles/{titleID}")
    public Film searchByTitleID(@PathVariable String titleID) throws IOException {
        return elasticUtil.searchByTitleID(titleID);
    }

}