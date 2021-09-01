package at.karl.hsm;

import io.quarkus.jsonb.JsonbConfigCustomizer;
import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.BinaryDataStrategy;

@Singleton
public class JsonbConfigurator implements JsonbConfigCustomizer {

    @Override
    public void customize(JsonbConfig config) {
        config.withBinaryDataStrategy(BinaryDataStrategy.BASE_64);
    }
}
