package com.searchpath.empathy.searchTest;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.QueryResponse;
import com.searchpath.empathy.pojo.aggregations.Aggregation;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public void testSearchFilterByType() throws IOException {
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

        assertTrue(Arrays.stream(responseWithFilter.getItems()).allMatch(f -> f.getType().equals("movie")));
    }

    @Test
    public void testSearchFilterByGenre() throws IOException {
        var responseWithFilter = elasticUtil.searchByParams(Map.of("query", "hulk",
                "filters", "genres:sci-fi"));
        var responseWithoutFilter = elasticUtil.search("hulk");

        var typeAggWithoutFilter = getAggregation(responseWithoutFilter, "types");
        var typeAggWithFilter = getAggregation(responseWithFilter, "types");
        var genresAggWithFiler = getAggregation(responseWithFilter, "genres");
        var genresAggWithoutFilter = getAggregation(responseWithoutFilter, "genres");
        var decadesAggWithoutFilter = getAggregation(responseWithoutFilter, "decades");
        var decadesAggWithFilter = getAggregation(responseWithFilter, "decades");

        assertNotEquals(typeAggWithoutFilter, typeAggWithFilter);
        assertEquals(genresAggWithFiler, genresAggWithoutFilter);
        assertNotEquals(decadesAggWithFilter, decadesAggWithoutFilter);

        assertNotEquals(responseWithFilter.getItems(), responseWithoutFilter.getItems());

        assertTrue(Arrays.stream(responseWithFilter.getItems()).allMatch(
                f -> Arrays.asList(f.getGenres()).contains("Sci-Fi")));
    }

    @Test
    public void testSearchFilterByDate() throws IOException {
        var responseWithFilter = elasticUtil.searchByParams(Map.of("query", "hulk",
                "filters", "date:2000-2008"));
        var responseWithoutFilter = elasticUtil.search("hulk");

        var typeAggWithoutFilter = getAggregation(responseWithoutFilter, "types");
        var typeAggWithFilter = getAggregation(responseWithFilter, "types");
        var genresAggWithFiler = getAggregation(responseWithFilter, "genres");
        var genresAggWithoutFilter = getAggregation(responseWithoutFilter, "genres");
        var decadesAggWithoutFilter = getAggregation(responseWithoutFilter, "decades");
        var decadesAggWithFilter = getAggregation(responseWithFilter, "decades");

        assertNotEquals(typeAggWithoutFilter, typeAggWithFilter);
        assertNotEquals(genresAggWithFiler, genresAggWithoutFilter);
        assertEquals(decadesAggWithFilter, decadesAggWithoutFilter);

        assertNotEquals(responseWithFilter.getItems(), responseWithoutFilter.getItems());

        assertTrue(Arrays.stream(responseWithFilter.getItems()).allMatch(f -> {
            var filmDate = LocalDate.parse(f.getStart_year(), DateTimeFormatter.ISO_DATE);
            var after = filmDate.isAfter(LocalDate.of(1999, 12, 31));
            var before = filmDate.isBefore(LocalDate.of(2008, 1, 2));
            return after && before;
        }));

    }

    private static Optional<Aggregation<?>> getAggregation(QueryResponse response, String aggName) {
        return Arrays.stream(response
                .getAggregations()).filter(ag -> ag.getName().equals(aggName)).findFirst();
    }

}
