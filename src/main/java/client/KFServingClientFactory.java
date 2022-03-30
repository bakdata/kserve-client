package client;

import java.time.Duration;

@FunctionalInterface
public interface KFServingClientFactory {
    KFServingClient<?> getKFServingClient(final String service, final String modelName, final Duration requestReadTimeout);
}

