package predictv2;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InferenceResponse<T> {
    private String model_name;
    private String model_version;
    private String id;
    private Parameters parameters;
    private List<ResponseOutput<T>> outputs;
}
