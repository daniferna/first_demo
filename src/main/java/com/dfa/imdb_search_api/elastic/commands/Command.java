package com.dfa.imdb_search_api.elastic.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;

import java.io.IOException;

public interface Command {

    default void execute(String line, BulkRequest bulk, ObjectMapper objectMapper) throws IOException {
        throw new RuntimeException("Interface not implemented");
    }

}