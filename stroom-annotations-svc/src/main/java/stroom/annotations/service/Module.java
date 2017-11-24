package stroom.annotations.service;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.service.resources.AnnotationsResource;
import stroom.annotations.service.resources.AnnotationsResourceImpl;
import stroom.annotations.service.resources.IndexResource;
import stroom.annotations.service.resources.IndexResourceImpl;

public class Module extends AbstractBinder {
    private final Config config;

    public Module(final Config config) {
        this.config = config;
    }

    protected void configure() {
        bind(config).to(Config.class);
        bind(AnnotationsResourceImpl.class).to(AnnotationsResource.class);
        bind(IndexResourceImpl.class).to(IndexResource.class);
    }
}
