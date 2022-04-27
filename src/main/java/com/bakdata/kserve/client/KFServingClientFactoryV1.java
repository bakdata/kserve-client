package com.bakdata.kserve.client;

import okhttp3.OkHttpClient;
import org.json.JSONObject;

import java.time.Duration;

public class KFServingClientFactoryV1 implements KFServingClientFactory {
    @Override
    public KFServingClient<JSONObject> getKFServingClient(
            final String service, final String modelName, final Duration requestReadTimeout) {
        OkHttpClient httpClient = KFServingClientV1.getHttpClient(requestReadTimeout);
        return new KFServingClientV1(service, modelName, httpClient);
    }
}
