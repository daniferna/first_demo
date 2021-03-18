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
        String expectedAggregations = "[{\"genres\":[{\"short\":21071},{\"comedy\":19647},{\"drama\":16065},{\"documentary\":8669},{\"adult\":8398},{\"talk-show\":7703},{\"music\":7695},{\"animation\":5295},{\"reality-tv\":4687},{\"family\":4375}]},{\"types\":[{\"tvepisode\":46545},{\"short\":13943},{\"video\":9787},{\"movie\":7209},{\"tvseries\":2783},{\"tvmovie\":1488},{\"tvminiseries\":446},{\"tvspecial\":402},{\"videogame\":201},{\"tvshort\":127}]}]";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsTermAggregations() throws IOException {
        String expectedAggregations = "[{\"genres\":[{\"drama\":217646},{\"comedy\":115620},{\"documentary\":110763},{\"action\":51316},{\"romance\":45133},{\"thriller\":38846},{\"crime\":35524},{\"horror\":32355},{\"adventure\":28384},{\"short\":21110}]},{\"types\":[{\"movie\":569618},{\"tvepisode\":46545},{\"short\":13943},{\"video\":9787},{\"tvseries\":2783},{\"tvmovie\":1488},{\"tvminiseries\":446},{\"tvspecial\":402},{\"videogame\":201},{\"tvshort\":127}]}]";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSimpleQueryDecadesAggregations() throws IOException {
        String expectedAggregations = "{\"decades\":[{\"1879-1889\":1},{\"1889-1899\":75},{\"1899-1909\":340},{\"1909-1919\":748},{\"1920-1930\":378},{\"1929-1939\":313},{\"1940-1950\":352},{\"1949-1959\":891},{\"1960-1970\":1619},{\"1970-1980\":1764},{\"1980-1990\":2592},{\"1990-2000\":4640},{\"2000-2010\":11817},{\"2010-2020\":47095},{\"2020-2030\":6983}]}";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

    @Test
    public void testSearchWithParamsDecadesAggregations() throws IOException {
        String expectedAggregations = "{\"decades\":[{\"1879-1889\":1},{\"1889-1899\":93},{\"1899-1909\":536},{\"1909-1919\":13722},{\"1920-1930\":21899},{\"1929-1939\":20318},{\"1940-1950\":14806},{\"1949-1959\":22584},{\"1960-1970\":31900},{\"1970-1980\":40578},{\"1980-1990\":44690},{\"1990-2000\":48226},{\"2000-2010\":83426},{\"2010-2020\":200358},{\"2020-2030\":29402}]}";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getDateHistogramAggregation()), expectedAggregations);
    }

}
