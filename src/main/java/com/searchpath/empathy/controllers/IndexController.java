package com.searchpath.empathy.controllers;

import com.searchpath.empathy.elastic.commands.Command;
import com.searchpath.empathy.elastic.commands.impl.FilmBulkCreationCommand;
import com.searchpath.empathy.elastic.commands.impl.RatingBulkCreationCommand;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Controller("/index")
public class IndexController extends BaseController {

    private final IElasticUtil elasticUtil;

    @Inject
    IndexController(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    /**
     * Manages the petitions to /index
     * Call which initiates the process of indexing of both the IMDB films and ratings stored in the resources folder.
     *
     * @return A response with OK status and the returned String of the called method if everything works well.
     * @throws IOException If something went wrong
     * @see IElasticUtil#loadIMDBMedia(String, int, Command) ()
     */
    @Get
    public String index() throws IOException {
        elasticUtil.loadIMDBMedia("films.tsv", 5000, new FilmBulkCreationCommand());
        return elasticUtil.loadIMDBMedia("ratings.tsv", 5000, new RatingBulkCreationCommand());
    }

    @Get("/background")
    public String indexBackground() {
        new Thread(this::runIndex).start();
        return "Process started";
    }

    /**
     * Manages the petitions to /index/films
     * Call which initiates the process of indexing of the IMDB films stored in the resources folder.
     *
     * @return A response with OK status and the returned String of the called method if everything works well.
     * @throws IOException If something went wrong
     * @see IElasticUtil#loadIMDBMedia(String, int, Command) ()
     */
    @Get("/films")
    public String indexFilms() throws IOException {
        return elasticUtil.loadIMDBMedia("films.tsv", 5000, new FilmBulkCreationCommand());
    }

    /**
     * Manages the petitions to /index/ratings
     * Call which initiates the process of indexing of the IMDB ratings stored in the resources folder.
     *
     * @return A response with OK status and the returned String of the called method if everything works well.
     * @throws IOException If something went wrong
     * @see IElasticUtil#loadIMDBMedia(String, int, Command) ()
     */
    @Get("/ratings")
    public String indexRatings() throws IOException {
        return elasticUtil.loadIMDBMedia("ratings.tsv", 5000, new RatingBulkCreationCommand());
    }

    private void runIndex() {
        try {
            elasticUtil.loadIMDBMedia("films.tsv", 12000, new FilmBulkCreationCommand());
            elasticUtil.loadIMDBMedia("ratings.tsv", 20000, new RatingBulkCreationCommand());
        } catch (IOException exception) {
            System.err.println("ERROR: Error while indexing.");
            exception.printStackTrace();
        }
        System.out.println("Index finished");
    }
}
