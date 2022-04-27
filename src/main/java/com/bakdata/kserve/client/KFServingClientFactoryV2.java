package com.bakdata.kserve.client;

import com.bakdata.kserve.predictv2.InferenceRequest;
import okhttp3.OkHttpClient;

import java.time.Duration;

public class KFServingClientFactoryV2 implements KFServingClientFactory {
    @Override
    public KFServingClient<InferenceRequest<?, ?>> getKFServingClient(
            final String service, final String modelName, final Duration requestReadTimeout) {
        OkHttpClient httpClient = KFServingClientV2.getHttpClient(requestReadTimeout);
        return new KFServingClientV2(service, modelName, httpClient);
    }
}
