package stroom.annotations;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.annotations.service.AnnotationsDocRefServiceImpl;
import stroom.annotations.service.AnnotationsService;
import stroom.annotations.service.AnnotationsServiceImpl;
import stroom.query.audit.service.DocRefService;

public class Module extends AbstractBinder {
    private final Config config;

    public Module(final Config config) {
        this.config = config;
    }

    protected void configure() {
        bind(config).to(Config.class);
        bind(AnnotationsServiceImpl.class).to(AnnotationsService.class);
        bind(AnnotationsDocRefServiceImpl.class).to(new TypeLiteral<DocRefService<AnnotationsDocRefEntity>>() {
        });
    }
}
