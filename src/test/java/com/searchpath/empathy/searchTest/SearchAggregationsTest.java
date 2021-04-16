package com.searchpath.empathy.searchTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class SearchAggregationsTest {

    @Inject
    ObjectMapper objectMapper;

    private final IElasticUtil elasticUtil;

    @Inject
    SearchAggregationsTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void testSimpleQueryAggregations() throws IOException {
        var response = elasticUtil.search("Avengers");

        var genresAggregation = response.findTermAggregation("genres");
        var typesAggregation = response.findTermAggregation("types");
        var decadesAggregation = response.getDateHistogramAggregation();

        assertEquals(25, genresAggregation.getBuckets().length);
        assertEquals(9, typesAggregation.getBuckets().length);
        assertEquals(9, decadesAggregation.getBuckets().length);

        assertTrue(Arrays.stream(genresAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("action") && b.getCount() == 107));
        assertTrue(Arrays.stream(typesAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("movie") && b.getCount() == 27));
        assertTrue(Arrays.stream(decadesAggregation.getBuckets())
                .anyMatch(b -> b.getDecade().equals("2011-2020") && b.getCount() == 844));
    }

    @Test
    public void testSearchWithParamsAggregations() throws IOException {
        var response = elasticUtil.searchByParams(Map.of("query", "Avengers",
                "genres", "action,adventure", "type", "movie", "date", "1995-2020"));

        var genresAggregation = response.findTermAggregation("genres");
        var typesAggregation = response.findTermAggregation("types");
        var decadesAggregation = response.getDateHistogramAggregation();

        assertEquals(5, genresAggregation.getBuckets().length);
        assertEquals(1, typesAggregation.getBuckets().length);
        assertEquals(2, decadesAggregation.getBuckets().length);

        assertTrue(Arrays.stream(genresAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("sci-fi") && b.getCount() == 4));
        assertTrue(Arrays.stream(typesAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("movie") && b.getCount() == 6));
        assertTrue(Arrays.stream(decadesAggregation.getBuckets())
                .anyMatch(b -> b.getDecade().equals("2011-2020") && b.getCount() == 5));
    }

    @Test
    public void testSearchWithPostFiltersAggregations() throws IOException {
        var response = elasticUtil.searchByParams(Map.of("query", "Avengers",
                "filters", "type:movie,genres:action"));

        var genresAggregation = response.findTermAggregation("genres");
        var typesAggregation = response.findTermAggregation("types");
        var decadesAggregation = response.getDateHistogramAggregation();

        assertEquals(10, genresAggregation.getBuckets().length);
        assertEquals(7, typesAggregation.getBuckets().length);
        assertEquals(6, decadesAggregation.getBuckets().length);

        assertTrue(Arrays.stream(genresAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("sci-fi") && b.getCount() == 6));
        assertTrue(Arrays.stream(typesAggregation.getBuckets())
                .anyMatch(b -> b.getName().equals("short") && b.getCount() == 12));
        assertTrue(Arrays.stream(decadesAggregation.getBuckets())
                .anyMatch(b -> b.getDecade().equals("2001-2010") && b.getCount() == 1));
    }

}
