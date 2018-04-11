package stroom.annotations.service;

import org.jooq.DSLContext;
import stroom.annotations.model.AnnotationsDocRefEntity;
import stroom.query.jooq.DocRefServiceJooqImpl;

import javax.inject.Inject;

public class AnnotationsDocRefServiceImpl
        extends DocRefServiceJooqImpl<AnnotationsDocRefEntity> {

    @Inject
    public AnnotationsDocRefServiceImpl(final DSLContext jooqConfig) {
        super(AnnotationsDocRefEntity.TYPE,
                dataMap -> new AnnotationsDocRefEntity.Builder(),
                (docRefEntity, consumer) -> { /* nothing to see here */ },
                AnnotationsDocRefEntity.class,
                jooqConfig);
    }
}
