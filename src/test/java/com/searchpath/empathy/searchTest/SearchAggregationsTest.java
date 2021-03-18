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
    public void testSimpleQuery() throws IOException {
        String expectedAggregations = "[{\"genres\":[{\"short\":21071},{\"comedy\":19647},{\"drama\":16065},{\"documentary\":8669},{\"adult\":8398},{\"talk-show\":7703},{\"music\":7695},{\"animation\":5295},{\"reality-tv\":4687},{\"family\":4375}]},{\"types\":[{\"tvepisode\":46545},{\"short\":13943},{\"video\":9787},{\"movie\":7209},{\"tvseries\":2783},{\"tvmovie\":1488},{\"tvminiseries\":446},{\"tvspecial\":402},{\"videogame\":201},{\"tvshort\":127}]}]";

        var response = elasticUtil.search("Call me by your name");
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

    @Test
    public void testSearchWithParams() throws IOException {
        String expectedAggregations = "[{\"genres\":[{\"drama\":217646},{\"comedy\":115620},{\"documentary\":110763},{\"action\":51316},{\"romance\":45133},{\"thriller\":38846},{\"crime\":35524},{\"horror\":32355},{\"adventure\":28384},{\"short\":21110}]},{\"types\":[{\"movie\":569618},{\"tvepisode\":46545},{\"short\":13943},{\"video\":9787},{\"tvseries\":2783},{\"tvmovie\":1488},{\"tvminiseries\":446},{\"tvspecial\":402},{\"videogame\":201},{\"tvshort\":127}]}]";

        var response = elasticUtil.searchByParams(new String[] {"Call me by your name",
                "drama,romance", "movie"});
        assertEquals(objectMapper.writeValueAsString(response.getTermAggregations()), expectedAggregations);
    }

}
