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

package com.bakdata.kserve.predictv2;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * A class to represent an
 * <a href="https://kserve.github.io/website/modelserving/inference_api/#inference-request-json-object">
 *     Inference Request JSON Object as defined in the v2 prediction protocol</a>.
 *
 * @param <I> The type of the input {@code data} contained in a {@link RequestInput}.
 * @param <O> The type of the output {@code data} contained in a {@link RequestOutput}.
 */
@Value
@Builder
public class InferenceRequest<I, O> {
    String id;
    Parameters parameters;
    @NonNull
    @Builder.Default
    List<RequestInput<I>> inputs = Collections.emptyList();
    @NonNull
    @Builder.Default
    List<RequestOutput<O>> outputs = Collections.emptyList();
}
