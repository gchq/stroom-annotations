package stroom.annotations.model;

import stroom.query.audit.model.DocRefEntity;
import stroom.query.jooq.DocRefJooqEntity;
import stroom.query.jooq.JooqEntity;

@JooqEntity(tableName="index_doc_ref")
public class AnnotationsDocRefEntity extends DocRefJooqEntity {

    public static final String TYPE = "AnnotationsIndex";

    public static final class Builder extends DocRefEntity.BaseBuilder<AnnotationsDocRefEntity, Builder> {

        public Builder() {
            super(new AnnotationsDocRefEntity());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
