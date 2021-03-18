package com.searchpath.empathy.pojo.aggregations.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.searchpath.empathy.pojo.aggregations.Aggregation;

import java.io.IOException;

public class AggregationSerializer extends StdSerializer<Aggregation<?>>
{
    public AggregationSerializer() {
        this(null);
    }

    protected AggregationSerializer(Class<Aggregation<?>> t) {
        super(t);
    }

    @Override
    public void serialize(Aggregation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField(value.getName(), value.getBuckets());
        gen.writeEndObject();
    }
}
