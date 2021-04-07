package com.searchpath.empathy.elastic.util;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.commands.Command;
import com.searchpath.empathy.pojo.Film;
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
     *
     * @param fileName The name of the file containing the data. It has to be located at the resources folder
     * @param chunkSize The size you want to divide the read data from the file.
     * @param command The command containing the logic for the creation of bulks for the specified type of media.
     * @return "Success loading data" if everything went well.
     * @throws IOException If an error occur during the reading or indexing process.
     */
    String loadIMDBMedia(String fileName, int chunkSize, Command command) throws IOException;

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
     * @param params The title, genre and type of the film we are looking for
     * @return A QueryResponse containing the films and the number of results
     * @throws IOException In case there is an error accessing elastic search.
     */
    QueryResponse searchByParams(String[] params) throws IOException;

    /**
     * Search titles by their id
     * @param id The id of the title you're looking for
     * @return  A {@link com.searchpath.empathy.pojo.Film} containing just that title
     * @throws IOException In case there is an error accessing elastic search.
     */
    Film searchByTitleID(String id) throws IOException;

}
