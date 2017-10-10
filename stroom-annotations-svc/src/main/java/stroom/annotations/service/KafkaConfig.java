package stroom.annotations.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KafkaConfig {
    @JsonProperty
    private String bootstrapServers;

    @JsonProperty
    private String loggingTopic;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getLoggingTopic() {
        return loggingTopic;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KafkaConfig{");
        sb.append("bootstrapServers='").append(bootstrapServers).append('\'');
        sb.append(", loggingTopic='").append(loggingTopic).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
