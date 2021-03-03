package com.searchpath.empathy.elastic.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.*;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.xcontent.XContentType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class containing helper methods to interact with the Elastic Client.
 * This class methods manage the exceptions in order to have a more readable code elsewhere.
 */
@Singleton
public class ElasticClientUtil implements IElasticUtil {

    @Inject
    ElasticClient client;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Method that calls the ElasticSearch client to get the cluster name.
     * @see ElasticClient#getClient()
     * @return The cluster name of the elastic search
     * @throws IOException If it can't access the elastic search
     */
    public String getClusterName() throws IOException {
        MainResponse response = null;
        response = client.getClient().info(RequestOptions.DEFAULT);
        return response.getClusterName();
    }

    /**
     * This method reads the file containing the IMDB films and index them into elastic search.
     * To do so, first it reads the file with the aux of a private method.
     * @see ElasticClientUtil#readFile(InputStream)
     * Then, it divides the entry into chunks of 50.000 lines with the help of the Guava's Iterators library help.
     * @see Iterators#partition(Iterator, int)
     * Following that, read each chunk and proceeds to create a
     * @see Film
     * object which is then deserialized into a Json and added to a
     * @see BulkRequest
     * object.
     * Next step is take the elastic search client and upload this bulk. And repeat this for every chunk.
     *
     * @return "Success loading data" if everything went well and "Error while loading data" if not.
     * @throws IOException If the method can't deserialize the film object into JSON or an error occur while loading
     * the bulk data through the client.
     */
    public String loadIMDBData() throws IOException {
        var reader = readFile(this.getClass().getClassLoader().getResourceAsStream("data.tsv"));
        var bulk = new BulkRequest();

        UnmodifiableIterator<List<String>> linesList = Iterators.partition(reader.lines().iterator(), 50000);

        while (linesList.hasNext()) {
            List<String> list = linesList.next();

            for (String line : list) {
                var data = line.split("\t");
                var film = new Film(data[0], data[2],
                        data[8].equals("\\N") ? null : data[8].split(","),
                        data[5],
                        data[6].equals("\\N") ? null : data[6]);
                bulk.add(new IndexRequest("imdb").id(film.getId())
                        .source(objectMapper.writeValueAsString(film), XContentType.JSON));
            }

            client.getClient().bulk(bulk, RequestOptions.DEFAULT);
            bulk = new BulkRequest();
        }

        return "Success loading data";
    }

    /**
     * Helper method, takes an InputStream and return a BufferedReader
     * @param dataPath The InputStream pointing to the file
     * @return The BufferedReader of the file
     */
    private BufferedReader readFile(InputStream dataPath) {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(dataPath, StandardCharsets.UTF_8));
        return reader;
    }

}
