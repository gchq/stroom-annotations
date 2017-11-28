package stroom.annotations.model;

import stroom.annotations.hibernate.AnnotationIndex;
import stroom.query.hibernate.DocRefEntityDTO;

public class AnnotationIndexDTO extends DocRefEntityDTO {

    public static final class Builder extends DocRefEntityDTO.ABuilder<AnnotationIndexDTO, AnnotationIndex, Builder> {

        public Builder() {
            super(new AnnotationIndexDTO());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
