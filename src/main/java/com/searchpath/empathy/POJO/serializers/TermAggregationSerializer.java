package com.searchpath.empathy.pojo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.searchpath.empathy.pojo.TermAggregationPojo;

import java.io.IOException;

public class TermAggregationSerializer extends StdSerializer<TermAggregationPojo>
{
    public TermAggregationSerializer() {
        this(null);
    }

    protected TermAggregationSerializer(Class<TermAggregationPojo> t) {
        super(t);
    }

    @Override
    public void serialize(TermAggregationPojo value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField(value.getName(), value.getTerms());
        gen.writeEndObject();
    }
}
