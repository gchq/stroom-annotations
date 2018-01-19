package stroom.annotations.resources;

import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.testing.DocRefResourceIT;

import java.util.HashMap;
import java.util.Map;

public class AnnotationsDocRefResourceIT
        extends DocRefResourceIT<AnnotationsDocRefEntity, Config, App> {

    public AnnotationsDocRefResourceIT() {
        super(App.class, AnnotationsDocRefEntity.class, AnnotationsDocRefEntity.TYPE);
    }

    @Override
    protected AnnotationsDocRefEntity createPopulatedEntity() {
        return new AnnotationsDocRefEntity.Builder().build();
    }

    @Override
    protected Map<String, String> exportValues(AnnotationsDocRefEntity docRefEntity) {
        final Map values = new HashMap<>();
        values.put(DocRefEntity.NAME, docRefEntity.getName());
        return values;
    }
}
