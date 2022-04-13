package com.bakdata.kserve.predictv2;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
public class InferenceResponse<T> {
    @NonNull
    private final String model_name;
    private final String model_version;
    @NonNull
    private final String id;
    private final Parameters parameters;
    @NonNull
    private final List<ResponseOutput<T>> outputs;
}
