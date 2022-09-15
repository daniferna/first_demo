package com.dfa.imdb_search_api.POJO.serializers;

import com.dfa.imdb_search_api.POJO.aggregations.Aggregation;
import com.dfa.imdb_search_api.POJO.aggregations.bucket.IBucket;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class AggregationSerializer extends StdSerializer<Aggregation<IBucket>> {
    public AggregationSerializer() {
        this(null);
    }

    protected AggregationSerializer(Class<Aggregation<IBucket>> t) {
        super(t);
    }

    @Override
    public void serialize(Aggregation<IBucket> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeRaw("\"" + value.getName() + "\"" + ": {");
        for (IBucket bucket : value.getBuckets())
            gen.writeObjectField(bucket.getName(), bucket.getCount());
        gen.writeRaw("}");
        gen.writeEndObject();
    }
}
