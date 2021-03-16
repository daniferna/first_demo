package com.searchpath.empathy.pojo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.searchpath.empathy.pojo.TermBucketPojo;

import java.io.IOException;

public class TermBucketSerializer extends StdSerializer<TermBucketPojo> {

    public TermBucketSerializer() {
        this(null);
    }

    protected TermBucketSerializer(Class<TermBucketPojo> t) {
        super(t);
    }

    @Override
    public void serialize(TermBucketPojo value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField(value.getName(), value.getCount());
        gen.writeEndObject();
    }
}
