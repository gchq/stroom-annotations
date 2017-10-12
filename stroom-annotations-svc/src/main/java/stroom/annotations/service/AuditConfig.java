package stroom.annotations.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditConfig {

    @JsonProperty("enabled")
    private boolean enabled = false;

    @JsonProperty("kafka")
    private KafkaConfig kafka = new KafkaConfig();

    public final KafkaConfig getKafka() {
        return this.kafka;
    }

    public final boolean getEnabled() {
        return enabled;
    }
}
