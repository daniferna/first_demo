package com.searchpath.empathy.searchTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.elastic.util.impl.ElasticClientUtil;
import com.searchpath.empathy.pojo.QueryResponse;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class SearchSuggestionsTest {

    private final IElasticUtil elasticUtil;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    SearchSuggestionsTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void searchAbengers() throws IOException {
        var response = elasticUtil.searchByParams(Map.of("query", "Abengers"));
        Stream<JsonNode> streamOptionsForIron = getSuggestionsStream(response, 0);

        assertTrue(streamOptionsForIron.anyMatch(p -> p.get("text").asText().equals("avengers")));
    }

    @Test
    public void searchViharsaroc() throws IOException {
        var response = elasticUtil.searchByParams(Map.of("query", "Viharsaroc"));
        Stream<JsonNode> streamOptionsForIron = getSuggestionsStream(response, 0);

        assertTrue(streamOptionsForIron.anyMatch(p -> p.get("text").asText().equals("viharsarok")));
    }

    /**
     * Helper method, it receives the search response and the position of the query word you want to get the suggestions
     * and returns an Stream of JsonNodes containing that suggestions.
     */
    private Stream<JsonNode> getSuggestionsStream(QueryResponse response, int indexWordQuery) {
        var suggestions = response.getSuggestion();

        var options = suggestions.get(ElasticClientUtil.TITLE_TERM_SUGGESTION_NAME)
                .get(indexWordQuery).get("options").elements();
        return StreamSupport.stream(Spliterators
                .spliteratorUnknownSize(options, Spliterator.ORDERED), false);
    }

}
