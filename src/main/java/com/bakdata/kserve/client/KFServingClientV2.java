package com.bakdata.kserve.client;

import com.bakdata.kserve.predictv2.InferenceError;
import com.bakdata.kserve.predictv2.InferenceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.Optional;

@Slf4j
public class KFServingClientV2 extends KFServingClient<InferenceRequest<?, ?>> {

    @Builder
    KFServingClientV2(final String service, final String modelName, final OkHttpClient httpClient) {
        super(service, modelName, httpClient);
    }

    @Override
    protected String extractErrorMessage(final String stringBody) {
        try {
            final InferenceError inferenceError = OBJECT_MAPPER.readValue(stringBody, InferenceError.class);
            return Optional.ofNullable(inferenceError.getError())
                    .or(() -> Optional.ofNullable(inferenceError.getDetail())) // fallback to details
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
    String getBodyString(final InferenceRequest<?, ?> inputObject) {
        try {
            return OBJECT_MAPPER.writeValueAsString(inputObject);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Could not process inference request body", e);
        }
    }

}
