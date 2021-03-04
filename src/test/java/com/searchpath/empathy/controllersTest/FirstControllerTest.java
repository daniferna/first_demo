package com.searchpath.empathy.controllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.FirstControllerResponse;
import com.searchpath.empathy.pojo.QueryResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class FirstControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    ObjectMapper objectMapper;

    private final IElasticUtil elasticUtil;

    @Inject FirstControllerTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void testSearch() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?query=The+simpsons+movie");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchFilms("The simpsons movie");
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchByTitle() throws IOException {
        HttpRequest<String> request = HttpRequest.GET("/search?title=Avengers");
        var body = client.toBlocking().retrieve(request);

        assertNotNull(body);
        var expectedResponse = elasticUtil.searchFilmByTitle("Avengers");
        assertEquals(objectMapper.writeValueAsString(expectedResponse), body);
    }

    @Test
    public void testSearchWithoutQueryParam() {
        HttpRequest<String> request = HttpRequest.GET("/search");

        Assertions.assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(request));
    }

    @Test
    @MockBean()
    public void testSearchWithElasticProblem() {
        //TODO Explore idea of use mock with wrong port
    }



}
