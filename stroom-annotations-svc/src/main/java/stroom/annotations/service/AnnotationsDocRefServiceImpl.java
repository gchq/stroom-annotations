package stroom.annotations.service;

import org.hibernate.SessionFactory;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.hibernate.DocRefServiceCriteriaImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnnotationsDocRefServiceImpl
        extends DocRefServiceCriteriaImpl<AnnotationsDocRefEntity, AnnotationsDocRefEntity.Builder> {

    @Inject
    public AnnotationsDocRefServiceImpl(final SessionFactory database) {
        super(database, AnnotationsDocRefEntity.class);
    }

    @Override
    protected AnnotationsDocRefEntity.Builder createDocumentBuilder() {
        return new AnnotationsDocRefEntity.Builder();
    }

    @Override
    protected AnnotationsDocRefEntity.Builder copyEntity(final AnnotationsDocRefEntity original) {
        return new AnnotationsDocRefEntity.Builder();
    }

    @Override
    protected AnnotationsDocRefEntity.Builder createImport(final Map<String, String> dataMap) {
        return new AnnotationsDocRefEntity.Builder();
    }

    @Override
    protected Map<String, Object> exportValues(final AnnotationsDocRefEntity annotationsDocRefEntity) {
        return new HashMap<>();
    }

    @Override
    public String getType() {
        return AnnotationsDocRefEntity.TYPE;
    }
}
