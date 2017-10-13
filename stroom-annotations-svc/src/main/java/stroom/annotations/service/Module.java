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

    public Module(final Config config,
                  final Configuration jooqConfig) {
        this.config = config;
        this.jooqConfig = jooqConfig;

    }

    protected void configure() {
        bind(config).to(Config.class);
        bind(jooqConfig).to(Configuration.class);
        bind(AnnotationsResourceImpl.class).to(AnnotationsResource.class);
        bind(AnnotationsEventLoggingService.class).to(EventLoggingService.class);
    }
}
