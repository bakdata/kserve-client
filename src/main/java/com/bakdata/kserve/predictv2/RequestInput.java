package com.bakdata.kserve.predictv2;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RequestInput<T> {
    private String name;
    private List<Integer> shape;
    private String datatype;
    private Parameters parameters;
    private T data;
}
