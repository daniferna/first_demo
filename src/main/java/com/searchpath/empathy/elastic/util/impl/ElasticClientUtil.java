package com.searchpath.empathy.elastic.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import com.searchpath.empathy.pojo.QueryResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    /**
     * This implementation reads the file {@see #readFile(InputStream)} and then, it divides the entry into chunks
     * of 50.000 lines with the help of the Guava's Iterators library help. {@see Iterators#partition(Iterator, int)}
     * <p>
     * Following that, read each chunk and proceeds to create a {@link Film} object which is then deserialized into
     * a Json and added to a {@link BulkRequest}. Next step is take the elastic search client and upload this bulk.
     * <p>
     * This is repeat for every chunk.
     *
     * @throws IOException If the method can't deserialize the film object into JSON
     *                     or an error occur while loading the bulk data through the client.
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
                        data[1], data[5],
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
     * This implementation use the {@link ElasticClient} API to look for the films.
     * It creates a {@link SearchRequest}, and using a {@link SearchSourceBuilder}, creates the query.
     * <p>
     * Once the response is obtained, is processed by a private helper method {@see #parseHitToFilms(SearchHits)}
     * which transform the hits into {@link Film} objects.
     *
     * @throws IOException If the method can't serialize the hit into a Film JSON or an error occur while searching
     *                     the query through the client.
     */
    @Override
    public QueryResponse searchFilmsByTitle(String title) throws IOException {
        var request = new SearchRequest("imdb");
        var source = new SearchSourceBuilder()
                .query(new MatchQueryBuilder("title", title))
                .size(10);
        request.source(source);

        var response = client.getClient().search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        Film[] films = parseHitToFilms(response.getHits());

        return new QueryResponse(total, films);
    }

    /**
     * Helper method which transforms the elastic search response hits into Film objects.
     *
     * @param hits The hits of the search
     * @return An array of films {@see Film}
     */
    private Film[] parseHitToFilms(SearchHits hits) {
        return Arrays.stream(hits.getHits()).map(hit -> {
            String source = hit.getSourceAsString();
            try {
                return objectMapper.readValue(source, Film.class);
            } catch (JsonProcessingException e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }).toArray(Film[]::new);
    }

    /**
     * Helper method, takes an {@link InputStream} and return a {@link BufferedReader}
     *
     * @param dataPath The InputStream pointing to the file
     * @return The BufferedReader of the file
     */
    private BufferedReader readFile(InputStream dataPath) {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(dataPath, StandardCharsets.UTF_8));
        return reader;
    }

}
