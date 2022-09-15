package com.dfa.imdb_search_api.POJO.serializers;

import com.dfa.imdb_search_api.POJO.aggregations.bucket.IBucket;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class BucketSerializer extends StdSerializer<IBucket> {

    public BucketSerializer() {
        this(null);
    }

    protected BucketSerializer(Class<IBucket> t) {
        super(t);
    }

    @Override
    public void serialize(IBucket value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField(value.getName(), value.getCount());
        gen.writeEndObject();
    }
}
