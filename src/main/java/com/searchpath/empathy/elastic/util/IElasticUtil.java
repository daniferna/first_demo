package com.searchpath.empathy.elastic.util;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.pojo.QueryResponse;

import java.io.IOException;
import java.text.ParseException;

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
    String loadIMDBData() throws IOException, ParseException;

    /**
     * Search films based on the query and transforms the result of the query into a QueryResponse object.
     *
     * @param query The query containing information of the movie you are looking for
     * @return A QueryResponse object containing the films and the number of the results
     * @throws IOException In case there is an error accessing elastic search.
     * @see QueryResponse
     */
    QueryResponse search(String query) throws IOException;

    /**
     * Search films by their title
     * @param title The title of the film we are looking for
     * @return A QueryResponse containing the films and the number of results
     * @throws IOException In case there is an error accessing elastic search.
     */
    QueryResponse searchByParams(String[] title) throws IOException;
}
