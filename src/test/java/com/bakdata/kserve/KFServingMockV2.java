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

package com.bakdata.kserve;

import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;


@Getter
@NoArgsConstructor
public class KFServingMockV2 extends KFServingMock {
    @Override
    MockResponse getModelNotFoundResponse(final String modelName) {
        return new MockResponse().setResponseCode(404).setBody(String.format(
                "{\n"
                        + "  \"error\": \"Model %s not found\"\n"
                        + "}",
                modelName));
    }

    @Override
    String getEndpointString(final String modelName) {
        return String.format("/v2/models/%s/infer", modelName);
    }
}
