/*
 * MIT License
 *
 * Copyright (c) 2024 bakdata
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

package com.bakdata.kserve;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

public abstract class KServeMock {
    @Getter
    private final MockWebServer mockWebServer = new MockWebServer();

    abstract MockResponse getModelNotFoundResponse(String modelName);

    /**
     * Get the base endpoint of the mock.
     *
     * @return A base endpoint
     * @deprecated This method is deprecated in favor of {@link #getServiceBaseUrl()}.
     */
    @Deprecated(since = "2.0.0")
    public String getBaseEndpoint() {
        return ":" + this.mockWebServer.getPort();
    }

    /**
     * Get the service name of the mock.
     *
     * @return A service name
     * @deprecated This method is deprecated in favor of {@link #getServiceBaseUrl()}.
     */
    @Deprecated(since = "2.0.0")
    public String getServiceName() {
        return this.mockWebServer.getHostName();
    }

    public URL getServiceBaseUrl() {
        try {
            return new URL(
                    String.format("http://%s:%s", this.mockWebServer.getHostName(), this.mockWebServer.getPort()));
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setModelEndpoint(final String modelName, final String body) {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) {
                if (KServeMock.this.getEndpointString(modelName).equals(recordedRequest.getPath())) {
                    return new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .setBody(body)
                            .setResponseCode(200);
                } else {
                    recordedRequest.getPath();
                    return KServeMock.this.getModelNotFoundResponse(modelName);
                }
            }
        };
        this.mockWebServer.setDispatcher(dispatcher);
    }

    public void setUpForRetryTest() {
        final AtomicInteger callCounter = new AtomicInteger();
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest recordedRequest) throws InterruptedException {
                if (callCounter.getAndIncrement() == 0) {
                    // Force request abortion because of 1s read timeout
                    Thread.sleep(2000);
                    return new MockResponse().setResponseCode(400).setBody(
                            "<html>\n<title>400: request should be aborted before responding</title>\n\n"
                                    + "<body>400</body>\n\n</html>");
                }
                return new MockResponse().setResponseCode(200).setBody("{ \"counter\": " + callCounter + "}");
            }
        };
        this.mockWebServer.setDispatcher(dispatcher);
    }

    abstract String getEndpointString(final String modelName);
}
