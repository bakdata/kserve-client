package com.bakdata.kserve;

import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;


@Getter
@NoArgsConstructor
public class KFServingMockV2 extends KFServingMock {
    @Override
    MockResponse getModelNotFoundResponse(final String modelName) {
        return new MockResponse().setResponseCode(404).setBody(String.format(
                "{\n"
                        + "  \"error\": \"Model %s not found\"\n"
                        + "}",
                modelName));
    }

    @Override
    String getEndpointString(final String modelName) {
        return String.format("/v2/models/%s/infer", modelName);
    }
}
