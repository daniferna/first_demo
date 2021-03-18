package com.searchpath.empathy.controllers;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.text.ParseException;

@Controller("/index")
public class IndexController extends BaseController {

    private final IElasticUtil elasticUtil;

    @Inject
    IndexController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    /**
     * Manages the petitions to /index
     * Call which initiates the process of indexing of the IMDB data stored in the resources folder.
     * @return A response with OK status and the returned String of the called method if everything works well.
     * @see IElasticUtil#loadIMDBData()
     * @throws IOException If something went wrong
     */
    @Get
    public String index() throws IOException, ParseException {
        return elasticUtil.loadIMDBData();
    }
}
