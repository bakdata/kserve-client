/*
 * MIT License
 *
 * Copyright (c) 2022 bakdata
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public abstract class KFServingClient<I> {

    private static final int RETRY_MAX_ATTEMPTS = Optional.ofNullable(System.getenv("KFSERVING_RETRY_MAX_ATTEMPTS"))
            .map(Integer::parseInt)
            .orElse(10);
    private static final Duration RETRY_INITIAL_INTERVAL =
            Optional.ofNullable(System.getenv("KFSERVING_RETRY_INITIAL_INTERVAL"))
                    .map(Integer::parseInt).map(Duration::ofMillis)
                    .orElse(Duration.ofMillis(500));
    private static final double RETRY_MULTIPLIER = Optional.ofNullable(System.getenv("KFSERVING_RETRY_MULTIPLIER"))
            .map(Double::parseDouble)
            .orElse(2.0);
    private static final Duration RETRY_MAX_INTERVAL =
            Optional.ofNullable(System.getenv("KFSERVING_RETRY_MAX_INTERVAL"))
                    .map(Integer::parseInt).map(Duration::ofMillis)
                    .orElse(Duration.ofMillis(16000));

    protected static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private final String service;
    private final String modelName;
    private final OkHttpClient httpClient;

    @Slf4j
    private static class RetryInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(final Chain chain) throws IOException {
            final Request request = chain.request();

            // wait_interval = min(max_interval, (initial_interval * multiplier^n) +/- (random_interval))
            final IntervalFunction intervalFn = IntervalFunction.ofExponentialRandomBackoff(
                    RETRY_INITIAL_INTERVAL, RETRY_MULTIPLIER, IntervalFunction.DEFAULT_RANDOMIZATION_FACTOR,
                    RETRY_MAX_INTERVAL);

            final RetryConfig retryConfig = RetryConfig.custom()
                    .retryExceptions(IOException.class)  // may be thrown by chain.proceed(request)
                    .maxAttempts(RETRY_MAX_ATTEMPTS)
                    .intervalFunction(intervalFn)
                    .failAfterMaxAttempts(true)
                    .build();
            final Retry retry = Retry.of("kfserving-request-retry", retryConfig);

            final Callable<Response> requestCallable = Retry.decorateCallable(retry, () -> {
                log.debug("Making or retrying request {}.", request);
                return chain.proceed(request);
            });

            try {
                return requestCallable.call();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    protected static OkHttpClient getHttpClient(final Duration requestReadTimeout) {
        return new OkHttpClient.Builder()
                .readTimeout(requestReadTimeout)
                .addInterceptor(new RetryInterceptor())
                .build();
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    private static String getStringBody(final Response response) throws IOException {
        return response.body().string();
    }

    private static <T> Optional<T> processJsonResponse(final String stringBody, final Class<? extends T> responseType) {
        try {
            return Optional.of(OBJECT_MAPPER.readValue(stringBody, responseType));
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Could not process response json", e);
        }
    }

    public <T> Optional<T> makeInferenceRequest(final I inputObject, final Class<? extends T> responseType,
            final String modelNameSuffix)
            throws IOException, InterruptedException {
        final Request httpRequest = getRequest(
                this.getBodyString(inputObject),
                this.getModelURI(this.service, String.format("%s%s", this.modelName, modelNameSuffix)));
        final Response response = this.executeRequest(httpRequest);
        return this.processResponse(response, responseType);
    }

    protected abstract String extractErrorMessage(String stringBody);

    protected final HttpUrl getModelURI(final String service, final String modelName) {
        return HttpUrl.get(this.getUrlString(service, modelName));
    }

    protected abstract String getUrlString(String service, String modelName);

    abstract String getBodyString(final I inputObject);

    private Response executeRequest(final Request httpRequest)
            throws IOException {
        return this.httpClient
                .newCall(httpRequest).execute();
    }

    private <T> Optional<T> processResponse(final Response response, final Class<? extends T> responseType)
            throws IOException {
        switch (response.code()) {
            case HttpURLConnection.HTTP_OK:
                return processJsonResponse(getStringBody(response), responseType);
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_BAD_REQUEST:
                final String errorMessage = this.extractErrorMessage(getStringBody(response));
                throw new InferenceRequestException(errorMessage);
            default:
                log.debug("Unknown response code: {}", response.code());
                return Optional.empty();
        }
    }

    private static Request getRequest(final String bodyString, final HttpUrl url) {
        final MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        final RequestBody requestBody = RequestBody
                .create(bodyString, mediaType);
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    public static final class InferenceRequestException extends IllegalArgumentException {
        private InferenceRequestException(final String message) {
            super("Inference request failed: " + message);
        }
    }
}
