package com.searchpath.empathy.controllersTest;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.pojo.FirstControllerResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class FirstControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void testSearch() {
        HttpRequest<String> request = HttpRequest.GET("/search?query=test");
        FirstControllerResponse body = client.toBlocking().retrieve(request, FirstControllerResponse.class);

        assertNotNull(body);
        var expectedResponse = new FirstControllerResponse("test", "docker-cluster");
        assertEquals(expectedResponse.getCluster_name(), body.getCluster_name());
        assertEquals(expectedResponse.getQuery(), body.getQuery());
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
