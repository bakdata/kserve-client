package predictv2;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
