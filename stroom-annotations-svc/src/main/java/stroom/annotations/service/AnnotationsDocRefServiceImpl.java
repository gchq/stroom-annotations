package stroom.annotations.service;

import org.hibernate.SessionFactory;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.hibernate.DocRefServiceCriteriaImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnnotationsDocRefServiceImpl
        extends DocRefServiceCriteriaImpl<AnnotationsDocRefEntity> {

    @Inject
    public AnnotationsDocRefServiceImpl(final SessionFactory database) {
        super(AnnotationsDocRefEntity.TYPE,
                AnnotationsDocRefEntity.class,
                dataMap -> new AnnotationsDocRefEntity.Builder(),
                (docRefEntity, consumer) -> { /* nothing to see here */ },
        database);
    }
}
