package stroom.annotations.service;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jooq.Configuration;
import stroom.annotations.service.audit.AuditService;
import stroom.annotations.service.audit.AuditServiceImpl;

public class Module extends AbstractBinder {
    private Config config;
    private Configuration jooqConfig;

    public Module(Config config, Configuration jooqConfig) {
        this.config = config;
        this.jooqConfig = jooqConfig;
    }

    protected void configure() {
        bind(AuditServiceImpl.class).to(AuditService.class);
        bind(config.getKafka()).to(KafkaConfig.class);
    }
}
