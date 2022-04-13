package com.bakdata.kserve.predictv2;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;

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

