package client;

import java.time.Duration;
import org.json.JSONObject;

public class KFServingClientFactoryV1 implements KFServingClientFactory {
    @Override
    public KFServingClient<JSONObject> getKFServingClient(
            final String service, final String modelName, final Duration requestReadTimeout) {
        return new KFServingClientV1(service, modelName, requestReadTimeout);
    }
}
