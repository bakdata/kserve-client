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

import java.time.Duration;

/**
 * An interface for a factory producing a {@link KServeClient} which supports a specific protocol version.
 *
 * @param <T> The type of the input object to a request
 */
@FunctionalInterface
public interface KServeClientFactory<T> {
    /**
     * Get a {@link KServeClient} to make requests to an inference service supporting either the v1 or the v2 prediction
     * protocol.
     *
     * @param service The host name of the service, e.g. "my-classifier.kserve-namespace.svc.cluster.local"
     * @param modelName The model name as specified in model-settings.json or as key metadata.name in the
     *                  InferenceService k8s object configuration file.
     * @param requestReadTimeout The read time out as documented for the
     * <a href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/-builder/read-timeout/">OkHttpClient
     *                           </a> which this library uses
     * @param httpsEnabled Whether HTTPS should be used (true) or HTTP (false)
     * @return An instance of {@link KServeClient}
     */
    KServeClient<T> getKServeClient(
            final String service,
            final String modelName,
            final Duration requestReadTimeout,
            final boolean httpsEnabled);
}
