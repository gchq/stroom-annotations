package stroom.annotations.service;

import event.logging.EventLoggingService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jooq.Configuration;
import stroom.annotations.service.audit.*;
import stroom.annotations.service.resources.AnnotationsResource;
import stroom.annotations.service.resources.AnnotationsResourceImpl;

public class Module extends AbstractBinder {
    private final Config config;
    private final Configuration jooqConfig;
    private final AuditExecutor auditExecutor;

    public Module(final Config config,
                  final Configuration jooqConfig,
                  final AuditExecutor auditExecutor) {
        this.config = config;
        this.jooqConfig = jooqConfig;
        this.auditExecutor = auditExecutor;

    }

    protected void configure() {
        bind(KafkaServiceImpl.class).to(KafkaService.class);
        bind(config.getAudit()).to(AuditConfig.class);
        bind(config.getAudit().getKafka()).to(KafkaConfig.class);
        bind(jooqConfig).to(Configuration.class);
        bind(AnnotationsResourceImpl.class).to(AnnotationsResource.class);
        bind(AnnotationsEventLoggingService.class).to(EventLoggingService.class);
        bind(auditExecutor).to(AuditExecutor.class);
    }
}
