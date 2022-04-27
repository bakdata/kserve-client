package com.bakdata.kserve.predictv2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseOutput<T> {
    private String name;
    private List<Integer> shape;
    private String datatype;
    private Parameters parameters;
    private T data;
}
