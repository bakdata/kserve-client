package predictv2;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestInput<T> {
    private String name;
    private List<Integer> shape;
    private String datatype;
    private Parameters parameters;
    private T data;
}
