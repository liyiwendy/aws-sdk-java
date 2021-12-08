package com.amazonaws.services.lambda.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test for customer lambda serde.
 *
 * https://github.com/aws/aws-sdk-java/issues/1743
 */
public class CustomLambdaSerdeTest {

    @Before
    public void setup(){
        serde = new TestSerde();
    }

    /**
     * Test serialization.
     */
    @Test
    public void testWriteValueAsString() throws JsonProcessingException {
        TestObject test = new TestObject();
        test.setTestValue("test");
        assertEquals("{\"testValue\":\"test\"}", serde.writeValueAsString(test));
    }

    /**
     * Test Deserialization
     */
    @Test
    public void testReadValue() throws IOException {
        TestObject object = serde.readValue("{\"testValue\":\"test\"}", TestObject.class);
        assertEquals("test", object.getTestValue());
    }

    private CustomLambdaSerde serde;

    private static class TestSerde implements CustomLambdaSerde {

        @Override
        public String writeValueAsString(Object value) throws JsonProcessingException {
            return mapper.writeValueAsString(value);
        }

        @Override
        public <T> T readValue(String src, Class<T> valueType) throws IOException {
            return mapper.readValue(src, valueType);
        }

        final ObjectMapper mapper = new ObjectMapper();
    }

    public static class TestObject {

        private String testValue;

        public String getTestValue() {
            return testValue;
        }

        public void setTestValue(String input) {
            testValue = input;
        }
    }

}
