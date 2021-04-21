package com.searchpath.empathy.controllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class SearchControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

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
    public void testSearchWithoutQueryParam() {
        HttpRequest<String> request = HttpRequest.GET("/search");

        Assertions.assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(request));
    }

}
