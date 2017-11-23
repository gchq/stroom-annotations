package stroom.annotations.service;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.service.resources.AnnotationsResource;
import stroom.annotations.service.resources.AnnotationsResourceImpl;

public class Module extends AbstractBinder {
    private final Config config;

    public Module(final Config config) {
        this.config = config;
    }

    protected void configure() {
        bind(config).to(Config.class);
        bind(AnnotationsResourceImpl.class).to(AnnotationsResource.class);
    }
}
