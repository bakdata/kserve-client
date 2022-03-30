package com.bakdata.kserve;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

public abstract class KFServingMock {
    protected final MockWebServer mockWebServer;

    protected KFServingMock() {
        this.mockWebServer = new MockWebServer();
    }

    abstract MockResponse getModelNotFoundResponse(String modelName);

    public String getWholeServiceEndpoint() {
        return this.mockWebServer.getHostName() + ":" + this.mockWebServer.getPort();
    }

    public String getBaseEndpoint() {
        return ":" + this.mockWebServer.getPort();
    }

    public String getServiceName() {
        return this.mockWebServer.getHostName();
    }

    public void setModelEndpoint(final String modelName, final String body) {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) {
                if (KFServingMock.this.getEndpointString(modelName).equals(recordedRequest.getPath())) {
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(body)
                            .setResponseCode(200);
                } else {
                    recordedRequest.getPath();
                    return KFServingMock.this.getModelNotFoundResponse(modelName);
                }
            }
        };
        this.mockWebServer.setDispatcher(dispatcher);
    }

    public void setUpForRetryTest() {
        final int[] callCounter = {0};
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) throws InterruptedException {
                callCounter[0]++;
                if (callCounter[0] == 1) {
                    // Force request abortion because of 1s read timeout
                    Thread.sleep(2000);
                    return new MockResponse().setResponseCode(400).setBody(
                            "<html>\n<title>400: request should be aborted before responding</title>\n\n"
                                    + "<body>400</body>\n\n</html>");
                }
                return new MockResponse().setResponseCode(200).setBody("{ \"counter\": " + callCounter[0] + "}");
            }
        };
        this.mockWebServer.setDispatcher(dispatcher);
    }

    abstract String getEndpointString(final String modelName);

    public okhttp3.mockwebserver.MockWebServer getMockWebServer() {
        return this.mockWebServer;
    }
}
