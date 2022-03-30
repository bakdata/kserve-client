package predictv2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parameters {
    private String content_type;
    private Object extra;
}
