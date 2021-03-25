package com.searchpath.empathy.elastic.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;

import java.io.IOException;

public interface Command {

    default void execute(Object[] args) throws IOException {
        throw new RuntimeException("Interface not implemented");
    }

    default void execute(String line, BulkRequest bulk, ObjectMapper objectMapper) throws IOException {
        throw new RuntimeException("Interface not implemented");
    }

}