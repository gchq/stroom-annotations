package stroom.annotations.hibernate;

import stroom.query.hibernate.DocRefEntity;

import javax.persistence.Entity;

@Entity(name="index_doc_ref")
public class AnnotationsDocRefEntity extends DocRefEntity {

    public static final class Builder extends DocRefEntity.Builder<AnnotationsDocRefEntity, Builder> {

        public Builder() {
            super(new AnnotationsDocRefEntity());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
