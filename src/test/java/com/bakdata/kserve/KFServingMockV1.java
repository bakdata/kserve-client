package com.bakdata.kserve;

import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;


@Getter
@NoArgsConstructor
public class KFServingMockV1 extends KFServingMock {
    @Override
    MockResponse getModelNotFoundResponse(final String modelName) {
        return new MockResponse().setResponseCode(404).setBody(String.format(
                "<html>\n<title>404: Model with name model does not exist.</title>\n\n<body>404: Model with name "
                        + "model does not exist.</body>\n\n</html>",
                modelName, modelName));
    }

    @Override
    String getEndpointString(final String modelName) {
        return String.format("/v1/models/%s:predict", modelName);
    }
}
