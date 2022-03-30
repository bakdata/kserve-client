package com.bakdata.kserve;

import com.bakdata.kserve.client.KFServingClient.InferenceRequestException;
import com.bakdata.kserve.client.KFServingClientV2;
import com.bakdata.kserve.predictv2.InferenceRequest;
import com.bakdata.kserve.predictv2.Parameters;
import com.bakdata.kserve.predictv2.RequestInput;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class KFServingClientV2Test {
    private KFServingMock mockServer = null;
    @InjectSoftAssertions
    private SoftAssertions softly;

    private static InferenceRequest<String, String> getFakeInferenceRequest(final String data) {
        return InferenceRequest.<String, String>builder()
                .inputs(List.of(RequestInput.<String>builder()
                        .name("tokenize")
                        .data(data)
                        .shape(List.of(1))
                        .datatype("BYTES")
                        .parameters(Parameters.builder()
                                .content_type("str")
                                .build())
                        .build()))
                .build();
    }

    @BeforeEach
    void init() {
        this.mockServer = new KFServingMockV2();
    }

    @Test
    void makeInferenceRequest() throws IOException, InterruptedException {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KFServingClientV2 client = KFServingClientV2.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("test-model")
                .requestReadTimeout(Duration.ofMillis(10000))
                .build();

        this.softly.assertThat(client.makeInferenceRequest(getFakeInferenceRequest("data"),
                FakePrediction.class, ""))
                .map(FakePrediction::getFake)
                .hasValue("data");
    }

    @Test
    void testPredictionNotExistingModel() {
        this.mockServer.setModelEndpoint("test-model", "{ \"fake\": \"data\"}");

        final KFServingClientV2 client = KFServingClientV2.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("fake-model")
                .requestReadTimeout(Duration.ofMillis(10000))
                .build();
        final InferenceRequest<String, String> fakeInferenceRequest = getFakeInferenceRequest("data");
        this.softly.assertThatThrownBy(() -> client.makeInferenceRequest(fakeInferenceRequest, FakePrediction.class, "") )
                .isInstanceOf(InferenceRequestException.class)
                .hasMessage("Inference request failed: Model test-model not found");
    }

    @Test
    void testFallbackNotFoundToDetail() {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) {
                return new MockResponse().setResponseCode(400).setBody("{\n"
                        + "  \"detail\": \"Not Found\"\n"
                        + "}");
            }
        };
        this.mockServer.getMockWebServer().setDispatcher(dispatcher);

        final KFServingClientV2 client = KFServingClientV2.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                .modelName("test-model")
                .requestReadTimeout(Duration.ofMillis(10000))
                .build();

        final InferenceRequest<String, String> fakeInferenceRequest = getFakeInferenceRequest("data");
        this.softly.assertThatThrownBy(() -> client.makeInferenceRequest(fakeInferenceRequest, FakePrediction.class, ""))
                .isInstanceOf(InferenceRequestException.class)
                .hasMessage("Inference request failed: Not Found");
    }

    @Test
    void testRetry() throws IOException, InterruptedException {
        this.mockServer.setUpForRetryTest();

        final KFServingClientV2 client = KFServingClientV2.builder()
                .service(this.mockServer.getWholeServiceEndpoint())
                // Important so that request is aborted and retried
                .requestReadTimeout(Duration.ofMillis(1000))
                .modelName("test-model")
                .build();

        final InferenceRequest<String, String> fakeInferenceRequest = getFakeInferenceRequest("data");
        this.softly.assertThat(client.makeInferenceRequest(fakeInferenceRequest,
                CallCounterFakePrediction.class, ""))
                .hasValueSatisfying(fakePrediction -> this.softly.assertThat(fakePrediction.getCounter()).isEqualTo(2));

        this.softly.assertThat(client.makeInferenceRequest(fakeInferenceRequest,
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
