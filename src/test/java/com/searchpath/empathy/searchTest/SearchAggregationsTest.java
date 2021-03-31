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
        String expectedAggregations = "[{\"genres\": {\"short\":21142,\"comedy\":19735,\"drama\":16204,\"documentary\":8737,\"adult\":8404,\"talk-show\":7704,\"music\":7702,\"animation\":5303,\"reality-tv\":4692,\"family\":4389}},{\"types\": {\"tvepisode\":46553,\"short\":14005,\"video\":9809,\"movie\":7418,\"tvseries\":2803,\"tvmovie\":1514,\"tvminiseries\":449,\"tvspecial\":415,\"videogame\":204,\"tvshort\":129}}]";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsTermAggregations() throws IOException {
        String expectedAggregations = "[{\"genres\": {\"drama\":405,\"romance\":405,\"comedy\":113,\"crime\":15,\"action\":10,\"thriller\":9,\"war\":8,\"fantasy\":7,\"music\":7,\"musical\":7}},{\"types\": {\"movie\":405}}]";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSimpleQueryDecadesAggregations() throws IOException {
        String expectedAggregations = "\"decades\": {\"1880-1889\":1,\"1890-1899\":75,\"1900-1909\":348,\"1910-1919\":814,\"1921-1930\":315,\"1930-1939\":346,\"1941-1950\":329,\"1950-1959\":1027,\"1961-1970\":1711,\"1971-1980\":1759,\"1981-1990\":2792,\"1991-2000\":5008,\"2001-2010\":13803,\"2011-2020\":50644,\"2021-2030\":1000}}";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsDecadesAggregations() throws IOException {
        String expectedAggregations = "{\"decades\": {\"1921-1930\":5,\"1930-1939\":17,\"1941-1950\":8,\"1950-1959\":19,\"1961-1970\":31,\"1971-1980\":18,\"1981-1990\":25,\"1991-2000\":42,\"2001-2010\":82,\"2011-2020\":129,\"2021-2030\":5}}";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

}
