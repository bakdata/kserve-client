/*
 * MIT License
 *
 * Copyright (c) 2026 bakdata
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.jetbrains.annotations.NotNull;

public abstract class KServeMock implements AutoCloseable {
    @Getter
    private final MockWebServer mockWebServer = new MockWebServer();

    public KServeMock start() {
        try {
            this.mockWebServer.start();
            return this;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.mockWebServer.close();
    }

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
                if (KServeMock.this.getEndpointString(modelName).equals(recordedRequest.getTarget())) {
                    return new MockResponse.Builder()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .body(body)
                            .code(200)
                            .build();
                } else {
                    recordedRequest.getTarget();
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
                    return new MockResponse.Builder()
                            .code(400)
                            .body("""
                                    <html>
                                    <title>400: request should be aborted before responding</title>
                                    
                                    <body>400</body>
                                    
                                    </html>""")
                            .build();
                }
                return new MockResponse.Builder()
                        .code(200)
                        .body("{ \"counter\": " + callCounter + "}")
                        .build();
            }
        };
        this.mockWebServer.setDispatcher(dispatcher);
    }

    abstract String getEndpointString(final String modelName);
}
