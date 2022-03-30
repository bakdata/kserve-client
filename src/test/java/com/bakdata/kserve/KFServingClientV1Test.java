package com.bakdata.kserve;

import com.bakdata.kserve.client.KFServingClient.InferenceRequestException;
import com.bakdata.kserve.client.KFServingClientV1;
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
class KFServingClientV1Test {
    private com.bakdata.kserve.KFServingMock mockServer = null;
    @InjectSoftAssertions
    private SoftAssertions softly;

    @BeforeEach
    void init() {
        this.mockServer = new com.bakdata.kserve.KFServingMockV1();
    }

    @Test
    void makeInferenceRequest() throws IOException, InterruptedException {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KFServingClientV1 client = KFServingClientV1.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("test-model")
                .requestReadTimeout(Duration.ofMillis(10000))
                .build();

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                FakePrediction.class, ""))
                .hasValueSatisfying(fakePrediction -> this.softly.assertThat(fakePrediction.getFake()).isEqualTo("data"));
    }

    @Test
    void testPredictionNotExistingModel() {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KFServingClientV1 client = KFServingClientV1.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("fake-model")
                .requestReadTimeout(Duration.ofMillis(10000))
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

        final KFServingClientV1 client = KFServingClientV1.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("test-model")
                .requestReadTimeout(Duration.ofMillis(10000))
                .build();

        this.softly.assertThatThrownBy(() -> client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                FakePrediction.class, ""))
                .isInstanceOf(KFServingClientV1.InferenceRequestException.class)
                .hasMessage("Inference request failed: 400: Unrecognized request format: Expecting ',' delimiter: line 3 column 1 (char 48)");
    }

    @Test
    void testRetry() throws IOException, InterruptedException {
        this.mockServer.setUpForRetryTest();

        final KFServingClientV1 client = KFServingClientV1.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                // Important so that request is aborted and retried
                .requestReadTimeout(Duration.ofMillis(1000))
                .modelName("test-model")
                .build();

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                CallCounterFakePrediction.class, ""))
                .hasValueSatisfying(fakePrediction -> this.softly.assertThat(fakePrediction.getCounter()).isEqualTo(2));

        this.softly.assertThat(client.makeInferenceRequest(new JSONObject("{ \"input\": \"data\" }"),
                CallCounterFakePrediction.class, ""))
                .hasValueSatisfying(fakePrediction -> this.softly.assertThat(fakePrediction.getCounter()).isEqualTo(3));
    }

    @Getter
    public static class FakePrediction {
        private String fake;
    }

    @Getter
    public static class CallCounterFakePrediction {
        private int counter;
    }
}
