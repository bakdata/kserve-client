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

import com.bakdata.kserve.predictv2.InferenceRequest;
import okhttp3.OkHttpClient;

import java.time.Duration;

/**
 * A factory producing a {@link KServeClient} to support the
 * <a href="https://kserve.github.io/website/modelserving/inference_api/">v2 prediction protocol</a>.
 */
public class KServeClientFactoryV2 implements KServeClientFactory<InferenceRequest<?>> {
    @Override
    public KServeClient<InferenceRequest<?>> getKServeClient(
            final String service, final String modelName, final Duration requestReadTimeout) {
        final OkHttpClient httpClient = KServeClient.getHttpClient(requestReadTimeout);
        return new KServeClientV2(service, modelName, httpClient);
    }
}
