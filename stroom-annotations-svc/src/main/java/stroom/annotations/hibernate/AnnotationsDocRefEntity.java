package stroom.annotations.hibernate;

import stroom.query.hibernate.DocRefHibernateEntity;

import javax.persistence.Entity;

@Entity(name="index_doc_ref")
public class AnnotationsDocRefEntity extends DocRefHibernateEntity {

    public static final class Builder extends DocRefHibernateEntity.Builder<AnnotationsDocRefEntity, Builder> {

        public Builder() {
            super(new AnnotationsDocRefEntity());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
