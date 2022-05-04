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

import com.bakdata.kserve.predictv2.InferenceError;
import com.bakdata.kserve.predictv2.InferenceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.Optional;

/**
 * An implementation of a {@link KServeClient} to support the
 * <a href="https://kserve.github.io/website/modelserving/inference_api/">v2 prediction protocol</a>.
 */
@Slf4j
public class KServeClientV2 extends KServeClient<InferenceRequest<?>> {

    @Builder
    KServeClientV2(final String service, final String modelName, final OkHttpClient httpClient) {
        super(service, modelName, httpClient);
    }

    @Override
    protected String extractErrorMessage(final String stringBody) {
        try {
            final InferenceError inferenceError = OBJECT_MAPPER.readValue(stringBody, InferenceError.class);
            return Optional.ofNullable(inferenceError.getError())
                    // fallback to details
                    .or(() -> Optional.ofNullable(inferenceError.getDetail()))
                    .orElseThrow();
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Could not process JSON error object", e);
        }
    }

    @Override
    protected String getUrlString(final String service, final String modelName) {
        return String.format("http://%s/v2/models/%s/infer", service, modelName);
    }

    @Override
    String getBodyString(final InferenceRequest<?> inputObject) {
        try {
            return OBJECT_MAPPER.writeValueAsString(inputObject);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Could not process inference request body", e);
        }
    }

}
