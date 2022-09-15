package com.dfa.imdb_search_api.controllersTest;

import com.dfa.imdb_search_api.elastic.util.IElasticUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SearchControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    ObjectMapper objectMapper;

    private final IElasticUtil elasticUtil;

    @Inject
    SearchControllerTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void testSearch() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?query=The+simpsons+movie");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.search("The simpsons movie");
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTitle() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(Map.of("query", "Avengers"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTitleAndType() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?query=Avengers&type=documentary");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(Map.of("query", "Avengers",
                "type", "documentary"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTypeAndGenre() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?query=&type=documentary&genres=Drama");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(Map.of("query", "",
                "genres", "drama", "type", "documentary"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTitleGenreAndType() throws IOException {
        HttpRequest<String> request = HttpRequest.GET(
                "/search?query=The+Simpsons&type=tvSeries&genres=animation,comedy");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(
                Map.of("query", "The Simpsons", "genres", "animation,comedy",
                        "type", "tvSeries"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTitleGenreTypeAndDate() throws IOException {
        HttpRequest<String> request = HttpRequest.GET(
                "/search?query=The+Simpsons&type=tvSeries&genres=animation,comedy&date=2000-2015");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(
                Map.of("query", "The Simpsons", "genres", "animation,comedy",
                        "type", "tvSeries", "date", "2000-2015"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByMultipleDate() throws IOException {
        HttpRequest<String> request = HttpRequest.GET(
                "/search?query=The+Simpsons&type=tvSeries&date=2000-2015,1990-1998");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchByParams(
                Map.of("query", "The Simpsons", "type", "tvSeries",
                        "date", "2000-2015,1990-1998"));
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchWithFilter() throws IOException {
        HttpRequest<String> request = HttpRequest.GET(
                "/search?query=The+Simpsons&filter=type:tvSeries");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var notExpectedResponse = elasticUtil.searchByParams(
                Map.of("query", "The Simpsons", "type", "tvSeries",
                        "date", "2000-2015,1990-1998"));
        var expectedResponse = elasticUtil.searchByParams(
                Map.of("query", "The Simpsons", "filters", "type:tvSeries"));

        assertNotEquals(objectMapper.writeValueAsString(notExpectedResponse), body);
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchWithoutQueryParam() {
        HttpRequest<String> request = HttpRequest.GET("/search");

        Assertions.assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(request));
    }

}
