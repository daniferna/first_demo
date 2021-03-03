package com.searchpath.empathy.elastic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterators;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.impl.ElasticClientUtil;
import com.searchpath.empathy.pojo.Film;
import org.elasticsearch.action.bulk.BulkRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

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

}
