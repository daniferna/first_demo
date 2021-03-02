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
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
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

    public String getClusterName() throws IOException {

        MainResponse response = null;
        response = client.getClient().info(RequestOptions.DEFAULT);
        return response.getClusterName();
    }


    public String loadIMDBData() {
        var reader = readFile(this.getClass().getClassLoader().getResourceAsStream("data.tsv"));
        var bulk = new BulkRequest();

        UnmodifiableIterator<List<String>> linesList = Iterators.partition(reader.lines().iterator(), 50000);

        while (linesList.hasNext()) {
            List<String> list = linesList.next();

            for (String line : list) {
                var data = line.split("\t");
                var film = new Film(data[0], data[2], data[8].split(","), data[5],
                        data[6].equals("\\N") ? null : data[6]);
                try {
                    bulk.add(new IndexRequest("imdb").id(film.getId())
                            .source(objectMapper.writeValueAsString(film), XContentType.JSON));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.getClient().bulk(bulk, RequestOptions.DEFAULT);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            bulk = new BulkRequest();
        }
        return "Success";
    }

    private BufferedReader readFile(InputStream dataPath) {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(dataPath, StandardCharsets.UTF_8));
        return reader;
    }

}
