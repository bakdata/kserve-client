package com.bakdata.kserve.predictv2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class InferenceError {
    String error;
    String detail;
}
