package com.searchpath.empathy.searchTest;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import com.searchpath.empathy.pojo.QueryResponse;
import com.searchpath.empathy.pojo.aggregations.Aggregation;
import com.searchpath.empathy.pojo.aggregations.bucket.impl.TermBucket;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SearchFiltersTest {

    private final IElasticUtil elasticUtil;

    @Inject
    SearchFiltersTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void testSearchFilterByGenre() throws IOException {
        var responseWithFilter = elasticUtil.searchByParams(Map.of("query", "hulk",
                "filters", "type:movie"));
        var responseWithoutFilter = elasticUtil.search("hulk");

        var typeAggWithoutFilter = getAggregation(responseWithoutFilter, "types");
        var typeAggWithFilter = getAggregation(responseWithFilter, "types");
        var genresAggWithFiler = getAggregation(responseWithFilter, "genres");
        var genresAggWithoutFilter = getAggregation(responseWithoutFilter, "genres");
        var decadesAggWithoutFilter = getAggregation(responseWithoutFilter, "decades");
        var decadesAggWithFilter = getAggregation(responseWithFilter, "decades");

        assertEquals(typeAggWithoutFilter, typeAggWithFilter);
        assertNotEquals(genresAggWithFiler, genresAggWithoutFilter);
        assertNotEquals(decadesAggWithFilter, decadesAggWithoutFilter);

        assertNotEquals(responseWithFilter.getItems(), responseWithoutFilter.getItems());

    }

    private static Optional<Aggregation<?>> getAggregation(QueryResponse response, String aggName) {
        return Arrays.stream(response
                .getAggregations()).filter(ag -> ag.getName().equals(aggName)).findFirst();
    }

}
