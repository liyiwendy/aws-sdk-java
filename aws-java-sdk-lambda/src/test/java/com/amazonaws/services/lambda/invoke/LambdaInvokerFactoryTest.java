package com.amazonaws.services.lambda.invoke;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.CustomLambdaSerde;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;

/**
 * Test Lambda invoker with customer serializer
 * https://github.com/aws/aws-sdk-java/issues/1743
 */
public class LambdaInvokerFactoryTest {

    /**
     * Test Lambda invoker with customer serializer and check the lambda input string
     * to be serialized as expected.
     */
    @Test
    public void testInvoke() {
        CustomLambdaSerde serde = new TestSerde();
        TestObject test = new TestObject();
        test.setTestValue("test");
        AWSLambdaClient client = Mockito.mock(AWSLambdaClient.class);
        java.nio.ByteBuffer buffer = Mockito.mock(java.nio.ByteBuffer.class);
        InvokeResult result = Mockito.mock(InvokeResult.class);
        when(client.invoke(any(InvokeRequest.class))).thenReturn(result);
        when(result.getLogResult()).thenReturn(null);
        when(result.getFunctionError()).thenReturn(null);
        when(result.getPayload()).thenReturn(buffer);
        ArgumentCaptor<InvokeRequest> argumentCaptor = ArgumentCaptor.forClass(InvokeRequest.class);

        MyLambda invoker = new LambdaInvokerFactory
                .Builder().customSerde(serde).functionAlias("Alias").functionVersion("1.0")
                .lambdaClient(client).build(MyLambda.class);
        invoker.myFunction(test);

        Mockito.verify(client).invoke(argumentCaptor.capture());
        InvokeRequest requestCapture = argumentCaptor.getValue();
        assertEquals("{\"testValue\":\"test\"}", StandardCharsets.UTF_8.decode(requestCapture.getPayload()).toString());
    }

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

    interface MyLambda {
        @LambdaFunction(functionName = "MyFunction")
        String myFunction(TestObject input);
    }
}
