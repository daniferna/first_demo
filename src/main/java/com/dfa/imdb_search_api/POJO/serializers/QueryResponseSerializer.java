package com.dfa.imdb_search_api.POJO.serializers;

import com.dfa.imdb_search_api.POJO.QueryResponse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class QueryResponseSerializer extends StdSerializer<QueryResponse> {

    public QueryResponseSerializer() {
        this(null);
    }

    protected QueryResponseSerializer(Class<QueryResponse> t) {
        super(t);
    }

    @Override
    public void serialize(QueryResponse value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        gen.writeNumberField("total", value.getTotal());
        gen.writeObjectField("items", value.getItems());
        gen.writeObjectField("aggregations", value.getAggregations());
        gen.writeObjectField("suggestions", value.getSuggestion());
        gen.writeEndObject();

    }
}
