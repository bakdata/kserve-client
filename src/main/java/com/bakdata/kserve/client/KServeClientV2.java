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

import com.bakdata.kserve.predictv2.InferenceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URL;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * An implementation of a {@link KServeClient} to support the
 * <a href="https://kserve.github.io/website/modelserving/inference_api/">v2 prediction protocol</a>.
 */
@Slf4j
public class KServeClientV2<T> extends KServeClient<InferenceRequest<T>> {

    public static final String DETAIL_FIELD = "detail";
    private static final String ERROR_FIELD = "error";

    @Builder
    KServeClientV2(
            final URL serviceBaseUrl, final String modelName, final OkHttpClient httpClient) {
        super(serviceBaseUrl, modelName, httpClient);
    }

    private static String extractDetailMessage(final JsonNode detail) {
        return detail.isArray() && !detail.isEmpty() ? detail.toString() : detail.asText();
    }

    private static String extractErrorFromParsedBody(final JsonNode root, final String stringBody) {
        final JsonNode errorNode = root.get(ERROR_FIELD);
        if (errorNode != null && !errorNode.isNull()) {
            return errorNode.asText();
        }
        if (root.has(DETAIL_FIELD)) {
            return extractDetailMessage(root.get(DETAIL_FIELD));
        }
        return "Unknown error occurred. Raw body: " + stringBody;
    }

    @Override
    protected String extractErrorMessage(final String stringBody) {
        try {
            final JsonNode root = OBJECT_MAPPER.readTree(stringBody);
            return extractErrorFromParsedBody(root, stringBody);
        } catch (final JsonProcessingException e) {
            log.warn("Could not parse error body as JSON: {}", stringBody, e);
            return stringBody;
        }
    }

    @Override
    protected String getUrlString(final URL serviceBaseUrl, final String modelName) {
        return String.format("%s/v2/models/%s/infer", serviceBaseUrl, modelName);
    }

    @Override
    String getBodyString(final InferenceRequest<T> inputObject) {
        try {
            return OBJECT_MAPPER.writeValueAsString(inputObject);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Could not process inference request body", e);
        }
    }

}
