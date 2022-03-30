package predictv2;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InferenceRequest<I, O> {
    private String id;
    private Parameters parameters;
    @Builder.Default
    private List<RequestInput<I>> inputs = Collections.emptyList();
    @Builder.Default
    private List<RequestOutput<O>> outputs = Collections.emptyList();
}

