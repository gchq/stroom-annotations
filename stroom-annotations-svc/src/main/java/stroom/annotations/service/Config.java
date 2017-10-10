package stroom.annotations.service;

import com.bendb.dropwizard.jooq.JooqFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Config extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty("flyway")
    private FlywayFactory flywayFactory = new FlywayFactory();

    @Valid
    @NotNull
    @JsonProperty("jooq")
    private JooqFactory jooqFactory = new JooqFactory();

    @JsonProperty("kafka")
    private KafkaConfig kafka = new KafkaConfig();

    public final DataSourceFactory getDataSourceFactory() {
        return this.dataSourceFactory;
    }

    public final FlywayFactory getFlywayFactory() {
        return this.flywayFactory;
    }

    public final JooqFactory getJooqFactory() {
        return this.jooqFactory;
    }

    public final KafkaConfig getKafka() {
        return this.kafka;
    }

    public static class KafkaConfig {
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
}
