package stroom.annotations.hibernate;

import stroom.query.hibernate.DocRefEntity;

import javax.persistence.Entity;

@Entity(name="index_doc_ref")
public class AnnotationIndex extends DocRefEntity {

    public static final class Builder extends DocRefEntity.Builder<AnnotationIndex, Builder> {

        public Builder() {
            super(new AnnotationIndex());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
