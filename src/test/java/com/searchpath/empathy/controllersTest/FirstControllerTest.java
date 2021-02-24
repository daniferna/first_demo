package com.searchpath.empathy.controllersTest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.searchpath.empathy.responses.FirstControllerResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.jackson.convert.JacksonConverterRegistrar;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;

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

}
