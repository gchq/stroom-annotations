package stroom.annotations;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.resources.AnnotationsResource;
import stroom.annotations.resources.AnnotationsResourceImpl;
import stroom.annotations.resources.IndexDocRefResourceImpl;
import stroom.annotations.service.AnnotationsService;
import stroom.annotations.service.AnnotationsServiceImpl;
import stroom.annotations.service.IndexService;
import stroom.annotations.service.IndexServiceImpl;
import stroom.query.audit.DocRefResource;

public class Module extends AbstractBinder {
    private final Config config;

    public Module(final Config config) {
        this.config = config;
    }

    protected void configure() {
        bind(config).to(Config.class);
        bind(AnnotationsResourceImpl.class).to(AnnotationsResource.class);
        bind(IndexDocRefResourceImpl.class).to(DocRefResource.class);
        bind(AnnotationsServiceImpl.class).to(AnnotationsService.class);
        bind(IndexServiceImpl.class).to(IndexService.class);
    }
}
