package com.dfa.imdb_search_api.elastic;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

/**
 * Class implementing the Singleton design pattern using @Singleton to access the ElasticSearch client
 * Access through @Inject tag
 */
@Singleton
public class ElasticClient {

    private final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http"),
                    new HttpHost("localhost", 9300, "http"))
    );

    /**
     * Method to be called when the context is closed.
     * It closes the pool connection created by the RestHighLevelClient
     */
    @PreDestroy
    private void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RestHighLevelClient getClient() {
        return client;
    }

}
