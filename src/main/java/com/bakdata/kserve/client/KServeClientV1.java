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

package com.bakdata.kserve.client;

import java.net.URL;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * An implementation of a {@link KServeClient} to support the v1 prediction protocol.
 */
@Slf4j
public class KServeClientV1 extends KServeClient<JSONObject> {
    @Builder
    KServeClientV1(
            final URL serviceBaseUrl, final String modelName, final OkHttpClient httpClient) {
        super(serviceBaseUrl, modelName, httpClient);
    }

    @Override
    protected String extractErrorMessage(final String stringBody) {
        final Document htmlResponse = Jsoup.parse(stringBody);
        return htmlResponse.select("title").first().text();
    }

    @Override
    protected String getUrlString(final URL serviceBaseUrl, final String modelName) {
        return String.format("%s/v1/models/%s:predict", serviceBaseUrl, modelName);
    }

    @Override
    String getBodyString(final JSONObject inputObject) {
        return inputObject.toString();
    }

}
