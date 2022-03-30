package client;

import java.time.Duration;
import predictv2.InferenceRequest;

public class KFServingClientFactoryV2 implements KFServingClientFactory {
    @Override
    public KFServingClient<InferenceRequest<?, ?>> getKFServingClient(
            final String service, final String modelName, final Duration requestReadTimeout) {
        return new KFServingClientV2(service, modelName, requestReadTimeout);
    }
}
