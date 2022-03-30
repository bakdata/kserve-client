package predictv2;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InferenceError {
    private String error;
    private String detail;
}
