package com.bakdata.kserve.client;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public class KFServingClientV1 extends KFServingClient<JSONObject> {
    @Builder
    KFServingClientV1(final String service, final String modelName, final OkHttpClient httpClient) {
        super(service, modelName, httpClient);
    }

    @Override
    protected String extractErrorMessage(final String stringBody) {
        final Document htmlResponse = Jsoup.parse(stringBody);
        return htmlResponse.select("title").first().text();
    }

    @Override
    protected String getUrlString(final String service, final String modelName) {
        return String.format("http://%s/v1/models/%s:predict", service, modelName);
    }

    @Override
    String getBodyString(final JSONObject inputObject) {
        return inputObject.toString();
    }

}
