package com.searchpath.empathy.elastic.util;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.pojo.QueryResponse;

import java.io.IOException;

public interface IElasticUtil {

    /**
     * Method that calls the ElasticSearch client to get the cluster name.
     * @see ElasticClient#getClient()
     * @return The cluster name of the elastic search
     * @throws IOException If it can't access the elastic search
     */
    String getClusterName() throws IOException;

    /**
     * This method reads the file containing the IMDB films and index them into elastic search.
     * All reading and processing operations shall be lazy in order to save memory.
     * @return "Success loading data" if everything went well.
     */
    String loadIMDBData() throws IOException;

    QueryResponse searchFilmsByTitle(String title) throws IOException;
}
