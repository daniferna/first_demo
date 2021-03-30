package com.searchpath.empathy.searchTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void testSimpleQueryTermAggregations() throws IOException {
        String expectedAggregations = "[{\"genres\": {\"short\":21071,\"comedy\":19647,\"drama\":16065,\"documentary\":8669,\"adult\":8398,\"talk-show\":7703,\"music\":7695,\"animation\":5295,\"reality-tv\":4687,\"family\":4375}},{\"types\": {\"tvepisode\":46545,\"short\":13943,\"video\":9787,\"movie\":7209,\"tvseries\":2783,\"tvmovie\":1488,\"tvminiseries\":446,\"tvspecial\":402,\"videogame\":201,\"tvshort\":127}}]";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsTermAggregations() throws IOException {
        String expectedAggregations = "[{\"genres\": {\"drama\":378,\"romance\":378,\"comedy\":106,\"crime\":15,\"action\":10,\"thriller\":9,\"fantasy\":7,\"music\":7,\"mystery\":7,\"family\":6}},{\"types\": {\"movie\":378}}]";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSimpleQueryDecadesAggregations() throws IOException {
        String expectedAggregations = "{\"decades\": {\"1880-1889\":1,\"1890-1899\":75,\"1900-1909\":340,\"1910-1919\":811,\"1921-1930\":315,\"1930-1939\":341,\"1941-1950\":324,\"1950-1959\":999,\"1961-1970\":1686,\"1971-1980\":1739,\"1981-1990\":2767,\"1991-2000\":4970,\"2001-2010\":13737,\"2011-2020\":50503,\"2021-2030\":1000}}";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsDecadesAggregations() throws IOException {
        String expectedAggregations = "{\"decades\": {\"1921-1930\":5,\"1930-1939\":16,\"1941-1950\":6,\"1950-1959\":14,\"1961-1970\":24,\"1971-1980\":18,\"1981-1990\":21,\"1991-2000\":39,\"2001-2010\":81,\"2011-2020\":125,\"2021-2030\":5}}";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

}
