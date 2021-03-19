package com.searchpath.empathy.pojo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.searchpath.empathy.pojo.QueryResponse;

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
        gen.writeObjectField("items",value.getItems());
        gen.writeObjectField("aggregations", value.getAggregations());
        gen.writeEndObject();

    }
}
