/*
 * MIT License
 *
 * Copyright (c) 2025 bakdata
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bakdata.kserve.client;

import com.bakdata.kserve.client.KServeClient.InferenceRequestException;
import java.io.IOException;
import java.time.Duration;
import lombok.Getter;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class KServeClientV1Test {
    private com.bakdata.kserve.KServeMock mockServer = null;
    @InjectSoftAssertions
    private SoftAssertions softly;

    @BeforeEach
    void init() {
        this.mockServer = new com.bakdata.kserve.KServeMockV1();
    }

    @Test
    void makeInferenceRequest() throws IOException {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KServeClientV1 client = KServeClientV1.builder()
                .serviceBaseUrl(this.mockServer.getServiceBaseUrl())
                .modelName("test-model")
                .httpClient(KServeClient.getHttpClient(Duration.ofMillis(10000)))
                .build();

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                        FakePrediction.class, ""))
                .satisfies(
                        fakePrediction -> this.softly.assertThat(fakePrediction.getFake()).isEqualTo("data"));
    }

    @Test
    void testPredictionNotExistingModel() {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KServeClientV1 client = KServeClientV1.builder()
                .serviceBaseUrl(this.mockServer.getServiceBaseUrl())
                .modelName("fake-model")
                .httpClient(KServeClient.getHttpClient(Duration.ofMillis(10000)))
                .build();

        this.softly.assertThatThrownBy(() -> client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                        FakePrediction.class, ""))
                .isInstanceOf(InferenceRequestException.class)
                .hasMessage("Inference request failed: 404: Model with name model does not exist.");
    }

    @Test
    void testMalformedInputException() {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) {
                return new MockResponse().setResponseCode(400).setBody("<html>\n"
                        + "<title>400: Unrecognized request format: Expecting ',' delimiter: line 3 column 1 (char "
                        + "48)</title>\n"
                        + "\n"
                        + "<body>400: Unrecognized request format: Expecting ',' delimiter: line 3 column 1 (char 48)"
                        + "</body>\n"
                        + "\n"
                        + "</html>");
            }
        };
        this.mockServer.getMockWebServer().setDispatcher(dispatcher);

        final KServeClientV1 client = KServeClientV1.builder()
                .serviceBaseUrl(this.mockServer.getServiceBaseUrl())
                .modelName("test-model")
                .httpClient(KServeClient.getHttpClient(Duration.ofMillis(10000)))
                .build();

        this.softly.assertThatThrownBy(() -> client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                        FakePrediction.class, ""))
                .isInstanceOf(KServeClient.InferenceRequestException.class)
                .hasMessage(
                        "Inference request failed: 400: Unrecognized request format: Expecting ',' delimiter: line 3 "
                                + "column 1 (char 48)");
    }

    @Test
    void testRetry() throws IOException {
        this.mockServer.setUpForRetryTest();

        final KServeClientV1 client = KServeClientV1.builder()
                .serviceBaseUrl(this.mockServer.getServiceBaseUrl())
                // Important so that request is aborted and retried
                .httpClient(KServeClient.getHttpClient(Duration.ofMillis(1000)))
                .modelName("test-model")
                .build();

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                        CallCounterFakePrediction.class, ""))
                .satisfies(fakePrediction -> this.softly.assertThat(fakePrediction.getCounter()).isEqualTo(2));

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                        CallCounterFakePrediction.class, ""))
                .satisfies(fakePrediction -> this.softly.assertThat(fakePrediction.getCounter()).isEqualTo(3));
    }

    @Getter
    private static class FakePrediction {
        private String fake;
    }

    @Getter
    private static class CallCounterFakePrediction {
        private int counter;
    }
}
