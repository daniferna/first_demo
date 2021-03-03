package com.searchpath.empathy.elastic.util;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.pojo.QueryResponse;

import java.io.IOException;

public interface IElasticUtil {

    /**
     * Method that calls the ElasticSearch client to get the cluster name.
     *
     * @return The cluster name of the elastic search
     * @throws IOException If it can't access the elastic search
     * @see ElasticClient#getClient()
     */
    String getClusterName() throws IOException;

    /**
     * This method reads the file containing the IMDB films and index them into elastic search.
     * All reading and processing operations shall be lazy in order to save memory.
     *
     * @return "Success loading data" if everything went well.
     */
    String loadIMDBData() throws IOException;

    /**
     * Search films by their title and transform the result of the query into a QueryResponse object.
     *
     * @param title The title of the movie you are looking for
     * @return A QueryResponse object containing the films and the number of the results
     * @throws IOException In case there is an error accessing elastic search.
     * @see QueryResponse
     */
    QueryResponse searchFilmsByTitle(String title) throws IOException;
}
